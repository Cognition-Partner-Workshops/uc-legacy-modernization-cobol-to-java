data "archive_file" "api_lambda" {
  type        = "zip"
  source_dir  = "${path.module}/../lambdas/api"
  output_path = "${path.module}/build/api_lambda.zip"
  excludes    = ["__pycache__", "*.pyc"]
}

data "archive_file" "batch_lambda" {
  type        = "zip"
  source_dir  = "${path.module}/../lambdas/batch"
  output_path = "${path.module}/build/batch_lambda.zip"
  excludes    = ["__pycache__", "*.pyc", "requirements.txt"]
}

resource "aws_cloudwatch_log_group" "api" {
  name              = "/aws/lambda/${var.project_name}-api"
  retention_in_days = var.log_retention_days
}

resource "aws_cloudwatch_log_group" "batch" {
  name              = "/aws/lambda/${var.project_name}-batch"
  retention_in_days = var.log_retention_days
}

resource "aws_lambda_function" "api" {
  function_name    = "${var.project_name}-api"
  role             = aws_iam_role.lambda_exec.arn
  runtime          = var.lambda_runtime
  handler          = "handler.handler"
  filename         = data.archive_file.api_lambda.output_path
  source_code_hash = data.archive_file.api_lambda.output_base64sha256
  timeout          = var.lambda_timeout
  memory_size      = var.lambda_memory_size

  environment {
    variables = merge(local.dynamodb_tables, {
      DATA_BUCKET = aws_s3_bucket.data.bucket
    })
  }

  depends_on = [aws_cloudwatch_log_group.api]
}

resource "aws_lambda_function" "batch" {
  function_name    = "${var.project_name}-batch"
  role             = aws_iam_role.lambda_exec.arn
  runtime          = var.lambda_runtime
  handler          = "daily_processor.handler"
  filename         = data.archive_file.batch_lambda.output_path
  source_code_hash = data.archive_file.batch_lambda.output_base64sha256
  timeout          = var.batch_lambda_timeout
  memory_size      = var.lambda_memory_size

  environment {
    variables = merge(local.dynamodb_tables, {
      DATA_BUCKET = aws_s3_bucket.data.bucket
    })
  }

  depends_on = [aws_cloudwatch_log_group.batch]
}

# --- Optional daily EventBridge trigger (disabled by default) ---
resource "aws_cloudwatch_event_rule" "daily_batch" {
  count               = var.enable_batch_schedule ? 1 : 0
  name                = "${var.project_name}-daily-batch"
  description         = "Triggers the CardDemo daily batch processor"
  schedule_expression = var.batch_schedule_expression
}

resource "aws_cloudwatch_event_target" "daily_batch" {
  count     = var.enable_batch_schedule ? 1 : 0
  rule      = aws_cloudwatch_event_rule.daily_batch[0].name
  target_id = "carddemo-batch-lambda"
  arn       = aws_lambda_function.batch.arn
}

resource "aws_lambda_permission" "allow_eventbridge" {
  count         = var.enable_batch_schedule ? 1 : 0
  statement_id  = "AllowExecutionFromEventBridge"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.batch.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.daily_batch[0].arn
}
