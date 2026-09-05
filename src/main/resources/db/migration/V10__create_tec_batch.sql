create table tec_batch (
    case_reference bigint primary key,
    batch_identifier varchar(10) not null,
    pcn_count integer not null,
    operation varchar(50) not null,
    received_via varchar(20) not null,
    received_at timestamptz not null,
    local_authority varchar(100) not null,
    batch_validation_result varchar(30) not null default 'BATCH_VALID',
    constraint tec_batch_identifier_uk unique (batch_identifier),
    constraint tec_batch_identifier_ck check (
        batch_identifier ~ '^R[A-Z]{2,3}[0-9]{6}$'
    ),
    constraint tec_batch_pcn_count_ck check (
        pcn_count >= 1 and pcn_count <= 100000
    ),
    constraint tec_batch_operation_ck check (
        operation in (
            'REGISTRATION',
            'WARRANT_AUTH_REQUESTS',
            'WARRANT_REISSUE_REQUESTS',
            'OUT_OF_TIME_DECISIONS',
            'CHANGE_OF_ADDRESS',
            'CASE_CLOSURE_REQUESTS'
        )
    ),
    constraint tec_batch_received_via_ck check (
        received_via in ('EMAIL', 'UPLOAD')
    ),
    constraint tec_batch_validation_result_ck check (
        batch_validation_result in ('BATCH_VALID', 'BATCH_INVALID')
    )
);

create table tec_batch_document (
    id uuid primary key default gen_random_uuid(),
    case_reference bigint not null references tec_batch (case_reference),
    category_id varchar(100) not null,
    document_url varchar(1000) not null,
    document_binary_url varchar(1000) not null,
    filename varchar(500) not null,
    created_at timestamptz not null default now()
);

create index tec_batch_document_case_reference_idx
    on tec_batch_document (case_reference);

create index tec_batch_local_authority_idx
    on tec_batch (local_authority);

create index tec_batch_received_at_idx
    on tec_batch (received_at desc);
