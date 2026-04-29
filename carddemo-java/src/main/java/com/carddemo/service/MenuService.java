package com.carddemo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.carddemo.dto.MenuOptionDto;
import com.carddemo.dto.MenuResponseDto;
import com.carddemo.model.User;

@Service
public class MenuService {

    private static final List<MenuOptionDto> ADMIN_ONLY_OPTIONS;
    private static final List<MenuOptionDto> REGULAR_OPTIONS;

    static {
        ADMIN_ONLY_OPTIONS = List.of(
                MenuOptionDto.builder()
                        .optionNumber(1)
                        .label("User List")
                        .apiEndpoint("/api/admin/users")
                        .httpMethod("GET")
                        .description("List all users (Security)")
                        .cobolProgram("COUSR00C")
                        .cobolTransaction("CU00")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(2)
                        .label("User Add")
                        .apiEndpoint("/api/admin/users")
                        .httpMethod("POST")
                        .description("Add a new user (Security)")
                        .cobolProgram("COUSR01C")
                        .cobolTransaction("CU01")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(3)
                        .label("User Update")
                        .apiEndpoint("/api/admin/users/{userId}")
                        .httpMethod("PUT")
                        .description("Update user details (Security)")
                        .cobolProgram("COUSR02C")
                        .cobolTransaction("CU02")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(4)
                        .label("User Delete")
                        .apiEndpoint("/api/admin/users/{userId}")
                        .httpMethod("DELETE")
                        .description("Delete a user (Security)")
                        .cobolProgram("COUSR03C")
                        .cobolTransaction("CU03")
                        .implemented(false)
                        .build()
        );

        REGULAR_OPTIONS = List.of(
                MenuOptionDto.builder()
                        .optionNumber(1)
                        .label("Account View")
                        .apiEndpoint("/api/accounts/{accountId}")
                        .httpMethod("GET")
                        .description("View account details")
                        .cobolProgram("COACTVWC")
                        .cobolTransaction("CAVW")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(2)
                        .label("Account Update")
                        .apiEndpoint("/api/accounts/{accountId}")
                        .httpMethod("PUT")
                        .description("Update account information")
                        .cobolProgram("COACTUPC")
                        .cobolTransaction("CAUP")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(3)
                        .label("Credit Card List")
                        .apiEndpoint("/api/cards")
                        .httpMethod("GET")
                        .description("List credit cards")
                        .cobolProgram("COCRDLIC")
                        .cobolTransaction("CCLI")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(4)
                        .label("Credit Card View")
                        .apiEndpoint("/api/cards/{cardNumber}")
                        .httpMethod("GET")
                        .description("View credit card details")
                        .cobolProgram("COCRDSLC")
                        .cobolTransaction("CCDL")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(5)
                        .label("Credit Card Update")
                        .apiEndpoint("/api/cards/{cardNumber}")
                        .httpMethod("PUT")
                        .description("Update credit card information")
                        .cobolProgram("COCRDUPC")
                        .cobolTransaction("CCUP")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(6)
                        .label("Transaction List")
                        .apiEndpoint("/api/transactions")
                        .httpMethod("GET")
                        .description("List transactions")
                        .cobolProgram("COTRN00C")
                        .cobolTransaction("CT00")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(7)
                        .label("Transaction View")
                        .apiEndpoint("/api/transactions/{transactionId}")
                        .httpMethod("GET")
                        .description("View transaction details")
                        .cobolProgram("COTRN01C")
                        .cobolTransaction("CT01")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(8)
                        .label("Transaction Add")
                        .apiEndpoint("/api/transactions")
                        .httpMethod("POST")
                        .description("Add a new transaction")
                        .cobolProgram("COTRN02C")
                        .cobolTransaction("CT02")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(9)
                        .label("Transaction Reports")
                        .apiEndpoint("/api/reports/transactions")
                        .httpMethod("POST")
                        .description("Generate transaction reports")
                        .cobolProgram("CORPT00C")
                        .cobolTransaction("CR00")
                        .implemented(false)
                        .build(),
                MenuOptionDto.builder()
                        .optionNumber(10)
                        .label("Bill Payment")
                        .apiEndpoint("/api/payments")
                        .httpMethod("POST")
                        .description("Process bill payment")
                        .cobolProgram("COBIL00C")
                        .cobolTransaction("CB00")
                        .implemented(false)
                        .build()
        );
    }

    public MenuResponseDto getMenuForUser(User user) {
        List<MenuOptionDto> options;
        if ("A".equals(user.getUserType())) {
            options = buildAdminMenu();
        } else {
            options = buildRegularMenu();
        }

        return MenuResponseDto.builder()
                .userId(user.getUserId())
                .userType(user.getUserType())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .menuOptions(options)
                .build();
    }

    private List<MenuOptionDto> buildAdminMenu() {
        List<MenuOptionDto> allOptions = new ArrayList<>();

        for (MenuOptionDto opt : ADMIN_ONLY_OPTIONS) {
            allOptions.add(MenuOptionDto.builder()
                    .optionNumber(opt.getOptionNumber())
                    .label(opt.getLabel())
                    .apiEndpoint(opt.getApiEndpoint())
                    .httpMethod(opt.getHttpMethod())
                    .description(opt.getDescription())
                    .cobolProgram(opt.getCobolProgram())
                    .cobolTransaction(opt.getCobolTransaction())
                    .implemented(opt.isImplemented())
                    .build());
        }

        int offset = ADMIN_ONLY_OPTIONS.size();
        for (MenuOptionDto opt : REGULAR_OPTIONS) {
            allOptions.add(MenuOptionDto.builder()
                    .optionNumber(offset + opt.getOptionNumber())
                    .label(opt.getLabel())
                    .apiEndpoint(opt.getApiEndpoint())
                    .httpMethod(opt.getHttpMethod())
                    .description(opt.getDescription())
                    .cobolProgram(opt.getCobolProgram())
                    .cobolTransaction(opt.getCobolTransaction())
                    .implemented(opt.isImplemented())
                    .build());
        }

        return Collections.unmodifiableList(allOptions);
    }

    private List<MenuOptionDto> buildRegularMenu() {
        List<MenuOptionDto> options = new ArrayList<>();
        for (MenuOptionDto opt : REGULAR_OPTIONS) {
            options.add(MenuOptionDto.builder()
                    .optionNumber(opt.getOptionNumber())
                    .label(opt.getLabel())
                    .apiEndpoint(opt.getApiEndpoint())
                    .httpMethod(opt.getHttpMethod())
                    .description(opt.getDescription())
                    .cobolProgram(opt.getCobolProgram())
                    .cobolTransaction(opt.getCobolTransaction())
                    .implemented(opt.isImplemented())
                    .build());
        }
        return Collections.unmodifiableList(options);
    }
}
