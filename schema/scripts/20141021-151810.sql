drop table if exists subscription;
drop table if exists publications;

create table publications (
  id                      bigserial primary key,
  key                     text not null check (lower(trim(key)) = key) check (key != '')
);

select schema_evolution_manager.create_basic_audit_data('public', 'publications');
alter table publications drop column updated_by_guid; -- no updates
create unique index publications_key_not_deleted_un_idx on publications(key) where deleted_at is null;

comment on table publications is '
  List of publications (e.g. email alerts) that a user can sign up
  for)
';

create table subscriptions (
  id                      bigserial primary key,
  organization_id         bigint not null references organizations,
  publication_id          bigint not null references publications,
  user_guid               uuid not null references users
);

select schema_evolution_manager.create_basic_audit_data('public', 'subscriptions');
alter table subscriptions drop column updated_by_guid; -- no updates
create index on subscriptions(organization_id);
create index on subscriptions(publication_id);
create index on subscriptions(user_guid);

create unique index subscriptions_organization_publication_user_not_deleted_un_idx
           on subscriptions(organization_id, publication_id, user_guid)
        where deleted_at is null;

comment on table subscriptions is '
  Keeps track of which publications a user has signed up for. If a
  user turns off a publication, we mark that record deleted
  (deleted_at).
';
