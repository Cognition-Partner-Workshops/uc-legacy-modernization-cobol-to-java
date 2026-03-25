package com.cardemo.batch;

import com.cardemo.model.Customer;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Customer Data Processor - converted from COBOL program CBCUS01C.cbl
 * 
 * Original: Customer File Read batch program
 * Reads all records from CUSTFILE (VSAM KSDS) sequentially and displays them.
 * 
 * COBOL logic flow:
 * 1000-CUSTFILE-GET-NEXT: READ CUSTFILE-FILE NEXT INTO CUSTOMER-RECORD
 * Loop until EOF, displaying each record.
 */
@Component
public class CustomerDataProcessor {

    private static final Logger log = LoggerFactory.getLogger(CustomerDataProcessor.class);

    private final CustomerRepository customerRepository;

    public CustomerDataProcessor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Process all customer records - equivalent to CBCUS01C.
     * Original: Sequential read of CUSTFILE VSAM KSDS file.
     * Returns count of records processed.
     */
    public int processAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        int count = 0;
        for (Customer customer : customers) {
            log.info("Customer ID: {}, Name: {} {} {}, State: {}, FICO: {}",
                    customer.getCustId(),
                    customer.getFirstName() != null ? customer.getFirstName().trim() : "",
                    customer.getMiddleName() != null ? customer.getMiddleName().trim() : "",
                    customer.getLastName() != null ? customer.getLastName().trim() : "",
                    customer.getAddrStateCd(),
                    customer.getFicoCreditScore());
            count++;
        }
        log.info("Total customer records processed: {}", count);
        return count;
    }
}
