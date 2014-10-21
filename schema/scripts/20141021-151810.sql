drop table if exists publications;
drop table if exists subscriptions;

create table subscriptions (
  id                      bigserial primary key,
  organization_id         bigint not null references organizations,
  publication             text not null,
  user_guid               uuid not null references users
);

select schema_evolution_manager.create_basic_audit_data('public', 'subscriptions');
alter table subscriptions drop column updated_by_guid; -- no updates
create index on subscriptions(organization_id);
create index on subscriptions(user_guid);

create unique index subscriptions_organization_publication_user_not_deleted_un_idx
           on subscriptions(organization_id, publication, user_guid)
        where deleted_at is null;

comment on table subscriptions is '
  Keeps track of which publications a user has signed up for. If a
  user turns off a publication, we mark that record deleted
  (deleted_at).
';
