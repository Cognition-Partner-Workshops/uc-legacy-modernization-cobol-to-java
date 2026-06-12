package com.carddemo.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.customer.entity.Customer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findById_existingCustomer_returnsCustomer() {
        Optional<Customer> customer = customerRepository.findById(1L);
        assertThat(customer).isPresent();
        assertThat(customer.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        Optional<Customer> customer = customerRepository.findById(999L);
        assertThat(customer).isEmpty();
    }

    @Test
    void findAll_returnsPaginated() {
        Page<Customer> page = customerRepository.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void search_byLastName_findsMatch() {
        Page<Customer> results = customerRepository.search("Doe", null, PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void search_byZip_findsMatch() {
        Page<Customer> results = customerRepository.search(null, "10001", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getZip()).isEqualTo("10001");
    }

    @Test
    void search_byLastNameAndZip_findsIntersection() {
        Page<Customer> results = customerRepository.search("Johnson", "75001", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
    }

    @Test
    void search_noMatch_returnsEmpty() {
        Page<Customer> results = customerRepository.search("ZZZ", "00000", PageRequest.of(0, 10));
        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void search_partialLastName_findsMatch() {
        Page<Customer> results = customerRepository.search("john", null, PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getLastName()).isEqualTo("Johnson");
    }
}
