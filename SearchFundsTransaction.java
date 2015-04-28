import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class SearchFundsTransaction extends Transaction {

	public SearchFundsTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		boolean flag = false;
		String selection = "";
		int selectionValue = -1;
		String termOne = "";
		String termTwo = "";

		//Ask search amount
		while(!flag){
			if(selectionValue != 1 || selectionValue != 2){
				System.out.println("\nWould you like to search for 1 term or 2?");
				try {
					selection = br.readLine().trim();
					selectionValue = Integer.parseInt(selection);
					flag = true;
				}
				catch ( IOException e ) {
					System.out.println( "IOException: " + e.toString() );
				}
				catch ( NumberFormatException e ) {
					System.out.println( "Parsing error: " + e.toString() );
					selectionValue = -1;
				}
			} 
		}

		//Get search terms
		if(selectionValue == 1){
			System.out.println("\nWhat term would you like to search for?");
			try {
				termOne = br.readLine().trim();
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
		} else {
			System.out.println("\nWhat is the FIRST term to search for?");
			try {
				termOne = br.readLine().trim();
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
			System.out.println("\nWhat is the SECOND term to search for?");
			try {
				termTwo = br.readLine().trim();
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
		}

		//Perform Search query
		try {
			statement = connection.createStatement();
			String query = (selectionValue==1)?"select * from mutualfund where description like '%"+termOne+"%'":"select * from mutualfund where description like '%"+termOne+"%' and description like '%"+termTwo+"%'";
			resultSet = statement.executeQuery( query );

			//Print results
			printRows(resultSet);
			
		}
		catch ( SQLException e ) {
			System.out.println( "Error with search query. Machine error: " + e.toString() );
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