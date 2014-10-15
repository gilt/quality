alter table agenda_items disable trigger user;
delete from agenda_items;
alter table agenda_items enable trigger user;

insert into meetings
(organization_id, scheduled_at, created_by_guid)
select id, '2014-10-02 11:00:00-04', '9472ae70-30c2-012c-8f71-0015177442e6'
  from organizations
 where key = 'gilt'
   and deleted_at is null;

insert into agenda_items
(meeting_id, incident_id, task, created_by_guid)
select (select max(id) from meetings),
       id, 'review_team', '9472ae70-30c2-012c-8f71-0015177442e6'
  from incidents where created_at <= '2014-10-09 11:00:00-04';
  
insert into meetings
(organization_id, scheduled_at, created_by_guid)
select id, '2014-10-09 11:00:00-04', '9472ae70-30c2-012c-8f71-0015177442e6'
  from organizations
 where key = 'gilt'
   and deleted_at is null;

insert into agenda_items
(meeting_id, incident_id, task, created_by_guid)
select (select max(id) from meetings),
       id, 'review_plan', '9472ae70-30c2-012c-8f71-0015177442e6'
  from incidents where created_at <= '2014-10-09 11:00:00-04';
 
