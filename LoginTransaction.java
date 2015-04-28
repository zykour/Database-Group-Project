import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class LoginTransaction extends Transaction {

	public LoginTransaction( Connection connection, int loginType ) {
		super( connection );
		this.loginType = loginType;
	}

	public void execute() {
		
		Statement statement = null;
		ResultSet resultSet = null;
		
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String username = "";
		String password = "";	
		String loginPrompt = ( loginType == 2 ) ? "Admin login --" : "User login --";
		
		try {
			System.out.print("\n" + loginPrompt + "\n\nUsername:\t");
			username = br.readLine().trim();
			System.out.print("Password:\t");
			password = br.readLine().trim();
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
		}

		try {
			statement = connection.createStatement();
			String tableName = ( loginType == 2 ) ? "administrator" : "customer";
			String query = "SELECT password FROM " + tableName + " WHERE login = '" + username + "'";
			resultSet = statement.executeQuery( query );

			int compare = 0;

			if ( resultSet.next() )
				compare = password.compareTo( resultSet.getString( 1 ) );
			else {
				success = false;
				results = "User '" + username + "' not found.";
				return;
			}
				

			success = ( compare == 0 ) ? true : false;
		}
		catch ( SQLException e ) {
			results = "Error validating user. Machine error: " + e.toString();
			success = false;
			return;
		}
		finally {
			try {
				if (statement != null) statement.close();
			} catch (SQLException e) {
				results = "Cannot close Statement. Machine error: " + e.toString();
			}
		}
		
		results = ( success ) ? "Logged in successfully!" : "Login attempt failed.";

		if(success){
			BetterFutures.setCurrentUser(username); //Save user for TRXLOG inserts
		}
	
	}
	
	private int loginType;
}
