create table tec_batch_datafile_case (
    case_reference bigint primary key,
    batch_file_url text not null,
    batch_file_name varchar(255) not null,
    submission_type varchar(32)
);
