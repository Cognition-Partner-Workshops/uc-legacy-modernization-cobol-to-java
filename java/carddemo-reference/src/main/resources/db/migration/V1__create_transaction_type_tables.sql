-- Phase 2 (Reference Data) schema.
--
-- Replicates the DB2 DDL from the legacy optional module:
--   app/app-transaction-type-db2/ddl/TRNTYPE.ddl
--   app/app-transaction-type-db2/ddl/TRNTYCAT.ddl
-- and the VSAM copybooks app/cpy/CVTRA03Y.cpy and app/cpy/CVTRA04Y.cpy.

CREATE TABLE TRANSACTION_TYPE (
    TR_TYPE         CHAR(2)      NOT NULL,
    TR_DESCRIPTION  VARCHAR(50)  NOT NULL,
    CONSTRAINT PK_TRANSACTION_TYPE PRIMARY KEY (TR_TYPE)
);

CREATE TABLE TRANSACTION_TYPE_CATEGORY (
    TRC_TYPE_CODE      CHAR(2)      NOT NULL,
    TRC_TYPE_CATEGORY  CHAR(4)      NOT NULL,
    TRC_CAT_DATA       VARCHAR(50)  NOT NULL,
    CONSTRAINT PK_TRANSACTION_TYPE_CATEGORY
        PRIMARY KEY (TRC_TYPE_CODE, TRC_TYPE_CATEGORY),
    -- Mirrors the DB2 FOREIGN KEY ... ON DELETE RESTRICT constraint.
    CONSTRAINT FK_TRC_TYPE_CODE FOREIGN KEY (TRC_TYPE_CODE)
        REFERENCES TRANSACTION_TYPE (TR_TYPE)
);
