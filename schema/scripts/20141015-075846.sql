drop table if exists icons;

create table icons (
  id                      bigserial primary key,
  team_id                 bigint not null references teams,
  name                    text not null
                          check (lower(trim(name)) = name)
                          check (name != ''),
  url                     text
);

select schema_evolution_manager.create_basic_audit_data('public', 'icons');
alter table icons drop column updated_by_guid; -- never updated

create index on icons(team_id);
create unique index on icons(team_id, name) where deleted_at is null;

comment on table icons is '
  Container to capture any overrides to default icons in use by a
  given team. We only store icons which are different from the global
  defaults for quality.
';

comment on column icons.url is '
  e.g. smiley or frowny
';
