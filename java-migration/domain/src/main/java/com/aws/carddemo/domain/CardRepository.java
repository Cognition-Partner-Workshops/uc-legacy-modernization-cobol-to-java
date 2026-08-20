package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, String> {
  java.util.List<Card> findByCardNumGreaterThanOrderByCardNum(String cardNum);

  java.util.List<Card> findByAcctId(Long acctId);
}
