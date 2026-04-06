package com.suitecrm.account.mapper;

import com.suitecrm.account.dto.AccountCreateRequest;
import com.suitecrm.account.dto.AccountDto;
import com.suitecrm.account.dto.AccountUpdateRequest;
import com.suitecrm.account.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto toDto(Account entity) {
        if (entity == null) return null;
        return AccountDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .accountType(entity.getAccountType())
                .industry(entity.getIndustry())
                .annualRevenue(entity.getAnnualRevenue())
                .employees(entity.getEmployees())
                .rating(entity.getRating())
                .phoneOffice(entity.getPhoneOffice())
                .phoneAlternate(entity.getPhoneAlternate())
                .phoneFax(entity.getPhoneFax())
                .website(entity.getWebsite())
                .email(entity.getEmail())
                .ownership(entity.getOwnership())
                .tickerSymbol(entity.getTickerSymbol())
                .sicCode(entity.getSicCode())
                .parentId(entity.getParentId())
                .billingAddressStreet(entity.getBillingAddressStreet())
                .billingAddressCity(entity.getBillingAddressCity())
                .billingAddressState(entity.getBillingAddressState())
                .billingAddressPostalcode(entity.getBillingAddressPostalcode())
                .billingAddressCountry(entity.getBillingAddressCountry())
                .shippingAddressStreet(entity.getShippingAddressStreet())
                .shippingAddressCity(entity.getShippingAddressCity())
                .shippingAddressState(entity.getShippingAddressState())
                .shippingAddressPostalcode(entity.getShippingAddressPostalcode())
                .shippingAddressCountry(entity.getShippingAddressCountry())
                .description(entity.getDescription())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }

    public Account toEntity(AccountCreateRequest request) {
        if (request == null) return null;
        return Account.builder()
                .name(request.getName())
                .accountType(request.getAccountType())
                .industry(request.getIndustry())
                .annualRevenue(request.getAnnualRevenue())
                .employees(request.getEmployees())
                .rating(request.getRating())
                .phoneOffice(request.getPhoneOffice())
                .phoneAlternate(request.getPhoneAlternate())
                .phoneFax(request.getPhoneFax())
                .website(request.getWebsite())
                .email(request.getEmail())
                .ownership(request.getOwnership())
                .tickerSymbol(request.getTickerSymbol())
                .sicCode(request.getSicCode())
                .parentId(request.getParentId())
                .billingAddressStreet(request.getBillingAddressStreet())
                .billingAddressCity(request.getBillingAddressCity())
                .billingAddressState(request.getBillingAddressState())
                .billingAddressPostalcode(request.getBillingAddressPostalcode())
                .billingAddressCountry(request.getBillingAddressCountry())
                .shippingAddressStreet(request.getShippingAddressStreet())
                .shippingAddressCity(request.getShippingAddressCity())
                .shippingAddressState(request.getShippingAddressState())
                .shippingAddressPostalcode(request.getShippingAddressPostalcode())
                .shippingAddressCountry(request.getShippingAddressCountry())
                .description(request.getDescription())
                .assignedUserId(request.getAssignedUserId())
                .build();
    }

    public void updateEntity(Account entity, AccountUpdateRequest request) {
        if (request == null || entity == null) return;
        entity.setName(request.getName());
        entity.setAccountType(request.getAccountType());
        entity.setIndustry(request.getIndustry());
        entity.setAnnualRevenue(request.getAnnualRevenue());
        entity.setEmployees(request.getEmployees());
        entity.setRating(request.getRating());
        entity.setPhoneOffice(request.getPhoneOffice());
        entity.setPhoneAlternate(request.getPhoneAlternate());
        entity.setPhoneFax(request.getPhoneFax());
        entity.setWebsite(request.getWebsite());
        entity.setEmail(request.getEmail());
        entity.setOwnership(request.getOwnership());
        entity.setTickerSymbol(request.getTickerSymbol());
        entity.setSicCode(request.getSicCode());
        entity.setParentId(request.getParentId());
        entity.setBillingAddressStreet(request.getBillingAddressStreet());
        entity.setBillingAddressCity(request.getBillingAddressCity());
        entity.setBillingAddressState(request.getBillingAddressState());
        entity.setBillingAddressPostalcode(request.getBillingAddressPostalcode());
        entity.setBillingAddressCountry(request.getBillingAddressCountry());
        entity.setShippingAddressStreet(request.getShippingAddressStreet());
        entity.setShippingAddressCity(request.getShippingAddressCity());
        entity.setShippingAddressState(request.getShippingAddressState());
        entity.setShippingAddressPostalcode(request.getShippingAddressPostalcode());
        entity.setShippingAddressCountry(request.getShippingAddressCountry());
        entity.setDescription(request.getDescription());
        entity.setAssignedUserId(request.getAssignedUserId());
    }
}
