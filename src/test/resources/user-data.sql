delete from tb_user where id notnull ;
delete from tb_preferences_key_words where id notnull ;

-- test user 1

insert into tb_user (id, name, enabled, country_code, mobile_number, created, updated, hard_logout_last_time, bonus_amount,
                     registration_promo_code, registration_promo_code_used, registration_promo_cashback_returned, bonuses_spent,
                     registration_promo_code_owner_user_id)
values (1, 'TEST_USER_1', true, '7', 'J6c8GZ1OATc94i2j3YjblA==', '2019-01-01', '2019-01-01', null, 100, 'AAAAA', false, false, 0, null);

-- test user 2
insert into tb_user (id, name, enabled, country_code, mobile_number, created, updated, hard_logout_last_time, bonus_amount,
                     registration_promo_code, registration_promo_code_used, registration_promo_cashback_returned, bonuses_spent,
                     registration_promo_code_owner_user_id)
values (2, 'TEST_USER_2', true, '7', 'dQceMjT94+hrh9mVwgZs8A==', '2019-01-01', '2019-01-01', null, 0, 'BBBBB', false, false, 0, null);

-- Адреса
-- test user 1
insert into tb_user_address (id, city, street, home, housing, apartment, entrance, floor, doorphone, address_comment,
                             created, updated, user_id, actual, delivery_terminal_id)
values (1, 'Нур-Султан', 'улица Жубанова', '1', '2A', '77', '3', '4', '58K6709', 'Вход со двора', '2019-01-01', '2019-01-01', 1, true, 1);

insert into tb_user_address (id, city, street, home, housing, apartment, entrance, floor, doorphone, address_comment,
                             created, updated, user_id, actual, delivery_terminal_id)
values (2, 'Нур-Султан', 'улица Жубанова', '3', '3A', '99', '4', '7', '51K7009', 'Вход с улицы', '2019-01-01', '2019-01-01', 1, false, 1);

-- test user 2
insert into tb_user_address (id, city, street, home, housing, apartment, entrance, floor, doorphone, address_comment,
                             created, updated, user_id, actual, delivery_terminal_id)
values (3, 'Нур-Султан', 'улица Жубанова', '5', '7A', '88', '4', '7', '51K7011', 'Вход с улицы', '2019-01-01', '2019-01-01', 2, true, 1);


-- Рефреш токен
-- test user 1
insert into tb_refresh_token (id, user_id, token, created, updated)
values (1, 1, 'REFRESH_TOKEN', '2019-01-01', '2019-01-01');

-- test user 2
insert into tb_refresh_token (id, user_id, token, created, updated)
values (2, 2, 'REFRESH_TOKEN_2', '2019-01-01', '2019-01-01');


-- Банк. карты
-- test user 1
insert into tb_user_bank_card (id, user_id, card_mask, card_type, created, updated, token, card_issuer, actual)
values (1, 1, '777777******9999', 'VISA', '2019-01-01', '2019-01-01', 'SOME_CARD_TOKEN', 'Iron Bank of Braavos', true);

insert into tb_user_bank_card (id, user_id, card_mask, card_type, created, updated, token, card_issuer, actual)
values (2, 1, '999999******7777', 'Master Card', '2019-01-01', '2019-01-01', 'SOME_ANOTHER_CARD_TOKEN', 'Iron Bank of Braavos', false);

-- test user 2
insert into tb_user_bank_card (id, user_id, card_mask, card_type, created, updated, token, card_issuer, actual)
values (3, 2, '111111******4444', 'Master Card', '2019-01-01', '2019-01-01', 'ONE_MORE_CARD_TOKEN', 'Iron Bank of Braavos', true);


-- Предпочтения юзера

insert into tb_user_preferences (id, user_id, vegetarian, not_spicy, without_nuts, dont_like, created, updated)
values (1, 1, false, false, false, 'лук#яйцо', '2019-01-01', '2019-01-01');

insert into tb_user_preferences (id, user_id, vegetarian, not_spicy, without_nuts, dont_like, created, updated)
values (2, 2, true, true, false, 'лук#яйцо', '2019-01-01', '2019-01-01');

insert into tb_preferences_key_words (id, word)
values (1, 'морепродукты');
