package com.carddemo.statement.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.statement.dto.StatementDto;
import com.carddemo.statement.model.Statement;
import com.carddemo.statement.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private StatementRepository statementRepository;

    private StatementService statementService;

    @BeforeEach
    void setUp() {
        statementService = new StatementService(statementRepository);
    }

    @Test
    void listStatements_returnsList() {
        Statement stmt = Statement.builder()
                .id(1L)
                .acctId("00000000001")
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 1, 31))
                .generatedAt(LocalDateTime.now())
                .build();
        when(statementRepository.findByAcctId("00000000001")).thenReturn(List.of(stmt));

        List<StatementDto> result = statementService.listStatements("00000000001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAcctId()).isEqualTo("00000000001");
    }

    @Test
    void getStatement_notFound_throws() {
        when(statementRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.getStatement(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateStatement_createsStatement() {
        when(statementRepository.save(any())).thenAnswer(i -> {
            Statement s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        StatementDto result = statementService.generateStatement(
                "00000000001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result.getAcctId()).isEqualTo("00000000001");
        assertThat(result.getFilePath()).contains("00000000001");
    }
}
