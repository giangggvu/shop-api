package io.github.giangggvu.shop.common;

/** A query parameter the API cannot work with. Mapped to 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
