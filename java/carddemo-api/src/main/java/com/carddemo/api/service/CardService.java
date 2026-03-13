package com.carddemo.api.service;

import com.carddemo.common.dto.CardDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Card service replacing COBOL programs COCRDLIC, COCRDSLC, and COCRDUPC.
 * Handles card listing, detail view, and updates.
 *
 * Original COBOL: app/cbl/COCRDLIC.cbl (list), app/cbl/COCRDSLC.cbl (select),
 *                 app/cbl/COCRDUPC.cbl (update)
 * CICS Transactions: CCLI, CCDL, CCUP
 */
@Service
public class CardService {

    /**
     * List all cards.
     * Replaces COCRDLIC STARTBR/READNEXT browsing of CARDDAT VSAM file.
     */
    public List<CardDto> list() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COCRDLIC");
    }

    /**
     * Find card by card number.
     * Replaces COCRDSLC READ of CARDDAT VSAM file.
     */
    public CardDto findByCardNumber(String cardNumber) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COCRDSLC");
    }

    /**
     * Update an existing card.
     * Replaces COCRDUPC REWRITE of CARDDAT VSAM file.
     */
    public CardDto update(String cardNumber, CardDto cardDto) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COCRDUPC");
    }
}
