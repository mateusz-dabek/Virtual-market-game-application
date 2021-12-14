package project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import project.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUserId(Long userId);

    boolean existsByUserId(Long id);
}
