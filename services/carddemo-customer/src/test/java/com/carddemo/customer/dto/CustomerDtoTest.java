package com.carddemo.customer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.carddemo.customer.entity.Customer;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CustomerDtoTest {

    @Test
    void fromEntity_masksFullSsn() {
        Customer customer = buildCustomer("123456789");
        CustomerDto dto = CustomerDto.fromEntity(customer);

        assertEquals("***-**-6789", dto.getSsnMasked());
    }

    @Test
    void fromEntity_masksShortSsn() {
        Customer customer = buildCustomer("12");
        CustomerDto dto = CustomerDto.fromEntity(customer);

        assertEquals("***-**-****", dto.getSsnMasked());
    }

    @Test
    void fromEntity_masksNullSsn() {
        Customer customer = buildCustomer(null);
        CustomerDto dto = CustomerDto.fromEntity(customer);

        assertEquals("***-**-****", dto.getSsnMasked());
    }

    @Test
    void fromEntity_mapsAllFields() {
        Customer customer = new Customer();
        customer.setId(42L);
        customer.setFirstName("John");
        customer.setMiddleName("M");
        customer.setLastName("Smith");
        customer.setAddressLine1("123 Main St");
        customer.setAddressLine2("Apt 4B");
        customer.setAddressLine3("");
        customer.setStateCode("NY");
        customer.setCountryCode("USA");
        customer.setZip("10001");
        customer.setPhoneNumber1("212-555-0100");
        customer.setPhoneNumber2("212-555-0101");
        customer.setSsn("123456789");
        customer.setGovtIssuedId("DL-NY-123");
        customer.setDateOfBirth(LocalDate.of(1985, 3, 15));
        customer.setEftAccountId("EFT001");
        customer.setPrimaryCardHolderInd("Y");
        customer.setFicoCreditScore(750);

        CustomerDto dto = CustomerDto.fromEntity(customer);

        assertNotNull(dto);
        assertEquals(42L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("M", dto.getMiddleName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("123 Main St", dto.getAddressLine1());
        assertEquals("NY", dto.getStateCode());
        assertEquals("USA", dto.getCountryCode());
        assertEquals("10001", dto.getZip());
        assertEquals("***-**-6789", dto.getSsnMasked());
        assertEquals(LocalDate.of(1985, 3, 15), dto.getDateOfBirth());
        assertEquals("Y", dto.getPrimaryCardHolderInd());
        assertEquals(750, dto.getFicoCreditScore());
    }

    private Customer buildCustomer(String ssn) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setSsn(ssn);
        return customer;
    }
}
