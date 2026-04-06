package com.suitecrm.productcatalog.repository;

import com.suitecrm.productcatalog.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findByDeletedFalseOrderByListOrderAsc();

    List<ProductCategory> findByParentIdAndDeletedFalse(UUID parentId);

    List<ProductCategory> findByParentIdIsNullAndDeletedFalse();
}
