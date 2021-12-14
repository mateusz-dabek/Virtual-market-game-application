package project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import project.model.TransactionHistory;

import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    List<TransactionHistory> findByUserId(Long id);
}
