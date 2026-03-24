"""Purge authorizations batch — from CBPAUP0C.cbl.

CBPAUP0C deletes processed pending authorization records:
  - Reads IMS CIPAUSMY/CIPAUDTY segments
  - Deletes records with status 'A' (approved) or 'D' (declined)
  - Keeps only 'P' (pending) records

In the Python version, this is a simple DELETE query on
pending_auth_summary and pending_auth_details tables.
"""

from sqlalchemy.orm import Session

from app.models.pending_auth_detail import PendingAuthDetail
from app.models.pending_auth_summary import PendingAuthSummary


def run_purge_authorizations(db: Session) -> dict:
    """Purge processed authorization records — replaces CBPAUP0C main program."""
    # Delete processed details (approved or declined)
    details_deleted = (
        db.query(PendingAuthDetail)
        .filter(PendingAuthDetail.auth_status.in_(["A", "D"]))
        .delete(synchronize_session="fetch")
    )

    # Delete summaries with no remaining pending details
    summaries = db.query(PendingAuthSummary).all()
    summaries_deleted = 0
    for summary in summaries:
        remaining = db.query(PendingAuthDetail).filter_by(summary_id=summary.id, auth_status="P").count()
        if remaining == 0:
            db.delete(summary)
            summaries_deleted += 1

    db.commit()
    return {
        "details_deleted": details_deleted,
        "summaries_deleted": summaries_deleted,
    }
