create table users (
  guid                    uuid primary key,
  email                   text not null constraint users_email_lower_ck check (lower(trim(email)) = email)
);

select schema_evolution_manager.create_basic_audit_data('public', 'users');
create unique index users_email_not_deleted_un_idx on users(email) where deleted_at is null;

comment on table users is '
  Represents a person interacting with the system.
';
