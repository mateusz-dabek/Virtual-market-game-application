package project.services;

import com.sun.javafx.css.CalculatedValue;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import project.dto.PendingPositionDTO;
import project.model.MarketSymbol;
import project.model.OpenPosition;
import project.model.PendingPosition;
import project.model.User;
import project.repositories.PendingPositionRepository;
import project.repositories.UserRepository;
import project.security.JwtProvider;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PendingPositionService {
    private final PendingPositionRepository pendingPositionRepository;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public PendingPositionService(PendingPositionRepository pendingPositionRepository, JwtProvider jwtProvider, UserRepository userRepository) {
        this.pendingPositionRepository = pendingPositionRepository;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    public void createPosition(PendingPosition parsePosition) {
        Optional<Double> currentPrice = PriceService.receiveCurrentPrice(parsePosition.getSymbol(), parsePosition.getTypePosition());
        if (currentPrice.get() <= parsePosition.getPriceExecute())
            parsePosition.setSmallerThanCurrentPrice(true);
        else parsePosition.setSmallerThanCurrentPrice(false);

        BigDecimal cost = PrecisionCalculate.calculateCost(parsePosition.getValue(), parsePosition.getLeverage());

        User user = userRepository.getOne(parsePosition.getUserId());
        if (cost.doubleValue() > user.getBalance()) {
            throw new AccessDeniedException("Insufficient balance, reduce value");
        }
        BigDecimal balance = new BigDecimal(user.getBalance().toString());
        user.setBalance(balance.subtract(cost).doubleValue());
        userRepository.save(user);
        pendingPositionRepository.save(parsePosition);
    }

    public void deletePosition(HttpServletRequest request, Long id) {
        Long currentUserId = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        if (!isCurrentUserOwner(id, currentUserId))
            throw new AccessDeniedException("Wrong position id, you are not owner");

        PendingPosition pendingPosition = pendingPositionRepository.getOne(id);
        User user = userRepository.getOne(currentUserId);

        BigDecimal cost = PrecisionCalculate.calculateCost(pendingPosition.getValue(), pendingPosition.getLeverage());
        user.setBalance(cost.add(new BigDecimal(user.getBalance().toString())).doubleValue());

        userRepository.save(user);
        pendingPositionRepository.deleteById(id);
    }

    private boolean isCurrentUserOwner(Long id, Long currentUserId) {
        return pendingPositionRepository.getOne(id).getUserId().equals(currentUserId);
    }

    public List<PendingPositionDTO> getUserOpenPositionsById(Long id) {
        return pendingPositionRepository.findByUserId(id)
                .stream()
                .map(PendingPosition::mapToDTO)
                .collect(Collectors.toList());
    }
}
