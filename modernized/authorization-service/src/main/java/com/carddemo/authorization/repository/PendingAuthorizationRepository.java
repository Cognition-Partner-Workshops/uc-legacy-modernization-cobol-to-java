package com.carddemo.authorization.repository;

import com.carddemo.authorization.model.PendingAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingAuthorizationRepository extends JpaRepository<PendingAuthorization, Long> {
    List<PendingAuthorization> findByCardNumAndStatus(String cardNum, String status);
    List<PendingAuthorization> findByStatus(String status);
    void deleteByStatus(String status);
}
