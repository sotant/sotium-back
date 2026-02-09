BEGIN;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- MODULO: IDENTITY
-- Responsabilidad: identidad, pertenencia y autorización
-- =====================================================

CREATE TABLE identity_users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_sub    VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | INVITED | DISABLED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE academy_memberships (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id      UUID NOT NULL,
    user_id         UUID NOT NULL,
    role            VARCHAR(30) NOT NULL, -- OWNER | TEACHER | STUDENT
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | INVITED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (academy_id, user_id)
);

CREATE INDEX idx_memberships_academy ON academy_memberships(academy_id);
CREATE INDEX idx_memberships_user ON academy_memberships(user_id);

-- =====================================================
-- MODULO: ACADEMIES
-- =====================================================
CREATE TABLE academies (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_academies_email ON academies(email);

CREATE TABLE academy_settings (
    academy_id      UUID PRIMARY KEY,
    phone           VARCHAR(50),
    timezone        VARCHAR(50) NOT NULL DEFAULT 'UTC',
    opening_hours   JSONB NOT NULL,
    holidays        JSONB NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================
-- MODULO: USERS & ROLES
-- =====================================================
CREATE TABLE user_profiles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    phone           VARCHAR(50),
    avatar_url      TEXT,
    bio             TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id)
);

CREATE INDEX idx_user_profiles_user ON user_profiles(user_id);

-- =====================================================
-- MODULO: SAAS PLANS
-- =====================================================
CREATE TABLE plans (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50) NOT NULL UNIQUE,
    max_users   INTEGER NOT NULL,
    max_classes INTEGER NOT NULL,
    trial_days  INTEGER NOT NULL DEFAULT 0,
    price       DECIMAL(10,2),
    active      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE academy_plan (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id  UUID NOT NULL,
    plan_name   VARCHAR(50) NOT NULL,
    valid_from  TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_until TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_academy_plan_academy ON academy_plan(academy_id);

-- =====================================================
-- MODULO: DISCOUNTS
-- =====================================================
CREATE TABLE discount_codes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code            VARCHAR(50) NOT NULL UNIQUE,
    percentage      INTEGER,
    max_uses        INTEGER,
    used_count      INTEGER NOT NULL DEFAULT 0,
    expires_at      TIMESTAMPTZ,
    duration_months INTEGER NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE academy_discounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id      UUID NOT NULL,
    discount_code   VARCHAR(50) NOT NULL,
    applied_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_academy_discounts_academy ON academy_discounts(academy_id);

-- =====================================================
-- MODULO: CLASSES
-- =====================================================
CREATE TABLE classes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id      UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(255),
    teacher_id      UUID NOT NULL,
    max_students    INTEGER NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_classes_academy ON classes(academy_id);

CREATE TABLE class_enrollments (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id  UUID NOT NULL,
    class_id    UUID NOT NULL,
    student_id  UUID NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_class_enrollments_unique
	ON class_enrollments(academy_id, class_id, student_id);

-- =====================================================
-- MODULO: CALENDAR
-- =====================================================
CREATE TABLE class_sessions (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id  UUID NOT NULL,
    class_id    UUID NOT NULL,
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ NOT NULL,
    status      VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    comments    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sessions_academy_time
	ON class_sessions(academy_id, start_time);

-- =====================================================
-- MODULO: DOCUMENTS (ACL SIMPLE)
-- =====================================================
CREATE TABLE documents (
    id          	UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id  	UUID NOT NULL,
    uploaded_by 	UUID NOT NULL,
    filename    	VARCHAR(255) NOT NULL,
    storage_path 	TEXT NOT NULL,
    created_at  	TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_academy ON documents(academy_id);

CREATE TABLE document_permissions (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id  UUID NOT NULL,
    document_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL, -- CLASS | STUDENT
    target_id   UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_doc_perm_doc ON document_permissions(document_id);

-- =====================================================
-- MODULO: PAYMENTS (ALUMNOS)
-- =====================================================
CREATE TABLE student_payments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id      UUID NOT NULL,
    student_id      UUID NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    payment_method  VARCHAR(50) NOT NULL,
    status          VARCHAR(20) NOT NULL, -- PAID | PARTIAL | DEBT
    comment         TEXT,
    paid_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_academy_student
ON student_payments(academy_id, student_id);

-- =====================================================
-- MODULO: NOTIFICATIONS
-- =====================================================
CREATE TABLE email_notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id      UUID NOT NULL,
    type            VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    payload         JSONB NOT NULL,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_academy ON email_notifications(academy_id);

-- =====================================================
-- MODULO: INVITATIONS
-- =====================================================
CREATE TABLE user_invitations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id      UUID NOT NULL,
    email           VARCHAR(255) NOT NULL,
    role            VARCHAR(30) NOT NULL, -- OWNER | TEACHER | STUDENT
    token           VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    accepted_at     TIMESTAMPTZ,
    created_by      UUID NOT NULL, -- identity_users.id (OWNER)
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- ============================================================
-- GDPR / CONSENT CONTEXT
-- ============================================================

CREATE TABLE user_consents (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id          UUID NOT NULL,
    user_id             UUID NOT NULL,
    accepted_terms      BOOLEAN NOT NULL,
    accepted_privacy    BOOLEAN NOT NULL,
    accepted_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_consents_user
    ON user_consents(academy_id, user_id);

COMMIT;