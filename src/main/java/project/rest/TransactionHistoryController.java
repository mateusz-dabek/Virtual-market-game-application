package project.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.dto.TransactionHistoryDTO;
import project.model.SymbolStatistic;
import project.security.JwtProvider;
import project.services.TransactionHistoryService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/api/transactionsHistory", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionHistoryController {
    private final TransactionHistoryService transactionHistoryService;
    private final JwtProvider jwtProvider;

    public TransactionHistoryController(TransactionHistoryService transactionHistoryService, JwtProvider jwtProvider) {
        this.transactionHistoryService = transactionHistoryService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping
    public List<TransactionHistoryDTO> getUserHistoryPositionsById(HttpServletRequest request) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(request));
        return transactionHistoryService.getUserHistoryPositionsById(id);
    }
}
