package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthFraudRepository extends JpaRepository<AuthFraud, AuthFraudId> {}
