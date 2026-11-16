package io.github.giangggvu.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.giangggvu.shop.ApiTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class CheckoutFlowTest extends ApiTestSupport {

    @Test
    void buyingTakesStockEmptiesTheCartAndGivesPaymentInstructions() throws Exception {
        JsonNode product = createProduct(adminToken(), 50_000, 5);
        String buyer = newCustomerToken();

        addToCart(buyer, product.get("id").asLong(), 2)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(100000))
            .andExpect(jsonPath("$.itemCount").value(2));

        JsonNode order = body(checkout(buyer)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
            .andExpect(jsonPath("$.total").value(100000))
            .andExpect(jsonPath("$.items", hasSize(1))));

        String code = order.get("code").asText();
        assertThat(code).startsWith("LH").hasSize(10);
        assertThat(order.get("payment").get("transferContent").asText()).isEqualTo(code);
        assertThat(order.get("payment").get("qrImageUrl").asText())
            .startsWith("https://img.vietqr.io/image/970436-0123456789-compact2.png")
            .contains("amount=100000")
            .contains("addInfo=" + code);

        assertThat(stockOf(product.get("slug").asText())).isEqualTo(3);
        mvc.perform(get("/api/cart").header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
            .andExpect(jsonPath("$.items", hasSize(0)));
        mvc.perform(get("/api/orders").header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
            .andExpect(jsonPath("$.items[0].code").value(code));
    }

    @Test
    void customersCannotSeeEachOthersOrders() throws Exception {
        JsonNode product = createProduct(adminToken(), 20_000, 5);
        String buyer = newCustomerToken();
        addToCart(buyer, product.get("id").asLong(), 1).andExpect(status().isOk());
        String code = body(checkout(buyer).andExpect(status().isCreated())).get("code").asText();

        mvc.perform(get("/api/orders/" + code).header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
            .andExpect(status().isOk());
        mvc.perform(get("/api/orders/" + code).header(HttpHeaders.AUTHORIZATION, bearer(newCustomerToken())))
            .andExpect(status().isNotFound());
    }

    @Test
    void cartRefusesMoreThanIsInStock() throws Exception {
        JsonNode product = createProduct(adminToken(), 30_000, 1);
        String buyer = newCustomerToken();

        addToCart(buyer, product.get("id").asLong(), 1).andExpect(status().isOk());
        addToCart(buyer, product.get("id").asLong(), 1)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail", containsString("only 1 left")));
    }

    @Test
    void checkoutFailsWithoutSideEffectsWhenStockRanOutMeanwhile() throws Exception {
        String admin = adminToken();
        JsonNode product = createProduct(admin, 40_000, 2);
        String buyer = newCustomerToken();
        addToCart(buyer, product.get("id").asLong(), 2).andExpect(status().isOk());

        // Someone else bought one in the meantime: the admin lowers the stock to 1.
        Map<String, Object> update = Map.of(
            "categoryId", product.get("category").get("id").asLong(),
            "name", product.get("name").asText(),
            "price", 40_000,
            "stock", 1);
        mvc.perform(put("/api/admin/products/" + product.get("id").asLong())
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(update)))
            .andExpect(status().isOk());

        checkout(buyer)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail", containsString("only 1 left")));

        assertThat(stockOf(product.get("slug").asText())).isEqualTo(1);
        mvc.perform(get("/api/cart").header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
            .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void cancellingAnUnpaidOrderReturnsTheStock() throws Exception {
        JsonNode product = createProduct(adminToken(), 10_000, 4);
        String slug = product.get("slug").asText();
        String buyer = newCustomerToken();
        addToCart(buyer, product.get("id").asLong(), 3).andExpect(status().isOk());
        String code = body(checkout(buyer).andExpect(status().isCreated())).get("code").asText();
        assertThat(stockOf(slug)).isEqualTo(1);

        mvc.perform(post("/api/orders/" + code + "/cancel").header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.payment", nullValue()));
        assertThat(stockOf(slug)).isEqualTo(4);

        mvc.perform(post("/api/orders/" + code + "/cancel").header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
            .andExpect(status().isConflict());
    }

    @Test
    void emptyCartCannotBeCheckedOut() throws Exception {
        checkout(newCustomerToken())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Your cart is empty"));
    }

    @Test
    void shippingDetailsAreValidated() throws Exception {
        Map<String, Object> shipping = Map.of(
            "recipientName", "Nguyễn Văn An",
            "phone", "12345",
            "address", "");
        mvc.perform(post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearer(newCustomerToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(shipping)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.phone").exists())
            .andExpect(jsonPath("$.errors.address").exists());
    }

    @Test
    void checkoutNeedsLogin() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    }
}
