package com.cardemo.web.service;

import com.cardemo.common.repository.CreditCardRepository;
import com.cardemo.common.repository.CardXrefRepository;
import org.springframework.stereotype.Service;

/**
 * Card service replacing business logic from COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl.
 *
 * TODO: Implement card list with pagination from COCRDLIC.cbl
 * TODO: Implement card detail view from COCRDSLC.cbl
 * TODO: Implement card update from COCRDUPC.cbl
 * TODO: Cross-reference lookup via CVACT03Y (CardXref)
 */
@Service
public class CardService {

    private final CreditCardRepository creditCardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CreditCardRepository creditCardRepository, CardXrefRepository cardXrefRepository) {
        this.creditCardRepository = creditCardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    // TODO: Implement card management from COCRDLIC/COCRDSLC/COCRDUPC
}
