package com.cardemo.service.batch;

import com.cardemo.model.CustomerRecord;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Customer File Processor - migrated from COBOL batch program CBCUS01C.cbl.
 * Reads and prints customer data file.
 * Original: Reads VSAM CUSTFILE (indexed) sequentially and displays records.
 */
@Service
public class CustomerFileProcessorService {

    private static final Logger log = LoggerFactory.getLogger(CustomerFileProcessorService.class);

    private final CustomerRepository customerRepository;

    public CustomerFileProcessorService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Process all customer records - migrated from main PROCEDURE DIVISION logic.
     *
     * @return number of customer records processed
     */
    public int processCustomerFile() {
        log.info("START OF EXECUTION OF PROGRAM CBCUS01C (CustomerFileProcessor)");

        List<CustomerRecord> customers = customerRepository.findAll();
        int count = 0;

        for (CustomerRecord customer : customers) {
            log.info("Customer: {}", customer);
            count++;
        }

        log.info("END OF EXECUTION OF PROGRAM CBCUS01C. Records processed: {}", count);
        return count;
    }
}
