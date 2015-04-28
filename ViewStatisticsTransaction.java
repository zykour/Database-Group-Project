import java.sql.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ViewStatisticsTransaction extends Transaction {

	public ViewStatisticsTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		results = "Statistics unsuccessfully retrieved";
		
		Statement statement = null;
		ResultSet resultSet = null;

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		
		String numMonthsToView = "0";
		String numHighestInvestedCategories = "0";
		String numMostInvestors = "0";
		
		try {
			System.out.print( "Enter the number of past months to include in the statistics: " );
			numMonthsToView = br.readLine().trim();
		
			System.out.print( "Enter the number of highest volumne categories to view: " );
			numHighestInvestedCategories = br.readLine().trim();
		
			System.out.print( "Enter the number of most investors to view: " );
			numMostInvestors = br.readLine().trim();
		}
		catch ( IOException e ) {
			// Do not attempt to recover from IOExceptions
			System.out.println( "IOException: " + e.toString() );
			System.exit(0);
		}

		int numMonths = 0;
		int numCategories = 0;
		int numInvestors = 0;
		
		try {
			numMonths = Integer.parseInt( numMonthsToView );
			numCategories = Integer.parseInt( numHighestInvestedCategories );	
			numInvestors = Integer.parseInt( numMostInvestors );
		}
		catch ( NumberFormatException e ) {
			results = "Invalid input entered, please only enter numbers";
			success = false;
			return;
		}

		try {
			statement = connection.createStatement();
			String query = "select max(c_date) from mutualdate";
			resultSet = statement.executeQuery( query );
			resultSet.next();
			Date date = resultSet.getDate(1);
			query = "select * from ( select * from ( select category, sum(sum_shares) as sh_count from mutualfund, ( select symbol as sym, sum(num_shares) as sum_shares from trxlog where action = 'buy' and t_date >= add_months( ( select to_date( '" + date.toString() + "', 'yyyy-mm-dd') from dual ), -" + numMonths + ") group by symbol )where symbol = sym group by category )order by sh_count desc ) where rownum <= " + numCategories;
			resultSet = statement.executeQuery( query );
			
			System.out.println("Category\tNumber of shares\n--------------------------------------");
			
			while ( resultSet.next() ) {
				System.out.println( resultSet.getString(1) + "\t\t" + resultSet.getInt(2) );
			}
			
			query = "select * from ( select name, sum_amounts from customer, ( select * from ( select login as username, sum(amount) as sum_amounts from trxlog where action = 'buy' and t_date >= add_months( ( select to_date( '" + date.toString() + "', 'yyyy-mm-dd' ) from dual ), -" + numMonths + ") group by login ) order by sum_amounts desc ) where login = username ) where rownum <= " + numInvestors;
			resultSet = statement.executeQuery( query );
			
			System.out.println("\nName\t\tInvested Amount\n------------------------------------");
			
			while ( resultSet.next() ) {
				System.out.println( resultSet.getString(1) + "\t\t" + resultSet.getFloat(2) );
			}
			
			success = true;
			results = "Statistics retrieved successfully!";
		}
		catch ( SQLException e ) {
			// Don't attempt to recover
		
			System.out.println( "Error validating user. Machine error: " + e.toString() );
			System.exit(0);
		}
		finally {
			try {
				if (statement != null) 
					statement.close();
			} 
			catch (SQLException e) {
				System.out.println( "Cannot close Statement. Machine error: " + e.toString() );
				System.exit(0);
			}
		}
	}
}
