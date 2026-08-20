CREATE TABLE customer (
    cust_id NUMERIC(9, 0) PRIMARY KEY,
    first_name VARCHAR(25),
    middle_name VARCHAR(25),
    last_name VARCHAR(25),
    addr_line_1 VARCHAR(50),
    addr_line_2 VARCHAR(50),
    addr_line_3 VARCHAR(50),
    addr_state_cd VARCHAR(2),
    addr_country_cd VARCHAR(3),
    addr_zip VARCHAR(10),
    phone_num_1 VARCHAR(15),
    phone_num_2 VARCHAR(15),
    ssn NUMERIC(9, 0),
    govt_issued_id VARCHAR(20),
    dob_yyyy_mm_dd VARCHAR(10),
    eft_account_id VARCHAR(10),
    pri_card_holder_ind VARCHAR(1),
    fico_credit_score NUMERIC(3, 0)
);

CREATE TABLE account (
    acct_id NUMERIC(11, 0) PRIMARY KEY,
    active_status VARCHAR(1),
    curr_bal NUMERIC(12, 2),
    credit_limit NUMERIC(12, 2),
    cash_credit_limit NUMERIC(12, 2),
    curr_cyc_credit NUMERIC(12, 2),
    curr_cyc_debit NUMERIC(12, 2),
    open_date VARCHAR(10),
    expiration_date VARCHAR(10),
    reissue_date VARCHAR(10),
    addr_zip VARCHAR(10),
    group_id VARCHAR(10)
);

CREATE TABLE card (
    card_num VARCHAR(16) PRIMARY KEY,
    acct_id NUMERIC(11, 0) NOT NULL,
    cvv_cd NUMERIC(3, 0),
    embossed_name VARCHAR(50),
    expiration_date VARCHAR(10),
    active_status VARCHAR(1),
    FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);

CREATE TABLE card_xref (
    card_num VARCHAR(16) PRIMARY KEY,
    cust_id NUMERIC(9, 0) NOT NULL,
    acct_id NUMERIC(11, 0) NOT NULL,
    FOREIGN KEY (card_num) REFERENCES card(card_num),
    FOREIGN KEY (cust_id) REFERENCES customer(cust_id),
    FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);

CREATE TABLE "transaction" (
    tran_id VARCHAR(16) PRIMARY KEY,
    tran_type_cd VARCHAR(2),
    tran_cat_cd NUMERIC(4, 0),
    tran_source VARCHAR(10),
    tran_desc VARCHAR(100),
    tran_amt NUMERIC(11, 2),
    merchant_id NUMERIC(9, 0),
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip VARCHAR(10),
    card_num VARCHAR(16),
    orig_ts VARCHAR(26),
    proc_ts VARCHAR(26),
    FOREIGN KEY (card_num) REFERENCES card(card_num)
);

CREATE TABLE tran_cat_bal (
    acct_id NUMERIC(11, 0),
    tran_type_cd VARCHAR(2),
    tran_cat_cd NUMERIC(4, 0),
    tran_cat_bal NUMERIC(11, 2),
    PRIMARY KEY (acct_id, tran_type_cd, tran_cat_cd),
    FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);

CREATE TABLE usrsec (
    sec_usr_id VARCHAR(8) PRIMARY KEY,
    sec_usr_fname VARCHAR(20),
    sec_usr_lname VARCHAR(20),
    sec_usr_pwd VARCHAR(8),
    sec_usr_type VARCHAR(1)
);

CREATE INDEX idx_card_xref_cust_id ON card_xref(cust_id);
CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
CREATE INDEX idx_transaction_card_num ON "transaction"(card_num);
