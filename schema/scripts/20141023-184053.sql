drop table if exists team_members;

create table team_members (
  id                      bigserial primary key,
  team_id                 bigint not null references teams,
  user_guid               uuid not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'team_members');

alter table team_members drop column updated_by_guid;

create index on team_members(team_id);
create index on team_members(user_guid);
create unique index team_members_team_id_user_guid_not_deleted_un_idx on team_members(team_id, user_guid) where deleted_at is null;

comment on table team_members is '
  Lists the members of a team.
';
