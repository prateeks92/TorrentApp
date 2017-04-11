package chokehandler;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import logging.*;
import peer2peer.*;

public class unchoking implements Runnable{
	
	public ScheduledFuture<?> task = null;
    public ScheduledExecutorService task_scheduler = null;
    private static unchoking unchokingHandler = null;
    private Starter threadController = null;
    private logger logs = null;
    
    
    public static synchronized unchoking createInstance(Starter controller)
    {
    	if(unchokingHandler == null)
    	{
    		if(controller == null)
    		{
    			return null;
    		}
    		    		
    		unchokingHandler = new unchoking();
    		boolean isInitialized = unchokingHandler.init();
    		
    		if(isInitialized == false)
    		{
    			unchokingHandler.task.cancel(true);
    			unchokingHandler = null;
    			return null;
    		}
    		
    		unchokingHandler.threadController = controller;
    		unchokingHandler.logs = controller.getLogger();
    	}	
    	
    	return unchokingHandler;
    }
    
    private boolean init()
    {
    	task_scheduler = Executors.newScheduledThreadPool(1);    	
    	return true;
    }
    
	public void run() 
	{
		ArrayList<String> choked = threadController.chokedPeers;

		if(choked.size() > 0)
		{
			Random random = new Random();
			threadController.unchokePeer(choked.get(random.nextInt(choked.size())));
		}
		
		threadController.checkIfDownloaded();
		
		if(threadController.msgHandler.fileDownloadCompletionCheck() == true)
		{
			logs.info("Download completed for Peer ["+threadController.getPeerID()+"].");
			threadController.sendShutdownSignal();
		};
	}
}