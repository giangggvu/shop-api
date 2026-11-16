package io.github.giangggvu.shop.auth;

import io.github.giangggvu.shop.user.UserView;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, UserView user) {
}
