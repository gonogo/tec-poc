alter table tec_case
    add column batch_case_reference bigint;

alter table tec_case
    add constraint tec_case_batch_case_fk
        foreign key (batch_case_reference) references tec_batch (case_reference);

create index tec_case_batch_case_reference_idx
    on tec_case (batch_case_reference);
