package project.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import project.model.OpenPosition;
import project.model.User;
import project.repositories.OpenPositionRepository;
import project.repositories.UserRepository;
import project.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PriceService.class)
public class OpenPositionServiceTest {


    @Mock
    private OpenPositionRepository openPositionRepository;

    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private OpenPositionService openPositionService;

    private static MockHttpServletRequest httpServletRequest;
    static OpenPosition openPosition;
    static User user;

    @Before
    public void init() {
        openPosition = new OpenPosition();
        openPosition.setId(1L);
        openPosition.setOpenTime(LocalDateTime.now());
        openPosition.setUserId(1L);
        openPosition.setValue(1000.0);
        openPosition.setSymbol("BITCOIN");
        openPosition.setTypePosition(OpenPosition.Position.SHORT);
        openPosition.setLeverage(5L);
        openPosition.setOpenPrice(10000.0);
        openPosition.setLiquidationPrice(12000.0);

        PowerMockito.mockStatic(PriceService.class);
        Mockito.when(PriceService.receiveCurrentPrice("BITCOIN", OpenPosition.Position.SHORT)).thenReturn(Optional.of(10300.0));
        user = new User();

        httpServletRequest = new MockHttpServletRequest();
    }

    @Test(expected = AccessDeniedException.class)
    public void createPosition_insufficientBalance_AccessDeniedExceptionThrown() {
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        user.setBalance(100.0);
        try {
            openPositionService.createPosition(openPosition, 1L);
        } catch (AccessDeniedException e) {
            Mockito.verify(openPositionRepository, Mockito.never()).save(ArgumentMatchers.isA(OpenPosition.class));
            throw e;
        }
    }

    @Test
    public void createPosition_rightValues_createdPosition() {
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        user.setBalance(5000.0);

        openPositionService.createPosition(openPosition, 1L);
        Mockito.verify(openPositionRepository, Mockito.times(1)).save(ArgumentMatchers.isA(OpenPosition.class));
    }

    @Test(expected = AccessDeniedException.class)
    public void deletePosition_notOwner_AccessDeniedExceptionThrown() {
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        Mockito.when(openPositionRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(openPosition);
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        openPosition.setUserId(10L);
        try {
            openPositionService.deletePosition(httpServletRequest, 1L);
        } catch (AccessDeniedException e) {
            Mockito.verify(openPositionRepository, Mockito.never()).deleteById(ArgumentMatchers.anyLong());
            throw e;
        }

    }

    @Test
    public void deletePosition_rightValues_deletedPosition() { //  AccessDeniedException("Wrong position id, you are not owner");
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        Mockito.when(openPositionRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(openPosition);
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        user.setBalance(10000.0);
        openPositionService.deletePosition(httpServletRequest, 1L);
        Mockito.verify(openPositionRepository, Mockito.times(1)).deleteById(ArgumentMatchers.anyLong());

    }

}