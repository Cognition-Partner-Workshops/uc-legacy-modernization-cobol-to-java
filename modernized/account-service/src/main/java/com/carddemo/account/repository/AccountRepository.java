package com.carddemo.account.repository;

import com.carddemo.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByActiveStatus(Character status);

    List<Account> findByGroupId(String groupId);
}
