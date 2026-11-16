package io.github.giangggvu.shop.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.giangggvu.shop.ApiTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AuthApiTest extends ApiTestSupport {

    @Test
    void registerReturnsATokenForANewCustomer() throws Exception {
        String email = uniqueEmail("new");
        register(email, "password-123")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    void emailIsStoredInLowerCase() throws Exception {
        String id = UUID.randomUUID().toString();
        register("MiXeD-" + id + "@Test.Local", "password-123")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.email").value("mixed-" + id + "@test.local"));
        login("mixed-" + id + "@test.local", "password-123").andExpect(status().isOk());
    }

    @Test
    void sameEmailCannotRegisterTwice() throws Exception {
        String email = uniqueEmail("twice");
        register(email, "password-123").andExpect(status().isCreated());
        register(email, "another-password").andExpect(status().isConflict());
    }

    @Test
    void invalidInputListsEveryBadField() throws Exception {
        register("not-an-email", "short")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        login(ADMIN_EMAIL, "wrong-password")
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Email or password is incorrect"));
    }

    @Test
    void unknownEmailGetsTheSameMessageAsAWrongPassword() throws Exception {
        login(uniqueEmail("nobody"), "password-123")
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Email or password is incorrect"));
    }

    @Test
    void meNeedsAToken() throws Exception {
        mvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Please log in to continue"));

        mvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, bearer(newCustomerToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = newCustomerToken();
        String tampered = token.substring(0, token.length() - 4) + "abcd";
        mvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, bearer(tampered)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail", containsString("expired")));
    }

    @Test
    void aBrokenTokenDoesNotBlockBrowsingTheShop() throws Exception {
        mvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, bearer("not-a-real-token")))
            .andExpect(status().isOk());
        mvc.perform(get("/api/categories").header(HttpHeaders.AUTHORIZATION, bearer("not-a-real-token")))
            .andExpect(status().isOk());
    }
}
