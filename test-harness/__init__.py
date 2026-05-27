"""
CardDemo Migration Test Harness

A comprehensive testing framework for validating the COBOL-to-Java migration
of the CardDemo credit card management application.

Four testing dimensions:
1. Golden-file: Parse ASCII data and compare against known-good references
2. Differential: Compare COBOL vs Java outputs side-by-side
3. Reconciliation: Verify cross-file integrity and business rules
4. Contract: Validate records against copybook-derived schemas
"""

__version__ = "1.0.0"
