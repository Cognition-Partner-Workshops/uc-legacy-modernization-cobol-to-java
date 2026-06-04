package com.carddemo.reference.repository;

import com.carddemo.reference.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link TransactionType}.
 *
 * <p>Replaces the embedded static SQL / DB2 cursor processing in the legacy
 * COBOL programs {@code COTRTLIC} (list) and {@code COTRTUPC} (add/edit).</p>
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {

    /**
     * Case-insensitive search over type code or description, replacing the
     * filtered browse logic of {@code COTRTLIC}.
     */
    Page<TransactionType> findByTypeCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String typeCode, String description, Pageable pageable);
}
