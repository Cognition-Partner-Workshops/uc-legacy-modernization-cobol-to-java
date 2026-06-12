package com.carddemo.refdata.service;

import java.util.List;

import com.carddemo.refdata.entity.TransactionType;
import com.carddemo.refdata.repository.TransactionTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionTypeService {

    private final TransactionTypeRepository repository;

    public TransactionTypeService(TransactionTypeRepository repository) {
        this.repository = repository;
    }

    public List<TransactionType> findAll() {
        return repository.findAll();
    }

    public TransactionType findByCode(String code) {
        return repository.findById(code).orElse(null);
    }

    public TransactionType create(TransactionType entity) {
        if (repository.existsById(entity.getTypeCode())) {
            throw new DuplicateEntityException("Transaction type already exists: " + entity.getTypeCode());
        }
        return repository.save(entity);
    }

    public TransactionType update(String code, TransactionType entity) {
        TransactionType existing = repository.findById(code).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setDescription(entity.getDescription());
        return repository.save(existing);
    }

    public boolean delete(String code) {
        if (!repository.existsById(code)) {
            return false;
        }
        repository.deleteById(code);
        return true;
    }
}
