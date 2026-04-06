package com.suitecrm.kb.repository;

import com.suitecrm.kb.entity.KBTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KBTagRepository extends JpaRepository<KBTag, UUID> {

    List<KBTag> findByDeletedFalse();

    Optional<KBTag> findByNameAndDeletedFalse(String name);

    List<KBTag> findByNameContainingIgnoreCaseAndDeletedFalse(String query);
}
