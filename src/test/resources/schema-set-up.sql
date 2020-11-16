
create table if not exists tb_auth_token_blacklist
(
    id      bigserial    not null
        constraint auth_token_blacklist_pk
            primary key,
    token   varchar(100) not null,
    created timestamp default now(),
    updated timestamp default now()
);

create unique index auth_token_blacklist_token_uindex
    on tb_auth_token_blacklist (token);

create table if not exists tb_sms_auth
(
	id bigserial not null
		constraint sms_auth_pk
			primary key,
	sms_code varchar(32) default ''::character varying,
	created timestamp default now(),
	updated timestamp default now(),
	attempt integer default 0,
	mobile_number varchar(100) not null,
	country_code varchar(10),
	last_send timestamp default now() not null,
	used boolean default false
);

create unique index if not exists sms_auth_mobile__index
    on tb_sms_auth (country_code, mobile_number);

create table if not exists tb_admin
(
    id       bigserial    not null
        constraint tb_admin_pk
            primary key,
    name     varchar(20)  not null,
    password varchar(255) not null,
    created  timestamp default now(),
    updated  timestamp default now()
);

create unique index tb_admin_name_uindex
    on tb_admin (name);

create table if not exists tb_admin_roles
(
    admin_id bigint      not null
        constraint tb_admin_roles_tb_admin__fk
            references tb_admin
            on delete cascade,
    role     varchar(20) not null,
    created  timestamp default now(),
    updated  timestamp default now()
);

create table if not exists tb_iiko_city_directory
(
    id      varchar(100) not null
        constraint iiko_city_directory_pk
            primary key,
    name    varchar(20),
    created timestamp default now(),
    updated timestamp default now()
);

create table if not exists tb_iiko_street_directory
(
    id      varchar(100) not null
        constraint tb_iiko_street_directory_pk
            primary key,
    name    varchar(100),
    created timestamp default now(),
    updated timestamp default now(),
    city_id varchar(100)
        constraint tb_iiko_street_directory_tb_iiko_city_directory__fk
            references tb_iiko_city_directory
            on delete cascade
);

create unique index tb_iiko_street_directory__street_index
    on tb_iiko_street_directory (name);

create table if not exists tb_new_street
(
    name varchar(100) not null
        constraint tb_new_streets_pk
            primary key
);

create table if not exists tb_product_category
(
    id         varchar(200)          not null
        constraint product_category_pk
            primary key,
    name       varchar(50) default ''::character varying,
    created    timestamp   default now(),
    updated    timestamp   default now(),
    view_order integer     default 0 not null
);

create table if not exists tb_product
(
    id                           varchar(200)              not null
        constraint product_pk
            primary key,
    code                         varchar(20),
    description                  varchar(1000),
    group_id                     varchar(200),
    product_category_id          varchar(200)
        constraint product_product_category__fk
            references tb_product_category
            on delete cascade,
    price                        integer     default 0     not null,
    carbohydrate_amount          numeric     default 0,
    energy_amount                numeric     default 0,
    fat_amount                   numeric     default 0,
    fiber_amount                 numeric     default 0,
    carbohydrate_full_amount     numeric     default 0,
    energy_full_amount           numeric     default 0,
    fat_full_amount              numeric     default 0,
    fiber_full_amount            numeric     default 0,
    weight                       numeric     default 0,
    type                         varchar(10),
    included_in_menu             boolean     default true,
    additional_info              varchar(1000),
    tags                         varchar(500),
    use_balance_for_sell         boolean     default false not null,
    created                      timestamp   default now(),
    updated                      timestamp   default now(),
    do_not_print_in_cheque       boolean     default false,
    measure_unit                 varchar(10),
    name                         varchar(200),
    image                        varchar(300),
    in_stop_list                 boolean     default false,
    vegetarian                   boolean     default false,
    spicy                        boolean     default false,
    with_nuts                    boolean     default false,
    product_preference_key_words varchar(1000),
    "order"                      integer     default 0,
    product_category_name        varchar(50) default ''::character varying
);

create table if not exists tb_user
(
    id                                    bigserial not null
        constraint user_pk
            primary key,
    name                                  varchar(20) default ''::character varying,
    enabled                               boolean     default true,
    country_code                          varchar(10) default ''::character varying,
    mobile_number                         varchar(100),
    created                               timestamp   default now(),
    updated                               timestamp   default now(),
    hard_logout_last_time                 timestamp,
    bonus_amount                          integer     default 0,
    registration_promo_code               varchar(10),
    registration_promo_code_used          boolean     default false,
    registration_promo_cashback_returned  boolean     default false,
    bonuses_spent                         integer     default 0,
    registration_promo_code_owner_user_id bigint
        constraint tb_user_promo_code_owner__fk
            references tb_user
            on delete set null,
    constraint user_pk_2
        unique (mobile_number, country_code)
);

