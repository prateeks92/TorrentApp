package property;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class PeerPropertyTokens 
{
	
	private static final Hashtable<String, String> tokens = new Hashtable<String, String>();

	static
	{
		try 
		{
			BufferedReader configFileReader =  new BufferedReader(new InputStreamReader(new FileInputStream(Constants.CONFIG_FILE)));
			
			String line = configFileReader.readLine();
			
			while(line != null)
			{
				String t[] = line.trim().split(" ");
				tokens.put(t[0].trim(), t[1].trim());
				line = configFileReader.readLine();
			}
			configFileReader.close();	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception: "+e.getMessage());
			throw new ExceptionInInitializerError("Error Loading properties");
		}
	}
	
	public static String returnPropertyValue(String value)
	{
		return tokens.get(value);
	}
}