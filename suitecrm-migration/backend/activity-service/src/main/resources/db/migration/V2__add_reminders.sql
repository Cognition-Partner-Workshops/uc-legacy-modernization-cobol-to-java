-- V2: Add reminders and reminder invitees
-- Maps to AS-IS SuiteCRM modules: Reminders, ReminderInvitees

CREATE TABLE IF NOT EXISTS activity_schema.reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    popup BOOLEAN DEFAULT FALSE,
    email_flag BOOLEAN DEFAULT FALSE,
    timer_popup VARCHAR(50),
    timer_email VARCHAR(50),
    related_event_module VARCHAR(100),
    related_event_module_id UUID,
    date_willexecute TIMESTAMP,
    description TEXT,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS activity_schema.reminder_invitees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reminder_id UUID NOT NULL REFERENCES activity_schema.reminders(id),
    related_invitee_module VARCHAR(100),
    related_invitee_module_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_reminders_related_event ON activity_schema.reminders(related_event_module, related_event_module_id);
CREATE INDEX idx_reminders_willexecute ON activity_schema.reminders(date_willexecute);
CREATE INDEX idx_reminder_invitees_reminder_id ON activity_schema.reminder_invitees(reminder_id);
