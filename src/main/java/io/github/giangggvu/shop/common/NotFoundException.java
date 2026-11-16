package io.github.giangggvu.shop.common;

/** The requested resource does not exist, or the caller may not see it. Mapped to 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
