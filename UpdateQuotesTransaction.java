import java.sql.*;
import java.text.ParseException;
import java.lang.Double;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateQuotesTransaction extends Transaction {

	public UpdateQuotesTransaction( Connection connection ) {
		super( connection );
	}

	
	public void execute() {
		
		Statement statement = null;
		ResultSet resultSet = null;

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		
		String symbol = "";
		String date = "";
		String price = "";
		
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

		try {
		
			boolean rightLength = false;
		
			while ( !rightLength ) {
				System.out.print( "Please select the mutual fund to be updated by it's symbol: " );
				symbol = br.readLine().trim().toUpperCase();
				
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
			
			try {
				statement = connection.createStatement();
				String query = "SELECT MAX(c_date) FROM mutualdate";
				resultSet = statement.executeQuery( query );
				resultSet.next();
				
				rightLength = false;
				
				while ( !rightLength ) {
					System.out.print( "\nPlease select the date (in yyyy-mm-dd format) that you wish to change the price for (today's mutual date is: " + resultSet.getDate(1) + "): " );
					date = br.readLine().trim();
				
					if ( date.matches("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])") )
						rightLength = true;
					else
						System.out.println( "Date pattern does not match." );
				}
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
			
			System.out.print( "\nPlease enter the new price: " );
			price = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
			System.exit(0);
		}
		
		double priceValue = 0.0;
		
		try {
			priceValue = Double.parseDouble( price );
		}
		catch ( NumberFormatException e ) {
			results = "Error: Price input is not a number.";
			success = false;
			return;
		}

		try {
			statement = connection.createStatement();
			String query = "UPDATE closingprice SET price = " + priceValue + " WHERE p_date = ( select to_date( '" + date + "', 'yyyy-mm-dd' ) from dual ) AND symbol = '" + symbol + "'";
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

		success = true;
		results = "Update successful.";
		
	}

}
