package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.AclRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AclRoleRepository extends JpaRepository<AclRole, UUID> {

    Optional<AclRole> findByIdAndDeletedFalse(UUID id);

    Page<AclRole> findByDeletedFalse(Pageable pageable);

    Optional<AclRole> findByNameAndDeletedFalse(String name);

    @Query("SELECT r FROM AclRole r WHERE r.deleted = false AND r.isAdmin = true")
    List<AclRole> findAdminRoles();

    @Query("SELECT r FROM AclRole r WHERE r.deleted = false AND LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<AclRole> searchByName(@Param("query") String query, Pageable pageable);
}
