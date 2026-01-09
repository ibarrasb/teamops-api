alter table users
  add column if not exists role varchar(30) not null default 'USER';
