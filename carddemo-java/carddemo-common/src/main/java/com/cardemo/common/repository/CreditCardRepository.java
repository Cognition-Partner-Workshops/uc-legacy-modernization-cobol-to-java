package com.cardemo.common.repository;

import com.cardemo.common.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for CreditCard entity (CVACT02Y.cpy → credit_cards table).
 *
 * TODO: Add queries for card list screen (COCRDLIC.cbl) with pagination
 */
@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, String> {

    List<CreditCard> findByAcctId(Long acctId);

    List<CreditCard> findByActiveStatus(String activeStatus);
}
