package com.carddemo.statement.model;

import java.util.ArrayList;
import java.util.List;

public record Customer(String firstName, String lastName, String address1, String address2,
                       String address3, String state, String country, String zip) {
  public List<String> addressLines() {
    List<String> lines = new ArrayList<>();
    if (!address1.isBlank()) lines.add(address1);
    if (!address2.isBlank()) lines.add(address2);
    String locality = (address3 + " " + state + " " + country + " " + zip).trim();
    if (!locality.isBlank()) lines.add(locality);
    return lines;
  }
}
