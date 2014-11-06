update teams
   set organization_id = (select id from organizations where deleted_at is null and key = 'gilt-process')
 where organization_id = (select id from organizations where deleted_at is null and key = 'gilt-tech')
   and deleted_at is null
   and key in ('creative', 'process-team-perfectday@gilt.com', 'process-team-sizing', 'marketing', 'merchandising');
