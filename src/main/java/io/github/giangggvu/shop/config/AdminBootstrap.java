package io.github.giangggvu.shop.config;

import io.github.giangggvu.shop.auth.AuthService;
import io.github.giangggvu.shop.user.Role;
import io.github.giangggvu.shop.user.User;
import io.github.giangggvu.shop.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first admin account from ADMIN_EMAIL and ADMIN_PASSWORD, so no password ever lives in a migration.
 */
@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppProperties properties;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    AdminBootstrap(AppProperties properties, UserRepository users, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = properties.admin();
        if (admin == null || !admin.isConfigured()) {
            return;
        }
        String email = AuthService.normalizeEmail(admin.email());
        if (users.existsByEmail(email)) {
            return;
        }
        users.save(new User(email, passwordEncoder.encode(admin.password()), "Admin", Role.ADMIN));
        log.info("Created admin account {}", email);
    }
}