create table if not exists tb_firebase_token
(
    user_id bigint       not null
        constraint firebase_token_user__fk
            references tb_user
            on delete cascade,
    token   varchar(300) not null,
    created timestamp default now(),
    updated timestamp default now(),
    id      bigserial    not null
        constraint firebase_token_pk
            primary key
);

create unique index firebase_token_user_id_uindex
    on tb_firebase_token (user_id);

create table if not exists tb_refresh_token
(
    id      bigserial    not null
        constraint table_name_pk
            primary key,
    user_id bigint       not null
        constraint table_name_user_id_fk
            references tb_user
            on delete cascade,
    token   varchar(100) not null,
    created timestamp default now(),
    updated timestamp default now()
);

create unique index table_name_user_id_uindex
    on tb_refresh_token (user_id);

create unique index tb_user_registration_promo_code_uindex
    on tb_user (registration_promo_code);

create table if not exists tb_user_bank_card
(
    id          bigserial not null
        constraint user_bank_card_pk
            primary key,
    user_id     bigint    not null
        constraint user_bank_card_user__fk
            references tb_user
            on delete cascade,
    card_mask   varchar(20),
    card_type   varchar(20),
    created     timestamp default now(),
    updated     timestamp default now(),
    token       varchar   not null,
    card_issuer varchar,
    actual      boolean   default false
);

create unique index tb_user_bank_card__unique_card
    on tb_user_bank_card (user_id, token);

create unique index tb_user_bank_card__card_mask_index
    on tb_user_bank_card (user_id, card_mask);

create table if not exists tb_user_roles
(
    user_id integer                                            not null
        constraint user_roles_user__fk
            references tb_user
            on delete cascade,
    role    varchar(20) default 'ROLE_USER'::character varying not null,
    created timestamp   default now(),
    updated timestamp   default now()
);

create table if not exists tb_preferences_key_words
(
    id   serial       not null
        constraint tb_preferences_key_words_pk
            primary key,
    word varchar(255) not null
);

create unique index tb_preferences_key_words_word_uindex
    on tb_preferences_key_words (word);

create table if not exists tb_user_preferences
(
    id           bigserial not null
        constraint tb_user_preferences_pk
            primary key,
    user_id      bigint    not null
        constraint tb_user_preferences_tb_user__fk
            references tb_user
            on delete cascade,
    vegetarian   boolean   default false,
    not_spicy    boolean   default false,
    without_nuts boolean   default false,
    dont_like    varchar(1000),
    created      timestamp default now(),
    updated      timestamp default now()
);

create table if not exists tb_properties
(
    id             serial not null
        constraint tb_properties_pk
            primary key,
    property_name  varchar(255),
    property_value varchar(255),
    description    varchar(255)
);

create unique index tb_properties_name_uindex
    on tb_properties (property_name);

create table if not exists tb_user_bank_card_bind_request
(
    id             bigserial               not null
        constraint tb_user_bank_card_bind_request_pk
            primary key,
    transaction_id bigint,
    user_id        bigint                  not null,
    created        timestamp default now() not null,
    updated        timestamp,
    queue_name     varchar,
    result         varchar
);

create unique index tb_user_bank_card_bind_request_transaction_id_uindex
    on tb_user_bank_card_bind_request (transaction_id);

create table if not exists tb_kitchen
(
    id                             bigserial               not null
        constraint tb_kitchen_pk
            primary key,
    organization_id                varchar,
    restaurant_name                varchar,
    restaurant_address             varchar,
    created                        timestamp default now() not null,
    updated                        timestamp,
    payment_type_id                varchar,
    payment_type_name              varchar,
    payment_type_code              varchar,
    payment_type_external_revision integer,
    payment_type_combinable        boolean   default false,
    payment_type_deleted           boolean   default false
);

create unique index tb_kitchen_organization_id_uindex
    on tb_kitchen (organization_id);

create table if not exists tb_kitchen_delivery_terminal
(
    id            bigserial               not null
        constraint tb_kitchen_delivery_terminal_pk
            primary key,
    terminal_name varchar,
    terminal_id   varchar,
    created       timestamp default now() not null,
    updated       timestamp,
    kitchen_id    bigint
        constraint tb_kitchen_delivery_terminal___kitchen_fk
            references tb_kitchen
            on delete cascade,
    time_zone     varchar
);

