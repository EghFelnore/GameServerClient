import java.io.*;
import java.net.*;

public class ServerApp{

	/** Requires one argument - the server port.  */
	public static void main(String... aArgs) throws IOException { // Starting point for running server via command line
		if (aArgs.length < 1) { // If TCP port is not supplied, use a default one
			int serverPort = 2004;
			ServerApp server = new ServerApp( serverPort ); // Instantiate object
			server.start(); // Start server
		} else { // If TCP port is supplied, use the supplied port
			int serverPort = Integer.parseInt(aArgs[0]);
			ServerApp server = new ServerApp( serverPort ); // Instantiate object
			server.start(); // Start server
		}
	}

	/** Constructor. */
	ServerApp(int aServerPort){ // Set server port with class is initialized
		fServerPort = aServerPort;
	}

	private synchronized void start() throws IOException{ // Server method
		if (threads != null) throw new IllegalStateException ("The receiver is already started.");
		providerSocket = new ServerSocket(fServerPort, 10); // Create server socket
		logInfo.append("Server started");
		while ( incrementalCount < maxClients ) { // Wait for client connections
			connection = providerSocket.accept();
			//logInfo("DEBUG: Socket connection accepted, incrementalCount is " + incrementalCount + "...");
			ServerThread connThread = new ServerThread(connection); // Send connection to a thread to allow multiple clients to connect
			Thread newThread = new Thread(connThread);
			incrementalCount = Thread.activeCount(); // Keep track of the number of active threads
			//logInfo("DEBUG: New thread created, number of threads is " + incrementalCount + "...");
			//logInfo("DEBUG: Starting new thread...");
			newThread.start();
			logInfo.append("Current clients connected is " + incrementalCount);
		}
	}

	// PRIVATE
	private static ServerLog logInfo = new ServerLog(); // Log file object
	private final int fServerPort; // TCP port to run server on
	private ServerSocket providerSocket; // Socket server
	private Socket connection = null; // Socket for client connections
	private int incrementalCount = 0; // Counter for number of client connections
	private final int maxClients = 10; // Max clients to allow to connect at once
	private Thread threads[]; // Thread array for clients

}