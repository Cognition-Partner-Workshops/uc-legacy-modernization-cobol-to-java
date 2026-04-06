package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {
    List<Currency> findByDeletedFalse();
    Optional<Currency> findByIso4217AndDeletedFalse(String iso4217);
    List<Currency> findByStatusAndDeletedFalse(String status);
}
