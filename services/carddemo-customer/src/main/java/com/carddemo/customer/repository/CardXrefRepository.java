package com.carddemo.customer.repository;

import com.carddemo.customer.entity.CardXref;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, Long> {

    List<CardXref> findByCustomerId(Long customerId);
}
