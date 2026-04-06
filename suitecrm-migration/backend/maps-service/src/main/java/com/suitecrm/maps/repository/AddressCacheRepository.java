package com.suitecrm.maps.repository;
import com.suitecrm.maps.entity.AddressCache;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface AddressCacheRepository extends JpaRepository<AddressCache, UUID> {
    Optional<AddressCache> findByAddressAndDeletedFalse(String address);
}
