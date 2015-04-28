import java.sql.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChangeAllocationTransaction extends Transaction {

	public ChangeAllocationTransaction( Connection connection ) {
		super( connection );
	}

	private class Allocation{
		private String symbol;
		private int percentage;

		public Allocation(String s, int p){
			this.symbol = s.toUpperCase();
			this.percentage = p;
		}

		public String getSymbol(){
			return symbol;
		}
		public int getPercentage(){
			return percentage;
		}
	}

	public void execute() {
		
		// get relevant data here from user
		Statement statement = null;
		ResultSet resultSet = null;
		int newAllocId = -1;
		String mutualDate = "";
		ArrayList<Allocation> allocationList = new ArrayList<Allocation>();
		String query = "";
		int percentageAvailable = 100;
		String symbol = "";
		String percentage = "";
		int percentageValue = 0;
		BufferedReader br = new BufferedReader( new InputStreamReader ( System.in ) );
		

		//Get incremented ID for Allocation
		try {
			statement = connection.createStatement();
			query = "SELECT MAX(allocation_no) FROM ALLOCATION";
			resultSet = statement.executeQuery( query );
			resultSet.next();
			newAllocId = resultSet.getInt(1);
			newAllocId++; //increment
		}
		catch ( SQLException e ) {
			System.out.println( "Error with allocation query (get new alloc id). Machine error: " + e.toString() );
			return;
		}

		//Get "current date" -- Mutual Date
		resultSet = null;
		try{
			query = "SELECT c_date FROM mutualdate where rownum=1";
			resultSet = statement.executeQuery( query );
			resultSet.next();
			mutualDate = resultSet.getString(1);
		}
		catch ( SQLException e ) {
			System.out.println( "Error with allocation query (get m_date). Machine error: " + e.toString() );
			return;
		}

		//Check mdate against last allocation date
		resultSet = null;
		try{
			//find a date within the same month, same year, and isnt in the future
			query = "SELECT p_date FROM allocation WHERE to_char(p_date, 'YY') = to_char(TIMESTAMP '"+mutualDate+"', 'YY') and to_char(p_date, 'MM') = to_char(TIMESTAMP '"+mutualDate+"', 'MM') and to_char(p_date, 'DD') <= to_char(TIMESTAMP '"+mutualDate+"', 'DD')";
			resultSet = statement.executeQuery( query );
			
			//return if there is a date
			if(resultSet.next()){
				System.out.println("You may only update your allocations once per month. Sorry -- your last update was: "+ resultSet.getString(1) + "\nAnd today is: "+mutualDate);
				return;
			}
		}
		catch ( SQLException e ) {
			System.out.println( "Error with allocation query (month check). Machine error: " + e.toString() );
			return;
		}

		//Alright, we have proven that it is ok to allocate, prompt user to get symbols and percentages to construct the ArrayList
		while(percentageAvailable > 0){

			System.out.println("\nPercent left to allocate: "+percentageAvailable+"%");

		 	try{
		 		System.out.println("What symbol?");
				symbol = br.readLine().toLowerCase().trim();

				System.out.println("What percentage?");
				percentage = br.readLine().toLowerCase().trim();
		 		percentageValue = Integer.parseInt(percentage);

		 		percentageAvailable -= percentageValue; 
			}
			catch ( IOException e ) {
				System.out.println( "IOException: " + e.toString() );
			}
		 	catch ( NumberFormatException e ) {
		 		System.out.println( "NumberFormatException: " + e.toString() );
		 	}

		 	allocationList.add(new Allocation(symbol, percentageValue));
		}

		//Make sure we didnt over-allocate
		if(percentageAvailable != 0){
			System.out.println( "You over-allocated by "+(-percentageAvailable)+"%! Sorry, can not complete transaction.");
			return;
		}

		//Finally, INSERT into allocation and prefers table
		try{

			//Allocations table
			query = "INSERT INTO ALLOCATION(allocation_no, login, p_date) values("+newAllocId+", '"+BetterFutures.getCurrentUser()+"', TIMESTAMP '"+mutualDate+"' )";
			resultSet = statement.executeQuery( query );
			
			//Commit;
			query = "commit";
			resultSet = statement.executeQuery( query );
			
			//Prefers table
			for(Allocation a : allocationList){
				System.out.println("a.sym, a.per "+a.getSymbol()+"    ."+a.getPercentage());
				query = "INSERT INTO PREFERS(allocation_no, symbol, percentage) values("+newAllocId+", '"+a.getSymbol()+"', ."+a.getPercentage()+")";

				resultSet = statement.executeQuery( query );
			}

			//Commit;
			query = "commit";
			resultSet = statement.executeQuery( query );
			
		}
		catch ( SQLException e ) {
			System.out.println( "Error with allocation query (inserts). Machine error: " + e.toString() );
			return;
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