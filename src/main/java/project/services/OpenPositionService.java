package project.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import project.dto.OpenPositionDTO;
import project.model.MarketSymbol;
import project.model.OpenPosition;
import project.model.TransactionHistory;
import project.model.User;
import project.repositories.OpenPositionRepository;
import project.repositories.UserRepository;
import project.security.JwtProvider;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpenPositionService {
    private final OpenPositionRepository openPositionRepository;
    private final TransactionHistoryService transactionHistoryService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public OpenPositionService(OpenPositionRepository openPositionRepository, TransactionHistoryService transactionHistoryService, JwtProvider jwtProvider, UserRepository userRepository) {
        this.openPositionRepository = openPositionRepository;
        this.transactionHistoryService = transactionHistoryService;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    public void createPosition(OpenPosition parsePosition, Long userId) {

        parsePosition.setUserId(userId);
        parsePosition.setOpenTime(LocalDateTime.now());

        Optional<Double> currentPrice = PriceService.receiveCurrentPrice(parsePosition.getSymbol(), parsePosition.getTypePosition());
        parsePosition.setOpenPrice(currentPrice.get());
        BigDecimal liquidationPrice = PrecisionCalculate.calculateLiquidationPrice(parsePosition.getOpenPrice(), parsePosition.getLeverage(), parsePosition.getTypePosition());
        parsePosition.setLiquidationPrice(liquidationPrice.doubleValue());
        BigDecimal cost = PrecisionCalculate.calculateCost(parsePosition.getValue(), parsePosition.getLeverage());

        User user = userRepository.getOne(userId);
        if (cost.doubleValue() > user.getBalance()) {
            throw new AccessDeniedException("Insufficient balance");
        }
        BigDecimal balance = new BigDecimal(user.getBalance().toString());
        user.setBalance(balance.subtract(cost).doubleValue());
        userRepository.save(user);
        openPositionRepository.save(parsePosition);
    }

    public void deletePosition(HttpServletRequest request, Long id) {
        Long currentUserId = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        if (!isCurrentUserOwner(id, currentUserId))
            throw new AccessDeniedException("Wrong position id, you are not owner");
        saveInTransactionsHistory(id);

        User user = userRepository.getOne(currentUserId);
        OpenPosition openPosition = openPositionRepository.getOne(id);

        BigDecimal cost = PrecisionCalculate.calculateCost(openPosition.getValue(), openPosition.getLeverage());

        Optional<Double> currentPrice = PriceService.receiveCurrentPrice(openPosition.getSymbol(), openPosition.getTypePosition());

        BigDecimal profit = PrecisionCalculate.calculateProfit(openPosition, currentPrice.get());
        cost = cost.add(profit);
        user.setBalance(cost.add(new BigDecimal(user.getBalance().toString())).doubleValue());
        openPositionRepository.deleteById(id);
    }

    private void saveInTransactionsHistory(Long id) {
        OpenPosition openPosition = openPositionRepository.getOne(id);
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setSymbol(openPosition.getSymbol());
        transactionHistory.setTypePosition(openPosition.getTypePosition());
        transactionHistory.setLeverage(openPosition.getLeverage());
        transactionHistory.setOpenPrice(openPosition.getOpenPrice());
        transactionHistory.setOpenTime(openPosition.getOpenTime());
        transactionHistory.setUserId(openPosition.getUserId());
        transactionHistory.setValue(openPosition.getValue());

        Optional<Double> currentPrice = PriceService.receiveCurrentPrice(openPosition.getSymbol(), openPosition.getTypePosition());
        transactionHistory.setExitPrice(currentPrice.get());

        BigDecimal profit = PrecisionCalculate.calculateProfit(openPosition, currentPrice.get());
        transactionHistory.setProfit(profit.doubleValue());
        transactionHistoryService.createHistoryTransaction(transactionHistory);
    }

    private boolean isCurrentUserOwner(Long id, Long currentUserId) {
        return openPositionRepository.getOne(id).getUserId().equals(currentUserId);
    }

    public List<OpenPositionDTO> getUserOpenPositionsById(Long id) {
        return openPositionRepository.findByUserId(id)
                .stream()
                .map(OpenPosition::mapToDTO)
                .collect(Collectors.toList());
    }
}
