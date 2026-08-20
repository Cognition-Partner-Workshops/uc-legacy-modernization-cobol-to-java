CREATE TABLE usrsec (
  sec_usr_id VARCHAR(8) PRIMARY KEY, sec_usr_fname VARCHAR(20) NOT NULL,
  sec_usr_lname VARCHAR(20) NOT NULL, sec_usr_pwd VARCHAR(8) NOT NULL, sec_usr_type VARCHAR(1) NOT NULL
);
CREATE TABLE customer (
  cust_id INTEGER PRIMARY KEY, first_name VARCHAR(25), middle_name VARCHAR(25), last_name VARCHAR(25),
  addr_line_1 VARCHAR(50), addr_line_2 VARCHAR(50), addr_line_3 VARCHAR(50), addr_state_cd VARCHAR(2),
  addr_country_cd VARCHAR(3), addr_zip VARCHAR(10), phone_num_1 VARCHAR(15), phone_num_2 VARCHAR(15),
  ssn INTEGER, govt_issued_id VARCHAR(20), dob_yyyy_mm_dd VARCHAR(10), eft_account_id VARCHAR(10),
  pri_card_holder_ind VARCHAR(1), fico_credit_score INTEGER
);
CREATE TABLE account (
  acct_id BIGINT PRIMARY KEY, active_status VARCHAR(1), curr_bal NUMERIC(12,2), credit_limit NUMERIC(12,2),
  cash_credit_limit NUMERIC(12,2), open_date VARCHAR(10), expiraion_date VARCHAR(10), reissue_date VARCHAR(10),
  curr_cyc_credit NUMERIC(12,2), curr_cyc_debit NUMERIC(12,2), addr_zip VARCHAR(10), group_id VARCHAR(10)
);
CREATE TABLE card (
  card_num VARCHAR(16) PRIMARY KEY, acct_id BIGINT, cvv_cd INTEGER, embossed_name VARCHAR(50),
  expiraion_date VARCHAR(10), active_status VARCHAR(1),
  CONSTRAINT fk_card_account FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);
CREATE INDEX idx_card_acct_id ON card(acct_id);
CREATE TABLE card_xref (
  card_num VARCHAR(16) PRIMARY KEY, cust_id INTEGER NOT NULL, acct_id BIGINT NOT NULL,
  CONSTRAINT fk_card_xref_card FOREIGN KEY (card_num) REFERENCES card(card_num),
  CONSTRAINT fk_card_xref_customer FOREIGN KEY (cust_id) REFERENCES customer(cust_id),
  CONSTRAINT fk_card_xref_account FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);
CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
