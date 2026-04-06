package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.AclAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AclActionRepository extends JpaRepository<AclAction, UUID> {

    List<AclAction> findByCategoryAndDeletedFalse(String category);

    @Query("SELECT a FROM AclAction a WHERE a.deleted = false AND a.name = :name AND a.category = :category")
    List<AclAction> findByNameAndCategory(@Param("name") String name, @Param("category") String category);

    @Query("SELECT DISTINCT a.category FROM AclAction a WHERE a.deleted = false ORDER BY a.category")
    List<String> findDistinctCategories();

    @Query("SELECT a FROM AclAction a WHERE a.deleted = false AND a.acltype = :acltype")
    List<AclAction> findByAclType(@Param("acltype") String acltype);
}
