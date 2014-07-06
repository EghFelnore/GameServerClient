import java.io.*;
import java.net.*;
public class ClientCLI{
	
    /** Requires two arguments - the server name, and the server port to use.  */
	public static void main(String... aArgs) throws IOException {
		 if (aArgs.length < 2) {
			logInfo("Syntax is:\njava Client [MyName] [Server]\n -OR-\njava Client [MyName] [Server] [Port]");
		} else {
			String clientName = aArgs[0];
			String serverName = aArgs[1];
			if (aArgs.length == 2) {
				ClientCLI client = new ClientCLI( clientName, serverName );
				client.run();
			} else {
				int serverPort = Integer.parseInt(aArgs[2]);
				ClientCLI client = new ClientCLI( clientName, serverName, serverPort );
				client.run();
			}
		}
	}
	
	/** Constructors. */
	ClientCLI(String aClientName, String aServerName, int aServerPort){
		fClientName = aClientName;
		fServerName = aServerName;
		fServerPort = aServerPort;
	}
	
	ClientCLI(String aClientName, String aServerName){
		fClientName = aClientName;
		fServerName = aServerName;
		fServerPort = 2004;
	}
	
	void run()
	{
		try{
			//1. creating a socket to connect to the server
			requestSocket = new Socket(fServerName, fServerPort);
			logInfo("Connected to server " + quote(fServerName) + " using port " + quote(fServerPort) + ".");
			//2. get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			//logInfo("DEBUG: Out stream object created...");
			out.flush();
			//logInfo("DEBUG: Out stream flushed...");
			in = new ObjectInputStream(requestSocket.getInputStream());
			//logInfo("DEBUG: In stream object created...");
			// Setup for keyboard input
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader typingIn = new BufferedReader(converter);
			//3: Communicating with the server
			do{
				try{
					// Get connection message from server
					message = (String)in.readObject();
					logInfo("Server> " + message);
					// Send client info to server
					sendMessage(fClientName +":");
					// Receive message back from server
					message = (String)in.readObject();
					logInfo("Server> " + message);

					System.out.print("Client> ");

					// Start loop to read keyboard input to send to server
					while (!(keyboardInput.equals("bye"))){
						keyboardInput = typingIn.readLine();
						if (!(keyboardInput.equals("bye"))){
							// Send message to server
							sendMessage(fClientName +":" + keyboardInput);
							// Receive message back from server
							message = (String)in.readObject();
							logInfo("Server> " + message);
							System.out.print("Client> ");
						}
					}
					// Set message to bye to stop this
					message = "bye";
				}
				catch(ClassNotFoundException classNot){
					logError("data received in unknown format");
				}
			}while(!message.equals("bye"));
		}
		catch(ConnectException refused){
			logError("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			logError(fServerPort + " dropped connection.");
		}
		finally{
			//4: Closing connection
			try{
				logInfo("Client> Closing connection to server...");
				in.close();
				out.close();
				requestSocket.close();
				logInfo("Client> Closed.");
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			logInfo("Client> " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	// PRIVATE
	private final String fClientName;
	private final String fServerName;
	private final int fServerPort;
	
	private Socket requestSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	private String keyboardInput = "";
	
	private static void logInfo(String aMessage){
		System.out.println(aMessage);
	}
	
	private static void logError(String aMessage){
		System.err.println(aMessage);
	}
	
	private String quote(String aText){
		String QUOTE = "'";
		return QUOTE + aText + QUOTE;
	}
	
	private String quote(int aInt){
		String QUOTE = "'";
		return QUOTE + aInt + QUOTE;
	}
	
}