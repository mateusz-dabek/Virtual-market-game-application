package project.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketSymbol {
    private String symbol;
    private double ask;
    private double bid;

    public MarketSymbol(String symbol, double ask, double bid) {
        this.symbol = symbol;
        this.ask = ask;
        this.bid = bid;
    }
}
