package messaging;

import beanClasses.*;

public class messageDefine implements message
{
	
	private static final long serialVersionUID = 1L;
	private int messageLength;
	private int messgageType;
	private int pieceIndex;
	private pieceDetails data;
	private bitField handler = null;
	public int messageNumber = 0;
	private static int messageCounter = 0;
	
	private messageDefine()
	{
		
	}
	
	public static messageDefine createInstance()
	{
		messageDefine message = new messageDefine();
		
		boolean success = message.init();
		
		if(success == false){
		
			message = null;
		}
		
		return message;
	}
	
	
	private boolean init()
	{
		messageCounter++;
		messageNumber = messageCounter;
		return true;
	}
	
	
	public byte[] getMessage()
	{
		return null;
	}

	
	public int returnMsgType() 
	{
		return this.messgageType;
	}

	
	public int getLength() 
	{
		return this.messageLength;
	}
	
	
	public int getMessgageType() 
	{
		return messgageType;
	}

	
	public void setMessgageType(int messgageType) 
	{
		this.messgageType = messgageType;
	}

	
	public int getPieceIndex() 
	{
		return pieceIndex;
	}

	
	public void setPieceIndex(int pieceIndex) 
	{
		this.pieceIndex = pieceIndex;
	}

	
	public pieceDetails getData() 
	{
		return data;
	}

	
	public void setData(pieceDetails data)
	{
		this.data = data;
	}

	
	public void setMessageLength(int messageLength) 
	{
		this.messageLength = messageLength;
	}

	
	public bitField returnBitFieldHandler() 
	{
		return handler;
	}

	
	public void setHandler(bitField handler) 
	{
		this.handler = handler;
	}

	
	public int getMessageIndex() 
	{
		return messageNumber;
	}	
}
