package io.github.giangggvu.shop.common;


public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
