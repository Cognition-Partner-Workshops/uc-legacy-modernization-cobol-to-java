package com.carddemo.statement.viewer;

import java.util.List;

public record Customer(String firstName, String lastName, List<String> addressLines) {
    public Customer {
        addressLines = List.copyOf(addressLines);
    }
}
