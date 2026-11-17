package io.github.giangggvu.shop.order;

import io.github.giangggvu.shop.config.AppProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * Builds VietQR transfer instructions. Any Vietnamese banking app can scan the QR image, which fills in
 * the account, the amount and the order code, so the shopper cannot mistype them.
 */
@Component
public class PaymentInstructions {

    private static final String DEFAULT_TEMPLATE = "compact2";

    private final AppProperties.Payment config;

    public PaymentInstructions(AppProperties properties) {
        this.config = properties.payment();
    }

    /** Null when the order no longer waits for payment, or when no bank account is configured. */
    public PaymentView forOrder(Order order) {
        if (config == null || !config.isConfigured() || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return null;
        }
        String template = hasText(config.template()) ? config.template() : DEFAULT_TEMPLATE;
        StringBuilder url = new StringBuilder("https://img.vietqr.io/image/")
            .append(encode(config.bankId())).append('-')
            .append(encode(config.accountNo())).append('-')
            .append(encode(template)).append(".png")
            .append("?amount=").append(order.getTotal())
            .append("&addInfo=").append(encode(order.getCode()));
        if (hasText(config.accountName())) {
            String plainName = io.github.giangggvu.shop.common.Slugs.of(config.accountName())
    .replace('-', ' ').toUpperCase(java.util.Locale.ROOT);
url.append("&accountName=").append(encode(plainName));
        }
        return new PaymentView(
            config.bankId(), config.accountNo(), config.accountName(), order.getTotal(), order.getCode(), url.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
