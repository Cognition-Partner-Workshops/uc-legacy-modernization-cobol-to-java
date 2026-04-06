package com.suitecrm.email.repository;

import com.suitecrm.email.entity.EmailAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailAddressRepository extends JpaRepository<EmailAddress, UUID> {
    Optional<EmailAddress> findByEmailAddressCapsAndDeletedFalse(String emailAddressCaps);
}
