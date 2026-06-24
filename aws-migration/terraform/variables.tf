variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name prefix for resource naming"
  type        = string
  default     = "carddemo"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "dev"
}

variable "lambda_runtime" {
  description = "Lambda runtime"
  type        = string
  default     = "python3.12"
}

variable "lambda_timeout" {
  description = "Lambda function timeout in seconds"
  type        = number
  default     = 30
}

variable "lambda_memory" {
  description = "Lambda function memory in MB"
  type        = number
  default     = 256
}

variable "batch_lambda_timeout" {
  description = "Batch Lambda function timeout in seconds"
  type        = number
  default     = 300
}

variable "enable_batch_schedule" {
  description = "Enable EventBridge scheduled batch processing"
  type        = bool
  default     = false
}

variable "batch_schedule_expression" {
  description = "EventBridge schedule expression for daily batch"
  type        = string
  default     = "cron(0 2 * * ? *)"
}
