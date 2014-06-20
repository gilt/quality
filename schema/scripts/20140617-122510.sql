drop table if exists grades;
drop table if exists reports;
drop table if exists incidents;

create table incidents (
  id                      bigserial primary key,
  team_key                text not null check (lower(trim(team_key)) = team_key),
  severity                text not null check (severity in ('low', 'high')),
  summary                 text not null,
  description             text
);

select schema_evolution_manager.create_basic_audit_data('public', 'incidents');

create index on incidents(team_key);

comment on table incidents is '
  A bug or error that affected public or internal users in a negative way. App
  will journal updates by soft deleting and re-inserting so no history is list.
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
  grade                   integer not null check (grade >= 0 and grade <= 100)
);

select schema_evolution_manager.create_basic_audit_data('public', 'grades');

create index on grades(report_id);
create unique index grades_report_id_not_deleted_un_idx on grades(report_id) where deleted_at is null;

comment on table grades is '
  A grade captures how well we feel the report is in terms of resolving the incident.
';

comment on column grades.grade is '
  A value from 0 - 100, inclusive. A value of 100 is considered perfect.
';
