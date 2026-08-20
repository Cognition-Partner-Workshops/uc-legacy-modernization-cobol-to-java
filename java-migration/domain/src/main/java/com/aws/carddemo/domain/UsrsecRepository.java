package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsrsecRepository extends JpaRepository<Usrsec, String> {
  java.util.List<Usrsec> findBySecUsrIdGreaterThanOrderBySecUsrId(String id);

  java.util.Optional<Usrsec> findBySecUsrId(String id);
}
