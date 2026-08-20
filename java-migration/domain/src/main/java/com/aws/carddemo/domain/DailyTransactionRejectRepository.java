package com.aws.carddemo.domain;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DailyTransactionRejectRepository extends JpaRepository<DailyTransactionReject,String> { java.util.List<DailyTransactionReject> findByTranIdGreaterThanOrderByTranId(String id); }
