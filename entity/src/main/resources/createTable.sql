CREATE TABLE public."user" (
    id uuid NOT NULL,
    provider character varying(32) NOT NULL,
    account character varying(64) NOT NULL,
    name character varying(16) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    last_modified_at timestamp(6) with time zone NOT NULL,
    last_login_at timestamp(6) with time zone,
    state character varying(12) DEFAULT 'ACTIVATED'::character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE public.workspace (
    id uuid NOT NULL,
    name character varying(32) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    created_by uuid NOT NULL,
    last_modified_at timestamp(6) with time zone NOT NULL,
    last_modified_by uuid NOT NULL,
    description text NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS type (
    workspace UUID NOT NULL,
    id UUID NOT NULL,
    name character varying(64),
    parent character varying(64),
    created_at timestamp(6) with time zone NOT NULL,
    created_by uuid NOT NULL,
    effective_at timestamp(6) with time zone NOT NULL,
    expire_at timestamp(6) with time zone NOT NULL,
    primitive boolean NOT NULL,
    version TEXT,
    description text NOT NULL,
    x smallint DEFAULT 0 NOT NULL,
    y smallint DEFAULT 0 NOT NULL,
    width smallint DEFAULT 0 NOT NULL,
    height smallint DEFAULT 0 NOT NULL,
    last boolean DEFAULT true NOT NULL,
    PRIMARY KEY (workspace, id)
) ;

CREATE TABLE public.attribute (
    workspace uuid NOT NULL,
    type uuid NOT NULL,
    name character varying(32) NOT NULL,
    attribute_type jsonb NOT NULL,
    "order" smallint NOT NULL,
    nullable boolean NOT NULL,
    description character varying(255),
    PRIMARY KEY (workspace, type, name),
    CONSTRAINT attribute_type_check CHECK (
        (attribute_type->>'base_type') IN ('Value', 'Array', 'Map', 'File', 'Document')
    )
);
CREATE TABLE public.layout (
    workspace uuid NOT NULL,
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    created_by uuid NOT NULL,
    effective_at timestamp(6) with time zone NOT NULL,
    expire_at timestamp(6) with time zone NOT NULL,
    PRIMARY KEY (workspace, id)
);

CREATE TABLE public.layout_type (
    workspace uuid NOT NULL,
    layout uuid NOT NULL,
    type character varying(64) NOT NULL,
    version character varying(64) NOT NULL,
    x smallint NOT NULL,
    y smallint NOT NULL,
    width smallint NOT NULL,
    height smallint NOT NULL,
    PRIMARY KEY (workspace, layout, type, version)
);

CREATE TABLE public.layout_attribute (
    workspace uuid NOT NULL,
    layout uuid NOT NULL,
    type character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    PRIMARY KEY (workspace, layout, type, version, name)
);

CREATE TABLE IF NOT EXISTS public.document (
    workspace uuid NOT NULL,
    id uuid NOT NULL,
    type character varying(64),
    serial character varying(128),
    created_at timestamp(6) with time zone NOT NULL,
    created_by uuid NOT NULL,
    effective_at timestamp(6) with time zone NOT NULL,
    expire_at timestamp(6) with time zone NOT NULL,
    data jsonb NOT NULL,
    last boolean DEFAULT true NOT NULL,
    PRIMARY KEY (workspace, id)
) ;

CREATE TABLE IF NOT EXISTS public.validation_task (
    workspace uuid NOT NULL,
    id uuid NOT NULL,
    document uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    started_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    results jsonb,
    status character varying(20) NOT NULL,
    priority integer NOT NULL,
    retry_count integer NOT NULL,
    last_error text,
    last boolean DEFAULT true NOT NULL,
    PRIMARY KEY (workspace, id),
    CONSTRAINT validation_task_status_check CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'PROCESSING'::character varying, 'DONE'::character varying, 'FAILED'::character varying])::text[])))
);

CREATE TABLE public."group" (
    workspace uuid NOT NULL,
    name character varying(32) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    created_by uuid NOT NULL,
    last_modified_at timestamp(6) with time zone NOT NULL,
    last_modified_by uuid NOT NULL,
    PRIMARY KEY (workspace, name)
);

CREATE TABLE public.group_member (
    workspace uuid NOT NULL,
    "group" character varying(32) NOT NULL,
    member uuid NOT NULL,
    PRIMARY KEY (workspace, "group", member)
);

CREATE TABLE public.group_role (
    workspace uuid NOT NULL,
    "group" character varying(32) NOT NULL,
    role uuid NOT NULL,
    PRIMARY KEY (workspace, "group", role)
);

CREATE TABLE public.role (
    id uuid NOT NULL,
    name character varying(16) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    created_by uuid NOT NULL,
    last_modified_at timestamp(6) with time zone NOT NULL,
    last_modified_by uuid NOT NULL
);