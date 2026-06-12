package com.carddemo.refdata.service;

import java.util.List;

import com.carddemo.refdata.entity.DisclosureGroup;
import com.carddemo.refdata.entity.DisclosureGroupId;
import com.carddemo.refdata.repository.DisclosureGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DisclosureGroupService {

    private final DisclosureGroupRepository repository;

    public DisclosureGroupService(DisclosureGroupRepository repository) {
        this.repository = repository;
    }

    public List<DisclosureGroup> findAll() {
        return repository.findAll();
    }

    public DisclosureGroup findByKey(String groupId, String typeCode, int catCode) {
        return repository.findById(new DisclosureGroupId(groupId, typeCode, catCode)).orElse(null);
    }

    public DisclosureGroup create(DisclosureGroup entity) {
        DisclosureGroupId id = new DisclosureGroupId(
                entity.getAccountGroupId(),
                entity.getTransactionTypeCode(),
                entity.getTransactionCategoryCode());
        if (repository.existsById(id)) {
            throw new IllegalArgumentException("Disclosure group already exists: "
                    + entity.getAccountGroupId() + "/" + entity.getTransactionTypeCode()
                    + "/" + entity.getTransactionCategoryCode());
        }
        return repository.save(entity);
    }

    public DisclosureGroup update(String groupId, String typeCode, int catCode, DisclosureGroup entity) {
        DisclosureGroupId id = new DisclosureGroupId(groupId, typeCode, catCode);
        DisclosureGroup existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setInterestRate(entity.getInterestRate());
        return repository.save(existing);
    }

    public boolean delete(String groupId, String typeCode, int catCode) {
        DisclosureGroupId id = new DisclosureGroupId(groupId, typeCode, catCode);
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
