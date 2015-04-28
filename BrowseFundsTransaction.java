import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class BrowseFundsTransaction extends Transaction {

	public BrowseFundsTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;
		boolean flag = false;
		String selection = "";
		int selectionValue = -1;
		String query = "";

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

		while(!flag){
			if(selectionValue < 1){
				System.out.println("\nWhat would you like to see?");
				System.out.println("1. Look at all funds.");
				System.out.println("2. Look at funds for a given category.");
				System.out.println("3. Look at all funds sorted alphabetically.");
				System.out.println("4. Look at all funds for a given date, sorted by highest price.");

				try {
					selection = br.readLine().trim();
					selectionValue = Integer.parseInt(selection);
				}
				catch ( IOException e ) {
					System.out.println( "IOException: " + e.toString() );
				}
				catch ( NumberFormatException e ) {
					System.out.println( "Parsing error: " + e.toString() );
					selectionValue = -1;
				}
			}

			switch(selectionValue){
				case 1:
					query = "SELECT * FROM MUTUALFUND";
					flag = true;
					break;
				case 3:
					query = "SELECT * FROM MUTUALFUND ORDER BY NAME ASC";
					flag = true;
					break;
				case 2: //get category
					System.out.println("Which category? (fixed, bonds, stocks, or mixed)");
					try {
						selection = br.readLine().trim();
						query = "SELECT * FROM MUTUALFUND WHERE category='"+selection+"'";
						flag = true;
					}
					catch ( IOException e ) {
						System.out.println( "IOException: " + e.toString() );
					}
					break;
				case 4: //get date 
					System.out.println("Which date? (Format: DD-MON-YY)");
					try {
						selection = br.readLine().trim();
						query = "select mutualfund.symbol, mutualfund.name, mutualfund.category, mutualfund.c_date, A.price, A.p_date FROM mutualfund JOIN (select * from closingprice where p_date='"+selection+"') A on (mutualfund.symbol = A.symbol) order by A.price desc";
						flag = true;
					}
					catch ( IOException e ) {
						System.out.println( "IOException: " + e.toString() );
					}
					break;
				default:
					//do nothing

			}
		}

		try {
			statement = connection.createStatement();
			
			resultSet = statement.executeQuery( query );

			//Print results
			printRows(resultSet);
		}
		catch ( SQLException e ) {
			System.out.println( "Error with browse query. Machine error: " + e.toString() );
		}
		finally {
			try {
				if (statement != null) statement.close();
			} catch (SQLException e) {
				System.out.println( "Cannot close Statement. Machine error: " + e.toString() );
			}
		}
		
	}

}