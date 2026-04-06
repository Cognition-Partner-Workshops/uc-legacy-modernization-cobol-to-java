package com.suitecrm.contact.repository;

import com.suitecrm.contact.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    Optional<Contact> findByIdAndDeletedFalse(UUID id);

    Page<Contact> findByDeletedFalse(Pageable pageable);

    Page<Contact> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);

    Page<Contact> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.deleted = false AND " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.emailPrimary) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.phoneWork) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Contact> searchContacts(@Param("search") String search, Pageable pageable);

    List<Contact> findByEmailPrimaryAndDeletedFalse(String email);

    long countByAccountIdAndDeletedFalse(UUID accountId);
}
