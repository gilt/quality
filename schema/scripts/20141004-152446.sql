drop table if exists organizations;

create table organizations (
  id                      bigserial primary key,
  key                     text not null check (lower(trim(key)) = key) check(key != ''),
  name                    text not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'organizations');
create unique index on organizations(key) where deleted_at is null;

comment on table organizations is '
  Top level organization using the quality app.
';

