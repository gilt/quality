drop table if exists agenda_items;
drop table if exists meetings;

create table meetings (
  id                      bigserial primary key,
  organization_id         bigint not null references organizations,
  scheduled_at            timestamptz not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'meetings');
create index on meetings(organization_id, scheduled_at);

alter table meetings drop column updated_by_guid;

comment on table meetings is '
  Meetings happen on a regular schedule (e.g. thursdays from 11-12
  EST). As incidents are created, they are automatically assigned to
  the next meeting. Incidents can then be reviewed from the context of
  a meeting, facilitating online navigation. Incidents within a
  meeting can require one of two actions - team assignment or plan
  review.
';


create table agenda_items (
  id                      bigserial primary key,
  meeting_id              bigint not null references meetings,
  incident_id             bigint not null references incidents,
  task                    text not null check(lower(trim(task)) = task)
);

select schema_evolution_manager.create_basic_audit_data('public', 'agenda_items');
alter table agenda_items drop column updated_by_guid;

create index on agenda_items(meeting_id);
create index on agenda_items(incident_id);
create unique index on agenda_items(meeting_id, incident_id) where deleted_at is null;

comment on table agenda_items is '
  Each meeting will have agenda items added - e.g. review team
  assignment for incident 32.
';

comment on column agenda_items.task is '
  e.g. review_team or review_plan
';
