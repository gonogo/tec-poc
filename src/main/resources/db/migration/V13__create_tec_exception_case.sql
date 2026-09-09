create table tec_exception_case (
    case_reference bigint primary key,
    penalty_charge_number varchar(12) not null,
    reject_reason varchar(50),
    created_at timestamptz not null default now(),
    constraint tec_exception_case_pcn_ck check (
        penalty_charge_number ~ '^[A-Z]{2,3}[0-9]{7}[0-9A][0-9]$'
    ),
    constraint tec_exception_case_reject_reason_ck check (
        reject_reason is null
        or reject_reason in (
            'PCN_COULD_NOT_BE_MATCHED',
            'ITEM_NOT_RELEVANT_TO_TEC_CASE',
            'OTHER'
        )
    )
);

create index tec_exception_case_pcn_idx
    on tec_exception_case (penalty_charge_number);
