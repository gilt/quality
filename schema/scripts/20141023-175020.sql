update agenda_items
   set deleted_at = now(),
       deleted_by_guid = created_by_guid
 where meeting_id in (select id from meetings where scheduled_at > now())
   and deleted_at is null;
