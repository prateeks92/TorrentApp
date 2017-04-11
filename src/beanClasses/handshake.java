package beanClasses;

import beanClasses.*;
import property.*;
/*
 * Author: Prem Ankur
 * */

public class handshake implements message
{
	private String peerID;	
	private static int numInstance;
	private int messageNumber;
	

	private handshake()
	{
		
	}
	
	public static handshake createNewInstance()
	{
		handshake handshakeMessage = new handshake();
		
		boolean success = true;
		
		numInstance++;
		
		if(success==true)
		{
			handshakeMessage.updateMsgNumber();
		}
		
		if(success == false)
		{
			handshakeMessage = null;
		}
		return handshakeMessage;
	}
	
	
	public void setPeerID(String peerID) 
	{
		this.peerID = peerID;
	}
	
	private void updateMsgNumber()
	{
			messageNumber=numInstance;
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