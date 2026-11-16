package io.github.giangggvu.shop.auth;

import io.github.giangggvu.shop.common.BusinessRuleException;
import io.github.giangggvu.shop.common.InvalidCredentialsException;
import io.github.giangggvu.shop.common.NotFoundException;
import io.github.giangggvu.shop.user.Role;
import io.github.giangggvu.shop.user.User;
import io.github.giangggvu.shop.user.UserRepository;
import io.github.giangggvu.shop.user.UserView;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new BusinessRuleException("This email is already registered");
        }
        User user = users.save(new User(
            email, passwordEncoder.encode(request.password()), request.fullName().trim(), Role.CUSTOMER));
        return tokens.issueFor(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = users.findByEmail(normalizeEmail(request.email()))
            .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
            .orElseThrow(InvalidCredentialsException::new);
        return tokens.issueFor(user);
    }

    @Transactional(readOnly = true)
    public UserView me(Long userId) {
        return users.findById(userId)
            .map(UserView::from)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
