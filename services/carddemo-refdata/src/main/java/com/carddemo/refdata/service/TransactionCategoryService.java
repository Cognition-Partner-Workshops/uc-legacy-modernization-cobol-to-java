package com.carddemo.refdata.service;

import java.util.List;

import com.carddemo.refdata.entity.TransactionCategory;
import com.carddemo.refdata.entity.TransactionCategoryId;
import com.carddemo.refdata.repository.TransactionCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionCategoryService {

    private final TransactionCategoryRepository repository;

    public TransactionCategoryService(TransactionCategoryRepository repository) {
        this.repository = repository;
    }

    public List<TransactionCategory> findAll() {
        return repository.findAll();
    }

    public TransactionCategory findByKey(String typeCode, int categoryCode) {
        return repository.findById(new TransactionCategoryId(typeCode, categoryCode)).orElse(null);
    }

    public TransactionCategory create(TransactionCategory entity) {
        TransactionCategoryId id = new TransactionCategoryId(entity.getTypeCode(), entity.getCategoryCode());
        if (repository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Transaction category already exists: " + entity.getTypeCode() + "/" + entity.getCategoryCode());
        }
        return repository.save(entity);
    }

    public TransactionCategory update(String typeCode, int categoryCode, TransactionCategory entity) {
        TransactionCategoryId id = new TransactionCategoryId(typeCode, categoryCode);
        TransactionCategory existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setDescription(entity.getDescription());
        return repository.save(existing);
    }

    public boolean delete(String typeCode, int categoryCode) {
        TransactionCategoryId id = new TransactionCategoryId(typeCode, categoryCode);
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
