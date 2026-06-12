package com.carddemo.refdata.repository;

import java.util.Optional;

import com.carddemo.refdata.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class TransactionTypeRepositoryTest {

    @Autowired
    private TransactionTypeRepository repository;

    @Test
    void saveAndFind() {
        repository.save(new TransactionType("SA", "Sale"));
        Optional<TransactionType> found = repository.findById("SA");
        assertTrue(found.isPresent());
        assertEquals("Sale", found.get().getDescription());
    }

    @Test
    void findAll_afterMultipleSaves() {
        repository.save(new TransactionType("SA", "Sale"));
        repository.save(new TransactionType("RE", "Return"));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void deleteById() {
        repository.save(new TransactionType("SA", "Sale"));
        repository.deleteById("SA");
        assertTrue(repository.findById("SA").isEmpty());
    }
}
