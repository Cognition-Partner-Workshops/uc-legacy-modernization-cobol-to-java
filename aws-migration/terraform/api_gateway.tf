resource "aws_apigatewayv2_api" "main" {
  name          = "${var.project_name}-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["Content-Type", "Authorization"]
    max_age       = 3600
  }
}

resource "aws_apigatewayv2_stage" "prod" {
  api_id      = aws_apigatewayv2_api.main.id
  name        = "prod"
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gw.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      routeKey       = "$context.routeKey"
      status         = "$context.status"
      protocol       = "$context.protocol"
      responseLength = "$context.responseLength"
      errorMessage   = "$context.error.message"
    })
  }
}

resource "aws_cloudwatch_log_group" "api_gw" {
  name              = "/aws/apigateway/${var.project_name}-api"
  retention_in_days = 7
}

resource "aws_apigatewayv2_integration" "lambda" {
  api_id                 = aws_apigatewayv2_api.main.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.api.invoke_arn
  payload_format_version = "2.0"
}

# --- Account Routes ---
resource "aws_apigatewayv2_route" "get_accounts" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /accounts"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "get_account" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /accounts/{acct_id}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "put_account" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "PUT /accounts/{acct_id}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

# --- Customer Routes ---
resource "aws_apigatewayv2_route" "get_customers" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /customers"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "get_customer" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /customers/{cust_id}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

# --- Card Routes ---
resource "aws_apigatewayv2_route" "get_cards" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /cards"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "get_card" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /cards/{card_num}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

# --- Transaction Routes ---
resource "aws_apigatewayv2_route" "get_transactions" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /transactions"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "get_transaction" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /transactions/{tran_id}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "post_transaction" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "POST /transactions"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

# --- Reference Data Routes ---
resource "aws_apigatewayv2_route" "get_transaction_types" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /transaction-types"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_apigatewayv2_route" "get_transaction_categories" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /transaction-categories"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

# --- Batch Routes ---
resource "aws_apigatewayv2_route" "post_batch" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "POST /batch/process-daily"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

# --- Dashboard Route ---
resource "aws_apigatewayv2_route" "get_dashboard" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /dashboard"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}
