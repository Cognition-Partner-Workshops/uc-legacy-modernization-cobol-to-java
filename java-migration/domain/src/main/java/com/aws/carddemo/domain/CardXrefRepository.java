package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardXrefRepository extends JpaRepository<CardXref, CardXrefId> {
  java.util.List<CardXref> findByAcctId(Long acctId);

  java.util.List<CardXref> findByCustId(Integer custId);
}
