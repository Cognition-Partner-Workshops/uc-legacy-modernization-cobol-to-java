package com.suitecrm.auth.engine;

import com.suitecrm.auth.entity.SecurityGroupRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordLevelAclService {

    private final JdbcTemplate jdbcTemplate;

    public boolean hasAccess(UUID userId, UUID recordId, String module, String action) {
        // Check if user belongs to a security group that has access to this record
        String sql = "SELECT COUNT(*) FROM auth_schema.securitygroups_records sgr " +
            "JOIN auth_schema.securitygroups_users sgu ON sgr.securitygroup_id = sgu.securitygroup_id " +
            "WHERE sgu.user_id = ? AND sgr.record_id = ? AND sgr.module = ? AND sgr.deleted = false AND sgu.deleted = false";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, recordId, module);
        return count != null && count > 0;
    }

    public List<UUID> getAccessibleRecordIds(UUID userId, String module) {
        String sql = "SELECT DISTINCT sgr.record_id FROM auth_schema.securitygroups_records sgr " +
            "JOIN auth_schema.securitygroups_users sgu ON sgr.securitygroup_id = sgu.securitygroup_id " +
            "WHERE sgu.user_id = ? AND sgr.module = ? AND sgr.deleted = false AND sgu.deleted = false";
        return jdbcTemplate.queryForList(sql, UUID.class, userId, module);
    }

    public void grantAccess(UUID securityGroupId, UUID recordId, String module) {
        String sql = "INSERT INTO auth_schema.securitygroups_records (id, securitygroup_id, record_id, module, deleted) " +
            "VALUES (gen_random_uuid(), ?, ?, ?, false) " +
            "ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, securityGroupId, recordId, module);
    }

    public void revokeAccess(UUID securityGroupId, UUID recordId, String module) {
        String sql = "UPDATE auth_schema.securitygroups_records SET deleted = true " +
            "WHERE securitygroup_id = ? AND record_id = ? AND module = ?";
        jdbcTemplate.update(sql, securityGroupId, recordId, module);
    }
}
