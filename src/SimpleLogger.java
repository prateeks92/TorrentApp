
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The Class SimpleLogger.
 *
 * @author Prem Ankur
 */
public class SimpleLogger extends Logger{
	

	private String logFileName;

	public static SimpleLogger logger = null;
	

	private FileHandler fileHandler;
	
	
	private String peerID;
	
	
	private SimpleDateFormat formatter = null;
		
	
	public SimpleLogger(String peerID, String logFileName, String name) {
		super(name, null);
		this.logFileName = logFileName;
		this.setLevel(Level.FINEST);
		this.peerID = peerID;
	}
	
	
	public void initialize() throws SecurityException, IOException{

		fileHandler = new FileHandler(logFileName);
		fileHandler.setFormatter(new SimpleFormatter());
		formatter = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss a");
		this.addHandler(fileHandler);
	}

	

	@Override
	public synchronized void log(Level level, String msg) {

		super.log(level, msg+"\n");
	}
	
	
	public void close(){
		try {
			if(fileHandler!=null){
				fileHandler.close();
			}			
		} catch (Exception e) {			
			System.out.println("Unable to close logger.");
			e.printStackTrace();
		}
	}


	public void warning(String msg){
		Calendar c = Calendar.getInstance();		
		String dateInStringFormat = formatter.format(c.getTime());
		this.log(Level.WARNING, "["+dateInStringFormat+"]: Peer [peer_ID "+peerID+"] "+msg);
	}
	
	/* (non-Javadoc)
	 * @see java.util.logging.Logger#info(java.lang.String)
	 */
	public synchronized void info(String msg)
	{
		Calendar c = Calendar.getInstance();
		String dateInStringFormat = formatter.format(c.getTime());
		this.log(Level.INFO, "["+dateInStringFormat+"] : "+msg);
	}


	/**
	 * Gets the log.
	 *
	 * @param peerID the peer id
	 * @return the log
	 */
	public static SimpleLogger getLogger(String peerID) {
		if (logger == null) {

			String directory = "" + Constants.LOG_FILE_DIRECTORY_NAME;
			File file = new File(directory);
			file.mkdir();
			logger = new SimpleLogger(peerID, directory + "/" + Constants.LOG_FILE_NAME_PREFIX + peerID + ".log", Constants.LOGGER_NAME);
			try {
				logger.initialize();
			} catch (Exception e) {
				logger.close();
				logger = null;
				System.out.println("Unable to create or initialize logger");
				e.printStackTrace();
			}
		}
		return logger;
	}
}
