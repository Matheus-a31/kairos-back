CREATE OR REPLACE FUNCTION fts_project_match(project_name text, project_desc text, query_text text)
RETURNS boolean AS $$
DECLARE
    tsq tsquery;
BEGIN
    IF query_text IS NULL OR trim(query_text) = '' THEN
        RETURN true;
    END IF;
    tsq := plainto_tsquery('portuguese', trim(query_text));
    IF tsq = ''::tsquery THEN
        RETURN coalesce(project_name, '') ILIKE '%' || trim(query_text) || '%'
            OR coalesce(project_desc, '') ILIKE '%' || trim(query_text) || '%';
    END IF;
    RETURN to_tsvector('portuguese', coalesce(project_name, '') || ' ' || coalesce(project_desc, '')) @@ tsq;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION fts_task_match(task_title text, task_desc text, query_text text)
RETURNS boolean AS $$
DECLARE
    tsq tsquery;
BEGIN
    IF query_text IS NULL OR trim(query_text) = '' THEN
        RETURN true;
    END IF;
    tsq := plainto_tsquery('portuguese', trim(query_text));
    IF tsq = ''::tsquery THEN
        RETURN coalesce(task_title, '') ILIKE '%' || trim(query_text) || '%'
            OR coalesce(task_desc, '') ILIKE '%' || trim(query_text) || '%';
    END IF;
    RETURN to_tsvector('portuguese', coalesce(task_title, '') || ' ' || coalesce(task_desc, '')) @@ tsq;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Índices GIN para busca full-text com suporte a Português
CREATE INDEX IF NOT EXISTS idx_projects_fts ON projects USING gin(to_tsvector('portuguese', coalesce(name, '') || ' ' || coalesce(description, '')));
CREATE INDEX IF NOT EXISTS idx_tasks_fts ON tasks USING gin(to_tsvector('portuguese', coalesce(title, '') || ' ' || coalesce(description, '')));
