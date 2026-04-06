package com.suitecrm.productcatalog.repository;

import com.suitecrm.productcatalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByStatusAndDeletedFalse(String status, Pageable pageable);

    Page<Product> findByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    List<Product> findByManufacturerIdAndDeletedFalse(UUID manufacturerId);

    List<Product> findByProductTypeIdAndDeletedFalse(UUID typeId);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.qtyInStock IS NOT NULL AND p.qtyInStock <= :threshold")
    List<Product> findLowStock(@Param("threshold") int threshold);
}
