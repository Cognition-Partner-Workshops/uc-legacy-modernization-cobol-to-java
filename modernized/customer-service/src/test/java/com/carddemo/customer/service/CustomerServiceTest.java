package com.carddemo.customer.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import com.carddemo.customer.dto.CreateCustomerRequest;
import com.carddemo.customer.dto.CustomerDto;
import com.carddemo.customer.dto.UpdateCustomerRequest;
import com.carddemo.customer.model.Customer;
import com.carddemo.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void getCustomer_shouldReturnCustomerWithMaskedSsn() {
        Customer customer = Customer.builder()
                .custId("000000001")
                .firstName("John")
                .lastName("Doe")
                .ssn("123456789")
                .build();
        when(customerRepository.findById("000000001")).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.getCustomer("000000001");

        assertThat(result.getCustId()).isEqualTo("000000001");
        assertThat(result.getSsn()).isEqualTo("***-**-6789");
    }

    @Test
    void getCustomer_notFound_shouldThrow() {
        when(customerRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomer("999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchBySsn_shouldReturnCustomer() {
        Customer customer = Customer.builder()
                .custId("000000001")
                .firstName("John")
                .lastName("Doe")
                .ssn("123456789")
                .build();
        when(customerRepository.findBySsn("123456789")).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.searchBySsn("123456789");

        assertThat(result.getCustId()).isEqualTo("000000001");
    }

    @Test
    void searchByName_shouldReturnMatches() {
        Customer customer = Customer.builder()
                .custId("000000001")
                .firstName("John")
                .lastName("Doe")
                .build();
        when(customerRepository.findByLastNameContainingIgnoreCase("Doe")).thenReturn(List.of(customer));

        List<CustomerDto> results = customerService.searchByName("Doe");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void createCustomer_shouldSucceed() {
        when(customerRepository.existsById("000000099")).thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .custId("000000099")
                .firstName("Jane")
                .lastName("Smith")
                .ssn("987654321")
                .dob(LocalDate.of(1990, 1, 15))
                .build();
        CustomerDto result = customerService.createCustomer(request);

        assertThat(result.getCustId()).isEqualTo("000000099");
        assertThat(result.getSsn()).isEqualTo("***-**-4321");
    }

    @Test
    void createCustomer_duplicate_shouldThrow() {
        when(customerRepository.existsById("000000001")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(
                CreateCustomerRequest.builder().custId("000000001").firstName("A").lastName("B").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateCustomer_shouldUpdateFields() {
        Customer customer = Customer.builder()
                .custId("000000001")
                .firstName("John")
                .lastName("Doe")
                .build();
        when(customerRepository.findById("000000001")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .firstName("Updated")
                .build();
        CustomerDto result = customerService.updateCustomer("000000001", request);

        assertThat(result.getFirstName()).isEqualTo("Updated");
    }
}
