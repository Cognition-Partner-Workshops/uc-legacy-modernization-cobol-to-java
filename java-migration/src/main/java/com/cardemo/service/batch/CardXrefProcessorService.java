package com.cardemo.service.batch;

import com.cardemo.model.CardXrefRecord;
import com.cardemo.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Card Cross-Reference File Processor - migrated from COBOL batch program CBACT03C.cbl.
 * Reads and prints account cross reference data file.
 * Original: Reads VSAM XREFFILE (indexed) sequentially and displays records.
 */
@Service
public class CardXrefProcessorService {

    private static final Logger log = LoggerFactory.getLogger(CardXrefProcessorService.class);

    private final CardXrefRepository cardXrefRepository;

    public CardXrefProcessorService(CardXrefRepository cardXrefRepository) {
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Process all card cross-reference records - migrated from main PROCEDURE DIVISION logic.
     *
     * @return number of cross-reference records processed
     */
    public int processXrefFile() {
        log.info("START OF EXECUTION OF PROGRAM CBACT03C (CardXrefProcessor)");

        List<CardXrefRecord> xrefs = cardXrefRepository.findAll();
        int count = 0;

        for (CardXrefRecord xref : xrefs) {
            log.info("XRef: {}", xref);
            count++;
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT03C. Records processed: {}", count);
        return count;
    }
}
