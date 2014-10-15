drop table if exists team_icons;

create table team_icons (
  id                      bigserial primary key,
  team_id                 bigint not null references teams,
  name                    text not null
                          check (lower(trim(name)) = name)
                          check (name != ''),
  url                     text
);

select schema_evolution_manager.create_basic_audit_data('public', 'team_icons');
alter table team_icons drop column updated_by_guid; -- never updated

create index on team_icons(team_id);
create unique index on team_icons(team_id, name) where deleted_at is null;

comment on table team_icons is '
  Container to capture any overrides to default team_icons in use by a
  given team. We only store team_icons which are different from the global
  defaults for quality.
';

comment on column team_icons.url is '
  e.g. smiley or frowny
';
