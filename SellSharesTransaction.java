import java.sql.*;

public class SellSharesTransaction extends Transaction {

	public SellSharesTransaction( Connection connection ) {
		super( connection );
	}

	public void execute() {
		
		// get relevant data here from user
		
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			statement = connection.createStatement();
			//String query = "";
			//resultSet = statement.executeQuery( query );
			
			//resultSet.next();
		}
		catch ( SQLException e ) {
			System.out.println( "Error while getting mutualdate. Machine error: " + e.toString() );
		}
			//user owns that amount
		try {
			query = "SELECT symbol FROM owns WHERE login ='"+BetterFutures.getCurrentUser()+"' AND symbol = '"+symbol+"' AND shares >= "+numSharesValue;
			resultSet = statement.executeQuery( query );
			if(!resultSet.next()){ //INVALID!
				System.out.println("Sorry! You do not seem to own that amount of shares to sell! We cannot process this SELL.");
				return;
			}
		}
		catch ( SQLException e ) {
			System.out.println( "Error while validating 'user owns'. Machine error: " + e.toString() );
		}
			//closing price exists && save closing price for insert stmt. below
		try {
			query = "SELECT price, p_date FROM closingprice WHERE symbol = '"+symbol+"' AND p_date < TIMESTAMP '"+mutualDate+"' ORDER BY p_date DESC" ;
			resultSet = statement.executeQuery( query );

			if(!resultSet.next()){ //INVALID!
				System.out.println("Sorry! This fund does not seem to have a closing price! We cannot process this SELL.");
				return;
			} else {
				priceValue = resultSet.getInt(1);
			}
		} 
		catch ( SQLException e ) {
			System.out.println( "Error while validating / getting closing price. Machine error: " + e.toString() );
		}
			
		
		//update tables
		try {
			//disables
			connection.setAutoCommit(false);
			query = "ALTER TRIGGER deposit_trigger DISABLE";
			statement.executeQuery( query );

			// Decrement shares in OWNS
			query = "UPDATE owns SET shares = (shares - "+numSharesValue+") WHERE login ='"+BetterFutures.getCurrentUser()+"' AND symbol = '"+symbol+"'";
			statement.executeQuery( query );

			// Insert into TRXLOG -> (sell trigger should fire)
				//get transaction ID++
			query = "SELECT max(trans_id)+1 FROM TRXLOG";
			resultSet = statement.executeQuery(query);
			resultSet.next();
			nextTRXid = Integer.parseInt(resultSet.getString(1));
				//calculate amount
			amountValue = numSharesValue * priceValue;
			int truncate = (int)(amountValue * 100);
			amountValue = truncate/100;
				//insert 
			query = "INSERT INTO TRXLOG(trans_id, login, symbol, t_date, action, num_shares, price, amount) values("+nextTRXid+", '"+BetterFutures.getCurrentUser()+"', '"+symbol+"', TIMESTAMP '"+mutualDate+"', 'sell', "+numSharesValue+", "+priceValue+", "+amountValue+")";
			statement.executeQuery( query );

			System.out.println("You sold "+numSharesValue+" shares of '"+symbol+"' at a price of $"+priceValue+" per share. $"+ amountValue+" has been added to your balance.");

			//re-enables
			query = "ALTER TRIGGER deposit_trigger ENABLE";
			statement.executeQuery( query );
			connection.setAutoCommit(false);
			connection.commit();
		}
		catch ( SQLException e ) {
			System.out.println( "Error while updating OWNS. Machine error: " + e.toString() );
		}
		catch ( NumberFormatException e ) {
			System.out.println( "Parsing error: " + e.toString() );
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