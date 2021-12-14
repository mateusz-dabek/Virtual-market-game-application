package project.model;

import lombok.*;
import project.dto.PendingPositionDTO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Pending_positions")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String symbol;

    @NotNull
    private Double priceExecute;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OpenPosition.Position typePosition;

    @NotNull
    private Double value;

    @NotNull
    private Long leverage;

    @Column(name = "user_id")
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "user_Id", insertable = false, updatable = false)
    private User user;

    @Column(name = "smaller_than_current_price")
    private Boolean smallerThanCurrentPrice;

    public PendingPositionDTO mapToDTO() {
        return PendingPositionDTO.builder()
                .id(this.getId())
                .symbol(this.getSymbol())
                .priceExecute(this.priceExecute)
                .typePosition(this.getTypePosition())
                .value(this.getValue())
                .leverage(this.getLeverage())
                .userId(this.userId)
                .build();
    }
}
