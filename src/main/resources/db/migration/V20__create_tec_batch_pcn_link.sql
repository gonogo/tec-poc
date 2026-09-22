-- Membership of a PCN in a non-registration TEC_BATCH (warrant auth, etc.).
-- Registration membership remains tec_case.batch_case_reference.

create table tec_batch_pcn_link (
    batch_case_reference bigint not null
        references tec_batch (case_reference),
    pcn_case_reference bigint not null
        references tec_case (case_reference),
    primary key (batch_case_reference, pcn_case_reference)
);

create index tec_batch_pcn_link_pcn_idx
    on tec_batch_pcn_link (pcn_case_reference);
