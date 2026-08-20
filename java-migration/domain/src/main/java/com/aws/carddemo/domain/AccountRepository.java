package com.aws.carddemo.domain;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account,Long> { java.util.List<Account> findByAcctIdGreaterThanOrderByAcctId(Long id); java.util.List<Account> findByGroupId(String groupId); }
