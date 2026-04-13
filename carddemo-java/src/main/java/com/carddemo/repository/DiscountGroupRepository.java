package com.carddemo.repository;

import com.carddemo.entity.DiscountGroup;
import com.carddemo.entity.DiscountGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountGroupRepository extends JpaRepository<DiscountGroup, DiscountGroupId> {
}
