package com.carddemo.statement.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.statement.dto.StatementDto;
import com.carddemo.statement.model.Statement;
import com.carddemo.statement.repository.StatementRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TIER 3 — MEDIUM RISK: Statement Generation & Retrieval
 * Risk factors: incorrect billing periods, missing statements, file path errors
 */
@ExtendWith(MockitoExtension.class)
@Tag("risk-tier-3")
@DisplayName("Tier 3 (Medium): Statement Generation & Retrieval")
class RiskBasedStatementTest {

    @Mock
    private StatementRepository statementRepository;

    private StatementService statementService;

    @BeforeEach
    void setUp() {
        statementService = new StatementService(statementRepository);
    }

    @Test
    @DisplayName("T3-STMT-001: List statements returns correct count for account")
    void listStatements_returnsCorrectCount() {
        Statement s1 = Statement.builder()
                .id(1L).acctId("00000000001")
                .periodStart(LocalDate.of(2024, 11, 1)).periodEnd(LocalDate.of(2024, 11, 30))
                .generatedAt(LocalDateTime.now()).build();
        Statement s2 = Statement.builder()
                .id(2L).acctId("00000000001")
                .periodStart(LocalDate.of(2024, 12, 1)).periodEnd(LocalDate.of(2024, 12, 31))
                .generatedAt(LocalDateTime.now()).build();
        when(statementRepository.findByAcctId("00000000001")).thenReturn(List.of(s1, s2));

        List<StatementDto> result = statementService.listStatements("00000000001");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("T3-STMT-002: List statements for account with no statements returns empty")
    void listStatements_noStatements_returnsEmpty() {
        when(statementRepository.findByAcctId("99999999999")).thenReturn(Collections.emptyList());

        List<StatementDto> result = statementService.listStatements("99999999999");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("T3-STMT-003: Get nonexistent statement throws ResourceNotFoundException")
    void getStatement_notFound_throws() {
        when(statementRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.getStatement(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("T3-STMT-004: Get existing statement returns correct data")
    void getStatement_found_returnsCorrectData() {
        Statement stmt = Statement.builder()
                .id(1L).acctId("00000000001")
                .periodStart(LocalDate.of(2024, 11, 1))
                .periodEnd(LocalDate.of(2024, 11, 30))
                .filePath("/statements/00000000001_2024-11.pdf")
                .htmlPath("/statements/00000000001_2024-11.html")
                .generatedAt(LocalDateTime.of(2024, 12, 1, 1, 0, 0)).build();
        when(statementRepository.findById(1L)).thenReturn(Optional.of(stmt));

        StatementDto result = statementService.getStatement(1L);

        assertThat(result.getAcctId()).isEqualTo("00000000001");
        assertThat(result.getPeriodStart()).isEqualTo(LocalDate.of(2024, 11, 1));
        assertThat(result.getPeriodEnd()).isEqualTo(LocalDate.of(2024, 11, 30));
        assertThat(result.getFilePath()).contains("00000000001");
    }

    @Test
    @DisplayName("T3-STMT-005: Generate statement creates record with correct period")
    void generateStatement_createsCorrectPeriod() {
        when(statementRepository.save(any())).thenAnswer(i -> {
            Statement s = i.getArgument(0);
            s.setId(10L);
            return s;
        });

        StatementDto result = statementService.generateStatement(
                "00000000001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result.getAcctId()).isEqualTo("00000000001");
        assertThat(result.getPeriodStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.getPeriodEnd()).isEqualTo(LocalDate.of(2024, 1, 31));
    }

    @Test
    @DisplayName("T3-STMT-006: Generate statement file path contains account ID")
    void generateStatement_filePathContainsAcctId() {
        when(statementRepository.save(any())).thenAnswer(i -> {
            Statement s = i.getArgument(0);
            s.setId(11L);
            return s;
        });

        StatementDto result = statementService.generateStatement(
                "00000000003", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30));

        assertThat(result.getFilePath()).contains("00000000003");
    }
}
