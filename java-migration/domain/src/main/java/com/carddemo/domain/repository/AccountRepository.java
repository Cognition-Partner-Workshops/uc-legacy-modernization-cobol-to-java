package com.carddemo.domain.repository;

import com.carddemo.domain.entity.Account;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, BigDecimal> {
}
