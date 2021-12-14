package project.model;

import lombok.*;
import project.dto.UserDTO;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "Users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    @Email
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @Column(nullable = false)
    private Double balance;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER) // bez EAGER: ailed to lazily initialize a collection of role: project.model.User.openPositions
    private Set<OpenPosition> openPositions = new HashSet<>();
    @Transient
    private Set<Long> openPositionIds = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<PendingPosition> pendingPositions = new HashSet<>();
    @Transient
    private Set<Long> pendingPositionIds = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TransactionHistory> transactionsHistory = new HashSet<>();
    @Transient
    private Set<Long> transactionsHistoryIds = new HashSet<>();

    @Column(name = "enabled")
    @NotNull
    private Boolean enabled;

    public UserDTO mapToDTO() {
        return UserDTO.builder()
                .id(this.getId())
                .email(this.getEmail())
                .password(this.getPassword())
                .firstName(this.getFirstName())
                .lastName(this.getLastName())
                .balance(this.getBalance())
                .enabled(this.enabled)
                .build();
    }
}
