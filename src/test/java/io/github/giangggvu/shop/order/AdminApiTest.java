package io.github.giangggvu.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.giangggvu.shop.ApiTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class AdminApiTest extends ApiTestSupport {

    @Test
    void anonymousVisitorsCannotManageProducts() throws Exception {
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customersCannotManageProducts() throws Exception {
        mvc.perform(post("/api/admin/products")
                .header(HttpHeaders.AUTHORIZATION, bearer(newCustomerToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void newProductsGetAReadableVietnameseSlug() throws Exception {
        Map<String, Object> request = Map.of(
            "categoryId", firstCategoryId(),
            "name", "Cà phê Đặc biệt",
            "price", 99_000,
            "stock", 3);
        String admin = adminToken();
        String first = body(createWith(admin, request).andExpect(status().isCreated())).get("slug").asText();
        String second = body(createWith(admin, request).andExpect(status().isCreated())).get("slug").asText();

        assertThat(first).startsWith("ca-phe-dac-biet");
        assertThat(second).isNotEqualTo(first).startsWith("ca-phe-dac-biet-");
    }

    @Test
    void hiddenProductsDisappearFromTheShop() throws Exception {
        String admin = adminToken();
        JsonNode product = createProduct(admin, 15_000, 2);

        mvc.perform(delete("/api/admin/products/" + product.get("id").asLong())
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + product.get("slug").asText()))
            .andExpect(status().isNotFound());
    }

    @Test
    void ordersMoveOnlyAlongAllowedSteps() throws Exception {
        String admin = adminToken();
        String code = placeOrder(admin);

        changeStatus(admin, code, "PAID")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.paidAt", notNullValue()));
        changeStatus(admin, code, "PENDING_PAYMENT").andExpect(status().isConflict());
        changeStatus(admin, code, "SHIPPING").andExpect(status().isOk());
        changeStatus(admin, code, "COMPLETED").andExpect(status().isOk());
        changeStatus(admin, code, "CANCELLED").andExpect(status().isConflict());
    }

    @Test
    void ordersCanBeListedByStatus() throws Exception {
        String admin = adminToken();
        placeOrder(admin);

        mvc.perform(get("/api/admin/orders").param("status", "PENDING_PAYMENT")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[*].status", everyItem(is("PENDING_PAYMENT"))));
    }

    @Test
    void unknownStatusIsABadRequest() throws Exception {
        String admin = adminToken();
        String code = placeOrder(admin);
        changeStatus(admin, code, "LOST_IN_THE_MAIL").andExpect(status().isBadRequest());
    }

    private String placeOrder(String admin) throws Exception {
        JsonNode product = createProduct(admin, 25_000, 3);
        String buyer = newCustomerToken();
        addToCart(buyer, product.get("id").asLong(), 1).andExpect(status().isOk());
        return body(checkout(buyer).andExpect(status().isCreated())).get("code").asText();
    }

    private ResultActions createWith(String admin, Map<String, Object> request) throws Exception {
        return mvc.perform(post("/api/admin/products")
            .header(HttpHeaders.AUTHORIZATION, bearer(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(request)));
    }

    private ResultActions changeStatus(String admin, String code, String status) throws Exception {
        return mvc.perform(patch("/api/admin/orders/" + code + "/status")
            .header(HttpHeaders.AUTHORIZATION, bearer(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(Map.of("status", status))));
    }
}
