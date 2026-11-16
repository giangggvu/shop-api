package io.github.giangggvu.shop.user;

public record UserView(Long id, String email, String fullName, Role role) {

    public static UserView from(User user) {
        return new UserView(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
