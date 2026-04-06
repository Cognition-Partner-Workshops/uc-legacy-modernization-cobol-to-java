-- Survey Service Schema
-- Migrated from SuiteCRM Surveys, SurveyQuestions, SurveyResponses modules

CREATE SCHEMA IF NOT EXISTS survey_schema;

-- Surveys
CREATE TABLE survey_schema.surveys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'draft',
    survey_type VARCHAR(50),
    submit_text VARCHAR(255),
    satisfied_text TEXT,
    dissatisfied_text TEXT,
    survey_url_parameters VARCHAR(500),
    is_anonymous BOOLEAN DEFAULT FALSE,
    response_count INTEGER DEFAULT 0,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Survey questions
CREATE TABLE survey_schema.survey_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    survey_id UUID NOT NULL REFERENCES survey_schema.surveys(id),
    question_type VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    is_required BOOLEAN DEFAULT FALSE,
    row_count INTEGER,
    col_count INTEGER,
    max_answers INTEGER,
    randomize_options BOOLEAN DEFAULT FALSE,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Survey question options
CREATE TABLE survey_schema.survey_question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    question_id UUID NOT NULL REFERENCES survey_schema.survey_questions(id),
    sort_order INTEGER DEFAULT 0,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Survey responses
CREATE TABLE survey_schema.survey_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    survey_id UUID NOT NULL REFERENCES survey_schema.surveys(id),
    contact_id UUID,
    account_id UUID,
    email_address VARCHAR(255),
    ip_address VARCHAR(45),
    happiness INTEGER,
    status VARCHAR(50) DEFAULT 'completed',
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Survey question responses (individual answers)
CREATE TABLE survey_schema.survey_question_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    survey_response_id UUID NOT NULL REFERENCES survey_schema.survey_responses(id),
    survey_question_id UUID NOT NULL REFERENCES survey_schema.survey_questions(id),
    answer TEXT,
    answer_option_id UUID REFERENCES survey_schema.survey_question_options(id),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_surveys_status ON survey_schema.surveys(status) WHERE deleted = FALSE;
CREATE INDEX idx_survey_questions_survey ON survey_schema.survey_questions(survey_id) WHERE deleted = FALSE;
CREATE INDEX idx_survey_options_question ON survey_schema.survey_question_options(question_id) WHERE deleted = FALSE;
CREATE INDEX idx_survey_responses_survey ON survey_schema.survey_responses(survey_id) WHERE deleted = FALSE;
CREATE INDEX idx_survey_qr_response ON survey_schema.survey_question_responses(survey_response_id);
CREATE INDEX idx_survey_qr_question ON survey_schema.survey_question_responses(survey_question_id);
