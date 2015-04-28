import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class BuySharesTransaction extends Transaction {

	public BuySharesTransaction( Connection connection, String username ) {
		super( connection );
		this.username = username;
	}

	public void execute() {
	
		Statement statement = null;
		ResultSet resultSet = null;
		double balance = 0.0;
		
		try {
			statement = connection.createStatement();
			String query = "SELECT balance FROM customer WHERE login = '" + username + "'";
			resultSet = statement.executeQuery( query );
			
			if ( resultSet.next() ) {
				System.out.println( "You have $" + resultSet.getDouble(1) + " dollars to spend." );
				balance = resultSet.getDouble(1);
			}
		}
		catch ( SQLException e ) { 
			System.out.println( "SQLException: " + e.toString() );
			System.exit(0);
		}
				
		try {
			statement = connection.createStatement();
			String query = "SELECT name, symbol FROM mutualfund";
			resultSet = statement.executeQuery( query );
			
			System.out.println();

			while ( resultSet.next() ) {
				System.out.println( resultSet.getString(1) + ": " + resultSet.getString(2) );
			}

			System.out.println();
		}
		catch ( SQLException e ) {
			System.out.println( "SQLException: " + e.toString() );
			System.exit(0);
		}
		
		String symbol = "";
		
		boolean rightLength = false;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		
		while ( !rightLength ) {
			System.out.print( "Please select the mutual fund to be updated by it's symbol: " );
			try {
				symbol = br.readLine().trim().toUpperCase();
			} catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
				System.exit(0);
			}

			try { 
				statement = connection.createStatement();
				String query = "Select count(*) from mutualfund where symbol = '" + symbol + "'";
				resultSet = statement.executeQuery( query );
				
				if ( resultSet.next() ) {
					if ( resultSet.getInt(1) > 0 )
						rightLength = true;
					else
						System.out.println( "Incorrect symbol." );
				}
				else {
					System.out.println( "Incorrect symbol." );
				}
			}
			catch ( SQLException e ) {
				System.out.println( "SQLException: " + e.toString() );
				System.exit(0);
			}
		}

		rightLength = false;
		String amount = "";
		String shares = "";
		double amountVal = 0.0;
		int sharesNum = 0;
		
		try {
			System.out.print( "How much do you want to spend? (Leave blank to specify number of shares): " );
			amount = br.readLine().trim();
			System.out.print( "How many shares do you want to buy? (Leave blank if spending amount was specified): ");
			shares = br.readLine().trim();
		} catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
			System.exit(0);
		}

	 	double fundPrice = 0.0;
		
		boolean numsFlag = false;
		
		if ( amount.length() > 0 ) {
			try {
				amountVal = Double.parseDouble( amount );
			}
			catch ( NumberFormatException e ) {
				results = "Amount input not a number.";
				success = false;
				return;
			}
		}
		else {
			try {
				sharesNum = Integer.parseInt( shares );
			}
			catch ( NumberFormatException e ) {
				results = "Shares input not a number.";
				success = false;
				return;
			}
			
			numsFlag = true;
		}
		
		try {		
			statement = connection.createStatement();
			String query = "select price from closingprice where symbol = '" + symbol +"' and p_date = ( select max(p_date) from closingprice where symbol = '" + symbol + "' )";
			resultSet = statement.executeQuery( query );
			
			if ( resultSet.next() ) {
			
				if ( numsFlag )
					amountVal = sharesNum * resultSet.getDouble(1);	
				else {
					amountVal = Math.floor( amountVal / resultSet.getDouble(1) ) * resultSet.getDouble(1);
					sharesNum = (int) Math.floor( amountVal / resultSet.getDouble(1) );	
				}
				
				fundPrice = resultSet.getDouble(1);
			}
			else {
				results = "There doesn't appear to be a closing price for fund '" + symbol + "'";
				success = false;
				return;
			}
		} catch ( SQLException e ) {
			System.out.println( "SQLException: " + e.toString() );
			System.exit(0);
		}
			
		if ( amountVal > balance ) {
			results = "Unable to buy funds. Not enough money in balance. \nOwned: $" + balance + "\nPurchase Amount: $" + amountVal;
			success = false;
			return;
		}

		try {
			statement = connection.createStatement();
			String query = "ALTER TRIGGER deposit_trigger DISABLE";
			statement.executeQuery( query );
			query = "UPDATE customer SET balance = ( balance - " + amountVal + " ) WHERE login = '" + username + "'";
			statement.executeQuery( query );
			query = "ALTER TRIGGER deposit_trigger ENABLE";
			statement.executeQuery( query );
			query = "INSERT INTO TRXLOG VALUES( ( select max(trans_id) from trxlog ) + 1, '" + username + "', '" + symbol + "', ( select max(c_date) from mutualdate ), 'buy', " + sharesNum + "," + fundPrice + "," + amountVal + ")";
			statement.executeQuery( query );
			
			query = "SELECT COUNT(*) FROM OWNS WHERE login = '" + username + "' and symbol = '" + symbol + "'";
			resultSet = statement.executeQuery( query );
			
			if ( resultSet.next() )
				query = "UPDATE OWNS SET shares = ( shares + " + sharesNum + " ) where login = '" + username + "' and symbol = '" + symbol + "'";
			else
				query = "INSERT INTO OWNS VALUES('" + username + "','" + symbol + "', " + sharesNum + ")";
			
			resultSet = statement.executeQuery( query );
		}
		catch ( SQLException e ) {
			System.out.println( "Error validating user. Machine error: " + e.toString() );
			System.exit(0);
		}
		finally {
			try {
				if (statement != null) statement.close();
			} catch (SQLException e) {
				System.out.println( "Cannot close Statement. Machine error: " + e.toString() );
				System.exit(0);
			}

		}
		
		results = "Purchase successful!";
		success = true;		
	}

	private String username;
}
