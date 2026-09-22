alter table tec_case
    drop constraint if exists tec_case_enforcement_case_fk;

drop index if exists tec_case_enforcement_case_reference_idx;

alter table tec_case
    drop column if exists enforcement_case_reference;

drop table if exists tec_enforcement_case_document;
drop table if exists tec_enforcement_case;
