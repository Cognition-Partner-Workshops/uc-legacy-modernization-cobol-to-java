package com.cardemo.common.repository;

import com.cardemo.common.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Account entity (CVACT01Y.cpy → accounts table).
 *
 * TODO: Add custom queries for account search used in COACTVWC.cbl and COACTUPC.cbl
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByActiveStatus(String activeStatus);

    List<Account> findByGroupId(String groupId);
}
