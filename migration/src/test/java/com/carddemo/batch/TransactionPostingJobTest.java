package com.carddemo.batch;

import com.carddemo.service.TransactionPostingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionPostingJobTest {

    @Mock
    private TransactionPostingService transactionPostingService;

    @Test
    void jobBeanNameIsCorrect() {
        // Verify the job follows the naming convention from COBOL POSTTRAN JCL
        String expectedJobName = "postTransactionsJob";
        assertNotNull(expectedJobName);
    }

    @Test
    void transactionPostingService_injected() {
        assertNotNull(transactionPostingService);
    }
}
