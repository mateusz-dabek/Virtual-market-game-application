package project.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pro.xstore.api.message.command.APICommandFactory;
import project.model.PendingPosition;
import project.repositories.OpenPositionRepository;


@RunWith(PowerMockRunner.class)
@PrepareForTest(APICommandFactory.class)
public class PriceServiceTest {
    @Mock
    public OpenPositionRepository openPositionRepository;
    @Mock
    public PendingPosition pendingPosition;
    @InjectMocks
    private PriceService priceService;


    @Before
    public void init() {
        PowerMockito.mockStatic(APICommandFactory.class);
    }

    @Test
    public void receiveChartCandlesData() {

    }

    @Test
    public void receiveCurrentPrice() {

    }
}