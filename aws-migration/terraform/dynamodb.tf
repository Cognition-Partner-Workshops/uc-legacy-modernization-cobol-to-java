resource "aws_dynamodb_table" "accounts" {
  name         = "${var.project_name}-accounts"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "acct_id"

  attribute {
    name = "acct_id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "customers" {
  name         = "${var.project_name}-customers"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "cust_id"

  attribute {
    name = "cust_id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "cards" {
  name         = "${var.project_name}-cards"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "card_num"

  attribute {
    name = "card_num"
    type = "S"
  }

  attribute {
    name = "acct_id"
    type = "S"
  }

  global_secondary_index {
    name            = "acct_id-index"
    hash_key        = "acct_id"
    projection_type = "ALL"
  }
}

resource "aws_dynamodb_table" "card_xref" {
  name         = "${var.project_name}-card-xref"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "card_num"

  attribute {
    name = "card_num"
    type = "S"
  }
}

resource "aws_dynamodb_table" "transactions" {
  name         = "${var.project_name}-transactions"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "tran_id"
  range_key    = "tran_orig_ts"

  attribute {
    name = "tran_id"
    type = "S"
  }

  attribute {
    name = "tran_orig_ts"
    type = "S"
  }

  attribute {
    name = "card_num"
    type = "S"
  }

  attribute {
    name = "acct_id"
    type = "S"
  }

  global_secondary_index {
    name            = "card_num-index"
    hash_key        = "card_num"
    range_key       = "tran_orig_ts"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "acct_id-index"
    hash_key        = "acct_id"
    range_key       = "tran_orig_ts"
    projection_type = "ALL"
  }
}

resource "aws_dynamodb_table" "transaction_types" {
  name         = "${var.project_name}-transaction-types"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "type_cd"

  attribute {
    name = "type_cd"
    type = "S"
  }
}

resource "aws_dynamodb_table" "transaction_categories" {
  name         = "${var.project_name}-transaction-categories"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "type_cd"
  range_key    = "cat_cd"

  attribute {
    name = "type_cd"
    type = "S"
  }

  attribute {
    name = "cat_cd"
    type = "S"
  }
}
