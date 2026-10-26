CREATE TABLE control_mappings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id uuid NOT NULL,
    control_id uuid NOT NULL,
    rationale text,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255) NOT NULL,
    CONSTRAINT control_mappings_requirement_id_control_id_key UNIQUE (requirement_id, control_id)
);

CREATE INDEX control_mappings_control_id_idx ON control_mappings (control_id);
