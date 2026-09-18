create table tec_enforcement_case (
    case_reference bigint primary key,
    local_authority varchar(100) not null,
    submitter_email varchar(320) not null,
    received_via varchar(20) not null,
    created_at timestamptz not null default now(),
    constraint tec_enforcement_received_via_ck check (
        received_via in ('EMAIL', 'UPLOAD')
    )
);

create table tec_enforcement_case_document (
    id uuid primary key default gen_random_uuid(),
    case_reference bigint not null references tec_enforcement_case (case_reference),
    category_id varchar(100),
    document_url varchar(1000) not null,
    document_binary_url varchar(1000) not null,
    filename varchar(500) not null,
    created_at timestamptz not null default now()
);

create index tec_enforcement_case_document_case_reference_idx
    on tec_enforcement_case_document (case_reference);

create index tec_enforcement_case_local_authority_idx
    on tec_enforcement_case (local_authority);

alter table tec_case
    add column enforcement_case_reference bigint;

alter table tec_case
    add constraint tec_case_enforcement_case_fk
        foreign key (enforcement_case_reference) references tec_enforcement_case (case_reference);

create index tec_case_enforcement_case_reference_idx
    on tec_case (enforcement_case_reference);
