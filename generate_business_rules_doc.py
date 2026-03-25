#!/usr/bin/env python3
"""
Generate the CardDemo Business Rules Document as a Word (.docx) file.

This script builds a professionally formatted Word document containing
data-element-level business rules extracted from the CardDemo COBOL codebase.

Usage:
    pip install python-docx
    python generate_business_rules_doc.py

Output:
    CardDemo_Business_Rules_Document.docx
"""

from docx import Document
from docx.shared import Pt, Inches, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.section import WD_ORIENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml


# ---------------------------------------------------------------------------
# Helper utilities
# ---------------------------------------------------------------------------

def set_cell_shading(cell, color_hex):
    """Apply background shading to a table cell."""
    shading = parse_xml(
        f'<w:shd {nsdecls("w")} w:fill="{color_hex}" w:val="clear"/>'
    )
    cell._tc.get_or_add_tcPr().append(shading)


def set_table_borders(table):
    """Apply borders to all cells of a table."""
    tbl = table._tbl
    tbl_pr = tbl.tblPr
    if tbl_pr is None:
        tbl_pr = parse_xml(
            f'<w:tblPr {nsdecls("w")}/>'
        )
        tbl.insert(0, tbl_pr)
    borders = parse_xml(
        f'<w:tblBorders {nsdecls("w")}>'
        '  <w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '  <w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '  <w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '  <w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '  <w:insideH w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '  <w:insideV w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '</w:tblBorders>'
    )
    tbl_pr.append(borders)


def add_styled_table(doc, headers, rows, col_widths=None):
    """Create a table with header shading and borders."""
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.LEFT
    table.autofit = True
    set_table_borders(table)

    # Header row
    for idx, header in enumerate(headers):
        cell = table.rows[0].cells[idx]
        cell.text = ""
        p = cell.paragraphs[0]
        run = p.add_run(header)
        run.bold = True
        run.font.size = Pt(10)
        run.font.name = "Calibri"
        run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_shading(cell, "2F5496")

    # Data rows
    for row_idx, row_data in enumerate(rows):
        for col_idx, cell_text in enumerate(row_data):
            cell = table.rows[row_idx + 1].cells[col_idx]
            cell.text = ""
            p = cell.paragraphs[0]
            run = p.add_run(str(cell_text))
            run.font.size = Pt(10)
            run.font.name = "Calibri"
            # Alternate row shading
            if row_idx % 2 == 1:
                set_cell_shading(cell, "D9E2F3")

    # Column widths
    if col_widths:
        for row in table.rows:
            for idx, width in enumerate(col_widths):
                row.cells[idx].width = Cm(width)

    return table


def add_heading_1(doc, text):
    h = doc.add_heading(text, level=1)
    for run in h.runs:
        run.font.name = "Calibri"
        run.font.size = Pt(16)
        run.font.color.rgb = RGBColor(0x1F, 0x38, 0x64)


def add_heading_2(doc, text):
    h = doc.add_heading(text, level=2)
    for run in h.runs:
        run.font.name = "Calibri"
        run.font.size = Pt(14)
        run.font.color.rgb = RGBColor(0x2F, 0x54, 0x96)


def add_heading_3(doc, text):
    h = doc.add_heading(text, level=3)
    for run in h.runs:
        run.font.name = "Calibri"
        run.font.size = Pt(12)
        run.font.color.rgb = RGBColor(0x2F, 0x54, 0x96)


def add_heading_4(doc, text):
    """Add a bold paragraph styled like Heading 4."""
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.bold = True
    run.font.size = Pt(11)
    run.font.name = "Calibri"
    run.font.color.rgb = RGBColor(0x2F, 0x54, 0x96)


def add_body(doc, text):
    p = doc.add_paragraph(text)
    for run in p.runs:
        run.font.size = Pt(11)
        run.font.name = "Calibri"
    return p


def add_note(doc, text):
    """Add an italicised note paragraph."""
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.italic = True
    run.font.size = Pt(10)
    run.font.name = "Calibri"
    run.font.color.rgb = RGBColor(0x59, 0x56, 0x59)
    return p


# ---------------------------------------------------------------------------
# Title page
# ---------------------------------------------------------------------------

