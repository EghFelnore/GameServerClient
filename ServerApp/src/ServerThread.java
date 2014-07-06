import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ServerThread implements Runnable {

	/** Constructor. */
	ServerThread(Socket aClientSocket){
		connection = aClientSocket;
	}

	public synchronized void run()
	{
		try{
			running = true;
			while (running){
				logInfo.append("Connection request received from " + quote(connection.getInetAddress().getHostName()));
				//3. get Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				//logInfo.append("Connection successful");
				sendMessage("Connection successful");
				boolean justConnected = true;
				//4. The two parts communicate via the input and output streams
				do{
					try{
						message = (String)in.readObject();
						// Scanner to parse the content of each line 
						Scanner scanner = new Scanner(message);
						scanner.useDelimiter(":");
						String clientName = scanner.next();
						currentClient = clientName;
						//connectionHash = this.connection.hashCode();
						connectionHash = Thread.currentThread().hashCode();
						String xStr = "";
						String yStr = "";
						String chatMessage = "";
						if ( scanner.hasNext() ) xStr = scanner.next();
						if ( scanner.hasNext() ) yStr = scanner.next();
						if ( scanner.hasNext() ) chatMessage = scanner.next();
						if ( justConnected ) {
							logInfo.append(currentClient + ": Connected, hashcode is: " + connectionHash);
						}
						//logInfo.append(currentClient + ": Receiving coordinates of " + quote(xStr) + "," + quote(yStr));
						// Send any chat message to chat log and get new messages back
						//logInfo.append(currentClient + ": Sending client info to ChatLogger");
						if ( chatMessage != null && !chatMessage.equals(" ") ) ChatLogger.queryChat(clientName, chatMessage);
						// Get results of the query
						//logInfo.append(currentClient + ": Sending client info to ClientTracker");
						message = ClientTracker.queryDB(clientName, connectionHash, xStr, yStr);
						nowTime = dateStamp();
						if ( message.equals("disconnect") ) {
							// Send already connected message and look for recent chat messages
							message = message + nowTime + ": CHAT\n" + nowTime + ": You're already connected, killing that connection.\n" + ChatLogger.readRecentChat();
						} else {
							// Look for recent chat messages
							message = message + nowTime + ": CHAT\n" + ChatLogger.readRecentChat();
						}
						//logInfo.append("Clients: " + message);
						//logInfo.append(currentClient + ": Sending query results to client");
						sendMessage(message);
						justConnected = false;
					}
					catch(ClassNotFoundException classNot){
						logInfo.append(currentClient + ": Data received in unknown format");
					}
				} while (running); 
			}
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			logInfo.append(currentClient + ": Client from " + quote(connection.getInetAddress().getHostName()) + " dropped");
			try {
				ClientTracker.loggedOff(currentClient);
				ChatLogger.queryChat(currentClient, "Logged off");
			} catch (IOException e) {
				logInfo.append("Unable to update database to log " + currentClient + " off");
			}
		}
	}

	private synchronized void sendMessage(String msg) // Return client list back to the client
	{
		try{
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	// PRIVATE
	private static ServerLog logInfo = new ServerLog(); // Log file object
	private Socket connection = null; // Socket passed to the thread for the client
	private volatile boolean running; // Used to keep thread running
	private ObjectOutputStream out; // Socket stream to send info back to client
	private ObjectInputStream in; // Socket stream to get info from client
	private String message; // String used to receive and send info to client
	private static String nowTime; // Used to time stamps in chat log
	private static int connectionHash;
	
	private String currentClient;

	//private ClientTracker1 tracker = new ClientTracker1();

	private String quote(String aText){
		String QUOTE = "'";
		return QUOTE + aText + QUOTE;
	}
	
	private static String dateStamp() {
		Date dNow = new Date( );
		SimpleDateFormat ft = 
				new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss:SSS");
		return ft.format(dNow);
	}
	
}