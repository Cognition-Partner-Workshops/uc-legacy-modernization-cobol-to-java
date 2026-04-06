package com.suitecrm.maps.repository;
import com.suitecrm.maps.entity.Area;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {
    Page<Area> findByDeletedFalse(Pageable pageable);
    Optional<Area> findByIdAndDeletedFalse(UUID id);
}
