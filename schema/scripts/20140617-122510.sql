drop table if exists incidents;
drop table if exists reports;
drop table if exists grades;

create table incidents (
  guid                    uuid not null primary key,
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
  guid                    uuid not null primary key,
  incident_guid           uuid not null references incidents,
  body                    text not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'reports');

create index on reports(incident_guid);
create unique index reports_incident_guid_not_deleted_un_idx on reports(incident_guid) where deleted_at is null;

comment on table reports is '
  A report describes what we are doing to prevent the recurrence of a
  given incident.
';

create table grades (
  guid                    uuid not null primary key,
  report_guid             uuid not null references reports,
  grade                   text not null check (grade in ('pass', 'fail'))
);

select schema_evolution_manager.create_basic_audit_data('public', 'grades');

create index on grades(report_guid);
create unique index grades_report_guid_not_deleted_un_idx on grades(report_guid) where deleted_at is null;

comment on table grades is '
  A grade captures how well we feel the report is in terms of resolving the incident.
';
