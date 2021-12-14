package project.services;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import project.model.OpenPosition;
import project.model.PendingPosition;
import project.model.User;
import project.repositories.OpenPositionRepository;
import project.repositories.PendingPositionRepository;
import project.repositories.UserRepository;
import project.security.JwtProvider;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PriceService.class)
public class PendingPositionServiceTest {

    @Mock
    private PendingPositionRepository pendingPositionRepository;

    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private PendingPositionService pendingPositionService;

    private static MockHttpServletRequest httpServletRequest;
    static PendingPosition pendingPosition;
    static User user;

    @Before public void init() {
        pendingPosition = new PendingPosition();
        pendingPosition.setId(2L);
        pendingPosition.setLeverage(24L);
        pendingPosition.setValue(1000.0);
        pendingPosition.setSymbol("BITCOIN");
        pendingPosition.setTypePosition(OpenPosition.Position.SHORT);
        pendingPosition.setLeverage(5L);
        pendingPosition.setPriceExecute(10000.0);
        pendingPosition.setUserId(1L);

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
            pendingPositionService.createPosition(pendingPosition);
        } catch (AccessDeniedException e) {
            Mockito.verify(pendingPositionRepository, Mockito.never()).save(ArgumentMatchers.isA(PendingPosition.class));
            throw e;
        }
    }

    @Test
    public void createPosition_rightValues_createdPosition() {
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        user.setBalance(5000.0);

        pendingPositionService.createPosition(pendingPosition);
        Mockito.verify(pendingPositionRepository, Mockito.times(1)).save(ArgumentMatchers.isA(PendingPosition.class));
    }

    @Test(expected = AccessDeniedException.class)
    public void deletePosition_notOwner_AccessDeniedExceptionThrown() {
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        Mockito.when(pendingPositionRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(pendingPosition);
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        pendingPosition.setUserId(10L);
        try {
            pendingPositionService.deletePosition(httpServletRequest, 1L);
        } catch (AccessDeniedException e) {
            Mockito.verify(pendingPositionRepository, Mockito.never()).deleteById(ArgumentMatchers.anyLong());
            throw e;
        }

    }

    @Test
    public void deletePosition_rightValues_deletedPosition() {
        Mockito.when(userRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(user);
        Mockito.when(pendingPositionRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(pendingPosition);
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        user.setBalance(10000.0);
        pendingPositionService.deletePosition(httpServletRequest, 1L);
        Mockito.verify(pendingPositionRepository, Mockito.times(1)).deleteById(ArgumentMatchers.anyLong());
    }
}