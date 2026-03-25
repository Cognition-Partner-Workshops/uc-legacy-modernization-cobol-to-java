"""
test_contracts.py - Contract validation tests for CardDemo copybook layouts.

Validates that:
1. Each copybook layout has the correct total record length.
2. Field offsets are contiguous (no gaps or overlaps).
3. The Java type mapping expectations are defined for all field types.
"""

from __future__ import annotations

import sys

import pytest

# Modules are bootstrapped into sys.modules by conftest.py
_contracts = sys.modules["test_harness.contracts"]
_parser = sys.modules["test_harness.copybook_parser"]
_generator = sys.modules["test_harness.golden_file_generator"]

ALL_BATCH_CONTRACTS = _contracts.ALL_BATCH_CONTRACTS
COBOL_TO_JAVA_TYPE = _contracts.COBOL_TO_JAVA_TYPE
validate_batch_io_contract = _contracts.validate_batch_io_contract
validate_record_layout_offsets = _contracts.validate_record_layout_offsets
pic_type = _parser.pic_type
ACCOUNT_LAYOUT = _generator.ACCOUNT_LAYOUT
CARD_LAYOUT = _generator.CARD_LAYOUT
CARDXREF_LAYOUT = _generator.CARDXREF_LAYOUT
CUSTOMER_LAYOUT = _generator.CUSTOMER_LAYOUT
DISCGRP_LAYOUT = _generator.DISCGRP_LAYOUT
TCATBAL_LAYOUT = _generator.TCATBAL_LAYOUT
TRANCATG_LAYOUT = _generator.TRANCATG_LAYOUT
TRANSACTION_LAYOUT = _generator.TRANSACTION_LAYOUT
TRANTYPE_LAYOUT = _generator.TRANTYPE_LAYOUT


class TestCopybookLayoutIntegrity:
    """Verify that our layout definitions match the declared record lengths."""

    @pytest.mark.parametrize(
        "name, layout, expected_length",
        [
            ("CVACT01Y (ACCOUNT)", ACCOUNT_LAYOUT, 300),
            ("CVACT02Y (CARD)", CARD_LAYOUT, 150),
            ("CVCUS01Y (CUSTOMER)", CUSTOMER_LAYOUT, 500),
            ("CVACT03Y (CARD-XREF)", CARDXREF_LAYOUT, 50),
            ("CVTRA05Y (TRANSACTION)", TRANSACTION_LAYOUT, 350),
            ("CVTRA03Y (TRAN-TYPE)", TRANTYPE_LAYOUT, 60),
            ("CVTRA04Y (TRAN-CATG)", TRANCATG_LAYOUT, 60),
            ("CVTRA01Y (TCAT-BAL)", TCATBAL_LAYOUT, 50),
            ("CVTRA02Y (DISC-GRP)", DISCGRP_LAYOUT, 50),
        ],
    )
    def test_record_length(self, name, layout, expected_length):
        report = validate_record_layout_offsets(
            copybook_name=name,
            copybook_layout=layout,
            expected_record_length=expected_length,
        )
        assert report.passed, (
            f"{name}: {[v.message for v in report.violations]}"
        )

    @pytest.mark.parametrize(
        "name, layout",
        [
            ("CVACT01Y", ACCOUNT_LAYOUT),
            ("CVACT02Y", CARD_LAYOUT),
            ("CVCUS01Y", CUSTOMER_LAYOUT),
            ("CVACT03Y", CARDXREF_LAYOUT),
            ("CVTRA05Y", TRANSACTION_LAYOUT),
            ("CVTRA03Y", TRANTYPE_LAYOUT),
            ("CVTRA04Y", TRANCATG_LAYOUT),
            ("CVTRA01Y", TCATBAL_LAYOUT),
            ("CVTRA02Y", DISCGRP_LAYOUT),
        ],
    )
    def test_contiguous_offsets(self, name, layout):
        """Verify that field offsets are contiguous with no gaps."""
        expected_offset = 0
        for f in layout:
            assert f.offset == expected_offset, (
                f"{name}.{f.name}: expected offset {expected_offset}, got {f.offset}"
            )
            expected_offset += f.length


class TestJavaTypeMappings:
    """Verify that every PIC type used in our layouts has a Java mapping."""

    def test_all_pic_types_mapped(self):
        all_layouts = [
            ACCOUNT_LAYOUT, CARD_LAYOUT, CUSTOMER_LAYOUT, CARDXREF_LAYOUT,
            TRANSACTION_LAYOUT, TRANTYPE_LAYOUT, TRANCATG_LAYOUT,
            TCATBAL_LAYOUT, DISCGRP_LAYOUT,
        ]
        unmapped = set()
        for layout in all_layouts:
            for f in layout:
                if f.name.upper().startswith("FILLER"):
                    continue
                ptype = pic_type(f.pic)
                if ptype not in COBOL_TO_JAVA_TYPE:
                    unmapped.add(ptype)

        assert len(unmapped) == 0, f"Unmapped PIC types: {unmapped}"


class TestBatchIOContracts:
    """Verify that batch I/O contract definitions are well-formed."""

    @pytest.mark.parametrize("contract", ALL_BATCH_CONTRACTS, ids=lambda c: c.job_name)
    def test_contract_has_inputs_and_outputs(self, contract):
        assert len(contract.input_datasets) > 0, (
            f"{contract.job_name} has no input datasets defined"
        )
        assert len(contract.output_datasets) > 0, (
            f"{contract.job_name} has no output datasets defined"
        )

    @pytest.mark.parametrize("contract", ALL_BATCH_CONTRACTS, ids=lambda c: c.job_name)
    def test_contract_datasets_have_record_lengths(self, contract):
        for ds in contract.input_datasets + contract.output_datasets:
            assert "record_length" in ds, (
                f"{contract.job_name}.{ds['name']} missing record_length"
            )
            assert ds["record_length"] > 0, (
                f"{contract.job_name}.{ds['name']} has zero record_length"
            )

    def test_validate_with_no_actuals_passes(self):
        """When no actual I/O is provided, validation should pass (nothing to check)."""
        for contract in ALL_BATCH_CONTRACTS:
            report = validate_batch_io_contract(contract)
            assert report.passed, (
                f"{contract.job_name}: {[v.message for v in report.violations]}"
            )
