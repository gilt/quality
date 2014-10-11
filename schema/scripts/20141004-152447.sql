alter table teams add organization_id bigint references organizations;
create index on teams(organization_id);
drop index teams_key_not_deleted_un_idx;
create unique index teams_organization_id_key_un_idx on teams(organization_id, key) where deleted_at is null;

alter table incidents add organization_id bigint references organizations;
create index on incidents(organization_id);

insert into organizations
(key, name, updated_by_guid, created_by_guid)
values
('gilt', 'Gilt', '9472ae70-30c2-012c-8f71-0015177442e6', '9472ae70-30c2-012c-8f71-0015177442e6');

update teams set organization_id = (select id from organizations where key = 'gilt' and deleted_at is null) where organization_id is null;
alter table teams alter column organization_id set not null;

update incidents set organization_id = (select id from organizations where key = 'gilt' and deleted_at is null) where organization_id is null;
alter table incidents alter column organization_id set not null;