def build_title_page(doc):
    """Create the title / cover page."""
    # Several blank lines for vertical spacing
    for _ in range(6):
        doc.add_paragraph()

    title = doc.add_paragraph()
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = title.add_run("CardDemo Application")
    run.bold = True
    run.font.size = Pt(28)
    run.font.name = "Calibri"
    run.font.color.rgb = RGBColor(0x1F, 0x38, 0x64)

    title2 = doc.add_paragraph()
    title2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run2 = title2.add_run("Business Rules Document")
    run2.bold = True
    run2.font.size = Pt(24)
    run2.font.name = "Calibri"
    run2.font.color.rgb = RGBColor(0x2F, 0x54, 0x96)

    doc.add_paragraph()  # spacer

    subtitle = doc.add_paragraph()
    subtitle.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run_s = subtitle.add_run("Data-Element-Level Business Rules Extraction")
    run_s.font.size = Pt(14)
    run_s.font.name = "Calibri"
    run_s.font.color.rgb = RGBColor(0x59, 0x56, 0x59)

    doc.add_paragraph()  # spacer

    meta_lines = [
        ("Application:", "CardDemo \u2014 Credit Card Management System"),
        ("Repository:", "Cognition-Partner-Workshops/uc-legacy-modernization-cobol-to-java"),
        ("Date:", "2026-03-25"),
        ("Classification:", "Formal Business Rules \u2014 Audit & Stakeholder Reference"),
    ]
    for label, value in meta_lines:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run_l = p.add_run(label + " ")
        run_l.bold = True
        run_l.font.size = Pt(11)
        run_l.font.name = "Calibri"
        run_v = p.add_run(value)
        run_v.font.size = Pt(11)
        run_v.font.name = "Calibri"

    doc.add_page_break()


# ---------------------------------------------------------------------------
# Section 1  Program Overview
# ---------------------------------------------------------------------------

def build_section_1(doc):
    add_heading_1(doc, "Section 1: Program Overview")

    headers = ["Program", "Type", "Function"]
    rows = [
        ("CBTRN01C", "Batch", "Daily transaction lookup and card-to-account cross-reference verification"),
        ("CBTRN02C", "Batch", "Daily transaction posting with validation, rejection, and balance updates"),
        ("CBTRN03C", "Batch", "Transaction detail report generation with date-range filtering"),
        ("CBACT01C", "Batch", "Account file reader with multi-format output (flat, array, variable-length)"),
        ("CBACT02C", "Batch", "Card data file sequential reader"),
        ("CBACT03C", "Batch", "Card-to-account cross-reference file sequential reader"),
        ("CBACT04C", "Batch", "Interest calculation and account balance update"),
        ("CBSTM03A", "Batch", "Account statement generation (plain text and HTML)"),
        ("CBEXPORT", "Batch", "Multi-record data export for branch migration"),
        ("COSGN00C", "Online (CICS)", "User sign-on and authentication"),
        ("COTRN02C", "Online (CICS)", "Online transaction entry with field-level validation"),
    ]
    add_styled_table(doc, headers, rows)

    doc.add_page_break()


# ---------------------------------------------------------------------------
# Section 2  Data Dictionary
# ---------------------------------------------------------------------------

