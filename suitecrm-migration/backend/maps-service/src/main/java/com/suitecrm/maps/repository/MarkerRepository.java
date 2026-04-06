package com.suitecrm.maps.repository;
import com.suitecrm.maps.entity.Marker;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface MarkerRepository extends JpaRepository<Marker, UUID> {
    Page<Marker> findByDeletedFalse(Pageable pageable);
    Optional<Marker> findByIdAndDeletedFalse(UUID id);
    List<Marker> findByRelatedModuleAndRelatedIdAndDeletedFalse(String module, UUID relatedId);
}
