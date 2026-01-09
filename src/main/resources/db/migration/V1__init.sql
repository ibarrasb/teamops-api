create extension if not exists "pgcrypto";

create table if not exists users (
  id uuid primary key default gen_random_uuid(),
  email varchar(255) not null unique,
  password_hash varchar(255) not null,
  display_name varchar(120) not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists workspaces (
  id uuid primary key default gen_random_uuid(),
  name varchar(160) not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists workspace_members (
  workspace_id uuid not null references workspaces(id) on delete cascade,
  user_id uuid not null references users(id) on delete cascade,
  role varchar(30) not null,
  created_at timestamptz not null default now(),
  primary key (workspace_id, user_id)
);

/* tasks moved to V4__create_tasks.sql */

create table if not exists comments (
  id uuid primary key default gen_random_uuid(),
  workspace_id uuid not null references workspaces(id) on delete cascade,
  entity_type varchar(30) not null,
  entity_id uuid not null,
  body text not null,
  created_by_user_id uuid not null references users(id) on delete restrict,
  created_at timestamptz not null default now()
);

create index if not exists idx_comments_workspace on comments(workspace_id);
create index if not exists idx_comments_entity on comments(entity_type, entity_id);

create table if not exists audit_events (
  id uuid primary key default gen_random_uuid(),
  workspace_id uuid not null references workspaces(id) on delete cascade,
  actor_user_id uuid references users(id) on delete set null,
  action varchar(80) not null,
  entity_type varchar(30) not null,
  entity_id uuid,
  metadata jsonb,
  created_at timestamptz not null default now()
);

create index if not exists idx_audit_workspace on audit_events(workspace_id);
create index if not exists idx_audit_created_at on audit_events(created_at);