def build_section_2(doc):
    add_heading_1(doc, "Section 2: Data Dictionary")

    # 2.1 Account Master Record
    add_heading_2(doc, "2.1 Account Master Record (Copybook CVACT01Y)")
    add_body(doc, "Referenced by: CBTRN01C, CBTRN02C, CBACT01C, CBACT04C, CBSTM03A, CBEXPORT")
    add_styled_table(doc,
        ["Data Element", "PIC", "Purpose"],
        [
            ("ACCT-ID", "9(11)", "Unique account identifier (primary key)"),
            ("ACCT-ACTIVE-STATUS", "X(01)", "Account active/inactive flag"),
            ("ACCT-CURR-BAL", "S9(10)V99", "Current account balance"),
            ("ACCT-CREDIT-LIMIT", "S9(10)V99", "Maximum credit limit"),
            ("ACCT-CASH-CREDIT-LIMIT", "S9(10)V99", "Cash advance credit limit"),
            ("ACCT-OPEN-DATE", "X(10)", "Date account was opened"),
            ("ACCT-EXPIRAION-DATE", "X(10)", "Account expiration date"),
            ("ACCT-REISSUE-DATE", "X(10)", "Card reissue date"),
            ("ACCT-CURR-CYC-CREDIT", "S9(10)V99", "Current billing cycle credit total"),
            ("ACCT-CURR-CYC-DEBIT", "S9(10)V99", "Current billing cycle debit total"),
            ("ACCT-GROUP-ID", "X(10)", "Disclosure/interest rate group identifier"),
        ]
    )

    # 2.2 Daily Transaction Record
    add_heading_2(doc, "2.2 Daily Transaction Record (Copybook CVTRA06Y)")
    add_body(doc, "Referenced by: CBTRN01C, CBTRN02C")
    add_styled_table(doc,
        ["Data Element", "Purpose"],
        [
            ("DALYTRAN-ID", "Unique daily transaction identifier"),
            ("DALYTRAN-CARD-NUM", "Card number originating the transaction"),
            ("DALYTRAN-TYPE-CD", "Transaction type code"),
            ("DALYTRAN-CAT-CD", "Transaction category code"),
            ("DALYTRAN-SOURCE", "Transaction origination source"),
            ("DALYTRAN-DESC", "Transaction description"),
            ("DALYTRAN-AMT", "Transaction monetary amount (signed)"),
            ("DALYTRAN-MERCHANT-ID", "Merchant identifier"),
            ("DALYTRAN-MERCHANT-NAME", "Merchant name"),
            ("DALYTRAN-MERCHANT-CITY", "Merchant city"),
            ("DALYTRAN-MERCHANT-ZIP", "Merchant ZIP code"),
            ("DALYTRAN-ORIG-TS", "Original transaction timestamp"),
        ]
    )

    # 2.3 Posted Transaction Record
    add_heading_2(doc, "2.3 Posted Transaction Record (Copybook CVTRA05Y)")
    add_body(doc, "Referenced by: CBTRN02C, CBTRN03C, CBACT04C, CBEXPORT, COTRN02C")
    add_styled_table(doc,
        ["Data Element", "Purpose"],
        [
            ("TRAN-ID", "Unique posted transaction identifier (primary key)"),
            ("TRAN-TYPE-CD", "Transaction type code"),
            ("TRAN-CAT-CD", "Transaction category code"),
            ("TRAN-SOURCE", "Transaction source"),
            ("TRAN-DESC", "Transaction description"),
            ("TRAN-AMT", "Transaction amount"),
            ("TRAN-MERCHANT-ID", "Merchant identifier"),
            ("TRAN-MERCHANT-NAME", "Merchant name"),
            ("TRAN-MERCHANT-CITY", "Merchant city"),
            ("TRAN-MERCHANT-ZIP", "Merchant ZIP code"),
            ("TRAN-CARD-NUM", "Card number"),
            ("TRAN-ORIG-TS", "Original timestamp"),
            ("TRAN-PROC-TS", "Processing timestamp"),
        ]
    )

    # 2.4 Card Cross-Reference Record
    add_heading_2(doc, "2.4 Card Cross-Reference Record (Copybook CVACT03Y)")
    add_styled_table(doc,
        ["Data Element", "Purpose"],
        [
            ("XREF-CARD-NUM", "Card number (primary key)"),
            ("XREF-CUST-ID", "Associated customer identifier"),
            ("XREF-ACCT-ID", "Associated account identifier"),
        ]
    )

    # 2.5 Transaction Category Balance Record
    add_heading_2(doc, "2.5 Transaction Category Balance Record (Copybook CVTRA01Y)")
    add_styled_table(doc,
        ["Data Element", "Purpose"],
        [
            ("TRANCAT-ACCT-ID", "Account identifier (composite key part 1)"),
            ("TRANCAT-TYPE-CD", "Transaction type code (composite key part 2)"),
            ("TRANCAT-CD", "Transaction category code (composite key part 3)"),
            ("TRAN-CAT-BAL", "Running balance for this account/type/category combination"),
        ]
    )

    # 2.6 Disclosure Group Record
    add_heading_2(doc, "2.6 Disclosure Group Record (Copybook CVTRA02Y)")
    add_styled_table(doc,
        ["Data Element", "Purpose"],
        [
            ("DIS-ACCT-GROUP-ID", "Account group identifier (composite key part 1)"),
            ("DIS-TRAN-TYPE-CD", "Transaction type code (composite key part 2)"),
            ("DIS-TRAN-CAT-CD", "Transaction category code (composite key part 3)"),
            ("DIS-INT-RATE", "Annual interest rate for this group/type/category"),
        ]
    )

    # 2.7 Export Record
    add_heading_2(doc, "2.7 Export Record (Copybook CVEXPORT)")
    add_styled_table(doc,
        ["Data Element", "PIC", "Purpose"],
        [
            ("EXPORT-REC-TYPE", "X(1)", "Record type discriminator: C=Customer, A=Account, T=Transaction, X=Xref, D=Card"),
            ("EXPORT-TIMESTAMP", "X(26)", "Export run timestamp"),
            ("EXPORT-SEQUENCE-NUM", "9(9) COMP", "Monotonically increasing sequence number (primary key)"),
            ("EXPORT-BRANCH-ID", "X(4)", "Originating branch identifier"),
            ("EXPORT-REGION-CODE", "X(5)", "Geographic region code"),
            ("EXPORT-RECORD-DATA", "X(460)", "Polymorphic data area (REDEFINES per record type)"),
        ]
    )

    # 2.8 CICS Communication Area
    add_heading_2(doc, "2.8 CICS Communication Area (Copybook COCOM01Y)")
    add_styled_table(doc,
        ["Data Element", "PIC", "Purpose"],
        [
            ("CDEMO-FROM-TRANID", "X(04)", "Originating CICS transaction ID"),
            ("CDEMO-FROM-PROGRAM", "X(08)", "Originating program name"),
            ("CDEMO-TO-TRANID", "X(04)", "Target CICS transaction ID"),
            ("CDEMO-TO-PROGRAM", "X(08)", "Target program name"),
            ("CDEMO-USER-ID", "X(08)", "Authenticated user ID"),
            ("CDEMO-USER-TYPE", "X(01)", "User type: 'A' = Admin, 'U' = Regular User"),
            ("CDEMO-PGM-CONTEXT", "9(01)", "Program context: 0 = First entry, 1 = Re-entry"),
            ("CDEMO-ACCT-ID", "9(11)", "Account ID in context"),
            ("CDEMO-CARD-NUM", "9(16)", "Card number in context"),
        ]
    )

    # 2.9 User Security Record
    add_heading_2(doc, "2.9 User Security Record (Copybook CSUSR01Y)")
    add_styled_table(doc,
        ["Data Element", "Purpose"],
        [
            ("SEC-USR-ID", "User identifier (primary key for USRSEC file)"),
            ("SEC-USR-PWD", "User password"),
            ("SEC-USR-TYPE", "User type (Admin or Regular)"),
        ]
    )

    doc.add_page_break()


