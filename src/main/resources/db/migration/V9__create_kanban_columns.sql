CREATE TABLE kanban_columns (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    color       VARCHAR(7) NOT NULL DEFAULT '#94A3B8',
    position    INTEGER NOT NULL DEFAULT 0,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Insert default columns for existing projects
INSERT INTO kanban_columns (project_id, name, color, position, is_default)
SELECT id, 'A Fazer', '#64748B', 0, true FROM projects;

INSERT INTO kanban_columns (project_id, name, color, position, is_default)
SELECT id, 'Em Andamento', '#3B82F6', 1, true FROM projects;

INSERT INTO kanban_columns (project_id, name, color, position, is_default)
SELECT id, 'Concluído', '#22C55E', 2, true FROM projects;

-- Add column reference to tasks (nullable for backwards compat)
ALTER TABLE tasks ADD COLUMN kanban_column_id BIGINT REFERENCES kanban_columns(id) ON DELETE SET NULL;

-- Link existing tasks to their default column per project
UPDATE tasks t
SET kanban_column_id = (
    SELECT kc.id FROM kanban_columns kc
    WHERE kc.project_id = t.project_id
    AND CASE t.status
        WHEN 'TODO' THEN kc.name = 'A Fazer'
        WHEN 'IN_PROGRESS' THEN kc.name = 'Em Andamento'
        WHEN 'DONE' THEN kc.name = 'Concluído'
        ELSE kc.position = 0
    END
    LIMIT 1
);
