package logging;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import property.Constants;
import logging.logger;

public class logger extends Logger
{
	private String logFN;
	public static logger log = null;
	private FileHandler fileHandler;
	private String peerID;	
	private SimpleDateFormat dateTime = null;
	
	public logger(String peerID, String logFN, String name) {
		super(name, null);
		this.logFN= logFN;
		this.setLevel(Level.FINEST);
		this.peerID = peerID;
	}
	
	public void init() throws SecurityException, IOException
	{
		fileHandler = new FileHandler(logFN);
		fileHandler.setFormatter(new SimpleFormatter());
		dateTime= new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss a");
		this.addHandler(fileHandler);
	}
	
	@Override
	public synchronized void log(Level lvl, String msg)
	{
		super.log(lvl, msg+"\n");
	}
	
	public void close()
	{
		try
		{
			if(fileHandler != null)
			{
				fileHandler.close();
			}
		}
		catch (Exception e)
		{			
			System.out.println("Logger not closed.");
			e.printStackTrace();
		}
	}
	
	public void warn(String msg)
	{
		Calendar c = Calendar.getInstance();		
		String dateInStringFormat = dateTime.format(c.getTime());
		this.log(Level.WARNING, "["+dateInStringFormat+"]: Peer [peer_ID "+peerID+"] "+msg);
	}

	public synchronized void info(String msg)
	{
		Calendar c = Calendar.getInstance();
		String dateInStringFormat = dateTime.format(c.getTime());
		this.log(Level.INFO, "["+dateInStringFormat+"] : "+msg);
	}
	
	public static logger getLogger(String peerID) 
	{
		if (log == null) 
		{
			String directory = "" + Constants.LOG_FILE_DIRECTORY_NAME;
			File file = new File(directory);
			file.mkdir();
			log = new logger(peerID, directory + "/" + Constants.LOG_FILE_NAME_PREFIX + peerID + ".log", Constants.LOGGER_NAME);
			try
			{
				log.init();
			} catch (Exception e) {
				log.close();
				log = null;
				System.out.println("Unable to initialize logger");
				e.printStackTrace();
			}
		}
		return log;
	}
}
