"""Placeholder API Lambda handler.

Replaced by Child Session 1 with the full regex-routed implementation described
in aws-migration/API_CONTRACT.md (§3, §4).
"""


def handler(event, context):
    return {
        "statusCode": 501,
        "headers": {"Content-Type": "application/json"},
        "body": '{"error": "API handler not yet implemented"}',
    }
