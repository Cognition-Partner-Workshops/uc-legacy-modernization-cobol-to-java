package com.suitecrm.email.repository;

import com.suitecrm.email.entity.EmailText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EmailTextRepository extends JpaRepository<EmailText, UUID> {
}
