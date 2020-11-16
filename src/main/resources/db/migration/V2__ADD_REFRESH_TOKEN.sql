-- auto-generated definition
create table refresh_token
(
    id      serial       not null
        constraint table_name_pk
            primary key
        constraint table_name_user_id_fk
            references tb_user,
    user_id integer      not null,
    token   varchar(100) not null
);

alter table tb_refresh_token
    owner to projectx;

create unique index table_name_user_id_uindex
    on refresh_token (user_id);

