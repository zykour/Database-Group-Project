import java.sql.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateDateTimeTransaction extends Transaction {

	public UpdateDateTimeTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		Statement statement = null;
		ResultSet resultSet = null;

		String date = "";
		BufferedReader br = new BufferedReader( new InputStreamReader ( System.in ) );
		
		try {
			boolean rightLength = false;
			
			while ( !rightLength ) {
				System.out.print( "Enter the new date in YYYY-MM-DD format: " );
				date = br.readLine().trim();
			
				if ( date.matches("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])") )
					rightLength = true;
				else
					System.out.println( "Date pattern does not match." );
			}
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
			System.exit(0);
		}
		
		try {
			statement = connection.createStatement();
			String query = "UPDATE mutualdate SET c_date = ( select to_date( '" + date + "', 'yyyy-mm-dd' ) from dual )";
			statement.executeQuery( query );
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
		results = "Date successfully updated!";

	}

}
