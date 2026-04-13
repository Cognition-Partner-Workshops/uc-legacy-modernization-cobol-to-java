-- CardDemo Database Schema
-- Mirrors COBOL copybook layouts exactly

-- From CSUSR01Y copybook
CREATE TABLE IF NOT EXISTS user_security (
    sec_usr_id       VARCHAR(8) PRIMARY KEY,
    sec_usr_fname    VARCHAR(20),
    sec_usr_lname    VARCHAR(20),
    sec_usr_pwd      VARCHAR(8),
    sec_usr_type     CHAR(1)
);

-- From CVACT01Y copybook
CREATE TABLE IF NOT EXISTS account (
    acct_id                BIGINT PRIMARY KEY,
    acct_active_status     CHAR(1),
    acct_curr_bal          NUMERIC(12,2),
    acct_credit_limit      NUMERIC(12,2),
    acct_cash_credit_limit NUMERIC(12,2),
    acct_open_date         VARCHAR(10),
    acct_expiraion_date    VARCHAR(10),
    acct_reissue_date      VARCHAR(10),
    acct_curr_cyc_credit   NUMERIC(12,2),
    acct_curr_cyc_debit    NUMERIC(12,2),
    acct_addr_zip          VARCHAR(10),
    acct_group_id          VARCHAR(10)
);

-- From CVACT02Y copybook
CREATE TABLE IF NOT EXISTS card (
    card_num             VARCHAR(16) PRIMARY KEY,
    card_acct_id         BIGINT,
    card_cvv_cd          SMALLINT,
    card_embossed_name   VARCHAR(50),
    card_expiraion_date  VARCHAR(10),
    card_active_status   CHAR(1)
);

-- From CVACT03Y copybook
CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num   VARCHAR(16) PRIMARY KEY,
    xref_cust_id    BIGINT,
    xref_acct_id    BIGINT
);

-- From CVCUS01Y copybook
CREATE TABLE IF NOT EXISTS customer (
    cust_id                  BIGINT PRIMARY KEY,
    cust_first_name          VARCHAR(25),
    cust_middle_name         VARCHAR(25),
    cust_last_name           VARCHAR(25),
    cust_addr_line_1         VARCHAR(50),
    cust_addr_line_2         VARCHAR(50),
    cust_addr_line_3         VARCHAR(50),
    cust_addr_state_cd       VARCHAR(2),
    cust_addr_country_cd     VARCHAR(3),
    cust_addr_zip            VARCHAR(10),
    cust_phone_num_1         VARCHAR(15),
    cust_phone_num_2         VARCHAR(15),
    cust_ssn                 VARCHAR(9),
    cust_govt_issued_id      VARCHAR(20),
    cust_dob_yyyy_mm_dd      VARCHAR(10),
    cust_eft_account_id      VARCHAR(10),
    cust_pri_card_holder_ind CHAR(1),
    cust_fico_credit_score   SMALLINT
);

-- From CVTRA05Y copybook
CREATE TABLE IF NOT EXISTS transaction (
    tran_id            VARCHAR(16) PRIMARY KEY,
    tran_type_cd       VARCHAR(2),
    tran_cat_cd        INTEGER,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           NUMERIC(12,2),
    tran_merchant_id   BIGINT,
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip  VARCHAR(10),
    tran_card_num      VARCHAR(16),
    tran_orig_ts       VARCHAR(26),
    tran_proc_ts       VARCHAR(26)
);

-- From CVTRA06Y copybook (daily transactions)
CREATE TABLE IF NOT EXISTS daily_transaction (
    dalytran_id            VARCHAR(16) PRIMARY KEY,
    dalytran_type_cd       VARCHAR(2),
    dalytran_cat_cd        INTEGER,
    dalytran_source        VARCHAR(10),
    dalytran_desc          VARCHAR(100),
    dalytran_amt           NUMERIC(12,2),
    dalytran_merchant_id   BIGINT,
    dalytran_merchant_name VARCHAR(50),
    dalytran_merchant_city VARCHAR(50),
    dalytran_merchant_zip  VARCHAR(10),
    dalytran_card_num      VARCHAR(16),
    dalytran_orig_ts       VARCHAR(26),
    dalytran_proc_ts       VARCHAR(26)
);

-- From CVTRA01Y copybook
CREATE TABLE IF NOT EXISTS tran_cat_bal (
    trancat_acct_id   BIGINT,
    trancat_type_cd   VARCHAR(2),
    trancat_cd        INTEGER,
    tran_cat_bal      NUMERIC(12,2),
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- From CVTRA02Y copybook
CREATE TABLE IF NOT EXISTS disclosure_group (
    dis_acct_group_id VARCHAR(10),
    dis_tran_type_cd  VARCHAR(2),
    dis_tran_cat_cd   INTEGER,
    dis_int_rate      NUMERIC(7,3),
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- From CVTRA03Y copybook
CREATE TABLE IF NOT EXISTS transaction_type (
    tran_type      VARCHAR(2) PRIMARY KEY,
    tran_type_desc VARCHAR(50)
);

-- From CVTRA04Y copybook
CREATE TABLE IF NOT EXISTS transaction_category (
    tran_type_cd      VARCHAR(2),
    tran_cat_cd       INTEGER,
    tran_cat_type_desc VARCHAR(50),
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);
