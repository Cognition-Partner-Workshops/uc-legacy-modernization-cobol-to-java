package com.suitecrm.opportunity.repository;

import com.suitecrm.opportunity.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    Optional<ProductCategory> findByIdAndDeletedFalse(UUID id);
    Page<ProductCategory> findByDeletedFalse(Pageable pageable);
    List<ProductCategory> findByParentCategoryIdAndDeletedFalse(UUID parentCategoryId);
    List<ProductCategory> findByIsParentTrueAndDeletedFalse();
}
