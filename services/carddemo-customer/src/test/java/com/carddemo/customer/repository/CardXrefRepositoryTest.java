package com.carddemo.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.customer.entity.CardXref;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CardXrefRepositoryTest {

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Test
    void findByCustomerId_customerWithMultipleCards_returnsAll() {
        List<CardXref> xrefs = cardXrefRepository.findByCustomerId(1L);
        assertThat(xrefs).hasSize(2);
        assertThat(xrefs).extracting(CardXref::getCardNumber)
                .containsExactlyInAnyOrder("4111111111111111", "4222222222222222");
    }

    @Test
    void findByCustomerId_customerWithOneCard_returnsOne() {
        List<CardXref> xrefs = cardXrefRepository.findByCustomerId(2L);
        assertThat(xrefs).hasSize(1);
        assertThat(xrefs.get(0).getAccountId()).isEqualTo(20000000002L);
    }

    @Test
    void findByCustomerId_nonExistingCustomer_returnsEmpty() {
        List<CardXref> xrefs = cardXrefRepository.findByCustomerId(999L);
        assertThat(xrefs).isEmpty();
    }
}
