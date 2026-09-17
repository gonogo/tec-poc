alter table tec_batch
    add column file_identifier varchar(9);

-- Existing rows: derive file id from batch id (same prefix, drop last digit).
update tec_batch
   set file_identifier = left(batch_identifier, length(batch_identifier) - 1)
 where file_identifier is null;

alter table tec_batch
    alter column file_identifier set not null;

alter table tec_batch
    add constraint tec_batch_file_identifier_uk unique (file_identifier);

alter table tec_batch
    add constraint tec_batch_file_identifier_ck check (
        file_identifier ~ '^R[A-Z]{2,3}[0-9]{5}$'
    );

alter table tec_batch
    add constraint tec_batch_file_batch_prefix_ck check (
        substring(file_identifier from 2 for char_length(file_identifier) - 6)
        = substring(batch_identifier from 2 for char_length(batch_identifier) - 7)
    );
