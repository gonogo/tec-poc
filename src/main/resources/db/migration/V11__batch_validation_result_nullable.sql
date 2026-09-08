alter table tec_batch
    alter column batch_validation_result drop not null,
    alter column batch_validation_result drop default;
