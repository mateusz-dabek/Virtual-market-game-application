package project.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;
import project.model.OpenPosition;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Builder
public class OpenPositionDTO {
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

    @Nullable
    private Long userId;

    @NotNull
    private Double liquidationPrice;

    public OpenPosition parsePosition() {
        return OpenPosition.builder()
                .id(this.getId())
                .symbol(this.getSymbol())
                .openTime(this.getOpenTime())
                .openPrice(this.getOpenPrice())
                .typePosition(this.getTypePosition())
                .value(this.getValue())
                .leverage(this.getLeverage())
                .liquidationPrice(this.getLiquidationPrice())
                .userId(this.userId)
                .build();
    }

}
