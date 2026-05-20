package com.carddemo.transaction.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.dto.TransactionTypeDto;
import com.carddemo.transaction.model.TransactionType;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionTypeService {

    private final TransactionTypeRepository repository;

    public TransactionTypeService(TransactionTypeRepository repository) {
        this.repository = repository;
    }

    public List<TransactionTypeDto> listAll() {
        return repository.findAll().stream()
                .map(t -> TransactionTypeDto.builder()
                        .tranType(t.getTranType())
                        .typeDesc(t.getTypeDesc())
                        .build())
                .collect(Collectors.toList());
    }

    public TransactionTypeDto create(TransactionTypeDto dto) {
        TransactionType entity = TransactionType.builder()
                .tranType(dto.getTranType())
                .typeDesc(dto.getTypeDesc())
                .build();
        entity = repository.save(entity);
        return TransactionTypeDto.builder()
                .tranType(entity.getTranType())
                .typeDesc(entity.getTypeDesc())
                .build();
    }

    public TransactionTypeDto update(String code, TransactionTypeDto dto) {
        TransactionType entity = repository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionType", "tranType", code));
        entity.setTypeDesc(dto.getTypeDesc());
        entity = repository.save(entity);
        return TransactionTypeDto.builder()
                .tranType(entity.getTranType())
                .typeDesc(entity.getTypeDesc())
                .build();
    }

    public void delete(String code) {
        if (!repository.existsById(code)) {
            throw new ResourceNotFoundException("TransactionType", "tranType", code);
        }
        repository.deleteById(code);
    }
}
