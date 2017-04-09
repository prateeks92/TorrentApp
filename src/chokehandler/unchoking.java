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
	
    private static unchoking Handler = null;
    
    private Starter threadController = null;
    
    private logger logger = null;
        
    public static synchronized unchoking createInstance(Starter controller)
    {
    	if(Handler == null)
    	{
    		if(controller == null)
    		{
    			return null;
    		}
    		    		
    		Handler = new unchoking();
    		boolean isInitialized = Handler.init();
    		
    		if(isInitialized == false)
    		{
    			Handler.task.cancel(true);
    			Handler = null;
    			return null;
    		}
    		
    		Handler.threadController = controller;
    		Handler.logger = controller.getLogger();
    	}	
    	
    	return Handler;
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
		
		threadController.checkAllPeersFileDownloadComplete();
		
		if(threadController.msgHandler.fileDownloadCompletionCheck() == true)
		{
			logger.info("Download completed for Peer ["+threadController.getPeerID()+"].");
			threadController.broadcastShutdown();
		};
	}
}