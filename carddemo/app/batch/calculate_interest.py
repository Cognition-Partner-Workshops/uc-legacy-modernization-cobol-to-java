"""Calculate interest batch — from CBACT04C.cbl.

Wraps the interest_calculator service for batch execution.
This is the Celery task entry point.
"""

from sqlalchemy.orm import Session

from app.services.interest_calculator import run_interest_calculation


def run_calculate_interest(db: Session) -> dict:
    """Run interest calculation batch — replaces CBACT04C main program."""
    return run_interest_calculation(db)
