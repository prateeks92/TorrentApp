package beanClasses;

import beanClasses.*;

public class messageDefine implements message
{
	
	private static final long serialVersionUID = 1L;
	private int msgLen;
	private int msgType;
	private int pieceIndex;
	private pieceDetails data;
	private bitField bitFieldhandler = null;
	public int msgNum = 0;
	private static int msgCount = 0;
	
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
		msgCount++;
		msgNum = msgCount;
		return true;
	}
	
	
	public byte[] getMessage()
	{
		return null;
	}
	
	
	public int getLength() 
	{
		return this.msgLen;
	}
	
	public void setMsgLen(int messageLength) 
	{
		this.msgLen = messageLength;
	}
	
	
	public int returnMsgType() 
	{
		return this.msgType;
	}	
	
	
	public int getMsgType() 
	{
		return msgType;
	}

	
	public void setMsgType(int messgageType) 
	{
		this.msgType = messgageType;
	}

	
	public pieceDetails getData() 
	{
		return data;
	}

	
	public void setData(pieceDetails data)
	{
		this.data = data;
	}
	
	
	public int getPieceIndex() 
	{
		return pieceIndex;
	}

	
	public void setPieceIndex(int pieceIndex) 
	{
		this.pieceIndex = pieceIndex;
	}
	
	
	public bitField returnBitFieldHandler() 
	{
		return bitFieldhandler;
	}

	
	public void setBitFieldhandler(bitField handler) 
	{
		this.bitFieldhandler = handler;
	}

	
	public int getMessageIndex() 
	{
		return msgNum;
	}	
}
