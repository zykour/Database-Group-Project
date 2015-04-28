DROP FUNCTION GrabPrice;
DROP PROCEDURE MakePurchases;

-- A trigger to update the balance. 
---- Assume that the insert statement that sets off the trigger
---- Contains the amount of the sale (this seems to be the case based on sample data)

CREATE OR REPLACE TRIGGER balance_update
	
	AFTER INSERT
	ON trxlog
	FOR EACH ROW
	WHEN ( new.action = 'sell' ) 

	DECLARE

		md_count INT := 0;
		tr_date DATE;
	
	BEGIN

		SELECT COUNT(c_date) INTO md_count FROM mutualdate;

		IF md_count > 0 THEN

			SELECT MAX(c_date) INTO tr_date FROM mutualdate;

			IF tr_date = :new.t_date THEN

				UPDATE customer
				SET balance = ( balance + :new.amount )
				WHERE login = :new.login;


			END IF;

		END IF;

	END;
/

-- A function to grab the price of a symbol based on the date before the one stored in MUTUALDATES
---- Assume that all triggers using this function are on NEW data, i.e. data that is being done on the date of MUTUALDATE

CREATE OR REPLACE FUNCTION GrabPrice( symb VARCHAR2 )

	RETURN FLOAT IS
	ret_price FLOAT := 0;
	tr_date DATE;
	t_date DATE;

	BEGIN

		SELECT c_date INTO tr_date FROM mutualdate WHERE rownum = 1;

		SELECT MAX(p_date) INTO t_date FROM closingprice WHERE p_date < tr_date;
	
		SELECT price INTO ret_price FROM CLOSINGPRICE
		WHERE ( symbol = symb AND p_date = t_date );

		RETURN ret_price;

	END;
/

-- A procedure used in the deposit trigger to buy funds after a deposit
---- Makes inserts into OWNS based on what is purchased
---- Updates balance if there is any leftover deposit money (anything the purchase of funds don't evenly go into)
---- Assume that we allocate PERCENT * DEPOSIT dollars for each fund type, excess is stored in CUSTOMER.balance

CREATE OR REPLACE PROCEDURE MakePurchases( login_name VARCHAR2, dep_amnt FLOAT, x OUT FLOAT )
	IS 

	CURSOR pref_cursor IS SELECT * FROM prefers
		WHERE ( allocation_no = ( SELECT allocation_no FROM 
			( SELECT * FROM ALLOCATION ORDER BY allocation_no DESC )
			WHERE rownum = 1 ) );

	pref_row prefers%ROWTYPE;

	partial_dep FLOAT := 0.0;
	num_shares INT := 0;
	t_date DATE;
	leftover FLOAT := 0.0;
	already_owned INT := 0;	

	BEGIN

		SELECT MAX(c_date) INTO t_date FROM mutualdate;

		IF NOT pref_cursor%ISOPEN
			THEN OPEN pref_cursor;
		END IF;

		LOOP
			FETCH pref_cursor INTO pref_row;
			EXIT WHEN pref_cursor%NOTFOUND;
			partial_dep := pref_row.percentage * dep_amnt;
			SELECT trunc( partial_dep / GrabPrice( pref_row.symbol ) ) INTO num_shares from dual;

			SELECT COUNT(*) into already_owned FROM owns WHERE ( login = login_name AND symbol = pref_row.symbol );

			IF already_owned = 1 THEN
				UPDATE owns
				SET shares = ( shares + num_shares )
				WHERE ( login = login_name AND symbol = pref_row.symbol );			
			ELSE
				INSERT INTO owns (login, symbol, shares)
				VALUES ( login_name, pref_row.symbol, num_shares );
			END IF;

			INSERT INTO trxlog ( trans_id, login, symbol, t_date, action, num_shares, price, amount)
				VALUES ( ( select max(trans_id) from trxlog ) + 1, login_name, pref_row.symbol, t_date, 'buy', num_shares, 
				GrabPrice( pref_row.symbol ), GrabPrice( pref_row.symbol ) * num_shares );

			leftover := leftover + ( partial_dep - ( num_shares * GrabPrice( pref_row.symbol ) ) );

		END LOOP;

		CLOSE pref_cursor;

		x := leftover;

	END;
/
						
-- The trigger to call the MakePurchases procedure
						
CREATE OR REPLACE TRIGGER deposit_trigger

	BEFORE UPDATE OF balance
	ON customer
	FOR EACH ROW
	WHEN (new.balance > 0)

	DECLARE

	x FLOAT := 0;

	BEGIN

		MakePurchases( :new.login, :new.balance, x );
		:new.balance := x;

	END;
/
