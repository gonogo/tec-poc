alter table tec_batch
    add column submitter_email varchar(255);

update tec_batch
   set submitter_email = 'unknown@example.com'
 where submitter_email is null;

alter table tec_batch
    alter column submitter_email set not null;
