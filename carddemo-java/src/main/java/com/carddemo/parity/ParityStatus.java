package com.carddemo.parity;

/** Per-account parity outcome, as defined by the frozen contract. */
public enum ParityStatus {
    MATCH,
    DIFF,
    MISSING
}
