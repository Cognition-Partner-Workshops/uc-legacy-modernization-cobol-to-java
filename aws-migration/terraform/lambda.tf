data "archive_file" "api_lambda" {
  type        = "zip"
  source_dir  = "${path.module}/../lambdas/api"
  output_path = "${path.module}/builds/api_lambda.zip"
  excludes    = ["__pycache__", "*.pyc", ".gitkeep"]
}

data "archive_file" "batch_lambda" {
  type        = "zip"
  source_dir  = "${path.module}/../lambdas/batch"
  output_path = "${path.module}/builds/batch_lambda.zip"
  excludes    = ["__pycache__", "*.pyc", ".gitkeep"]
}

resource "aws_lambda_function" "api" {
  function_name    = "${var.project_name}-api"
  role             = aws_iam_role.lambda_execution.arn
  handler          = "handler.handler"
  runtime          = var.lambda_runtime
  timeout          = var.lambda_timeout
  memory_size      = var.lambda_memory
  filename         = data.archive_file.api_lambda.output_path
  source_code_hash = data.archive_file.api_lambda.output_base64sha256

  environment {
    variables = {
      TABLE_PREFIX = var.project_name
      AWS_REGION_NAME = var.aws_region
    }
  }
}

resource "aws_lambda_function" "batch" {
  function_name    = "${var.project_name}-batch"
  role             = aws_iam_role.lambda_execution.arn
  handler          = "daily_processor.handler"
  runtime          = var.lambda_runtime
  timeout          = var.batch_lambda_timeout
  memory_size      = var.lambda_memory
  filename         = data.archive_file.batch_lambda.output_path
  source_code_hash = data.archive_file.batch_lambda.output_base64sha256

  environment {
    variables = {
      TABLE_PREFIX = var.project_name
      AWS_REGION_NAME = var.aws_region
    }
  }
}

resource "aws_lambda_permission" "api_gateway" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.api.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.main.execution_arn}/*/*"
}

resource "aws_cloudwatch_log_group" "api_lambda" {
  name              = "/aws/lambda/${aws_lambda_function.api.function_name}"
  retention_in_days = 7
}

resource "aws_cloudwatch_log_group" "batch_lambda" {
  name              = "/aws/lambda/${aws_lambda_function.batch.function_name}"
  retention_in_days = 7
}

resource "aws_cloudwatch_event_rule" "daily_batch" {
  count               = var.enable_batch_schedule ? 1 : 0
  name                = "${var.project_name}-daily-batch"
  description         = "Trigger daily transaction batch processing"
  schedule_expression = var.batch_schedule_expression
}

resource "aws_cloudwatch_event_target" "daily_batch" {
  count     = var.enable_batch_schedule ? 1 : 0
  rule      = aws_cloudwatch_event_rule.daily_batch[0].name
  target_id = "${var.project_name}-batch-lambda"
  arn       = aws_lambda_function.batch.arn
}

resource "aws_lambda_permission" "eventbridge_batch" {
  count         = var.enable_batch_schedule ? 1 : 0
  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.batch.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.daily_batch[0].arn
}
