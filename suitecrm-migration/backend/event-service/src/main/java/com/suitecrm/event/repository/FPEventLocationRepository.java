package com.suitecrm.event.repository;

import com.suitecrm.event.entity.FPEventLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FPEventLocationRepository extends JpaRepository<FPEventLocation, UUID> {

    List<FPEventLocation> findByDeletedFalse();

    List<FPEventLocation> findByCityAndDeletedFalse(String city);
}
