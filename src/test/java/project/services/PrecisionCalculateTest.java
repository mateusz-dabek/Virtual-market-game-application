package project.services;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import project.model.OpenPosition;

import java.math.BigDecimal;


public class PrecisionCalculateTest {
    @InjectMocks
    private PrecisionCalculate precisionCalculate;

    @Test
    public void calculateCost_calculate_calculatedAccurateCost() throws InterruptedException {
        BigDecimal cost = PrecisionCalculate.calculateCost(1085.65, 3L);
        Assert.assertEquals(361.883333333333D, cost.doubleValue(), 0.000000000001);
        Assert.assertNotEquals(361.883333333334D, cost.doubleValue(), 0.000000000001);
    }

    @Test
    public void calculateProfit_calculate_calculatedAccurateProfit() {
        OpenPosition openPosition = new OpenPosition();
        openPosition.setOpenPrice(90.0);
        openPosition.setValue(1000.0);
        openPosition.setTypePosition(OpenPosition.Position.LONG);
        BigDecimal profit = PrecisionCalculate.calculateProfit(openPosition, 100.0);
        Assert.assertEquals(profit.doubleValue() ,100.000000000000D, 0.000000000001);
    }

    @Test
    public void calculateLiquidationPrice_calculate_calculatedAccurateLiquidationPrice() {
        BigDecimal liquidationPrice = PrecisionCalculate.calculateLiquidationPrice(100.0,  5L, OpenPosition.Position.SHORT);
        Assert.assertEquals(120.0000000000000, liquidationPrice.doubleValue(), 0.000000000001);
    }
}