variable "aws_region" {
  description = "AWS region for all resources."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project/name prefix for resources (also the DynamoDB table prefix)."
  type        = string
  default     = "carddemo"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "prod"
}

variable "lambda_runtime" {
  description = "Python runtime for Lambda functions."
  type        = string
  default     = "python3.12"
}

variable "lambda_timeout" {
  description = "Lambda timeout in seconds."
  type        = number
  default     = 30
}

variable "lambda_memory_size" {
  description = "Lambda memory size in MB."
  type        = number
  default     = 256
}

variable "batch_lambda_timeout" {
  description = "Batch Lambda timeout in seconds (longer for full scans)."
  type        = number
  default     = 300
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days."
  type        = number
  default     = 14
}

variable "enable_batch_schedule" {
  description = "Whether to enable the daily EventBridge batch schedule."
  type        = bool
  default     = false
}

variable "batch_schedule_expression" {
  description = "EventBridge schedule expression for the daily batch run."
  type        = string
  default     = "cron(0 6 * * ? *)"
}
