package com.carddemo.repository;

import com.carddemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    List<CardXref> findByAcctId(String acctId);
    List<CardXref> findByCustId(Long custId);
    Optional<CardXref> findFirstByAcctId(String acctId);
}
