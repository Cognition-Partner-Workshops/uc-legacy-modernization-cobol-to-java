package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranCategoryBalanceRepository
    extends JpaRepository<TranCategoryBalance, TranCategoryBalanceId> {
  java.util.List<TranCategoryBalance> findByIdAcctId(Long acctId);

  java.util.List<TranCategoryBalance> findByIdAcctIdAndIdTypeCd(Long acctId, String typeCd);
}
