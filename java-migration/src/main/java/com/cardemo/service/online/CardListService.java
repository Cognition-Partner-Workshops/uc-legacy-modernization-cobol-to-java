/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.model.CardRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Credit Card List Service - migrated from COBOL program COCRDLIC.cbl.
 * Handles listing credit cards for an account.
 * Original: CICS program with TRANID CCLI, browses CARDAIX (alternate index by account).
 */
@Service
public class CardListService {

    private static final Logger log = LoggerFactory.getLogger(CardListService.class);
    private static final int PAGE_SIZE = 7;

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardListService(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List cards for an account - migrated from STARTBR/READNEXT on CARDAIX.
     */
    public List<CardRecord> listCardsForAccount(long acctId) {
        return cardRepository.findByCardAcctId(acctId);
    }

    /**
     * List all cards with pagination - migrated from browse logic.
     */
    public Page<CardRecord> listCards(int page) {
        return cardRepository.findAll(PageRequest.of(page, PAGE_SIZE, Sort.by("cardNum")));
    }

    /**
     * Get cross-reference records for a customer.
     */
    public List<CardXrefRecord> getXrefsForCustomer(long custId) {
        return cardXrefRepository.findByXrefCustId(custId);
    }
}
