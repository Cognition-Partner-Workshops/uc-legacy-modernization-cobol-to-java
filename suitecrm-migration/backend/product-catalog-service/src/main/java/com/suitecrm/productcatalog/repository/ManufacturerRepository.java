package com.suitecrm.productcatalog.repository;

import com.suitecrm.productcatalog.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, UUID> {

    List<Manufacturer> findByDeletedFalse();

    List<Manufacturer> findByStatusAndDeletedFalse(String status);
}
