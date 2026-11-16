package io.github.giangggvu.shop.common;

/** The request is valid but conflicts with the current state, such as buying more than is in stock. Mapped to 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
