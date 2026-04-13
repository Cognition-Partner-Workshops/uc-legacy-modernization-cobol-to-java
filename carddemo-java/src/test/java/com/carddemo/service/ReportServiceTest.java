package com.carddemo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job transactionReportJob;

    @InjectMocks
    private ReportService reportService;

    @Test
    void requestReport_validDateRange_launchesJob() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);

        assertDoesNotThrow(() ->
                reportService.requestReport("TRANSACTION", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));

        verify(jobLauncher).run(eq(transactionReportJob), any(JobParameters.class));
    }

    @Test
    void requestReport_nullStartDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.requestReport("TRANSACTION", null, LocalDate.of(2024, 12, 31)));
    }

    @Test
    void requestReport_nullEndDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.requestReport("TRANSACTION", LocalDate.of(2024, 1, 1), null));
    }

    @Test
    void requestReport_startAfterEnd_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.requestReport("TRANSACTION",
                        LocalDate.of(2024, 12, 31), LocalDate.of(2024, 1, 1)));
    }
}
