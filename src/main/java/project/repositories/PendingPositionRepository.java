package project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import project.model.OpenPosition;
import project.model.PendingPosition;

import java.time.LocalDateTime;
import java.util.List;

public interface PendingPositionRepository extends JpaRepository<PendingPosition, Long> {
    List<PendingPosition> findByUserId(Long id);

//    void deleteByOpenTime(LocalDateTime openTime);
}
