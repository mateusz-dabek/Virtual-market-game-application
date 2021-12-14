package project.dto;

import lombok.Builder;
import lombok.Data;
import project.model.OpenPosition;
import project.model.PendingPosition;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class PendingPositionDTO {
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

    @NotNull
    private Long userId;

    public PendingPosition parsePosition() {
        return PendingPosition.builder()
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
