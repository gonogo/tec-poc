create table tec_case_warrant_authorisation (
    id uuid primary key default gen_random_uuid(),
    case_reference bigint not null references tec_case (case_reference),
    date_of_issue date not null,
    date_of_expiry date not null,
    status varchar(50) not null,
    created_at timestamptz not null default now()
);

create index tec_case_warrant_authorisation_case_reference_idx
    on tec_case_warrant_authorisation (case_reference);
