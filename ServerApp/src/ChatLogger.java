import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public final class ChatLogger {

	/** Update client DB 
	 * @return 
	 * @throws IOException */  
	protected final synchronized static void queryChat(String aClientName, String aMessage) throws IOException {
		fClientName = aClientName; // Client name
		if (aMessage == null) { // Client X coordinate
			aMessage = " ";
		} else {
			fClientReceivedMessage = aMessage;
		}
		updateChat();
		//return newChat; // Return updated client list back to client
	}

	/** Results from the client DB file. */
	private synchronized static void updateChat() throws IOException  { // Write updated data to the file
		nowTime = dateStamp();
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(fChatLog, true));
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			logInfo.append("server>" + nowTime + ": Can not access chat log file " + fChatLog);
		}
		try {
			out.write(nowTime + ": " + fClientName + ">" + " " + fClientReceivedMessage + "\n");
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			logInfo.append("server>" + nowTime + ": Can not write to chat log file " + fChatLog);
		}
		finally {
			try {
				out.close();
			}
			catch(IOException ioException){
				//ioException.printStackTrace();
				logInfo.append("server>" + nowTime + ": Can not close chat log file " + fChatLog);
			}
		}
	}

	protected final synchronized static String readRecentChat() throws IOException {
		nowTime = dateStamp();
		// Check to make sure chat file exists, skip reading if it does not
		File checkFile = new File(fChatLog);
		if ( checkFile.exists() ) {
			fClientReturnMessages = "";
			Scanner scanner = new Scanner(new FileReader(fChatLog)); // Read chat lines in from file
			try { // Increment through chat lines that were just read in
				//Get each line
				while ( scanner.hasNextLine() ){
					//Process each line
					String lineToCheck = scanner.nextLine();
					if ( myDateCompare.isChatRecent(lineToCheck, nowTime) ) { // Only keep line if it was written recently
						fClientReturnMessages = fClientReturnMessages + lineToCheck + "\n";
					}
				}
			}
			finally { // Close out scanner that was reading line by line
				scanner.close();
			}
		} else {
			logInfo.append("Chat log file does not exist yet.");
		}
		return fClientReturnMessages; // Return recent chats to client
	}
	/*
	private synchronized static boolean checkLine(String aLine){
		boolean lineMatches = false;
		// Use a second Scanner to parse the content of each line 
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter(":"); // Use a space for parsing each line
		if ( scanner.hasNext() ){
			String chatLine = scanner.next();
			if ( myDateCompare.isChatRecent(chatLine, nowTime) ) {
				lineMatches = true;
			} else {
				lineMatches = false;
			}
		}
		else {
			logInfo.append("Chat log line has empty or invalid line. Unable to process");
		}
		return lineMatches;
	}*/
	
	// PRIVATE
	private static ServerLog logInfo = new ServerLog(); // Log file object
	private final static String fChatLog = "Chat.log"; // Name of the chat log file
	private static String fClientName; // Used for clients name that sent chat message
	private static String fClientReceivedMessage; // Used for received client chat messages
	private static String fClientReturnMessages; // Used to send recent chat back to all clients
	//private static String newChat = " "; // Used to send client any new chat messages
	private static String nowTime; // Used to time stamps in chat log
	private static DateCompare myDateCompare = new DateCompare(); // Set of parsing methods that will be used

	private static String dateStamp() {
		Date dNow = new Date( );
		SimpleDateFormat ft = 
				new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss:SSS");
		return ft.format(dNow);
	}
	
}
