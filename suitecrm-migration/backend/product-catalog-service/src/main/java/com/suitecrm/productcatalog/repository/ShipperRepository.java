package com.suitecrm.productcatalog.repository;

import com.suitecrm.productcatalog.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, UUID> {

    List<Shipper> findByDeletedFalse();
}
