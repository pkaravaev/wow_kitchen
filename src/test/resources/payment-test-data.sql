insert into tb_order (id, total_cost, status, user_id, created, updated, applied_bonus_amount, products_cost,
                      address_id, cutlery, status_queue_name, delivery_time, transaction_id)
values (4, 50, 'NEW', 1, '2019-02-02', '2019-02-02', 50, 100, 1, false, 'queue-name_2', 30, 1111);

insert into tb_order_items (product_id, amount, order_id)
values ('1', 1, 4);

insert into tb_user_bank_card_bind_request (id, transaction_id, user_id, created, updated, queue_name)
values (1, 4444, 1, '2019-01-01', '2019-01-01', 'queue-name');