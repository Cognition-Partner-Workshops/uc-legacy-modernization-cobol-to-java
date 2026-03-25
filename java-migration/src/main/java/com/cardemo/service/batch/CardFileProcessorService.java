/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.batch;

import com.cardemo.model.CardRecord;
import com.cardemo.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Card File Processor - migrated from COBOL batch program CBACT02C.cbl.
 * Reads and prints card data file.
 * Original: Reads VSAM CARDFILE (indexed) sequentially and displays records.
 */
@Service
public class CardFileProcessorService {

    private static final Logger log = LoggerFactory.getLogger(CardFileProcessorService.class);

    private final CardRepository cardRepository;

    public CardFileProcessorService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Process all card records - migrated from main PROCEDURE DIVISION logic.
     *
     * @return number of card records processed
     */
    public int processCardFile() {
        log.info("START OF EXECUTION OF PROGRAM CBACT02C (CardFileProcessor)");

        List<CardRecord> cards = cardRepository.findAll();
        int count = 0;

        for (CardRecord card : cards) {
            log.info("Card: {}", card);
            count++;
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT02C. Records processed: {}", count);
        return count;
    }
}
