import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class NewCustomerTransaction extends Transaction {

	public NewCustomerTransaction( Connection connection ) {
		super( connection );
		isAdmin = false;
	}

	private boolean nameConfirmed() {
	
		boolean nameAvailable = false;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

		try {
			System.out.print( "Enter their username: " );
			username = br.readLine().trim();
			
			if ( username.length() > 10 ) {
				System.out.println( "Name exceeds maximum length (must be 10 characters or less)" );
				return false;
			}
			
			String tableName = ( isAdmin ) ? "administrator" : "customer";

			statement = connection.createStatement();
			String query = "SELECT COUNT(*) FROM " + tableName + " WHERE login = '" + username + "'";
			resultSet = statement.executeQuery( query );
			
			if ( resultSet.next() ) {
				if ( resultSet.getInt( 1 ) == 0 )
					nameAvailable = true;
				else 
					System.out.print("Name already taken!\n");
			}
			else
				return true;
			
		}
		catch ( SQLException e ) {
			System.out.println( "Error validating user. Machine error: " + e.toString() );
			System.exit(0);
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString());
			System.exit(0);
		}
		finally {
			try {
				if (statement != null) 
					statement.close();
			} catch (SQLException e) {
				System.out.println( "Cannot close Statement. Machine error: " + e.toString() );
				System.exit(0);
			}
		}
		
		return nameAvailable;
	}

	public void execute() {
	
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String name = "";
		String address = "";
		String email = "";
		String password = "";
		String userType = "";
		
		try {
			
			boolean rightLength = false;
		
			while ( !rightLength ) {
				System.out.print( "\nPlease enter the new user's name: " );
				name = br.readLine().trim();
				
				if ( name.length() <= 20 ) {
					rightLength = true;
				}
				else {
					System.out.println( "Name too long, must be less than or equal to 20 characters." );
				}
			}
			
			rightLength = false;
			
			while ( !rightLength ) {
				System.out.print( "Please enter their address: " );
				address = br.readLine().trim();
				
				if ( address.length() <= 30 ) {
					rightLength = true;
				}
				else {
					System.out.println( "Address too long, must be less than or equal to 30 characters." );
				}
			}
			
			rightLength = false;
			
			while ( !rightLength ) {
				System.out.print( "Please enter their email: " );
				email = br.readLine().trim();
			
				if ( email.length() <= 25 ) {
					rightLength = true;
				}
				else {
					System.out.println( "Email too long, must be less than or equal to 25 characters." );
				}
			}
						
			rightLength = false;
			
			while ( !rightLength ) {
				System.out.print( "User is admin? (y/n): " );
				userType = br.readLine().trim();
				
				if ( userType.length() == 0 ) 
					System.out.println( "Please enter input." );
				else
					rightLength = true;
			}
			
			isAdmin = ( userType.toLowerCase().charAt(0) == 'y' ) ? true : false;

			while ( nameConfirmed() == false );

			rightLength = false;

			while ( !rightLength ) {
				System.out.print( "Please enter their password: " );
				password = br.readLine().trim();

				if ( password.length() <= 10 )
					rightLength = true;
				else
					System.out.println( "Password too long, must be less than or equal to 10 characters." );
			}
		}
		catch ( IOException e ) {
			System.out.println( "IOException: " + e.toString() );
			System.exit(0);
		}
				
		try {
			statement = connection.createStatement();
			
			String query;
			
			if ( isAdmin )
				query = "INSERT INTO administrator (login, name, email, address, password) VALUES ('" + username + "','" + name + "','" + email + "','" + address + "','" + password + "')";
			else
				query = "INSERT INTO customer (login, name, email, address, password, balance) VALUES ('" + username + "','" + name + "','" + email + "','" + address + "','" + password + "',0)";
				
			statement.executeQuery( query );
			results = "User added successfully!";
			success = true;
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
		
	}

	private String username;
	private Statement statement;
	private ResultSet resultSet;
	private boolean isAdmin;
}
