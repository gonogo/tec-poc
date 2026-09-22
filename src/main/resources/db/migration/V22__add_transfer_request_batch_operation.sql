alter table tec_batch
    drop constraint if exists tec_batch_operation_ck;

alter table tec_batch
    add constraint tec_batch_operation_ck check (
        operation in (
            'REGISTRATION',
            'WARRANT_AUTH_REQUESTS',
            'WARRANT_REISSUE_REQUESTS',
            'OUT_OF_TIME_DECISIONS',
            'CHANGE_OF_ADDRESS',
            'CASE_CLOSURE_REQUESTS',
            'TRANSFER_REQUEST'
        )
    );
