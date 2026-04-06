package com.suitecrm.productcatalog.repository;

import com.suitecrm.productcatalog.entity.ProductBundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductBundleRepository extends JpaRepository<ProductBundle, UUID> {

    Page<ProductBundle> findByDeletedFalse(Pageable pageable);
}