# ---------------------------------------------------------------------------
# Section 3  Detailed Data Element Rules
# ---------------------------------------------------------------------------

def build_section_3(doc):
    add_heading_1(doc, "Section 3: Detailed Data Element Rules")

    # -----------------------------------------------------------------------
    # 3.1 CBTRN02C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.1 Program CBTRN02C \u2014 Daily Transaction Posting")

    # 3.1.1
    add_heading_3(doc, "3.1.1 DALYTRAN-CARD-NUM")
    add_body(doc, "Defined in: Daily Transaction File (FD), via copybook CVTRA06Y")
    add_body(doc, "Purpose: Identifies the credit card used for the transaction.")
    add_body(doc, "Dependencies: XREF-CARD-NUM, XREF-ACCT-ID")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-001", "Validation",
             "Every incoming daily transaction must have a card number that exists in the Card "
             "Cross-Reference file. If the card number is not found, the transaction is rejected "
             "with reason code 100 (\u201cInvalid Card Number Found\u201d)."),
            ("BR-T02-002", "Data Movement",
             "The daily transaction card number is used as the lookup key to retrieve the "
             "associated account ID from the cross-reference file."),
        ]
    )

    # 3.1.2
    add_heading_3(doc, "3.1.2 XREF-ACCT-ID (derived from cross-reference lookup)")
    add_body(doc, "Purpose: Links a card number to its parent account.")
    add_body(doc, "Dependencies: ACCT-ID, ACCT-CREDIT-LIMIT, ACCT-CURR-CYC-CREDIT, ACCT-CURR-CYC-DEBIT")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-003", "Validation",
             "After a successful card cross-reference lookup, the associated account must exist "
             "in the Account Master file. If the account is not found, the transaction is rejected "
             "with reason code 101 (\u201cAccount Record Not Found\u201d)."),
        ]
    )

    # 3.1.3
    add_heading_3(doc, "3.1.3 DALYTRAN-AMT")
    add_body(doc, "Purpose: The monetary amount of the daily transaction (signed; positive = credit, negative = debit).")
    add_body(doc, "Dependencies: ACCT-CREDIT-LIMIT, ACCT-CURR-CYC-CREDIT, ACCT-CURR-CYC-DEBIT, ACCT-CURR-BAL")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-004", "Calculation / Validation (Credit Limit Check)",
             "A temporary balance is computed as: Current Cycle Credits \u2212 Current Cycle Debits "
             "+ Transaction Amount. If this temporary balance exceeds the account\u2019s credit limit, "
             "the transaction is rejected with reason code 102 (\u201cOverlimit Transaction\u201d)."),
            ("BR-T02-005", "Conditional / Calculation (Balance Update)",
             "When a transaction is posted: the transaction amount is added to the account\u2019s "
             "current balance. If the amount is non-negative (\u2265 0), it is added to the current "
             "cycle credits. If the amount is negative, it is added to the current cycle debits."),
            ("BR-T02-006", "Data Movement",
             "The daily transaction amount is moved to the posted transaction record\u2019s TRAN-AMT "
             "field during posting."),
        ]
    )

    # 3.1.4
    add_heading_3(doc, "3.1.4 ACCT-EXPIRAION-DATE")
    add_body(doc, "Purpose: The date after which the account is no longer valid.")
    add_body(doc, "Dependencies: DALYTRAN-ORIG-TS")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-007", "Validation (Expiration Check)",
             "The account expiration date must be on or after the transaction origination date "
             "(first 10 characters of the origination timestamp). If the transaction was originated "
             "after the account expired, it is rejected with reason code 103 (\u201cTransaction Received "
             "After Account Expiration\u201d)."),
        ]
    )

    # 3.1.5
    add_heading_3(doc, "3.1.5 WS-VALIDATION-FAIL-REASON")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC 9(04)")
    add_body(doc, "Purpose: Numeric code indicating the reason a transaction failed validation.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-008", "Conditional",
             "If the validation fail reason is zero after all validation checks, the transaction "
             "is posted. If non-zero, the transaction is written to the daily rejects file along "
             "with the failure reason and description."),
            ("BR-T02-009", "Defaulting",
             "The validation fail reason is initialized to zero and the description to spaces "
             "before each transaction\u2019s validation begins."),
        ]
    )

    # 3.1.6
    add_heading_3(doc, "3.1.6 WS-TRANSACTION-COUNT / WS-REJECT-COUNT")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC 9(09), initial value 0")
    add_body(doc, "Purpose: Running counters for processed and rejected transactions.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-010", "Calculation",
             "The transaction counter is incremented by 1 for every record read from the daily "
             "transaction file. The reject counter is incremented by 1 for every transaction that "
             "fails validation."),
            ("BR-T02-011", "Conditional (Return Code)",
             "If any transactions were rejected (reject count > 0), the program sets the job "
             "return code to 4, signaling a warning condition to the batch scheduler."),
        ]
    )

    # 3.1.7
    add_heading_3(doc, "3.1.7 TRAN-PROC-TS (Processing Timestamp)")
    add_body(doc, "Purpose: Records the exact date and time the transaction was posted.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-012", "Derivation",
             "The processing timestamp is derived from the system\u2019s current date and time at the "
             "moment of posting, formatted as a DB2-compatible timestamp "
             "(YYYY-MM-DD-HH.MM.SS.HH0000)."),
        ]
    )

    # 3.1.8
    add_heading_3(doc, "3.1.8 TRAN-CAT-BAL (Transaction Category Balance)")
    add_body(doc, "Purpose: Running balance per account/transaction-type/category combination.")
    add_body(doc, "Dependencies: TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD, DALYTRAN-AMT")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-013", "Conditional / Calculation",
             "When posting a transaction, the system looks up the transaction category balance "
             "record by the composite key (Account ID + Type Code + Category Code). If no record "
             "exists, a new one is created with the transaction amount as the initial balance. If "
             "a record exists, the transaction amount is added to the existing balance."),
        ]
    )

    # 3.1.9
    add_heading_3(doc, "3.1.9 WS-CREATE-TRANCAT-REC")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC X(01), default 'N'")
    add_body(doc, "Purpose: Flag indicating whether a new transaction category balance record must be created.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T02-014", "Conditional",
             "This flag is set to 'Y' when a READ of the transaction category balance file "
             "returns an INVALID KEY (record not found, status '23'). When 'Y', the system "
             "creates a new record; when 'N', it updates the existing record."),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.2 CBACT04C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.2 Program CBACT04C \u2014 Interest Calculation")

    # 3.2.1
    add_heading_3(doc, "3.2.1 DIS-INT-RATE")
    add_body(doc, "Defined in: Disclosure Group file (copybook CVTRA02Y)")
    add_body(doc, "Purpose: Annual interest rate applicable to a specific account group, transaction type, and category.")
    add_body(doc, "Dependencies: ACCT-GROUP-ID, TRANCAT-TYPE-CD, TRANCAT-CD")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-001", "Conditional (Fallback to Default)",
             "The interest rate is looked up using the account\u2019s group ID, transaction type, and "
             "category. If no matching disclosure group record is found (file status '23'), the "
             "system falls back to a \u201cDEFAULT\u201d group code and retries the lookup."),
            ("BR-A04-002", "Conditional",
             "If the interest rate is zero, no interest is computed and no interest transaction "
             "is generated for that category balance."),
        ]
    )

    # 3.2.2
    add_heading_3(doc, "3.2.2 WS-MONTHLY-INT")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC S9(09)V99")
    add_body(doc, "Purpose: Computed monthly interest for a single transaction category balance.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-003", "Calculation",
             "Monthly interest is computed as: (Transaction Category Balance \u00d7 Annual Interest "
             "Rate) \u00f7 1200. This converts the annual rate to a monthly rate and applies it to "
             "the outstanding balance."),
        ]
    )

    # 3.2.3
    add_heading_3(doc, "3.2.3 WS-TOTAL-INT")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC S9(09)V99")
    add_body(doc, "Purpose: Accumulated total interest across all transaction categories for a single account.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-004", "Calculation",
             "Each monthly interest amount is accumulated into the total interest. When the "
             "account changes (or at end-of-file), the total interest is added to the account\u2019s "
             "current balance."),
            ("BR-A04-005", "Defaulting",
             "The total interest accumulator is reset to zero each time processing begins for a "
             "new account."),
        ]
    )

    # 3.2.4
    add_heading_3(doc, "3.2.4 ACCT-CURR-CYC-CREDIT / ACCT-CURR-CYC-DEBIT (in CBACT04C context)")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-006", "Defaulting (Cycle Reset)",
             "After interest is applied to an account, both the current cycle credit and current "
             "cycle debit fields are reset to zero. This represents the end-of-cycle reset for "
             "the billing period."),
        ]
    )

    # 3.2.5
    add_heading_3(doc, "3.2.5 Interest Transaction Record Generation")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-007", "Derivation / Data Movement",
             "For each interest charge, a new transaction record is generated with: Transaction "
             "ID = concatenation of the JCL parameter date + a sequential suffix; Type Code = "
             "'01'; Category Code = '05'; Source = 'System'; Description = 'Int. for a/c ' + "
             "Account ID; Amount = computed monthly interest; Card Number = from cross-reference "
             "lookup; Timestamps = current system date/time."),
            ("BR-A04-008", "Inferred Rule",
             "The transaction type '01' and category '05' are hardcoded values that represent "
             "system-generated interest charges. These codes must exist in the transaction type "
             "and category reference files."),
        ]
    )

    # 3.2.6
    add_heading_3(doc, "3.2.6 PARM-DATE (Linkage Section)")
    add_body(doc, "Defined in: Linkage Section, PIC X(10)")
    add_body(doc, "Purpose: Date parameter passed from JCL, used as the date component of generated interest transaction IDs.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-009", "Data Movement",
             "The JCL parameter date is used as the first 10 characters of every interest "
             "transaction ID, ensuring traceability to the batch run date."),
        ]
    )

    # 3.2.7
    add_heading_3(doc, "3.2.7 1400-COMPUTE-FEES")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A04-010", "Inferred Rule (Stub)",
             "A fee computation paragraph exists but is not yet implemented (contains only EXIT). "
             "This indicates a planned business rule for computing account fees that has not been "
             "developed."),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.3 CBTRN01C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.3 Program CBTRN01C \u2014 Transaction Lookup/Verification")

    add_heading_3(doc, "3.3.1 DALYTRAN-CARD-NUM (in CBTRN01C context)")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T01-001", "Validation",
             "Each daily transaction\u2019s card number is looked up in the cross-reference file. If "
             "the card number is invalid (not found), the transaction is skipped and a message is "
             "displayed identifying the unverifiable card number and the skipped transaction ID."),
            ("BR-T01-002", "Conditional",
             "If the cross-reference lookup succeeds, the associated account ID is used to read "
             "the account master. If the account is not found, a message is displayed but "
             "processing continues (no abend)."),
        ]
    )
    add_note(doc,
        "Note: CBTRN01C is a read-only verification program. Unlike CBTRN02C, it does not post "
        "transactions, reject records, or update balances. [Inferred Rule] This program likely "
        "serves as a pre-validation or audit step before the actual posting run."
    )

    # -----------------------------------------------------------------------
    # 3.4 CBTRN03C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.4 Program CBTRN03C \u2014 Transaction Detail Report")

    # 3.4.1
    add_heading_3(doc, "3.4.1 WS-START-DATE / WS-END-DATE")
    add_body(doc, "Defined in: Working-Storage, PIC X(10)")
    add_body(doc, "Purpose: Date range parameters for report filtering.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T03-001", "Validation (Date Range Filter)",
             "Only transactions whose processing timestamp (first 10 characters, representing "
             "the date portion) falls within the start and end dates (inclusive) are included in "
             "the report. Transactions outside this range are skipped."),
        ]
    )

    # 3.4.2
    add_heading_3(doc, "3.4.2 WS-PAGE-TOTAL / WS-ACCOUNT-TOTAL / WS-GRAND-TOTAL")
    add_body(doc, "Defined in: Working-Storage, PIC S9(09)V99, initial value 0")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T03-002", "Calculation",
             "Transaction amounts are accumulated at three levels: page total (reset every 20 "
             "lines), account total (reset when the card number changes), and grand total "
             "(accumulated across the entire report)."),
            ("BR-T03-003", "Conditional (Page Break)",
             "When the line counter reaches a multiple of the page size (20 lines), page totals "
             "are printed and headers are reprinted."),
            ("BR-T03-004", "Conditional (Account Break)",
             "When the card number changes between consecutive transactions, account totals for "
             "the previous card are printed before processing the new card\u2019s transactions."),
        ]
    )

    # 3.4.3
    add_heading_3(doc, "3.4.3 TRAN-TYPE-DESC / TRAN-CAT-TYPE-DESC")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-T03-005", "Data Movement (Reference Lookup)",
             "For each transaction, the type code and category code are used to look up their "
             "human-readable descriptions from the Transaction Type file and Transaction Category "
             "file, respectively. These descriptions are printed on the report."),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.5 CBACT01C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.5 Program CBACT01C \u2014 Account File Reader/Writer")

    # 3.5.1
    add_heading_3(doc, "3.5.1 OUT-ACCT-CURR-CYC-DEBIT")
    add_body(doc, "Defined in: FD OUT-FILE, Level 05, PIC S9(10)V99 USAGE IS COMP-3")
    add_body(doc, "Purpose: Current cycle debit amount in the flat output file.")
    add_body(doc, "Dependencies: ACCT-CURR-CYC-DEBIT")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A01-001", "Conditional / Defaulting",
             "If the account\u2019s current cycle debit is zero, the output debit field is set to a "
             "hardcoded value of 2525.00 instead of zero. [Inferred Rule] This likely serves as "
             "a test data seeding mechanism or a sentinel value to distinguish \u201cno debits\u201d from "
             "\u201czero balance.\u201d"),
        ]
    )

    # 3.5.2
    add_heading_3(doc, "3.5.2 OUT-ACCT-REISSUE-DATE")
    add_body(doc, "Defined in: FD OUT-FILE, Level 05, PIC X(10)")
    add_body(doc, "Purpose: Reformatted card reissue date in the output file.")
    add_body(doc, "Dependencies: ACCT-REISSUE-DATE, CODATECN-REC")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A01-002", "Transformation",
             "The account reissue date is passed through an external date formatting routine "
             "(COBDATFT) using the CODATECN copybook with type code '2' for both input and "
             "output format. The reformatted date is written to the output file."),
        ]
    )

    # 3.5.3
    add_heading_3(doc, "3.5.3 ARR-ACCT-BAL (Array Output)")
    add_body(doc, "Defined in: FD ARRY-FILE, Level 05, OCCURS 5 TIMES")
    add_body(doc, "Purpose: Array of balance/debit pairs for test data generation.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A01-003", "Data Movement / Defaulting",
             "The array output populates 3 of 5 available slots: Slot 1 receives the actual "
             "current balance and a hardcoded debit of 1005.00. Slot 2 receives the actual "
             "current balance and a hardcoded debit of 1525.00. Slot 3 receives hardcoded values "
             "of -1025.00 (balance) and -2500.00 (debit). Slots 4 and 5 remain initialized "
             "(zeros). [Inferred Rule] This appears to be test/sample data generation rather "
             "than production business logic."),
        ]
    )

    # 3.5.4
    add_heading_3(doc, "3.5.4 Variable-Length Records (VBRC-REC1, VBRC-REC2)")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A01-004", "Data Movement",
             "Each account produces two variable-length records: Record 1 (12 bytes) contains "
             "Account ID + Active Status. Record 2 (39 bytes) contains Account ID + Current "
             "Balance + Credit Limit + Reissue Year (4-digit year extracted from the reissue "
             "date)."),
            ("BR-A01-005", "Derivation",
             "The reissue year (VB2-ACCT-REISSUE-YYYY) is extracted from the first 4 characters "
             "of the reformatted reissue date string."),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.6 CBACT02C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.6 Program CBACT02C \u2014 Card Data File Reader")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A02-001", "Inferred Rule",
             "This program sequentially reads and displays all records from the Card Master VSAM "
             "file. It serves as a diagnostic/audit utility for verifying card data file "
             "contents. No transformations, validations, or derivations are applied."),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.7 CBACT03C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.7 Program CBACT03C \u2014 Cross-Reference File Reader")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-A03-001", "Inferred Rule",
             "This program sequentially reads and displays all records from the Card-to-Account "
             "Cross-Reference VSAM file. It serves as a diagnostic/audit utility. No "
             "transformations, validations, or derivations are applied."),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.8 COSGN00C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.8 Program COSGN00C \u2014 User Sign-On (CICS Online)")

    # 3.8.1
    add_heading_3(doc, "3.8.1 WS-USER-ID / USERIDI")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC X(08)")
    add_body(doc, "Purpose: User identifier entered on the sign-on screen.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-S00-001", "Validation (Required Field)",
             "The User ID field must not be blank or low-values. If empty, the user is prompted "
             "with \u201cPlease enter User ID...\u201d and the cursor is positioned on the User ID field."),
            ("BR-S00-002", "Transformation",
             "The entered User ID is converted to uppercase before any lookup or storage."),
            ("BR-S00-003", "Validation (Existence Check)",
             "The User ID is looked up in the USRSEC (User Security) VSAM file. If the record "
             "is not found (CICS response code 13), the message \u201cUser not found. Try again...\u201d "
             "is displayed. For any other error, \u201cUnable to verify the User...\u201d is displayed."),
        ]
    )

    # 3.8.2
    add_heading_3(doc, "3.8.2 WS-USER-PWD / PASSWDI")
    add_body(doc, "Defined in: Working-Storage, Level 05, PIC X(08)")
    add_body(doc, "Purpose: Password entered on the sign-on screen.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-S00-004", "Validation (Required Field)",
             "The Password field must not be blank or low-values. If empty, the user is prompted "
             "with \u201cPlease enter Password...\u201d"),
            ("BR-S00-005", "Transformation",
             "The entered password is converted to uppercase before comparison."),
            ("BR-S00-006", "Validation (Authentication)",
             "The entered password is compared against the stored password (SEC-USR-PWD) from "
             "the security file. If they do not match, the message \u201cWrong Password. Try "
             "again...\u201d is displayed and the cursor is positioned on the password field."),
        ]
    )

    # 3.8.3
    add_heading_3(doc, "3.8.3 SEC-USR-TYPE / CDEMO-USER-TYPE")
    add_body(doc, "Purpose: Determines the user\u2019s role and post-authentication navigation.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-S00-007", "Conditional (Role-Based Routing)",
             "After successful authentication, the user type from the security record is moved "
             "to the COMMAREA. If the user type is 'A' (Admin), control is transferred to the "
             "Admin Menu program (COADM01C). If the user type is 'U' (Regular User), control is "
             "transferred to the User Menu program (COMEN01C)."),
        ]
    )

    # 3.8.4
    add_heading_3(doc, "3.8.4 EIBAID (AID Key Handling)")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-S00-008", "Conditional",
             "On the sign-on screen: ENTER key processes the login attempt. PF3 displays a "
             "\u201cThank You\u201d message and ends the session. Any other key displays \u201cInvalid key "
             "pressed...\u201d"),
        ]
    )

    # -----------------------------------------------------------------------
    # 3.9 COTRN02C
    # -----------------------------------------------------------------------
    add_heading_2(doc, "3.9 Program COTRN02C \u2014 Add Transaction (CICS Online)")

    # 3.9.1
    add_heading_3(doc, "3.9.1 ACTIDINI (Account ID Input)")
    add_body(doc, "Purpose: Account ID entered by the user on the transaction entry screen.")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-OT2-001", "Validation (Mutual Exclusivity)",
             "Either an Account ID or a Card Number must be provided, but at least one is "
             "required. If neither is entered, the message \u201cAccount or Card Number must be "
             "entered...\u201d is displayed."),
            ("BR-OT2-002", "Validation (Numeric Check)",
             "If an Account ID is entered, it must be numeric. If non-numeric, the message "
             "\u201cAccount ID must be Numeric...\u201d is displayed."),
            ("BR-OT2-003", "Validation (Existence Check)",
             "The Account ID is looked up in the CXACAIX (Cross-Reference Alternate Index) "
             "file. If not found, \u201cAccount ID NOT found...\u201d is displayed."),
            ("BR-OT2-004", "Derivation",
             "When an Account ID is provided, the system looks up the associated card number "
             "from the cross-reference and auto-populates the Card Number field."),
        ]
    )

    # 3.9.2
    add_heading_3(doc, "3.9.2 CARDNINI (Card Number Input)")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Business Rule"],
        [
            ("BR-OT2-005", "Validation (Numeric Check)",
             "If a Card Number is entered, it must be numeric. If non-numeric, \u201cCard Number "
             "must be Numeric...\u201d is displayed."),
            ("BR-OT2-006", "Validation (Existence Check)",
             "The Card Number is looked up in the CCXREF file. If not found, \u201cCard Number NOT "
             "found...\u201d is displayed."),
            ("BR-OT2-007", "Derivation",
             "When a Card Number is provided, the system looks up the associated Account ID from "
             "the cross-reference and auto-populates the Account ID field."),
        ]
    )

    # 3.9.3
    add_heading_3(doc, "3.9.3 Transaction Data Fields \u2014 Required Field Validations")
    add_styled_table(doc,
        ["Rule ID", "Rule Type", "Data Element", "Business Rule"],
        [
            ("BR-OT2-008", "Validation", "TTYPCDI (Type Code)",
             "Must not be blank. Must be numeric."),
            ("BR-OT2-009", "Validation", "TCATCDI (Category Code)",
             "Must not be blank. Must be numeric."),
            ("BR-OT2-010", "Validation", "TRNSRCI (Source)",
             "Must not be blank."),
            ("BR-OT2-011", "Validation", "TDESCI (Description)",
             "Must not be blank."),
            ("BR-OT2-012", "Validation", "TRNAMTI (Amount)",
             "Must not be blank. Must conform to format \u00b199999999.99 (sign + 8 digits + "
             "decimal point + 2 digits)."),
            ("BR-OT2-013", "Validation", "TORIGDTI (Origination Date)",
             "Must not be blank. Must conform to format YYYY-MM-DD. Must be a valid calendar "
             "date (verified by calling CSUTLDTC date validation routine)."),
            ("BR-OT2-014", "Validation", "TPROCDTI (Processing Date)",
             "Must not be blank. Must conform to format YYYY-MM-DD. Must be a valid calendar "
             "date."),
            ("BR-OT2-015", "Validation", "MIDI (Merchant ID)",
             "Must not be blank. Must be numeric."),
            ("BR-OT2-016", "Validation", "MNAMEI (Merchant Name)",
             "Must not be blank."),
            ("BR-OT2-017", "Validation", "MCITYI (Merchant City)",
             "Must not be blank."),
            ("BR-OT2-018", "Validation", "MZIPI (Merchant ZIP)",
             "Must not be blank."),
        ]
    )


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    doc = Document()

    # Set default font for the document
    style = doc.styles["Normal"]
    font = style.font
    font.name = "Calibri"
    font.size = Pt(11)

    # Configure heading styles
    for level in range(1, 4):
        heading_style = doc.styles[f"Heading {level}"]
        heading_style.font.name = "Calibri"
        heading_style.font.color.rgb = RGBColor(0x1F, 0x38, 0x64)

    doc.styles["Heading 1"].font.size = Pt(16)
    doc.styles["Heading 2"].font.size = Pt(14)
    doc.styles["Heading 3"].font.size = Pt(12)

    # Build all sections
    build_title_page(doc)
    build_section_1(doc)
    build_section_2(doc)
    build_section_3(doc)

    output_path = "CardDemo_Business_Rules_Document.docx"
    doc.save(output_path)
    print(f"Document generated successfully: {output_path}")


if __name__ == "__main__":
    main()
