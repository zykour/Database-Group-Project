
-- Simple tests of the triggers. May want to change so that it works regardless of existing data, with checks to make sure the inserts
-- don't violate Unique/primary key constraints (i.e. the new test data doesn't have the same primary key as something already in the database)
-- When fixing the test data, make sure to use dates relative to MUTUALDATE

-- For now, we are using the data already in data.sql as a basis for these tests

-- Run order: team1-db.sql --> team1-data.sql --> team1-triggers.sql --> team1-trigger-test.sql
-- data and triggers should be able to be run in reversed order (triggers before data), triggers only
-- trigger if the date in mutualdate is the date in a triggered trxlog action. This way we can load old data into the database without
-- considering it a live action

select balance from customer where login = 'mike';
INSERT INTO TRXLOG(trans_id, login, symbol, t_date, action, num_shares, price, amount) values(4, 'mike', 'RE', '04-APR-14', 'sell', 50, 15, 750);
select balance from customer where login = 'mike';
INSERT INTO TRXLOG(trans_id, login, symbol, t_date, action, num_shares, price, amount) values(5, 'mike', NULL, '04-APR-14', 'deposit', NULL, NULL, 1000);
select * from trxlog where trans_id > 5;