create table if not exists tb_delivery_zone
(
    id                   bigserial                                  not null
        constraint tb_delivery_zone_pk
            primary key,
    name                 varchar(100) default ''::character varying not null,
    created              timestamp    default now(),
    updated              timestamp    default now(),
    active               bool         default false,
    delivery_terminal_id bigint
        constraint tb_delivery_zone___terminal_fk
            references tb_kitchen_delivery_terminal
);

create unique index tb_delivery_zone_name_uindex
    on tb_delivery_zone (name);

create table if not exists tb_delivery_coordinate
(
    location_order   integer,
    latitude         numeric not null,
    longitude        numeric not null,
    delivery_zone_id bigint
        constraint tb_coordinate___delivery_zone
            references tb_delivery_zone
            on delete cascade
);

create table if not exists tb_user_address
(
    id                   bigserial not null
        constraint tb_user_address_pk
            primary key,
    city                 varchar(255),
    street               varchar(255),
    home                 varchar(20),
    housing              varchar(10),
    apartment            varchar(10),
    entrance             varchar(10),
    floor                varchar(10),
    doorphone            varchar(10),
    address_comment      varchar(500),
    created              timestamp default now(),
    updated              timestamp,
    user_id              bigint
        constraint tb_user_address___user_fk
            references tb_user
            on delete cascade,
    actual               boolean   default false,
    delivery_terminal_id bigint
        constraint tb_user_address_tb_kitchen_delivery_terminal__fk
            references tb_kitchen_delivery_terminal
);

create table if not exists tb_order
(
    id                          bigint                  not null
        constraint order_pk
            primary key,
    total_cost                  integer,
    status                      varchar(20),
    user_id                     bigint                  not null
        constraint order_user__fk
            references tb_user
            on delete cascade,
    created                     timestamp default now(),
    updated                     timestamp default now(),
    applied_bonus_amount        integer   default 0,
    products_cost               integer,
    iiko_order_id               varchar(100),
    address_id                  bigint
        constraint tb_order___address_fk
            references tb_user_address,
    check_status                boolean   default false,
    transaction_id              bigint,
    cloud_payment_status        varchar,
    payment_decline_reason      varchar,
    payment_decline_reason_code integer,
    payment_type                varchar,
    bank_card_id                bigint
        constraint tb_order___bank_card_fk
            references tb_user_bank_card
            on delete set null,
    cutlery                     boolean   default false not null,
    status_queue_name           varchar,
    paid_time                   timestamp,
    delivery_time               integer,
    iiko_problem                varchar,
    iiko_short_id               varchar,
    payment_queue_name          varchar,
    in_processing               boolean   default false not null,
    payment_complete_required   boolean   default false
);

create table if not exists tb_order_items
(
    product_id varchar(100) not null
        constraint tb_order_item___product
            references tb_product,
    amount     integer      not null,
    order_id   bigint       not null
        constraint tb_order_item___order
            references tb_order
            on delete cascade,
    id         bigserial    not null
        constraint tb_order_items_pk
            primary key
);

create unique index tb_kitchen_delivery_terminal_terminal_id_uindex
    on tb_kitchen_delivery_terminal (terminal_id);

create table if not exists tb_opening_hours
(
    day_of_week          integer   not null,
    from_time            time      not null,
    to_time              time,
    all_day              boolean default false,
    closed               boolean default false,
    delivery_terminal_id bigint
        constraint tb_opening_hours___terminal_fk
            references tb_kitchen_delivery_terminal
            on delete cascade,
    id                   bigserial not null
        constraint tb_opening_hours_pk
            primary key
);

create table if not exists tb_promo_code_impersonal
(
    id         bigserial         not null
        constraint tb_promo_code_impersonal_pk
            primary key,
    promo_code varchar(10)       not null,
    amount     integer default 0 not null,
    created    timestamp,
    updated    timestamp
);

create unique index tb_promo_code_impersonal_promo_code_uindex
    on tb_promo_code_impersonal (promo_code);

create table if not exists tb_promo_code_impersonal_used
(
    promo_code_id bigint not null
        constraint tb_promo_code_impersonal_used___promo_code_fk
            references tb_promo_code_impersonal
            on delete cascade,
    user_id       bigint not null
        constraint tb_promo_code_impersonal_used___user_fk
            references tb_user
            on delete cascade
);

create unique index tb_promo_code_impersonal_used_promo_code_id_user_id_uindex
    on tb_promo_code_impersonal_used (promo_code_id, user_id);

create table if not exists tb_address_directory
(
    id        bigserial not null
        constraint tb_address_directory_pk
            primary key,
    street    varchar,
    house     varchar,
    latitude  numeric,
    longitude numeric,
    created   timestamp default now(),
    updated   timestamp
);

