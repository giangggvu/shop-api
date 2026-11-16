package io.github.giangggvu.shop.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Emails are stored lower-cased, so callers normalise before looking up. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
