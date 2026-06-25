output "api_url" {
  description = "Base URL of the HTTP API (prod stage). Use as window.CARDDEMO_API_URL."
  value       = aws_apigatewayv2_stage.prod.invoke_url
}

output "frontend_url" {
  description = "S3 static website endpoint for the CardDemo frontend."
  value       = "http://${aws_s3_bucket_website_configuration.frontend.website_endpoint}"
}

output "frontend_bucket" {
  description = "Name of the frontend S3 bucket."
  value       = aws_s3_bucket.frontend.bucket
}

output "data_bucket" {
  description = "Name of the private data S3 bucket."
  value       = aws_s3_bucket.data.bucket
}

output "api_lambda_name" {
  description = "Name of the API Lambda function."
  value       = aws_lambda_function.api.function_name
}

output "batch_lambda_name" {
  description = "Name of the batch Lambda function."
  value       = aws_lambda_function.batch.function_name
}

output "dynamodb_tables" {
  description = "Map of logical name to DynamoDB table name."
  value       = local.dynamodb_tables
}
