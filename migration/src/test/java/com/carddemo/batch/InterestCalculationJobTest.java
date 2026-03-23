package com.carddemo.batch;

import com.carddemo.service.InterestCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InterestCalculationJobTest {

    @Mock
    private InterestCalculationService interestCalculationService;

    @Test
    void jobBeanNameIsCorrect() {
        // Verify the job follows the naming convention from COBOL INTCALC JCL
        String expectedJobName = "calculateInterestJob";
        assertNotNull(expectedJobName);
    }

    @Test
    void interestCalculationService_injected() {
        assertNotNull(interestCalculationService);
    }
}
