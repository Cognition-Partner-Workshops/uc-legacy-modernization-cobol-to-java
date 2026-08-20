CREATE TABLE "transaction" (
  tran_id VARCHAR(16) PRIMARY KEY, type_cd VARCHAR(2) NOT NULL, cat_cd INTEGER NOT NULL, source VARCHAR(10),
  tran_desc VARCHAR(100), amt NUMERIC(11,2), merchant_id INTEGER, merchant_name VARCHAR(50),
  merchant_city VARCHAR(50), merchant_zip VARCHAR(10), card_num VARCHAR(16), orig_ts VARCHAR(26), proc_ts VARCHAR(26),
  CONSTRAINT fk_transaction_category FOREIGN KEY (type_cd, cat_cd) REFERENCES transaction_category(type_cd, cat_cd)
);
CREATE INDEX idx_transaction_card_num ON "transaction"(card_num);
CREATE TABLE daily_transaction (
  tran_id VARCHAR(16) PRIMARY KEY, type_cd VARCHAR(2), cat_cd INTEGER, source VARCHAR(10), tran_desc VARCHAR(100),
  amt NUMERIC(11,2), merchant_id INTEGER, merchant_name VARCHAR(50), merchant_city VARCHAR(50), merchant_zip VARCHAR(10),
  card_num VARCHAR(16), orig_ts VARCHAR(26), proc_ts VARCHAR(26)
);
CREATE INDEX idx_daily_transaction_card_num ON daily_transaction(card_num);
CREATE TABLE daily_transaction_reject (
  tran_id VARCHAR(16) PRIMARY KEY, type_cd VARCHAR(2), cat_cd INTEGER, source VARCHAR(10), tran_desc VARCHAR(100),
  amt NUMERIC(11,2), merchant_id INTEGER, merchant_name VARCHAR(50), merchant_city VARCHAR(50), merchant_zip VARCHAR(10),
  card_num VARCHAR(16), orig_ts VARCHAR(26), proc_ts VARCHAR(26), validation_trailer VARCHAR(80),
  validation_fail_reason INTEGER, validation_fail_reason_desc VARCHAR(76)
);
CREATE TABLE tran_category_balance (
  acct_id BIGINT NOT NULL, type_cd VARCHAR(2) NOT NULL, cat_cd INTEGER NOT NULL, tran_cat_bal NUMERIC(11,2),
  PRIMARY KEY (acct_id, type_cd, cat_cd), CONSTRAINT fk_balance_account FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);
