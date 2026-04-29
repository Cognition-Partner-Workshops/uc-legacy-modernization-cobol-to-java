package com.carddemo.repository;

import com.carddemo.entity.CardCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {
}
