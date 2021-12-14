package project.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AccountActivationCode {
    private static final int EXPIRATION = 60 * 2; // 2 godziny

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false, unique = true)
    private User user;

    private LocalDateTime expiryDate;

    public AccountActivationCode(Long userId) {
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
