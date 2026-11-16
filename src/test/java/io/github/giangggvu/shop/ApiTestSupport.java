package io.github.giangggvu.shop;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Shared helpers for tests that call the API over HTTP. All test classes share one application and one
 * in-memory database, so every test creates its own users and products instead of relying on order.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestSupport {

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "admin-password-123";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    /** Reads the response as UTF-8, so Vietnamese text survives the round trip. */
    protected JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    protected String toJson(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.local";
    }

    protected ResultActions register(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(Map.of("email", email, "password", password, "fullName", "Người Mua Thử"))));
    }

    protected ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(Map.of("email", email, "password", password))));
    }

    protected String newCustomerToken() throws Exception {
        return body(register(uniqueEmail("buyer"), "password-123").andExpect(status().isCreated()))
            .get("accessToken").asText();
    }

    protected String adminToken() throws Exception {
        return body(login(ADMIN_EMAIL, ADMIN_PASSWORD).andExpect(status().isOk())).get("accessToken").asText();
    }

    protected long firstCategoryId() throws Exception {
        return body(mvc.perform(get("/api/categories")).andExpect(status().isOk())).get(0).get("id").asLong();
    }

    /** Creates a fresh product with a unique name, so a test never shares stock with another test. */
    protected JsonNode createProduct(String adminToken, long price, int stock) throws Exception {
        Map<String, Object> request = Map.of(
            "categoryId", firstCategoryId(),
            "name", "Sản phẩm thử " + UUID.randomUUID(),
            "price", price,
            "stock", stock);
        return body(mvc.perform(post("/api/admin/products")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated()));
    }

    protected ResultActions addToCart(String token, long productId, int quantity) throws Exception {
        return mvc.perform(post("/api/cart/items")
            .header(HttpHeaders.AUTHORIZATION, bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(Map.of("productId", productId, "quantity", quantity))));
    }

    protected ResultActions checkout(String token) throws Exception {
        Map<String, Object> shipping = Map.of(
            "recipientName", "Nguyễn Văn An",
            "phone", "0912345678",
            "address", "12 Võ Thị Sáu, Hai Bà Trưng, Hà Nội");
        return mvc.perform(post("/api/orders")
            .header(HttpHeaders.AUTHORIZATION, bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(shipping)));
    }

    protected long stockOf(String slug) throws Exception {
        return body(mvc.perform(get("/api/products/" + slug)).andExpect(status().isOk())).get("stock").asLong();
    }
}
