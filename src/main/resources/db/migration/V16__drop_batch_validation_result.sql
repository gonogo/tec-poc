alter table tec_batch
    drop constraint if exists tec_batch_validation_result_ck;

alter table tec_batch
    drop column if exists batch_validation_result,
    drop column if exists batch_validation_result_display;
