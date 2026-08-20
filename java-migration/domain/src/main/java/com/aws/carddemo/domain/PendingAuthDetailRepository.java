package com.aws.carddemo.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAuthDetailRepository extends JpaRepository<PendingAuthDetail, Long> {
  List<PendingAuthDetail> findByAcctIdOrderByAuthDateAscAuthTimeAsc(Long acctId);

  void deleteByAuthDateLessThan(String date);
}
