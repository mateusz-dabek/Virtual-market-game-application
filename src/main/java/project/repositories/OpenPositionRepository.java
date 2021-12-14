package project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import project.model.OpenPosition;

import java.time.LocalDateTime;
import java.util.List;

public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    List<OpenPosition> findByUserId(Long id);
    
//    void deleteByOpenTime(LocalDateTime openTime);
}
