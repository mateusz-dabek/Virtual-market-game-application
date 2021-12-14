package project.model;

import lombok.*;
import project.dto.OpenPositionDTO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "Open_positions")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String symbol;

    @NotNull
    private LocalDateTime openTime;

    @NotNull
    private Double openPrice;

    public enum Position {
        LONG,
        SHORT,
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private Position typePosition;

    @NotNull
    private Double value;

    @NotNull
    private Long leverage;

    @NotNull
    private Double liquidationPrice;

    @Column(name = "user_id")
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "user_Id", insertable = false, updatable = false)
    private User user;


    public OpenPositionDTO mapToDTO() {
        return OpenPositionDTO.builder()
                .id(this.getId())
                .symbol(this.getSymbol())
                .openTime(this.getOpenTime())
                .openPrice(this.getOpenPrice())
                .typePosition(this.getTypePosition())
                .value(this.getValue())
                .leverage(this.getLeverage())
                .liquidationPrice(
                        this.getLiquidationPrice())
                .userId(this.getUserId())
                .build();
    }
}
