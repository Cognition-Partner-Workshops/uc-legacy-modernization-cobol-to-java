package com.suitecrm.email.repository;

import com.suitecrm.email.entity.InboundEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InboundEmailRepository extends JpaRepository<InboundEmail, UUID> {

    List<InboundEmail> findByDeletedFalseAndStatus(String status);

    List<InboundEmail> findByDeletedFalseAndIsPersonal(Boolean isPersonal);

    List<InboundEmail> findByCreatedByAndDeletedFalse(UUID createdBy);
}
