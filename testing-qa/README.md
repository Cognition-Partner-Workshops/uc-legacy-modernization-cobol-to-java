# testing-qa Module

QA test suite for the CardDemo mainframe-to-Java modernization project.

## Overview

This module contains comprehensive JUnit 5 test cases that validate the business logic and data integrity of the CardDemo application components as they are migrated from COBOL to Java.

## Test Coverage

| Test Class | COBOL Program(s) | Transaction(s) | Test Cases |
|:-----------|:-----------------|:---------------|:-----------|
| `SignOnTest` | COSGN00C | CC00 | 13 tests |
| `AccountManagementTest` | COACTVWC, COACTUPC | CAVW, CAUP | 11 tests |
| `CreditCardManagementTest` | COCRDLIC, COCRDSLC, COCRDUPC | CCLI, CCDL, CCUP | 10 tests |
| `TransactionProcessingTest` | COTRN00C, COTRN01C, COTRN02C | CT00, CT01, CT02 | 16 tests |
| `UserAdministrationTest` | COUSR00C-03C | CU00-CU03 | 12 tests |
| `BatchProcessingTest` | CBTRN02C, CBACT04C, CBSTM03A/B | POSTTRAN, INTCALC, CREASTMT | 11 tests |

**Total: 73 test cases**

## Model Classes

Java POJOs derived from COBOL copybooks:

| Model | Copybook | Description |
|:------|:---------|:------------|
| `UserSecurity` | CSUSR01Y | User security record (80 bytes) |
| `AccountRecord` | CVACT01Y | Account entity (300 bytes) |
| `CardRecord` | CVACT02Y | Credit card entity (150 bytes) |
| `TransactionRecord` | CVTRA05Y | Transaction record (350 bytes) |

## Prerequisites

- Java 17+
- Maven 3.8+

## Running Tests

```bash
cd testing-qa
mvn test
```
