package project.services;

import project.model.OpenPosition;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PrecisionCalculate {

    static BigDecimal calculateCost(Double value, Long leverage) {
        BigDecimal cost = new BigDecimal(value.toString());
        cost = cost.divide(new BigDecimal(leverage), 12, RoundingMode.HALF_DOWN);
        return cost;
    }

    static BigDecimal calculateProfit(OpenPosition openPosition, Double currentPrice) {
        BigDecimal profit = new BigDecimal(openPosition.getOpenPrice().toString());

        profit = profit.divide(new BigDecimal(currentPrice.toString()), 12, RoundingMode.HALF_DOWN);
        profit = profit.multiply(new BigDecimal(openPosition.getValue().toString()));
        profit = profit.subtract(new BigDecimal(openPosition.getValue().toString()));
        if (openPosition.getTypePosition().equals(OpenPosition.Position.LONG)) {
            profit = profit.multiply(new BigDecimal("-1"));
        }
        return profit;
    }

    static BigDecimal calculateLiquidationPrice(Double priceExecute, Long leverage, OpenPosition.Position type) {
        BigDecimal liquidationPrice = new BigDecimal(priceExecute.toString());
        BigDecimal variable = BigDecimal.ONE;

        variable = variable.divide(new BigDecimal(leverage.toString()), 12, RoundingMode.HALF_DOWN);
        variable = variable.multiply(new BigDecimal(priceExecute.toString()));
        if (type.equals(OpenPosition.Position.SHORT)) {
            liquidationPrice = liquidationPrice.add(variable);
        } else
            liquidationPrice = liquidationPrice.subtract(variable);

        return liquidationPrice;
    }
}
