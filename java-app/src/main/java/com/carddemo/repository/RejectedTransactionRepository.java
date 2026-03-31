package com.carddemo.repository;

import com.carddemo.model.RejectedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RejectedTransactionRepository extends JpaRepository<RejectedTransaction, Long> {
}
