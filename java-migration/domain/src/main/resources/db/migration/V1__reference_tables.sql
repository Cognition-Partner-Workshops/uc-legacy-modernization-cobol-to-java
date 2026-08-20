CREATE TABLE transaction_type (
  type_cd VARCHAR(2) PRIMARY KEY,
  type_desc VARCHAR(50) NOT NULL
);
CREATE TABLE transaction_category (
  type_cd VARCHAR(2) NOT NULL,
  cat_cd INTEGER NOT NULL,
  cat_type_desc VARCHAR(50) NOT NULL,
  PRIMARY KEY (type_cd, cat_cd),
  CONSTRAINT fk_transaction_category_type FOREIGN KEY (type_cd) REFERENCES transaction_type(type_cd)
);
CREATE TABLE disclosure_group (
  acct_group_id VARCHAR(10) NOT NULL,
  tran_type_cd VARCHAR(2) NOT NULL,
  tran_cat_cd INTEGER NOT NULL,
  int_rate NUMERIC(6,2) NOT NULL,
  PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);
