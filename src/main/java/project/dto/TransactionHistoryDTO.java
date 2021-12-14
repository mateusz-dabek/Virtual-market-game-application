package project.dto;

import lombok.Builder;
import lombok.Data;
import project.model.OpenPosition;
import project.model.TransactionHistory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionHistoryDTO {
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

    @NotNull
    private Long userId;

    public TransactionHistory parseOffer() {
        return TransactionHistory.builder()
                .id(this.getId())
                .symbol(this.getSymbol())
                .openPrice(this.getOpenPrice())
                .typePosition(this.getTypePosition())
                .value(this.getValue())
                .leverage(this.getLeverage())
                .exitPrice(this.getExitPrice())
                .profit(this.getProfit())
                .openTime(this.getOpenTime())
                .userId(this.userId)
                .build();
    }
}
