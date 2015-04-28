/*
 * 	A simple Transaction class which all transaction must extend.
 */

import java.sql.Connection;
import java.sql.*; 

public class Transaction {

	/*
	 *		Inheriting classes can call super( Connection var ) in constructor
	 */
	public Transaction( Connection connection ) {
		success = false;
		results = "";
		this.connection = connection;
	}
	
	private Connection createNewConnection() {
		Connection connection = null;
		try {
			DriverManager.registerDriver( new oracle.jdbc.driver.OracleDriver() );
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
			connection = DriverManager.getConnection(url, "ajc148", "3861324");
		} catch ( Exception e ) {
			System.out.println(e.toString());
		} finally {
			try {
				connection.close();
			}
			catch ( SQLException e ) {
				System.out.println( e.toString() );
			}
		}
		return connection;
	}

	public Transaction() {
		success = false;
		results = "";
	}
	
	public boolean isSuccessful() {
		return success;
	}
	
	public String toString() {
		return results;
	}

	//Helper function for all results
	protected void printRows(ResultSet resultSet){
		try{
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int numCols = rsmd.getColumnCount();
			while(resultSet.next()){
				for( int i=1; i <= numCols; i++ ){
					if (i > 1) System.out.print(",\t");
					System.out.print(resultSet.getString(i));
				}
				System.out.println();

			}
		} catch( SQLException e ) {
			System.out.println( "SQLException while printing rows: " + e.toString() );
		}
	}	

	/*
	 *		Inheriting classes must set success inside execute
	 */
	public void execute() {}
	
	protected Connection connection;
	protected String results;
	protected boolean success;
}
