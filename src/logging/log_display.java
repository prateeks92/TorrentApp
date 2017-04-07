package logging;

import java.io.BufferedReader;
import java.io.IOException;

public class log_display implements Runnable
{
	BufferedReader reader;
	String peerID;
	
	public void run()
	{
		try
		{
			String l = null;
			while( (l = reader.readLine()) != null )
			{
				System.out.println("["+peerID+"]: "+l);
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public log_display(String peerID, BufferedReader reader)
	{
		this.reader = reader;
		this.peerID = peerID;
	}
}