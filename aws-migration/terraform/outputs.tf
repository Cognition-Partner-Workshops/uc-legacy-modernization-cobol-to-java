output "api_url" {
  description = "API Gateway endpoint URL"
  value       = "${aws_apigatewayv2_api.main.api_endpoint}/prod"
}

output "frontend_url" {
  description = "S3 static website URL"
  value       = "http://${aws_s3_bucket.frontend.bucket}.s3-website-${var.aws_region}.amazonaws.com"
}

output "data_bucket" {
  description = "S3 bucket for COBOL data files"
  value       = aws_s3_bucket.data_bucket.bucket
}

output "frontend_bucket" {
  description = "S3 bucket for frontend static files"
  value       = aws_s3_bucket.frontend.bucket
}

output "api_lambda_name" {
  description = "API Lambda function name"
  value       = aws_lambda_function.api.function_name
}

output "batch_lambda_name" {
  description = "Batch Lambda function name"
  value       = aws_lambda_function.batch.function_name
}

output "dynamodb_tables" {
  description = "DynamoDB table names"
  value = {
    accounts               = aws_dynamodb_table.accounts.name
    customers              = aws_dynamodb_table.customers.name
    cards                  = aws_dynamodb_table.cards.name
    card_xref              = aws_dynamodb_table.card_xref.name
    transactions           = aws_dynamodb_table.transactions.name
    transaction_types      = aws_dynamodb_table.transaction_types.name
    transaction_categories = aws_dynamodb_table.transaction_categories.name
  }
}
