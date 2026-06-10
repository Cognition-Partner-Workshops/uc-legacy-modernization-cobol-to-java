package com.carddemo.repository;

import com.carddemo.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByActiveStatus(String status);

    Page<Account> findByGroupId(String groupId, Pageable pageable);

    List<Account> findByAddressZip(String zip);
}
