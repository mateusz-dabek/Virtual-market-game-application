package project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import project.model.AccountActivationCode;

public interface AccountActivationCodeRepository  extends JpaRepository<AccountActivationCode, Long> {
    AccountActivationCode findByCode(String code);

    boolean existsByCode(String code);

    AccountActivationCode findByUserId(Long userId);

    boolean existsByUserId(Long id);


}
