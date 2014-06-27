drop table if exists grades;
drop table if exists reports;
drop table if exists incident_tags;
drop table if exists incidents;

create table teams (
  id                      bigserial primary key,
  key                     text not null
                            check (lower(trim(key)) = key)
                            check (key != '')
);

select schema_evolution_manager.create_basic_audit_data('public', 'teams');

create unique index teams_key_not_deleted_un_idx on teams(key) where deleted_at is null;

comment on table teams is '
  Teams are the primary actors in the system - they own incidents.
';

comment on column teams.key is '
  Unique team identifier - in lower case and trimmed.
';


create table incidents (
  id                      bigserial primary key,
  team_id                 bigint not null references teams,
  severity                text not null check (severity in ('low', 'high')),
  summary                 text not null,
  description             text
);

select schema_evolution_manager.create_basic_audit_data('public', 'incidents');

create index on incidents(team_id);

comment on table incidents is '
  A bug or error that affected public or internal users in a negative way. App
  will journal updates by soft deleting and re-inserting so no history is list.
';

create table incident_tags (
  id                      bigserial not null primary key,
  incident_id             bigint not null references incidents,
  tag                     text not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'incident_tags');

create index on incident_tags(incident_id);
create unique index incident_tags_incident_id_lower_trim_tag_not_deleted_un_idx on incident_tags(incident_id, lower(trim(tag))) where deleted_at is null;

comment on table incident_tags is '
  Incident tags are used for things like reporting where we can assign
  tags to a given incident.
';


create table reports (
  id                      bigserial not null primary key,
  incident_id             bigint not null references incidents,
  body                    text not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'reports');

create index on reports(incident_id);
create unique index reports_incident_id_not_deleted_un_idx on reports(incident_id) where deleted_at is null;

comment on table reports is '
  A report describes what we are doing to prevent the recurrence of a
  given incident.
';

create table grades (
  id                      bigserial primary key,
  report_id               bigint not null references reports,
  score                   integer not null check (score >= 0 and score <= 100)
);

select schema_evolution_manager.create_basic_audit_data('public', 'grades');

create index on grades(report_id);
create unique index grades_report_id_not_deleted_un_idx on grades(report_id) where deleted_at is null;

comment on table grades is '
  A grade captures how well we feel the report is in terms of resolving the incident.
';

comment on column grades.score is '
  A value from 0 - 100, inclusive. A value of 100 is considered perfect.
';
