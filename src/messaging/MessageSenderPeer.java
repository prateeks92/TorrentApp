package messaging;

import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import beanClasses.*;
import messaging.*;
import peer2peer.*;
import property.*;

public class MessageSenderPeer implements Runnable {
	
	private static final String LOGGER_PREFIX = MessageSenderPeer.class.getSimpleName();	
	private BlockingQueue<message> queue_Messages;
	private ObjectOutputStream outStream = null;
	private boolean shutDown = false;
	
	
	private MessageSenderPeer(){
		
	}
	
	public static MessageSenderPeer instanceCreate(ObjectOutputStream outputStream, Peer handler){
		
		MessageSenderPeer msgSender = new MessageSenderPeer();
		boolean isInitialized = msgSender.init();		
		if(isInitialized == false){
			msgSender.deinitialize();
			msgSender = null;
			return null;
		}
		
		msgSender.outStream = outputStream;
		return msgSender;
	}
	
	public void deinitialize(){
		if(queue_Messages !=null && queue_Messages.size()!=0){
			queue_Messages.clear();
		}
		queue_Messages = null;
	}
	
	private boolean init(){
		queue_Messages = new ArrayBlockingQueue<message>(Constants.SENDER_SIZE_QUEUE);
		return true;
	}
	
	public void messageDetailsShow(message msg){
		if(msg.returnMsgType() != Constants.MESSAGE_HAVE && msg.returnMsgType() != Constants.MESSAGE_NOT_INTERESTED && msg.returnMsgType() != Constants.MESSAGE_INTERESTED){
			if(msg.returnMsgType() == Constants.MESSAGE_PIECE || msg.returnMsgType() == Constants.MESSAGE_REQUEST){

			}else{

			}			
		}		
	}
	
	public void run() {
		
		if(queue_Messages == null){
			throw new IllegalStateException(LOGGER_PREFIX+": This object is not initialized properly. This might be result of calling deinit() method");
		}
		
		while(shutDown == false){
			try {				
				message msg = queue_Messages.take();
				
				outStream.writeUnshared(msg);
				outStream.flush();
				messageDetailsShow(msg);				
				
				msg = null;
			} catch (Exception e) {				
					//e.printStackTrace();	
				System.out.println("SYSTEM GOT TERMINATED..!!");
				
				break;
			}
		}
	}
	
	public void messageSend(message msg) throws InterruptedException{
		if(queue_Messages == null){
			throw new IllegalStateException("");
		}else{
			queue_Messages.put(msg);
		}
	}
	
	public void shutdown(){
		shutDown = true;
	}
}
