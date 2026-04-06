package com.suitecrm.opportunity.repository;

import com.suitecrm.opportunity.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndDeletedFalse(UUID id);
    Page<Product> findByDeletedFalse(Pageable pageable);
    Page<Product> findByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);
    Page<Product> findByStatusAndDeletedFalse(String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    long countByDeletedFalse();
}