create unique index tb_address_directory_street_house_uindex
    on tb_address_directory (street, house);

alter sequence tb_user_id_seq restart with 100;
alter sequence tb_user_address_id_seq restart with 100;
alter sequence tb_user_bank_card_id_seq restart with 100;
alter sequence tb_user_bank_card_bind_request_id_seq restart with 100;
alter sequence tb_user_preferences_id_seq restart with 100;
alter sequence tb_refresh_token_id_seq restart with 100;

INSERT INTO public.tb_kitchen (id, organization_id, restaurant_name, restaurant_address, created, updated, payment_type_id, payment_type_name, payment_type_code, payment_type_external_revision, payment_type_combinable, payment_type_deleted) 
VALUES (1, '589e721e-9e1a-11e9-80dd-d8d385655247', 'Столовая ERG', 'Кабанбай Батыра 30', '2019-09-19 14:15:17.299683', null, 'c16e3c4b-57ef-4a49-8df5-a5d402cb01b3', 'Оплата доставки банковской картой', '16', 300528, true, false);

INSERT INTO public.tb_kitchen_delivery_terminal (id, terminal_name, terminal_id, created, updated, kitchen_id, time_zone) 
VALUES (1, 'Доставка IIKO', '35dae8b5-a641-e8a8-016d-20456d3f0415', '2019-09-19 14:16:02.882658', null, 1, 'UTC+6');

INSERT INTO public.tb_delivery_zone (id, name, created, updated, delivery_terminal_id, active)
VALUES (6, 'Кухня 1', '2019-07-18 16:38:27.263543', null, 1, true);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (1, '09:00:00', '20:00:00', true, false, 1);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (2, '09:00:00', '20:00:00', false, true, 1);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (3, '09:00:00', '15:00:00', false, false, 1);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (4, '09:00:00', '20:00:00', false, false, 1);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (5, '09:00:00', '20:00:00', false, false, 1);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (6, '09:00:00', '20:00:00', false, true, 1);

insert into tb_opening_hours (day_of_week, from_time, to_time, all_day, closed, delivery_terminal_id)
values (7, '09:00:00', '20:00:00', false, true, 1);

INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (0, 51.1460117, 71.411814, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (1, 51.1492423, 71.4124148, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (2, 51.1531187, 71.4125006, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (3, 51.1571024, 71.411299, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (4, 51.1544646, 71.4040034, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (5, 51.1536032, 71.3985102, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (6, 51.1510728, 71.3918154, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (7, 51.1497807, 71.3923304, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (8, 51.1482193, 71.3860648, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (9, 51.1266774, 71.4044325, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (10, 51.1176268, 71.401171, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (11, 51.1123464, 71.4387648, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (12, 51.1210748, 71.4418547, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (13, 51.1207516, 71.4449446, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (14, 51.1227449, 71.4455454, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (15, 51.1230681, 71.4491503, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (16, 51.1248458, 71.4506953, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (17, 51.1270544, 71.4505236, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (18, 51.1286704, 71.447777, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (19, 51.1311481, 71.4463179, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (20, 51.1333564, 71.4343874, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (21, 51.1365879, 71.4356749, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (22, 51.143804, 71.4322417, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (23, 51.1488116, 71.43207, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (24, 51.1512344, 71.427521, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (25, 51.1470886, 71.4226286, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (26, 51.1460117, 71.411814, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (0, 51.1460117, 71.411814, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (1, 51.1492423, 71.4124148, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (2, 51.1531187, 71.4125006, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (3, 51.1571024, 71.411299, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (4, 51.1544646, 71.4040034, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (5, 51.1536032, 71.3985102, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (6, 51.1510728, 71.3918154, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (7, 51.1497807, 71.3923304, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (8, 51.1482193, 71.3860648, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (9, 51.1266774, 71.4044325, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (10, 51.1176268, 71.401171, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (11, 51.1123464, 71.4387648, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (12, 51.1210748, 71.4418547, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (13, 51.1207516, 71.4449446, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (14, 51.1227449, 71.4455454, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (15, 51.1230681, 71.4491503, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (16, 51.1248458, 71.4506953, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (17, 51.1270544, 71.4505236, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (18, 51.1286704, 71.447777, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (19, 51.1311481, 71.4463179, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (20, 51.1333564, 71.4343874, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (21, 51.1365879, 71.4356749, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (22, 51.143804, 71.4322417, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (23, 51.1488116, 71.43207, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (24, 51.1512344, 71.427521, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (25, 51.1470886, 71.4226286, 6);
INSERT INTO public.tb_delivery_coordinate (location_order, latitude, longitude, delivery_zone_id) VALUES (26, 51.1460117, 71.411814, 6);