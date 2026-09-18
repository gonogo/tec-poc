alter table tec_batch_datafile_case
    add column owning_la_organisation_id varchar(255) not null,
    add column owning_la_organisation_name varchar(255);
