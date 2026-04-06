package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.SecurityGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityGroupRepository extends JpaRepository<SecurityGroup, UUID> {

    Optional<SecurityGroup> findByIdAndDeletedFalse(UUID id);

    Page<SecurityGroup> findByDeletedFalse(Pageable pageable);

    Optional<SecurityGroup> findByNameAndDeletedFalse(String name);

    @Query("SELECT sg FROM SecurityGroup sg WHERE sg.deleted = false AND LOWER(sg.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<SecurityGroup> searchByName(@Param("query") String query, Pageable pageable);
}
