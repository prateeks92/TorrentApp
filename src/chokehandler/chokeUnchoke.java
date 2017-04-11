package chokehandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import logging.*;
import property.*;
import peer2peer.*;


public class chokeUnchoke implements Runnable 
{

	private static chokeUnchoke Handler = null;


	public ScheduledFuture<?> task = null;

	private ScheduledExecutorService taskscheduler = null;

	private Starter threadController = null;
	
	private logger logger = null;

	public static synchronized chokeUnchoke createInstance(Starter controller)
	{
		if (Handler == null) 
		{
			if (controller == null) 
			{
				return null;
			}

			Handler = new chokeUnchoke();
			Handler.taskscheduler = Executors.newScheduledThreadPool(1);	
			Handler.logger = controller.getLogger();
			Handler.threadController = controller;
		}
		return Handler;
	}


	public void run() 
	{

		Integer preferredNeighbors = 0;
		HashMap<String, Double> speedMap = threadController.PeerDownloadSpeeds();
		
		if (PeerPropertyTokens.returnPropertyValue("NumberOfPreferredNeighbors") != null)
			preferredNeighbors = Integer.parseInt(PeerPropertyTokens.returnPropertyValue("NumberOfPreferredNeighbors"));
		else
			{	
			}

		if (preferredNeighbors > speedMap.size()) 
		{

		}
		else
		{
			ArrayList<String> unchoked = new ArrayList<String>();
			
			Set<Entry<String, Double>> entrySet = speedMap.entrySet();

			@SuppressWarnings("unchecked")
			Entry<String, Double>[] tempArr = new Entry[speedMap.size()];
			
			LinkedHashMap<String, Double> ssmap = new LinkedHashMap<String, Double>();
			
			tempArr = entrySet.toArray(tempArr);
			
			int count = 0;
			
			ArrayList<String> choked = new ArrayList<String>();

			for (int i = 0; i < tempArr.length; i++) 
			{
				for (int j = i + 1; j < tempArr.length; j++) 
				{
					if (tempArr[i].getValue().compareTo(tempArr[j].getValue()) == -1) 
					{
						Entry<String, Double> tempEntry = tempArr[i];
						tempArr[i] = tempArr[j];
						tempArr[j] = tempEntry;
					}
				}
			}
			
			
			for (int i = 0; i < tempArr.length; i++) 
			{
				ssmap.put(tempArr[i].getKey(), tempArr[i].getValue());	
			}


			for (Entry<String, Double> entry : ssmap.entrySet())
			{
				String key = entry.getKey();
				unchoked.add(key);
				count++; 
				if (count == preferredNeighbors)
				{
					break;
				}
			}


			for (String peerID : unchoked) 
			{
				ssmap.remove(peerID);
			}
			
			choked.addAll(ssmap.keySet());

			String log = "Peer ["+threadController.getPeerID()+"] has neighbors [";
			
			for (String peerID : unchoked) 
			{
				log += peerID + " , ";
			}

			log +="]";
			
			logger.info(log);
			
			threadController.unchokePeers(unchoked);
			threadController.chokeThePeers(choked);
		}
	}

	public void start(int startDelay, int intervalDelay)
	{
		task = taskscheduler.scheduleAtFixedRate(this, startDelay, intervalDelay, TimeUnit.SECONDS);
	}
}