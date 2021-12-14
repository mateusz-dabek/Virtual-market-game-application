package project.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class PasswordResetToken {
    private static final int EXPIRATION = 60 * 24; // doba

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false, unique = true)
    private User user;

    private LocalDateTime expiryDate;

    public PasswordResetToken(Long userId) {
        this.userId = userId;
        this.expiryDate = calculateExpiryDate();
    }

    public boolean isValid() {
        return getExpiryDate().isAfter(LocalDateTime.now());
    }

    public static LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION);
    }


}
