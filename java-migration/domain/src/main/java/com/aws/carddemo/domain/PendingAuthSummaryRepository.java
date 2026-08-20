package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAuthSummaryRepository extends JpaRepository<PendingAuthSummary, Long> {}
