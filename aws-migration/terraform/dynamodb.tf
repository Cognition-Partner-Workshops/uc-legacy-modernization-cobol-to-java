locals {
  table_prefix = var.project_name
}

resource "aws_dynamodb_table" "accounts" {
  name         = "${local.table_prefix}-accounts"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "acct_id"

  attribute {
    name = "acct_id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "customers" {
  name         = "${local.table_prefix}-customers"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "cust_id"

  attribute {
    name = "cust_id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "cards" {
  name         = "${local.table_prefix}-cards"
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
  name         = "${local.table_prefix}-card-xref"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "card_num"

  attribute {
    name = "card_num"
    type = "S"
  }
}

resource "aws_dynamodb_table" "transactions" {
  name         = "${local.table_prefix}-transactions"
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
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "acct_id-index"
    hash_key        = "acct_id"
    projection_type = "ALL"
  }
}

resource "aws_dynamodb_table" "transaction_types" {
  name         = "${local.table_prefix}-transaction-types"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "type_cd"

  attribute {
    name = "type_cd"
    type = "S"
  }
}

resource "aws_dynamodb_table" "transaction_categories" {
  name         = "${local.table_prefix}-transaction-categories"
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

locals {
  dynamodb_tables = {
    ACCOUNTS_TABLE                = aws_dynamodb_table.accounts.name
    CUSTOMERS_TABLE               = aws_dynamodb_table.customers.name
    CARDS_TABLE                   = aws_dynamodb_table.cards.name
    CARD_XREF_TABLE               = aws_dynamodb_table.card_xref.name
    TRANSACTIONS_TABLE            = aws_dynamodb_table.transactions.name
    TRANSACTION_TYPES_TABLE       = aws_dynamodb_table.transaction_types.name
    TRANSACTION_CATEGORIES_TABLE  = aws_dynamodb_table.transaction_categories.name
  }

  dynamodb_table_arns = [
    aws_dynamodb_table.accounts.arn,
    aws_dynamodb_table.customers.arn,
    aws_dynamodb_table.cards.arn,
    aws_dynamodb_table.card_xref.arn,
    aws_dynamodb_table.transactions.arn,
    aws_dynamodb_table.transaction_types.arn,
    aws_dynamodb_table.transaction_categories.arn,
  ]

  # GSIs need explicit ARNs for IAM (table-arn/index/*)
  dynamodb_index_arns = [for arn in [
    aws_dynamodb_table.accounts.arn,
    aws_dynamodb_table.customers.arn,
    aws_dynamodb_table.cards.arn,
    aws_dynamodb_table.card_xref.arn,
    aws_dynamodb_table.transactions.arn,
    aws_dynamodb_table.transaction_types.arn,
    aws_dynamodb_table.transaction_categories.arn,
  ] : "${arn}/index/*"]
}
