import java.io.*;
import java.util.Scanner;

public final class ClientTracker {

	/** Update client DB 
	 * @throws IOException */  
	protected final synchronized static String queryDB(String aClientName, int aConnectionHash, String aClientX, String aClientY) throws IOException {
		fClientName = aClientName; // Client name
		fConnectionHash = aConnectionHash;  // Client network connection
		//logInfo.append(fClientName + ": Running queryDB method now");
		if (aClientX == null) { // Client X coordinate
			fClientX = "";
		} else {
			fClientX = aClientX;
		}
		if (aClientY == null) { // Client Y coordinate
			fClientY = "";
		} else {
			fClientY = aClientY;
		}
		newDB = ""; // Clear client data string before reading from database file
		clientsLoggedIn = ""; // Used to return clients logged in
		newClient = true; // Reset client to new unless otherwise set
		//logInfo.append(fClientName + ": Getting ready to read client DB inside of queryDB method");
		// Check to make sure client database file exists, skip reading if it does not
		File checkFile = new File(fDBName);
		if ( checkFile.exists() ) {
			Scanner scanner = new Scanner(new FileReader(fDBName)); // Read client data in from file
			try { // Increment through client data that was just read in
				//Get each line
				while ( scanner.hasNextLine() ){
					//Process each line
					queryLine( scanner.nextLine() ); // Send line that was just read to queryLine
				}
				if ( newClient ) { // If client didn't exist already and coordinates supplied, add to string
					if ( fClientX.equals("") || fClientY.equals("")) {
						//logInfo.append(fClientName + ": Queried client " + quote(fClientName) + " doesn't exist");
					} else { // New client with coordinates, add to list of clients
						//logInfo.append(fClientName + ": Name is " + quote(fClientName) + ", and coords are " + quote(fClientX) + "," + quote(fClientY) );
						newDB = newDB.concat(fClientName + ":" + fConnectionHash + ":" + fClientX + ":" + fClientY + ":true\n");
						clientsLoggedIn = clientsLoggedIn.concat(fClientName + ":" + fClientX + ":" + fClientY + "\n");
					}
				}
			}
			finally { // Close out scanner that was reading line by line
				scanner.close();
			}
		} else {
			logInfo.append("Client data file does not exist, recreating.");
			checkFile.createNewFile();
		}
		updateDB();
		if ( !disconnectClient ) {
			return clientsLoggedIn; // Return updated client list back to client
		} else {
			disconnectClient = false;
			return "disconnect"; // Return disconnect client message
		}
			
	}

	private synchronized static void queryLine(String aLine) {
		// Use a second Scanner to parse the content of each line 
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter(":"); // Use a colon for parsing each line
		if ( scanner.hasNext() ){
			String name = scanner.next();
			int conn = Integer.parseInt(scanner.next());
			String x = scanner.next();
			String y = scanner.next();
			String loggedIn = scanner.next();
			if ( name.trim().toLowerCase().equals(fClientName.toLowerCase()) ) { // Check to see if this line is the client that is sending new coordinates
				//logInfo.append("New conn is " + fConnectionHash + ", conn in current line I'm checking is " + conn);
				
				/*****
				 * 
				 * the following check doesn't seem to be working
				 * 
				 */

				if ( loggedIn.toLowerCase().equals("true") && fConnectionHash != conn ) { // Client with the same connected from another client
					disconnectClient = true;
				}
				
				if ( fClientX.equals("") || fClientY.equals("") ) { // If no new coordinates sent, leave them the same
					newDB = newDB.concat(name.trim() + ":" + conn + ":" + x.trim() +  ":" + y.trim() + ":true\n");
					clientsLoggedIn = clientsLoggedIn.concat(name.trim() + ":" + x.trim() +  ":" + y.trim() + "\n");
				} else { // If new coordinates sent, update them in the file
					newDB = newDB.concat(name.trim() + ":" + fConnectionHash + ":" + fClientX + ":" + fClientY + ":true\n");
					clientsLoggedIn = clientsLoggedIn.concat(name.trim() + ":" + fClientX + ":" + fClientY + "\n");
				}
				newClient = false; // No longer a new client since we've added it
			} else { // Not the querying client, keep the supplied info
				newDB = newDB.concat(name.trim() + ":" + conn + ":" + x.trim() +  ":" + y.trim() + ":" + loggedIn.trim() + "\n");
				if ( loggedIn.trim().equals("true") ) clientsLoggedIn = clientsLoggedIn.concat(name.trim() + ":" + x.trim() +  ":" + y.trim() + "\n");
			}
		}
		else {
			logInfo.append(fClientName + ": Empty or invalid line. Unable to process");
		}
	}
	
	protected final synchronized static void loggedOff(String aClientName) throws IOException {
		fClientName = aClientName; // Client name
		newDB = ""; // Clear client data string before reading from database file
		// Check to make sure client database file exists, skip reading if it does not
		File checkFile = new File(fDBName);
		if ( checkFile.exists() ) {
			Scanner scanner = new Scanner(new FileReader(fDBName)); // Read client data in from file
			try { // Increment through client data that was just read in
				//Get each line
				while ( scanner.hasNextLine() ){
					//Process each line
					loggedOffLine( scanner.nextLine() ); // Send line that was just read to queryLine
				}
			}
			finally { // Close out scanner that was reading line by line
				scanner.close();
			}
		} else {
			logInfo.append("Client data file does not exist, recreating.");
			checkFile.createNewFile();
		}
		updateDB();
	}
	
	private synchronized static void loggedOffLine(String aLine) {
		// Use a second Scanner to parse the content of each line 
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter(":"); // Use a colon for parsing each line
		if ( scanner.hasNext() ){
			String name = scanner.next();
			int conn = Integer.parseInt(scanner.next());
			String x = scanner.next();
			String y = scanner.next();
			String loggedIn = scanner.next();
			if ( name.trim().toLowerCase().equals(fClientName.toLowerCase()) ) { // Check to see if this line is the client that is logging off
				newDB = newDB.concat(name.trim() + ":" + conn + ":" + x.trim() +  ":" + y.trim() + ":false\n");
			} else { // Not the logging client, keep the supplied info
				newDB = newDB.concat(name.trim() + ":" + conn + ":" + x.trim() +  ":" + y.trim() + ":" + loggedIn.trim() + "\n");
			}
		}
		else {
			logInfo.append(fClientName + ": Empty or invalid line. Unable to process");
		}
	}

	/** Results from the client DB file. */
	private synchronized static void updateDB() throws IOException  { // Write updated data to the file
		// Writing new client data out to database
		Writer out = new OutputStreamWriter(new FileOutputStream(fDBName));
		try {
			out.write(newDB); // Write to file
		}
		finally {
			out.close(); // Close out connection to the file
		}
	}

	// PRIVATE
	private static ServerLog logInfo = new ServerLog(); // Log file object
	private final static String fDBName = "Client.DB"; // Name of the client database
	private static String fClientName; // Used for clients as they attach
	private static int fConnectionHash; // Used to identify network connection for this client
	private static String fClientX; // Used for client X coordinates
	private static String fClientY; // Used for client Y coordinates
	private static String newDB = ""; // Used to read from and update client database
	private static String clientsLoggedIn = ""; // Used to return clients logged in
	private static boolean newClient = true; // Used when a new or existing client attaches
	private static boolean disconnectClient = false; // Used when a client needs to be disconnected

}