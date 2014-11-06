update organizations
   set key = 'gilt-tech', name = 'Gilt Tech'
 where key = 'gilt'
   and deleted_at is null;

insert into organizations
(key, name, created_by_guid, updated_by_guid)
values
('gilt-process', 'Gilt Process', '9472ae70-30c2-012c-8f71-0015177442e6', '9472ae70-30c2-012c-8f71-0015177442e6');

