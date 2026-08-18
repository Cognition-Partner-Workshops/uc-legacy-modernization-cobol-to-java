package com.carddemo.parity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Root of the frozen parity JSON contract
 * (see {@code carddemo-java/docs/parity-contract.md}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ParitySummary(
        String generatedAt,
        String sourceProgram,
        Totals summary,
        List<AccountParity> accounts) {

    public List<AccountParity> accounts() {
        return accounts == null ? List.of() : accounts;
    }

    public Totals summary() {
        return summary == null ? new Totals(0, 0, 0, 0) : summary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Totals(int total, int match, int diff, int missing) {
    }
}
