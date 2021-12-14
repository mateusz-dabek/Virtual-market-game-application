package project.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymbolStatistic {
    private String symbol;
    private double profit;
    private double loss;

    public SymbolStatistic(String symbol, double profit, double loss) {
        this.symbol = symbol;
        this.profit = profit;
        this.loss = loss;
    }

    public static SymbolStatistic mapToSymbolStatistic(TransactionHistory transactionHistory) {
        if (transactionHistory.getProfit() >= 0.0)
            return new SymbolStatistic(transactionHistory.getSymbol(), transactionHistory.getProfit(), 0);
        else return new SymbolStatistic(transactionHistory.getSymbol(), 0, transactionHistory.getProfit());
    }
}
