package messaging;

import beanClasses.*;
import property.*;
/*
 * Author: Prem Ankur
 * */

public class handshake implements message
{
	private String peerID;
	
	private static int countInstance;
	
	private int messageNumber;
	
	public void setPeerID(String peerID) 
	{
		this.peerID = peerID;
	}

	private handshake()
	{
		
	}
	
	public static handshake createInstance()
	{
		handshake handshakeMessage = new handshake();
		
		boolean success = true;
		
		countInstance++;
		
		if(success==true)
		{
			handshakeMessage.updateMessageNumber();
		}
		
		if(success == false)
		{
			handshakeMessage = null;
		}
		return handshakeMessage;
	}
	
	
	private void updateMessageNumber()
	{
			messageNumber=countInstance;
	}
	
	
	public int returnMsgType() 
	{
		return Constants.MESSAGE_HANDSHAKE;
	}

	public int getLength() 
	{
		return 0;
	}
	
	public String getPeerID()
	{
		return peerID;
	}

	public int getMessageIndex() 
	{
		return messageNumber;
	}

	public int getMessageLength() 
	{
		return 0;
	}
}