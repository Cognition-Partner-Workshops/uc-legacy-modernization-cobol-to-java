package com.carddemo.statement.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.statement.dto.StatementDto;
import com.carddemo.statement.model.Statement;
import com.carddemo.statement.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatementService {

    private final StatementRepository statementRepository;

    public StatementService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public List<StatementDto> listStatements(String acctId) {
        return statementRepository.findByAcctId(acctId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public StatementDto getStatement(Long id) {
        Statement stmt = statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement", "id", id.toString()));
        return toDto(stmt);
    }

    public StatementDto generateStatement(String acctId, LocalDate periodStart, LocalDate periodEnd) {
        Statement stmt = Statement.builder()
                .acctId(acctId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .filePath("/statements/" + acctId + "_" + periodStart + "_" + periodEnd + ".pdf")
                .htmlPath("/statements/" + acctId + "_" + periodStart + "_" + periodEnd + ".html")
                .generatedAt(LocalDateTime.now())
                .build();
        stmt = statementRepository.save(stmt);
        return toDto(stmt);
    }

    private StatementDto toDto(Statement s) {
        return StatementDto.builder()
                .id(s.getId())
                .acctId(s.getAcctId())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .filePath(s.getFilePath())
                .htmlPath(s.getHtmlPath())
                .generatedAt(s.getGeneratedAt())
                .build();
    }
}
