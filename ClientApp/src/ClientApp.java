import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.imageio.ImageIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ClientApp extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Requires two arguments - the server name, and the server port to use.  */
	public static void main(String... aArgs) throws IOException {
		if (aArgs.length == 1) {
			fServerName = aArgs[0];
		} 
		if (aArgs.length == 2) {
			fServerName = aArgs[0];
			fServerPort = Integer.parseInt(aArgs[1]);
		}
		if (aArgs.length == 3) {
			fServerName = aArgs[0];
			fServerPort = Integer.parseInt(aArgs[1]);
			fClientName = aArgs[2];
		}
		ClientApp client = new ClientApp(); // Create ClientWindow object
		start(client);
	}
	
	private ClientApp() {
    	
    	File imagePath = new File("sprite_walking.jpg");

    	// Read sprite image in
    	try {
			myToonPane = ImageIO.read(imagePath);
		} catch (IOException e) {
			System.out.println("Can not find image file " + imagePath);
			myToonPane = errorImage();
		}

    	myToonSize = new Rectangle(myToonPane.getWidth(null)/4,myToonPane.getHeight(null)/2);
    	
    	// Build frames for myToon
    	int currentClipX = 0;
    	int currentClipY = 0;
    	for ( int i = 0; i < myToonFrames; i++ ) {
    		myToon[i] = myToonPane.getSubimage(currentClipX, currentClipY, myToonSize.width, myToonSize.height);
    		myToon[i] = makeColorTrans(myToon[i]);
    		currentClipX += myToonSize.width;
    		if ( i == 3 ) {
    			currentClipX = 0;
    			currentClipY = myToonSize.height;
    		}
    	}
    	
    	// Build frames for otherToons
    	currentClipX = 0;
    	currentClipY = 0;
    	for ( int i = 0; i < myToonFrames; i++ ) {
    		otherToon[i] = myToonPane.getSubimage(currentClipX, currentClipY, myToonSize.width, myToonSize.height);
    		otherToon[i] = makeColorTrans(otherToon[i]);
    		currentClipX += myToonSize.width;
    		if ( i == 3 ) {
    			currentClipX = 0;
    			currentClipY = myToonSize.height;
    		}
    	}
    	
    	// Set start values for boolean arrays being used
    	for ( int i = 0; i < maxToons; i++ ) {
    		otherMoveUp[i] = false;
    	    otherMoveLeft[i] = false;
    	    otherMoveRight[i] = false;
    	    otherMoveDown[i] = false;
    	    otherFaceLeft[i] = false;
    	}
    	
    	// Set size of game window
        windowSize.width = windowWidth;
        windowSize.height = windowHeight;
        setPreferredSize(windowSize);
        
        // Setup higher quality graphics
        renderHints.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    }
	
    private static void start(ClientApp client) {
    	
    	if ( fClientName.equals("") ) client.loginDialog();
    	
    	JFrame frame = new JFrame(fClientName); // Create new window
    	frame.setResizable(false); // Makes window so user can't change it's size
        frame.add(inputField, BorderLayout.SOUTH); // Add text entry area at the bottom
        frame.add(gameArea, BorderLayout.NORTH); // Add main game area
        gameArea.add(client); // Add game object to new window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set window to close when exiting
        
        frame.setBackground(Color.white); // Set window starting background color
        frame.pack(); // Spreads contents to fill window appropriately
        frame.setLocationRelativeTo(null); // Center window on the computer screen
        inputField.addKeyListener(client); // Wait for keyboard entries in text field
        gameArea.addKeyListener(client); // Wait for keyboard in main game area
        gameArea.addMouseListener(client); // Wait for mouse actions in main game area
        gameArea.addMouseMotionListener(client); // Wait for mouse movement in main game area
        gameArea.setFocusable(true); // Set JPanel and focusable (can tab to it)
        gameArea.requestFocusInWindow(); // Set focus to game area at start
        chatArrayUpdate(""); // Populate array for chat area
        frame.setVisible(true); // Make new window visible on computer screen
        client.runGame(); // Run animation method
    }
	
    private void loginDialog() {
        
        boolean validName = false;

        String loginMessage = "Enter your character name:";

        while ( !validName ) {

        	// Used for entering character name
        	fClientName = JOptionPane.showInputDialog(null,
        			loginMessage,
        			"Login",
        			JOptionPane.QUESTION_MESSAGE);

        	fClientName = fClientName.trim();

        	if ( !fClientName.equals("") && fClientName.length() > 3 ) {
        		validName = true; // Set to true and then mark as not true in next loop if needed
        		for ( int i = 0; i != fClientName.length(); i++ ) { // Check to make sure numbers or special characters are not used
        			if ( fClientName.toLowerCase().charAt(i) < 'a' || fClientName.toLowerCase().charAt(i) > 'z' ) {
        				validName = false; // Found non-alphabetical character
        				break; // If a non-alphabetical letter is found, break out of for loop
        			}
        		}
        	}
            
            loginMessage = "Name not valid, try again";
            
        }
    }
    
	private void runGame() {

		// Setup for animation
		boolean keepRunning = true;
		long graphicsStartTime = System.nanoTime();
		long graphicsElapsedTime = System.nanoTime() - graphicsStartTime;

		// Setup for network
		long networkStartTime = System.nanoTime();
		long networkElapsedTime = System.nanoTime() - networkStartTime;

		// Establish network connection to the server
		try{
			requestSocket = new Socket(fServerName, fServerPort); // Create socket to server
			// Record time that you attached to server
			nowTime = dateStamp();
			logWindow(nowTime + ": Connected to server " + quote(fServerName) + " using port " + quote(fServerPort));
			out = new ObjectOutputStream(requestSocket.getOutputStream()); // Create an outgoing stream
			//logInfo("DEBUG: Out stream object created...");
			out.flush();
			//logInfo("DEBUG: Out stream flushed...");
			in = new ObjectInputStream(requestSocket.getInputStream()); // Create an incoming stream
			//logInfo("DEBUG: In stream object created...");
			do{ // Start receiving and sending to server
				try{
					// Get connection message from server
					message = (String)in.readObject();
					//logInfo("Server> " + message);
					
					// Send client info to server
					sendMessage(fClientName +":");
					
					// Receive message back from server
					message = (String)in.readObject();
					//logInfo("Server> " + message);
					
					// Get my coordinates as received from server
					parseCoordsStart(message);

					//System.out.print("Client> ");
					
					// Start loop to read keyboard input to send to server
					while (keepRunning) {

						// Check for elapsed time since last running loop
						graphicsElapsedTime = System.nanoTime() - graphicsStartTime; // Check how long its been since the last frame was displayed
						networkElapsedTime = System.nanoTime() - networkStartTime; // Check how long its been since the last frame was displayed
						
						if (graphicsElapsedTime>graphicsNanoSecDelay) { // Check to see if its time to animate yet
							
							// Draw my sprite based on key input
							if (myMoveDown || myMoveRight) { myCurFrame++; myFaceLeft = false; } // Move to next sprite frame if moved
							if (myMoveLeft || myMoveUp) { myCurFrame++; myFaceLeft = true; } // Move to previous sprite frame if moved
							if (myMoveDown && myMoveLeft) { myCurFrame--; myFaceLeft = true; } // Move to previous sprite frame if moved
							if (myMoveRight && myMoveUp) { myCurFrame--; myFaceLeft = false; } // Move to next sprite frame if moved
							if (myCurFrame>(myToonFrames-1)) myCurFrame=0; // Moving forward, start at the first frame again
							if (myCurFrame<0) myCurFrame=myToonFrames-1; // Moving backward, start at the last frame again
							if (myMoveUp) myYPos = myYPos - moveSpeed; // Move sprite up
							if (myMoveLeft) myXPos = myXPos - moveSpeed; // Move sprite to the left
							if (myMoveRight) myXPos = myXPos + moveSpeed; // Move sprite to the right
							if (myMoveDown) myYPos = myYPos + moveSpeed; // Move sprite down
							if (myXPos < 0) myXPos = 0; // Stop sprite from moving off of the left side of the window
							if (myYPos < 0) myYPos = 0; // Stop sprite from moving off of the top of the window
							if (myXPos > windowSize.width-myToonSize.width) myXPos = windowSize.width-myToonSize.width; // Stop sprite from moving off of the right side of the window
							if (myYPos > windowSize.height-myToonSize.height-chatHeight-chatBorder) myYPos = windowSize.height-myToonSize.height-chatHeight-chatBorder; // Stop sprite from moving off of the bottom of the window
							myCurToon = myToon[myCurFrame];
							
							// Draw other sprites
							for ( int i = 0; i < otherToons; i++ ) {
								// Draw my sprite based on key input
								if (otherMoveDown[i] || otherMoveRight[i]) { otherCurFrame[i]++; otherFaceLeft[i] = false; } // Move to next sprite frame if moved
								if (otherMoveLeft[i] || otherMoveUp[i]) { otherCurFrame[i]++; otherFaceLeft[i] = true; } // Move to previous sprite frame if moved
								if (otherMoveDown[i] && otherMoveLeft[i]) { otherCurFrame[i]--; otherFaceLeft[i] = true; } // Move to previous sprite frame if moved
								if (otherMoveRight[i] && otherMoveUp[i]) { otherCurFrame[i]--; otherFaceLeft[i] = false; } // Move to next sprite frame if moved
								if (otherCurFrame[i]>(myToonFrames-1)) otherCurFrame[i]=0; // Moving forward, start at the first frame again
								if (otherCurFrame[i]<0) otherCurFrame[i]=myToonFrames-1; // Moving backward, start at the last frame again
								otherCurToon[i] = otherToon[otherCurFrame[i]]; // For now, set others to the same frame as my sprite
							}
							
							// Reset timer for next round of animations
							graphicsStartTime = System.nanoTime(); // Set a start point to time when to increment to the next frame
							
							// Paint window with new animation frames
							repaint();  // Cause paint to run again
						}

						if (networkElapsedTime>networkNanoSecDelay) { // Check to see if its time to send new location to server
							// Send message to server to send location
							sendMessage(fClientName +":" + myXPos + ":" + myYPos + ":" + chatMessage);
							chatMessage = " ";
							// Receive message back from server to get other player locations
							message = (String)in.readObject();
							//logInfo("Server> " + message);
							// Reset timer for next round of server communication
							networkStartTime = System.nanoTime(); // Set a start point to time when to increment to the next frame
							// Get my coordinates as received from server
							parseCoords(message);
						}
						
					}
					// Set message to bye to stop this
					message = "bye";
				}
				catch(ClassNotFoundException classNot){
					//logError("data received in unknown format");
				}
			}while(!message.equals("bye"));
		}
		catch(ConnectException refused){
			//logError("You are trying to connect to an unknown host!");
			serverNotAvailable = true;
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			//logError(fServerPort + " dropped connection.");
		}
		finally{
			//4: Closing connection
			try{
				logWindow("Client> Closing connection to server...");
				in.close();
				out.close();
				requestSocket.close();
				logWindow("Client> Closed.");
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
    
    private void parseCoordsStart(String allClients){ // Get starting location coordinates from server data received
    	Scanner scanner = new Scanner(allClients);
    	otherToons = 0;
    	boolean chatMessageLines = false;
		try { // Get each line
			while ( scanner.hasNextLine() ){ // Process each line
				String messageLine = scanner.nextLine();
    			if ( messageLine.endsWith("CHAT") ) {
    				serverTime = messageLine;
    				chatMessageLines = true; // Found a line ending with CHAT, stop reading coordinates
    				if ( scanner.hasNextLine() ) {
    					messageLine = scanner.nextLine(); // Grab last chat line if there are any
    				} else {
    					messageLine = ""; // Set chat line ot blank since there aren't any
    				}
    			}
    			if ( !chatMessageLines ) { // Haven't found chat lines yet, so keep reading coordinates
    				parseClientLineStart(messageLine);
    			} else {
    				lastChatLine = messageLine;
    			}
			}
		}
		finally {
			scanner.close();
		}
		// Don't need the following lines here, will use in parseCoords method to compare new lines to old
    	logWindow(lastChatLine); // Temp till I figure this out
    }
	
    private void parseClientLineStart(String aClient){ // Getting my starting location from server
    	Scanner scanner = new Scanner(aClient);
    	scanner.useDelimiter(":");
    	if ( scanner.hasNext() ){
    		String name = scanner.next();
    		String x = scanner.next();
    		String y = scanner.next();
    		if ( name.trim().toLowerCase().equals(fClientName.toLowerCase()) ) { // Is my name
       			if ( !x.equals("") ) {
    				myXPos = Integer.parseInt(x.trim());
    			} else myXPos = resetXPos; // Got no X value for me, I must be new
    			if ( !y.equals("") ) {
    				myYPos = Integer.parseInt(y.trim());
    			} else myYPos = resetYPos; // Got no Y value for me, I must be new
    		} else { // Not my name
    			otherLabel[otherToons] = name.trim();
    			if ( !x.equals("") ) {
    				otherXPos[otherToons] = Integer.parseInt(x.trim()); // Set new X value for other toon
    			}
    			if ( !y.equals("") ) {
    				otherYPos[otherToons] = Integer.parseInt(y.trim()); // Set new Y value for other toon
    			}
    			// Remember previous coords
    			otherXPosPrev[otherToons] = otherXPos[otherToons];
    			otherYPosPrev[otherToons] = otherYPos[otherToons];
    			otherToons ++;
    		}
    	}
    }
    
	// Method for making parts of the sprite transparent
	private static BufferedImage makeColorTrans(BufferedImage imageToClear) {
		BufferedImage newImage = new BufferedImage(imageToClear.getWidth(), imageToClear.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Color transColor=Color.white;
		Graphics2D g = newImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(imageToClear, null, 0, 0);
		g.dispose();
		for(int y = 0; y < newImage.getHeight(); y++) {
			for(int x = 0; x < newImage.getWidth(); x++) {
				int testColor=newImage.getRGB(x, y);
				//System.out.println("Checking " + x + ", " + y + ", currently its " + testColor);
				if(testColor == transColor.getRGB()) {
					//System.out.println("Got a hit!");
					newImage.setRGB(x, y, 0x00000000);
				}
			}
		}
		return newImage;
	}

	// Create error image if toon pane file can not be found
	private static BufferedImage errorImage(){
		BufferedImage blankImage = new BufferedImage(500, 250, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = blankImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setColor(Color.red);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 44));
		String missingMessage = "Can not find image!";
		int messageLength = (int) g.getFontMetrics().getStringBounds(missingMessage, g).getWidth();
		g.drawString(missingMessage, 500/2-messageLength/2, 250/4);
		g.drawString(missingMessage, 500/2-messageLength/2, 250/4*3);
		g.dispose();
		return blankImage;
	}
	
	// Method to horizontally flip an image
	private static BufferedImage imageFlip(BufferedImage imageToFlip) {
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-imageToFlip.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, 
                                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage newImage = op.filter(imageToFlip, null);
		return newImage;
	}
	
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setRenderingHints(renderHints); // Set graphics to higher quality
			
		myLabel = fClientName;
		myLabelLength = (int) g2d.getFontMetrics().getStringBounds(myLabel, g2d).getWidth(); // Get width of the text label
		
		g2d.clearRect(0, 0, windowSize.width, windowSize.height); // Clean background to avoid artifacts
		
		g2d.drawRect(chatBorder, chatBorder, windowSize.width-chatBorder*2, titleHeight); // Create title area
		g2d.drawRect(chatBorder, windowSize.height-chatHeight-chatBorder, windowSize.width-chatBorder*2, chatHeight); // Create chat area
		
		if (serverNotAvailable){ // If server is not available, put warning message on screen
			g2d.setFont(errorFont);
			g2d.setColor(Color.red);
			String noServerMessage = "Can not find server!";
			int messageLength = (int) g2d.getFontMetrics().getStringBounds(noServerMessage, g2d).getWidth();
			g2d.drawString(noServerMessage, windowSize.width/2-messageLength/2, windowSize.height/2);
		} else { // Connected to server, go ahead and run game
			
			titleAreaDraw(g2d); // Draw title area
			chatAreaDraw(g2d); // Draw chat area
			chatAreaButtons(g2d); // Draw chat buttons
			
			g2d.setFont(labelFont); // Set the font to use
			g2d.setColor(Color.BLUE); // Set font color
			if (!myFaceLeft){
				g2d.drawImage(myCurToon, myXPos, myYPos, null); // Draw sprite at new location
			} else {
				g2d.drawImage(imageFlip(myCurToon), myXPos, myYPos, null); // Draw left facing sprite at new location
			}
			g2d.drawString(myLabel, myXPos+myToonSize.width/2-myLabelLength/2, myYPos); // Add a text label centered over the toon
			// Draw other sprites
			g2d.setColor(Color.gray); // Set font color
			for ( int i = 0; i < otherToons; i++ ) {
				otherLabelLength = (int) g2d.getFontMetrics().getStringBounds(otherLabel[i], g2d).getWidth(); // Get width of the text label
				if (!otherFaceLeft[i]) {
					g2d.drawImage(otherCurToon[i], otherXPos[i], otherYPos[i], null); // Draw sprite
				} else {
					g2d.drawImage(imageFlip(otherCurToon[i]), otherXPos[i], otherYPos[i], null); // Draw left facing sprite
				}
				g2d.drawString(otherLabel[i], otherXPos[i]+myToonSize.width/2-otherLabelLength/2, otherYPos[i]); // Draw label
			}
		}
	}
	
	public void actionPerformed(ActionEvent buttonClicked) {
		// Nothing to do right now (no JButtons being used)
	}
	
	public void mouseMoved(MouseEvent mouseMove) {
		mouseLocX = mouseMove.getX();
		mouseLocY = mouseMove.getY();
	}
	
	public void mouseDragged(MouseEvent mouseMove) {
		// Nothing to do right now
	}
	
	public void mouseClicked(MouseEvent mouseButton){
		
		// If clicking inside of show time button, toggle time stamps in chat area
		if ( mouseLocX > chatAreaButtonX && mouseLocX < chatAreaButtonX+buttonSize.width ) {
			if ( mouseLocY > chatAreaButtonY && mouseLocY < chatAreaButtonY+buttonSize.height ) {
				if ( showChatDates ) {
					showChatDates = false;
				} else {
					showChatDates = true; }
			}
		}
		
		// Checks for mouse over of the up button, increment the last chat line to be displayed
    	if ( mouseLocX > chatAreaButtonX && mouseLocX < chatAreaButtonX+buttonSize.width ) {
			if ( mouseLocY > windowSize.height-chatBorder-chatHeight && mouseLocY < windowSize.height-chatBorder-chatHeight+buttonSize.height ) {
				if ( lastDisplayLine < chatLines-1 ) lastDisplayLine++;
			}
		}
    	
    	// Checks for mouse over of the down button, decrement the last chat line to be displayed
    	if ( mouseLocX > chatAreaButtonX && mouseLocX < chatAreaButtonX+buttonSize.width ) {
			if ( mouseLocY > windowSize.height-chatBorder-buttonSize.height && mouseLocY < windowSize.height-chatBorder ) {
				if ( lastDisplayLine > 0 ) lastDisplayLine--;
			}
		}
		
	}

	public void mouseEntered(MouseEvent mouseMove) {
		// Nothing to do right now
	}

	public void mouseExited(MouseEvent mouseMove) {
		// Nothing to do right now
	}

	public void mouseReleased(MouseEvent mouseButton) {
		// Nothing to do right now
	}

	public void mousePressed(MouseEvent mouseButton){
		// Nothing to do right now
	}
	
	public void keyPressed(KeyEvent keyPressed) {
		//System.out.println("You just pressed: " + keyPressed);
		if (!isChatting()) {
			if (keyPressed.getKeyCode() == KeyEvent.VK_W || keyPressed.getKeyCode() == KeyEvent.VK_UP) myMoveUp = true;
			if (keyPressed.getKeyCode() == KeyEvent.VK_A || keyPressed.getKeyCode() == KeyEvent.VK_LEFT) myMoveLeft = true;
			if (keyPressed.getKeyCode() == KeyEvent.VK_S || keyPressed.getKeyCode() == KeyEvent.VK_DOWN) myMoveDown = true;
			if (keyPressed.getKeyCode() == KeyEvent.VK_D || keyPressed.getKeyCode() == KeyEvent.VK_RIGHT) myMoveRight = true;
			if (keyPressed.getKeyCode() == KeyEvent.VK_ENTER) inputField.requestFocusInWindow(); // If you hit enter, you go into text field
		} else {
			if (keyPressed.getKeyCode() == KeyEvent.VK_ENTER) { // Once you hit enter in text file, it shows-up in the text area
				String youTyped = inputField.getText();
				inputField.setText("");
				if (youTyped.equals("")) { // If you hit enter without typing anything, return to game area
					gameArea.requestFocusInWindow();
				} else {
					//logWindow(fClientName + "> " + youTyped);
					chatMessage = youTyped;
				}
			}
			if (keyPressed.getKeyCode() == KeyEvent.VK_ESCAPE) gameArea.requestFocusInWindow(); // If you hit escape, go back to game window
		}
	}

	public void keyReleased(KeyEvent keyReleased) {
		//System.out.println("You just pressed: " + keyReleased);
		if (!isChatting()) {
			if (keyReleased.getKeyCode() == KeyEvent.VK_W || keyReleased.getKeyCode() == KeyEvent.VK_UP) myMoveUp = false;
			if (keyReleased.getKeyCode() == KeyEvent.VK_A || keyReleased.getKeyCode() == KeyEvent.VK_LEFT) myMoveLeft = false;
			if (keyReleased.getKeyCode() == KeyEvent.VK_S || keyReleased.getKeyCode() == KeyEvent.VK_DOWN) myMoveDown = false;
			if (keyReleased.getKeyCode() == KeyEvent.VK_D || keyReleased.getKeyCode() == KeyEvent.VK_RIGHT) myMoveRight = false;
		}
	}

	public void keyTyped(KeyEvent keyTyped) {
		//System.out.println("You just typed: " + keyTyped);
		if (!isChatting()) {
			if (keyTyped.getKeyCode() == KeyEvent.VK_W || keyTyped.getKeyCode() == KeyEvent.VK_UP) myMoveUp = true;
			if (keyTyped.getKeyCode() == KeyEvent.VK_A || keyTyped.getKeyCode() == KeyEvent.VK_LEFT) myMoveLeft = true;
			if (keyTyped.getKeyCode() == KeyEvent.VK_S || keyTyped.getKeyCode() == KeyEvent.VK_DOWN) myMoveDown = true;
			if (keyTyped.getKeyCode() == KeyEvent.VK_D || keyTyped.getKeyCode() == KeyEvent.VK_RIGHT) myMoveRight = true;
		}
	}
    
    private void parseCoords(String allClients){ // Get updated location coordinates from server data received
    	Scanner scanner = new Scanner(allClients);
    	otherToons = 0;
    	boolean chatMessageLines = false;
    	try { // Get each line
    		while ( scanner.hasNextLine() ){ // Process each line
    			String messageLine = scanner.nextLine();
    			if ( messageLine.endsWith("CHAT") ) {
    				serverTime = messageLine;
    				chatMessageLines = true; // Found a line ending with CHAT, stop reading coordinates
    				if ( scanner.hasNextLine() ) {
    					messageLine = scanner.nextLine(); // Grab last chat line if there are any
    				} else {
    					messageLine = ""; // Set chat line ot blank since there aren't any
    				}
    			}
    			if ( !chatMessageLines ) { // Haven't found chat lines yet, so keep reading coordinates
    				parseClientLine(messageLine);
    			} else {
    				parseNewChatLines(messageLine);
    			}
    		}
    	}
    	finally {
    		scanner.close();
    	}
    }
    
    private void parseClientLine(String aClient){
    	Scanner scanner = new Scanner(aClient);
    	scanner.useDelimiter(":");
    	if ( scanner.hasNext() ){
    		String name = scanner.next();
    		String x = scanner.next();
    		String y = scanner.next();
    		if ( name.trim().toLowerCase().equals(fClientName.toLowerCase()) ) { // Is my name
    			if ( !x.equals("") ) {
    				myXPos = Integer.parseInt(x.trim());
    			} else myXPos = resetXPos;
    			if ( !y.equals("") ) {
    				myYPos = Integer.parseInt(y.trim());
    			} else myYPos = resetYPos;
    		} else { // Not my name
    			otherLabel[otherToons] = name.trim();
    			if ( !x.equals("") ) {
    				otherXPos[otherToons] = Integer.parseInt(x.trim()); // Set new X value for other toon
    			}
    			if ( !y.equals("") ) {
    				otherYPos[otherToons] = Integer.parseInt(y.trim()); // Set new Y value for other toon
    			}
    			// Reset set move checks for this other toon
    			otherMoveUp[otherToons] = false;
    			otherMoveLeft[otherToons] = false;
    			otherMoveDown[otherToons] = false;
    			otherMoveRight[otherToons] = false;
    			// Check to see if other toon has moved
    			if (otherYPos[otherToons] < otherYPosPrev[otherToons]) otherMoveUp[otherToons] = true;
    			if (otherXPos[otherToons] < otherXPosPrev[otherToons]) otherMoveLeft[otherToons] = true;
    			if (otherYPos[otherToons] > otherYPosPrev[otherToons]) otherMoveDown[otherToons] = true;
    			if (otherXPos[otherToons] > otherXPosPrev[otherToons]) otherMoveRight[otherToons] = true;
    			otherXPosPrev[otherToons] = otherXPos[otherToons];
    			otherYPosPrev[otherToons] = otherYPos[otherToons];
    			otherToons ++;
    		}
    	}
    }
    
    private void parseNewChatLines(String chatLinesFromServer){ // Read chat lines to find new ones
    	String chatNewLine = "";
    	boolean foundNewerChatLine = false;
    	Scanner scanner = new Scanner(chatLinesFromServer);
    	try { // Get each line
    		while ( scanner.hasNextLine() ){ // Process each line
    			chatNewLine = scanner.nextLine();
    			//System.out.println("Going to check the following: " + lastChatLine + "\nTo see if it is older than the following: " + chatNewLine);
    			if ( myDateParse.isChatNewer(lastChatLine, chatNewLine) ) {
    				foundNewerChatLine = true;
    				logWindow(chatNewLine);
    			}
    		}
    	}
    	finally {
    		scanner.close();
    	}
    	if ( foundNewerChatLine ) lastChatLine = chatNewLine;
    }
    
	private void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			//logInfo("Client> " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
    
    //PRIVATE
	RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); // Used for higher quality graphics
	private Dimension windowSize = new Dimension(); // Will be used to hold the size of the game window
	private int windowWidth = 800; // Set game window width
	private int windowHeight = 600; // Set game window height
	private static JPanel gameArea = new JPanel();
	private static JTextField inputField = new JTextField();
	private Dimension buttonSize = new Dimension(30, 30); // Will be used to hold the size of buttons
    private static boolean serverNotAvailable = false;
	private int chatBorder = 10;
	private static int titleHeight = 20;
	private static int chatHeight = 100;
    private BufferedImage myToonPane; // Used to read the file in that contains the sprite frames
    private int myToonFrames = 8; // Number of frames that will be used in the animation
    private int mouseLocX = 0; // Used to track X location of mouse
	private int mouseLocY = 0; // Used to track Y location of mouse
	private int chatAreaButtonX = 0; // Used to locate and track mouse for button
	private int chatAreaButtonY = 0; // Used to locate and track mouse for button
	private Color chatColor = Color.darkGray; // Set chat text color
	private Color buttonTimeColor = Color.lightGray; // Set time button color
	private Color buttonUpColor = Color.lightGray; // Set up button color
	private Color buttonDownColor = Color.lightGray; // Set down button color
	private Color graphicsTimeColor = Color.BLACK; // Set color for clock.
	private Color graphicsUpArrowColor = Color.BLACK; // Set color for up arrow
	private Color graphicsDownArrowColor = Color.BLACK; // Set color for dowm arrow
    
    // Network settings
	private static String fClientName = "";
	private static String fServerName = "makegames.org";
	private static int fServerPort = 2004;
	private Socket requestSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	private int networkNanoSecDelay = 400000000; // .4 second delay between sprite frames
    
    // The following is for my toon
	private BufferedImage[] myToon = new BufferedImage[myToonFrames]; // Create an array for the frames
	private BufferedImage myCurToon; // Image that will hold the currently displayed frame
	private static String myLabel = "Me!"; // Label that will be displayed over the sprite
	private int myXPos = 0; // Will be used for the position of my sprite in the game window
    private int myYPos = 20; // Will be used for the position of my sprite in the game window
	private int myCurFrame = 0; // Used for animation frames
	private boolean myMoveUp = false; // Will be used to tell sprite to move up
    private boolean myMoveLeft = false; // Will be used to tell sprite to move left
    private boolean myMoveRight = false; // Will be used to tell sprite to move right
    private boolean myMoveDown = false; // Will be used to tell sprite to move down
    private boolean myFaceLeft = false; // Will be used when you need to flip image horizontally
    private int myLabelLength = 0; // Will be used to help find the center of the label
    private static String chatMessage = "Logged in"; // Used to capture typed messages to send to others
    private DateParse myDateParse = new DateParse(); // Set of parsing methods that will be used
    private boolean showChatDates = false;
    private static String nowTime = "";
	
	// The following arrays will be used for other toons
    private int otherToons = 0; // Number of others to be on the screen with me
    private int maxToons = 100; // Maximum number of others that could be on the screen with me
	private BufferedImage[] otherToon = new BufferedImage[myToonFrames]; // Create an array for the frames
	private BufferedImage[] otherCurToon = new BufferedImage[maxToons]; // Array of images that will hold the currently displayed frame
	private String otherLabel[] = new String[maxToons]; // Label that will be displayed over other sprites
	private int [] otherXPos = new int [maxToons]; // Array for X position for other users
	private int [] otherYPos = new int [maxToons]; // Array for Y position for other users
	private int [] otherXPosPrev = new int [maxToons]; // Array for X previous position for other users
	private int [] otherYPosPrev = new int [maxToons]; // Array for Y previous position for other users
	private int [] otherCurFrame = new int [maxToons]; // Used for animation frames
	private boolean [] otherMoveUp = new boolean [maxToons]; // Will be used to tell sprite to move up
    private boolean [] otherMoveLeft = new boolean [maxToons]; // Will be used to tell sprite to move left
    private boolean [] otherMoveRight = new boolean [maxToons]; // Will be used to tell sprite to move right
    private boolean [] otherMoveDown = new boolean [maxToons]; // Will be used to tell sprite to move down
    private boolean [] otherFaceLeft = new boolean [maxToons]; // Will be used when you need to flip image horizontally
    private int otherLabelLength = 0; // Will be used to help find the center of the label
	
	// Following are shared by all toons
    private int errorFontHeight = 40;
    private int labelFontHeight = 16;
    private static int chatFontHeight = 12;
	private Rectangle myToonSize; // Will be used to hold the size of a single frame of the sprite
	private Font errorFont = new Font(Font.MONOSPACED, Font.BOLD, errorFontHeight); // Setup font that will be used
	private Font labelFont = new Font(Font.DIALOG, Font.BOLD, labelFontHeight); // Setup font that will be used
	private Font chatFont = new Font(Font.DIALOG, Font.BOLD, chatFontHeight); // Setup font that will be used
    private int graphicsNanoSecDelay = 200000000; // .2 second delay between sprite frames
    private int moveSpeed = 10; // Set the number of pixels to move per animation
	private int resetXPos = 0; // Will be used for the position of my sprite in the game window
    private int resetYPos = 20; // Will be used for the position of my sprite in the game window
    private static int chatLines = 500; // Number of chat lines to store
    private static int chatLinesShown = chatHeight/chatFontHeight-1; // Number of chat lines to show
    private static int lastDisplayLine = 0; // Line to display at the bottom of the chat window (scroll buttons change this)
	private int bottomChatLine = windowHeight-chatBorder-chatFontHeight/2; // Bottom line in chat area
    private static String chatLine[] = new String [chatLines]; // Array to store text of chat area
    private String lastChatLine = "";
    private String serverTime = "";
    
    private static void logInfo(String aMessage){
		System.out.println(aMessage);
		//chatArrayUpdate(aMessage);
	}
    
    private static void logWindow(String aMessage){
		System.out.println(aMessage);
		chatArrayUpdate(aMessage);
	}
    
    private static void chatArrayUpdate(String chatNewText){ // Updates text area array when needed
    	int lastArrayItem = chatLine.length-1;
    	for ( int i = lastArrayItem; i >= 0; i-- ) { // Go through the array and move each line up to make way for the new line
    		if ( i > 0 ) {
    			if (chatLine[i-1] != null) {
    				chatLine[i] = chatLine[i-1];
    			} else {
    				chatLine[i] = "";
    			}
    		} else {
    			chatLine[i] = chatNewText; // Set the beginning of the array to the new line
    		}
    	}
    	if ( lastDisplayLine != 0 ) lastDisplayLine++; // This is so that the display doesn't move if it's scrolled-up already
    }

    private void titleAreaDraw(Graphics2D g2d){ // Draw information in title area
    	String myLocationTitle = "Location: "; // Location title
    	String mouseLocationTitle = "Target: "; // Location title
    	String timeClientTitle = "Client Time: "; // Client time title
    	String timeServerTitle = "Server Time: "; // Server time title
    	g2d.setFont(chatFont); // Set font for title area
    	g2d.setColor(Color.darkGray); // Set the font color for the title area
    	String myLocation = myXPos + "," + myYPos; // Set my location text
    	String mouseLocation = mouseLocX + "," + mouseLocY; // Set mouse location text
    	g2d.drawString(myLocationTitle + myLocation, chatBorder+chatFontHeight/2, chatBorder+titleHeight/2+chatFontHeight/2); // Your location text
    	g2d.drawString(mouseLocationTitle + mouseLocation, chatBorder+chatFontHeight/2+160, chatBorder+titleHeight/2+chatFontHeight/2); // Mouse location text
    	nowTime = dateStamp(); // Get current time
    	g2d.drawString(timeClientTitle + nowTime.substring(10,16), windowSize.width-330, chatBorder+titleHeight/2+chatFontHeight/2); // Client time text
    	if ( !serverTime.equals("") ) g2d.drawString(timeServerTitle + serverTime.substring(10,16), windowSize.width-160, chatBorder+titleHeight/2+chatFontHeight/2); // Server time text
    }
    
    private void chatAreaDraw(Graphics2D g2d){ // Draw lines in text area
    	String thisChatLine = "";
    	g2d.setFont(chatFont); // Set font for chat area
    	g2d.setColor(chatColor); // Set the font color for the chat area
    	int tempChatLine = bottomChatLine; // Set starting line to be bottom of chat area
    	for ( int i = lastDisplayLine; i < lastDisplayLine+chatLinesShown; i++ ) { // Write the first few lines of the array in the chat area
    		if ( !showChatDates ) {
    			thisChatLine = removeChatDate(chatLine[i]);
    		} else {
    			thisChatLine = chatLine[i];
    		}
    		g2d.drawString(thisChatLine, chatBorder+chatFontHeight/2, tempChatLine);
    		tempChatLine = tempChatLine-chatFontHeight;
    	}
    }
    
    private void chatAreaButtons(Graphics2D g2d){ // Draw buttons in text area
    	chatAreaButtonX = windowSize.width-chatBorder-buttonSize.width; // Set upper left point of time button
    	chatAreaButtonY = windowSize.height-chatBorder-chatHeight/2-buttonSize.height/2; // Set lower right point of time button
    	
    	// Checks for mouse over of the time button
    	if ( mouseLocX > chatAreaButtonX && mouseLocX < chatAreaButtonX+buttonSize.width ) {
			if ( mouseLocY > chatAreaButtonY && mouseLocY < chatAreaButtonY+buttonSize.height ) {
				chatColor = Color.BLUE;
				buttonTimeColor = Color.BLUE;
				graphicsTimeColor = Color.WHITE;
			} else {
				chatColor = Color.darkGray;
				buttonTimeColor = Color.lightGray;
				graphicsTimeColor = Color.BLACK;
			}
		} else {
			chatColor = Color.darkGray;
			buttonTimeColor = Color.lightGray;
			graphicsTimeColor = Color.BLACK;
		}
    	
    	// Checks for mouse over of the up button
    	if ( mouseLocX > chatAreaButtonX && mouseLocX < chatAreaButtonX+buttonSize.width ) {
			if ( mouseLocY > windowSize.height-chatBorder-chatHeight && mouseLocY < windowSize.height-chatBorder-chatHeight+buttonSize.height ) {
				buttonUpColor = Color.BLUE;
				graphicsUpArrowColor = Color.WHITE;
			} else {
				buttonUpColor = Color.lightGray;
				graphicsUpArrowColor = Color.BLACK;
			}
		} else {
			buttonUpColor = Color.lightGray;
			graphicsUpArrowColor = Color.BLACK;
		}
    	
    	// Checks for mouse over of the down button
    	if ( mouseLocX > chatAreaButtonX && mouseLocX < chatAreaButtonX+buttonSize.width ) {
			if ( mouseLocY > windowSize.height-chatBorder-buttonSize.height && mouseLocY < windowSize.height-chatBorder ) {
				buttonDownColor = Color.BLUE;
				graphicsDownArrowColor = Color.WHITE;
			} else {
				buttonDownColor = Color.lightGray;
				graphicsDownArrowColor = Color.BLACK;
			}
		} else {
			buttonDownColor = Color.lightGray;
			graphicsDownArrowColor = Color.BLACK;
		}
    	
    	g2d.setStroke(new BasicStroke(3)); // Set lines to be thicker
    	
    	g2d.setColor(buttonTimeColor); // Set the time button color
    	g2d.fill3DRect(chatAreaButtonX, chatAreaButtonY, buttonSize.width, buttonSize.height, true); // Draw time button
    	g2d.setColor(graphicsTimeColor); // Set the time button clock color
    	g2d.drawOval(chatAreaButtonX+4, chatAreaButtonY+4, buttonSize.width-8, buttonSize.width-8); // Draw clock circle
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, chatAreaButtonY+6, chatAreaButtonX+buttonSize.width/2, chatAreaButtonY+buttonSize.height/2); // Draw clock minute hand
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, chatAreaButtonY+buttonSize.height/2, chatAreaButtonX+buttonSize.width/2+6, chatAreaButtonY+buttonSize.height/2); // Draw clock hour hand
    	
    	g2d.setColor(buttonUpColor); // Set the up button color
    	g2d.fill3DRect(chatAreaButtonX, windowSize.height-chatBorder-chatHeight, buttonSize.width, buttonSize.height, true); // Draw up button
    	g2d.setColor(graphicsUpArrowColor); // Set the up arrow color
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-chatHeight+5, chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-chatHeight+buttonSize.height-5); // Draw arrow stem
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-chatHeight+5, chatAreaButtonX+buttonSize.width/2-4, windowSize.height-chatBorder-chatHeight+16); // Draw arrow side
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-chatHeight+5, chatAreaButtonX+buttonSize.width/2+4, windowSize.height-chatBorder-chatHeight+16); // Draw arrow side
    	
    	g2d.setColor(buttonDownColor); // Set the down button color
    	g2d.fill3DRect(chatAreaButtonX, windowSize.height-chatBorder-buttonSize.height, buttonSize.width, buttonSize.height, true); // Draw down button
    	g2d.setColor(graphicsDownArrowColor); // Set the down arrow color
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-5, chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-buttonSize.height+5); // Draw arrow stem
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-5, chatAreaButtonX+buttonSize.width/2-4, windowSize.height-chatBorder-buttonSize.height+16); // Draw arrow side
    	g2d.drawLine(chatAreaButtonX+buttonSize.width/2, windowSize.height-chatBorder-5, chatAreaButtonX+buttonSize.width/2+4, windowSize.height-chatBorder-buttonSize.height+16); // Draw arrow side
    }
    
    private String removeChatDate(String lineToRemoveDate) { // Removes date and time from each chat line
    	String lineWithRemovedDate = "";
    	int startCharsToCut = 25;
    	if ( lineToRemoveDate.length() > startCharsToCut ) lineWithRemovedDate = lineToRemoveDate.substring(startCharsToCut);
		return lineWithRemovedDate;
    }
    
    private boolean isChatting(){
    	boolean chatting = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner().equals(inputField);
    	return chatting;
    }
    
	private String quote(String aText){
		String QUOTE = "'";
		return QUOTE + aText + QUOTE;
	}
	
	private String quote(int aInt){
		String QUOTE = "'";
		return QUOTE + aInt + QUOTE;
	}
	
	private static String dateStamp() {

		Date dNow = new Date( );
		SimpleDateFormat ft = 
				new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss:SSS");
		return ft.format(dNow);
	}
	
}