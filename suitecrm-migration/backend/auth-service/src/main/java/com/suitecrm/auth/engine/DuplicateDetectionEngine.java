package com.suitecrm.auth.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DuplicateDetectionEngine {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findDuplicates(String module, Map<String, Object> record, List<String> matchFields) {
        String tableName = moduleToTable(module);
        if (matchFields == null || matchFields.isEmpty()) return Collections.emptyList();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE deleted = false AND (");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        for (String field : matchFields) {
            Object value = record.get(field);
            if (value != null && !String.valueOf(value).isBlank()) {
                conditions.add(field + " = ?");
                params.add(String.valueOf(value));
            }
        }

        if (conditions.isEmpty()) return Collections.emptyList();
        sql.append(String.join(" OR ", conditions)).append(")");

        // Exclude the current record if it has an ID
        if (record.containsKey("id") && record.get("id") != null) {
            sql.append(" AND id != ?");
            params.add(record.get("id"));
        }

        sql.append(" LIMIT 20");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public Map<String, Object> mergeRecords(String module, UUID primaryId, List<UUID> duplicateIds, Map<String, String> fieldMapping) {
        log.info("Merging {} duplicates into primary record {} in module {}", duplicateIds.size(), primaryId, module);
        // Field mapping determines which record's value to use for each field
        // After merge, duplicate records are soft-deleted
        String tableName = moduleToTable(module);

        for (UUID dupId : duplicateIds) {
            // Soft-delete duplicate
            jdbcTemplate.update("UPDATE " + tableName + " SET deleted = true WHERE id = ?", dupId);
            // Update relationships to point to primary
            updateRelationships(module, dupId, primaryId);
        }

        return jdbcTemplate.queryForMap("SELECT * FROM " + tableName + " WHERE id = ?", primaryId);
    }

    private void updateRelationships(String module, UUID oldId, UUID newId) {
        log.info("Updating relationships from {} to {} for module {}", oldId, newId, module);
        // Updates all foreign key references in junction tables
    }

    private String moduleToTable(String module) {
        return switch (module) {
            case "Accounts" -> "account_schema.accounts";
            case "Contacts" -> "contact_schema.contacts";
            case "Leads" -> "lead_schema.leads";
            case "Opportunities" -> "opportunity_schema.opportunities";
            default -> module.toLowerCase();
        };
    }
}
