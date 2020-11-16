-- Заказы
--#1
-- test user 1
insert into tb_order (id, total_cost, status, user_id, created, updated, applied_bonus_amount, products_cost,
                      iiko_order_id, address_id, transaction_id, cloud_payment_status, payment_decline_reason, payment_decline_reason_code,
                      payment_type, bank_card_id, cutlery, status_queue_name, paid_time, delivery_time, in_processing)
values (1, 200, 'PAID', 1, '2019-01-01', '2019-01-01', 0, 200, 'IIKO_ORDER_ID', 1, 333777, 'Authorized', 'Approved', 0,
        'CARD', 1, false, 'queue-name', '2019-01-01', 30, true);

insert into tb_order_items (product_id, amount, order_id)
values ('1', 1, 1);

insert into tb_order_items (product_id, amount, order_id)
values ('2', 1, 1);

--#2
insert into tb_order (id, total_cost, status, user_id, created, updated, applied_bonus_amount, products_cost,
                      iiko_order_id, address_id, transaction_id, cloud_payment_status, payment_decline_reason, payment_decline_reason_code,
                      payment_type, bank_card_id, cutlery, status_queue_name, paid_time, delivery_time)
values (2, 300, 'PAID', 1, '2019-02-02', '2019-02-02', 0, 300, 'IIKO_ORDER_ID_2', 2, 999444, 'Authorized', 'Approved', 0,
        'CARD', 2, false, 'queue-name_2', '2019-02-02', 30);

insert into tb_order_items (product_id, amount, order_id)
values ('3', 1, 2);

insert into tb_order_items (product_id, amount, order_id)
values ('4', 1, 2);

insert into tb_order_items (product_id, amount, order_id)
values ('5', 1, 2);

--#3
-- test user 2
insert into tb_order (id, total_cost, status, user_id, created, updated, applied_bonus_amount, products_cost,
                      iiko_order_id, address_id, transaction_id, cloud_payment_status, payment_decline_reason, payment_decline_reason_code,
                      payment_type, bank_card_id, cutlery, status_queue_name, paid_time, delivery_time)
values (3, 100, 'PAID', 2, '2019-02-02', '2019-02-02', 0, 100, 'IIKO_ORDER_ID_3', 3, 111111, 'Authorized', 'Approved', 0,
        'CARD', 3, false, 'queue-name_2', '2019-02-02', 30);

insert into tb_order_items (product_id, amount, order_id)
values ('1', 1, 3);