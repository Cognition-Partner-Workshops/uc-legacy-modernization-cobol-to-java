package com.suitecrm.cases.repository;

import com.suitecrm.cases.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, UUID> {
    List<Release> findByDeletedFalseOrderByListOrderAsc();
    List<Release> findByStatusAndDeletedFalse(String status);
}
