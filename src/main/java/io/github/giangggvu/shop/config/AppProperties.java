package io.github.giangggvu.shop.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under the {@code app} prefix in application.yml.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Admin admin, Cors cors, Payment payment) {

    public record Jwt(String secret, Duration ttl) {
    }

    public record Admin(String email, String password) {

        public boolean isConfigured() {
            return hasText(email) && hasText(password);
        }
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Payment(String bankId, String accountNo, String accountName, String template) {

        public boolean isConfigured() {
            return hasText(bankId) && hasText(accountNo);
        }
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
