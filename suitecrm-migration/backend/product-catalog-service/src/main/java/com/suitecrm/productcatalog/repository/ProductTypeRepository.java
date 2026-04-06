package com.suitecrm.productcatalog.repository;

import com.suitecrm.productcatalog.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, UUID> {

    List<ProductType> findByDeletedFalse();
}
