 /*********************************************************************/
 /* Data structure for card cross-reference (RECLN 50)                */
 /* Converted from COBOL copybook: app/cpy/CVACT03Y.cpy              */
 /*********************************************************************/

 DCL 1 CARD_XREF_RECORD,
       2 XREF_CARD_NUM              CHAR(16),
       2 XREF_CUST_ID               PIC '(9)9',
       2 XREF_ACCT_ID               PIC '(11)9',
       2 FILLER_XREF                CHAR(14);
