create table tasks (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references projects(id) on delete cascade,
  title text not null,
  status text not null default 'TODO',
  due_at timestamptz,
  owner_email text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_tasks_project_id on tasks(project_id);
create index idx_tasks_owner_email on tasks(owner_email);
create index idx_tasks_owner_project on tasks(owner_email, project_id);
