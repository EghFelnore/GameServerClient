import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLog {

	void append(String logText){
		nowTime = dateStamp();
		System.out.println("server> " + nowTime + ": " + logText);
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(logFile, true));
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			System.out.println("server>" + nowTime + ": Can not access log file " + logFile);
		}
		try {
			out.write(nowTime + ": " + logText + "\n");
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
			System.out.println("server>" + nowTime + ": Can not write to log file " + logFile);
		}
		finally {
			try {
				out.close();
			}
			catch(IOException ioException){
				//ioException.printStackTrace();
				System.out.println("server>" + nowTime + ": Can not close log file " + logFile);
			}
		}
	}

	//PRIVATE
	private static final String logFile = "Server.log";
	private static String nowTime;
	
	private static String dateStamp() {

		Date dNow = new Date( );
		SimpleDateFormat ft = 
				new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		return ft.format(dNow);
	}

}
