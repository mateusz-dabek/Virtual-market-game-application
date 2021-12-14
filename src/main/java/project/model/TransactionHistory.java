package project.model;

import lombok.*;
import project.dto.TransactionHistoryDTO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions_history")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String symbol;

    @NotNull
    private LocalDateTime openTime;

    @NotNull
    private Double openPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OpenPosition.Position typePosition;

    @NotNull
    private Double value;

    @NotNull
    private Long leverage;

    @NotNull
    private Double exitPrice;

    @NotNull
    private Double profit;

    @Column(name = "user_id")
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "user_Id", insertable = false, updatable = false)
    private User user;


}
