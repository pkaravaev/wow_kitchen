-- Категории
insert into tb_product_category (id, name, created, updated, view_order)
values ('1', 'TEST_CATEGORY_1', '2019-01-01', '2019-01-01', 0);

insert into tb_product_category (id, name, created, updated, view_order)
values ('2', 'TEST_CATEGORY_2', '2019-01-01', '2019-01-01', 1);

insert into tb_product_category (id, name, created, updated, view_order)
values ('3', 'TEST_CATEGORY_3', '2019-01-01', '2019-01-01', 2);

-- Продукты
insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list, vegetarian, spicy, product_preference_key_words)
values ('1', '1', 'TEST_PRODUCT_1', 100, '2019-01-01', '2019-01-01', true, false, true, true, 'яйцо');

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('2', '1', 'TEST_PRODUCT_2', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('3', '1', 'TEST_PRODUCT_3', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('4', '2', 'TEST_PRODUCT_4', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('5', '2', 'TEST_PRODUCT_5', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('6', '2', 'TEST_PRODUCT_6', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('7', '3', 'TEST_PRODUCT_7', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('8', '3', 'TEST_PRODUCT_8', 100, '2019-01-01', '2019-01-01', true, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('9', '3', 'TEST_PRODUCT_9', 100, '2019-01-01', '2019-01-01', true, false);
-- Не включены в меню
insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('10', '3', 'TEST_PRODUCT_10', 100, '2019-01-01', '2019-01-01', false, false);

insert into tb_product (id, product_category_id, name, price, created, updated, included_in_menu, in_stop_list)
values ('11', '3', 'TEST_PRODUCT_11', 100, '2019-01-01', '2019-01-01', true, true);