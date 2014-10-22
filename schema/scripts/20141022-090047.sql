drop table if exists meeting_adjournments;

create table meeting_adjournments (
  id                      bigserial primary key,
  meeting_id              bigint not null references meetings,
  adjourned_at            timestamptz not null
);

select schema_evolution_manager.create_basic_audit_data('public', 'meeting_adjournments');

alter table meeting_adjournments drop column updated_by_guid;

create index on meeting_adjournments(meeting_id);
create unique index on meeting_adjournments(meeting_id) where deleted_at is null;

comment on table meeting_adjournments is '
  Records when a meeting has been adjourned.
';

