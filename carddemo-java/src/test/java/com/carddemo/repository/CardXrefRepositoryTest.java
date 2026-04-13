package com.carddemo.repository;

import com.carddemo.entity.CardXref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CardXrefRepositoryTest {

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();

        CardXref xref1 = new CardXref();
        xref1.setCardNum("4000123456789010");
        xref1.setCustId(1L);
        xref1.setAcctId(1L);
        cardXrefRepository.save(xref1);

        CardXref xref2 = new CardXref();
        xref2.setCardNum("4000123456789020");
        xref2.setCustId(1L);
        xref2.setAcctId(1L);
        cardXrefRepository.save(xref2);

        CardXref xref3 = new CardXref();
        xref3.setCardNum("4000123456789030");
        xref3.setCustId(2L);
        xref3.setAcctId(2L);
        cardXrefRepository.save(xref3);
    }

    @Test
    void findById_existingXref_returnsXref() {
        Optional<CardXref> found = cardXrefRepository.findById("4000123456789010");

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getCustId());
        assertEquals(1L, found.get().getAcctId());
    }

    @Test
    void findByAcctId_returnsXrefsForAccount() {
        List<CardXref> xrefs = cardXrefRepository.findByAcctId(1L);
        assertEquals(2, xrefs.size());
    }

    @Test
    void findByCustId_returnsXrefsForCustomer() {
        List<CardXref> xrefs = cardXrefRepository.findByCustId(1L);
        assertEquals(2, xrefs.size());
    }

    @Test
    void findByAcctId_noXrefs_returnsEmptyList() {
        List<CardXref> xrefs = cardXrefRepository.findByAcctId(999L);
        assertTrue(xrefs.isEmpty());
    }

    @Test
    void findByCustId_noXrefs_returnsEmptyList() {
        List<CardXref> xrefs = cardXrefRepository.findByCustId(999L);
        assertTrue(xrefs.isEmpty());
    }

    @Test
    void delete_removesXref() {
        cardXrefRepository.deleteById("4000123456789010");
        assertFalse(cardXrefRepository.findById("4000123456789010").isPresent());
    }
}
