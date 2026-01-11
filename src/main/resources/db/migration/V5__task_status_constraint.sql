-- Normalize any existing values (just in case)
update tasks
set status = upper(status)
where status is not null;

-- Add constraint safely (Postgres doesn't support ADD CONSTRAINT IF NOT EXISTS)
do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'tasks_status_check'
  ) then
    alter table tasks
      add constraint tasks_status_check
      check (status in ('TODO', 'IN_PROGRESS', 'DONE'));
  end if;
end $$;
