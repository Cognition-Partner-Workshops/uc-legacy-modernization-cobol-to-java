package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void showReportForm_returnsForm() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("report-request"))
                .andExpect(model().attributeExists("reportRequestForm"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void requestReport_redirectsToReports() throws Exception {
        doNothing().when(reportService).requestReport(any(), any(), any());

        mockMvc.perform(post("/reports")
                        .with(csrf())
                        .param("reportType", "TRANSACTION")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));
    }
}
