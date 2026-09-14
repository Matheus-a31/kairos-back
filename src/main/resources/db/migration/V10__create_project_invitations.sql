CREATE TABLE project_invitations (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(64) NOT NULL UNIQUE,
    role        VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invitations_token ON project_invitations(token);
