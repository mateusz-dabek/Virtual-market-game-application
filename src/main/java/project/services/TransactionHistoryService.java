package project.services;

import org.springframework.stereotype.Service;
import project.dto.TransactionHistoryDTO;
import project.model.SymbolStatistic;
import project.model.TransactionHistory;
import project.repositories.TransactionHistoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    public TransactionHistoryService(TransactionHistoryRepository transactionHistoryRepository) {
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    public void createHistoryTransaction(TransactionHistory parsePosition) {
        transactionHistoryRepository.save(parsePosition);
    }

    public List<TransactionHistoryDTO> getUserHistoryPositionsById(Long id) {
        return transactionHistoryRepository.findByUserId(id)
                .stream()
                .map(TransactionHistory::mapToDTO)
                .collect(Collectors.toList());
    }

}
