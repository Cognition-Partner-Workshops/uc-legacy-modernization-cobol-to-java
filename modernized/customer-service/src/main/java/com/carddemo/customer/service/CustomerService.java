package com.carddemo.customer.service;

import com.carddemo.common.dto.PageResponse;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import com.carddemo.customer.dto.CreateCustomerRequest;
import com.carddemo.customer.dto.CustomerDto;
import com.carddemo.customer.dto.UpdateCustomerRequest;
import com.carddemo.customer.model.Customer;
import com.carddemo.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerDto getCustomer(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "custId", id));
        return toDto(customer);
    }

    public CustomerDto searchBySsn(String ssn) {
        Customer customer = customerRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "ssn", "****" + ssn.substring(Math.max(0, ssn.length() - 4))));
        return toDto(customer);
    }

    public List<CustomerDto> searchByName(String lastName) {
        return customerRepository.findByLastNameContainingIgnoreCase(lastName).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CustomerDto createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsById(request.getCustId())) {
            throw new ValidationException("Customer with ID '" + request.getCustId() + "' already exists");
        }
        Customer customer = Customer.builder()
                .custId(request.getCustId())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .addrLine1(request.getAddrLine1())
                .addrLine2(request.getAddrLine2())
                .addrLine3(request.getAddrLine3())
                .addrStateCd(request.getAddrStateCd())
                .addrCountryCd(request.getAddrCountryCd())
                .addrZip(request.getAddrZip())
                .phoneNum1(request.getPhoneNum1())
                .phoneNum2(request.getPhoneNum2())
                .ssn(request.getSsn())
                .govtIssuedId(request.getGovtIssuedId())
                .dob(request.getDob())
                .eftAccountId(request.getEftAccountId())
                .priCardHolderInd(request.getPriCardHolderInd())
                .ficoCreditScore(request.getFicoCreditScore())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        customerRepository.save(customer);
        return toDto(customer);
    }

    public CustomerDto updateCustomer(String id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "custId", id));

        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getMiddleName() != null) customer.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getAddrLine1() != null) customer.setAddrLine1(request.getAddrLine1());
        if (request.getAddrLine2() != null) customer.setAddrLine2(request.getAddrLine2());
        if (request.getAddrLine3() != null) customer.setAddrLine3(request.getAddrLine3());
        if (request.getAddrStateCd() != null) customer.setAddrStateCd(request.getAddrStateCd());
        if (request.getAddrCountryCd() != null) customer.setAddrCountryCd(request.getAddrCountryCd());
        if (request.getAddrZip() != null) customer.setAddrZip(request.getAddrZip());
        if (request.getPhoneNum1() != null) customer.setPhoneNum1(request.getPhoneNum1());
        if (request.getPhoneNum2() != null) customer.setPhoneNum2(request.getPhoneNum2());
        if (request.getSsn() != null) customer.setSsn(request.getSsn());
        if (request.getGovtIssuedId() != null) customer.setGovtIssuedId(request.getGovtIssuedId());
        if (request.getDob() != null) customer.setDob(request.getDob());
        if (request.getEftAccountId() != null) customer.setEftAccountId(request.getEftAccountId());
        if (request.getPriCardHolderInd() != null) customer.setPriCardHolderInd(request.getPriCardHolderInd());
        if (request.getFicoCreditScore() != null) customer.setFicoCreditScore(request.getFicoCreditScore());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        return toDto(customer);
    }

    public PageResponse<CustomerDto> listCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        List<CustomerDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private CustomerDto toDto(Customer c) {
        return CustomerDto.builder()
                .custId(c.getCustId())
                .firstName(c.getFirstName())
                .middleName(c.getMiddleName())
                .lastName(c.getLastName())
                .addrLine1(c.getAddrLine1())
                .addrLine2(c.getAddrLine2())
                .addrLine3(c.getAddrLine3())
                .addrStateCd(c.getAddrStateCd())
                .addrCountryCd(c.getAddrCountryCd())
                .addrZip(c.getAddrZip())
                .phoneNum1(c.getPhoneNum1())
                .phoneNum2(c.getPhoneNum2())
                .ssn(CustomerDto.maskSsn(c.getSsn()))
                .govtIssuedId(c.getGovtIssuedId())
                .dob(c.getDob())
                .eftAccountId(c.getEftAccountId())
                .priCardHolderInd(c.getPriCardHolderInd())
                .ficoCreditScore(c.getFicoCreditScore())
                .build();
    }
}
