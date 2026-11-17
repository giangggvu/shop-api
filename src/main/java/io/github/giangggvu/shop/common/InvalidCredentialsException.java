package io.github.giangggvu.shop.common;


public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email or password is incorrect");
    }
}
