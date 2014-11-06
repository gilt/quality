update teams
   set key = 'general'
 where key = 'process-team-perfectday@gilt.com'
   and organization_id = (select id from organizations where deleted_at is null and key = 'gilt-process')
   and deleted_at is null;

update teams
   set key = 'sizing'
 where key = 'process-team-sizing'
   and organization_id = (select id from organizations where deleted_at is null and key = 'gilt-process')
   and deleted_at is null;
