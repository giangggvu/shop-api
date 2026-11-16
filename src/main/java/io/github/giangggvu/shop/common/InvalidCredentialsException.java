package io.github.giangggvu.shop.common;

/** Wrong email or password. Mapped to 401, with one message for both cases so accounts cannot be probed. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email or password is incorrect");
    }
}
