package com.carddemo.statement;

public record Customer(String id, String firstName, String lastName, String address1,
                       String address2, String address3, String state, String country,
                       String zip, int ficoScore) {}
