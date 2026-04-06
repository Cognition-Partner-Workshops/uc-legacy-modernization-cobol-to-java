package com.suitecrm.maps.repository;
import com.suitecrm.maps.entity.GeoMap;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface GeoMapRepository extends JpaRepository<GeoMap, UUID> {
    Page<GeoMap> findByDeletedFalse(Pageable pageable);
    Optional<GeoMap> findByIdAndDeletedFalse(UUID id);
}
