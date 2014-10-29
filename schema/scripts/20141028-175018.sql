create table external_services (
  id                    bigserial primary key,
  organization_id       bigint not null references organizations,
  name                  text not null,
  url                   text not null,
  username              text not null,
  password              text not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'external_services');
alter table external_services drop column updated_by_guid;

create index on external_services(organization_id);
create unique index external_services_organization_name_not_deleted_un_idx on external_services(organization_id, name) where deleted_at is null;


comment on table external_services is '
  Stores information on how to connect to third party services (e.g.
  JIRA)
';

comment on column external_services.name is '
  Populated from the external_service enum in api.json
';
