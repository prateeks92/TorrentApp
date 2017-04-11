package messaging;


import java.nio.ByteBuffer;
import peer2peer.*;
import property.*;
import beanClasses.*;

public class messageID 
{

	private static messageID messageIdentifier;
	
	private messageID(){
		
	}
	
	
	public void close()
	{
		
	}
	
	
	public static messageID createIdentfier()
	{
		if(messageIdentifier == null)
		{
			messageIdentifier = new messageID();
			boolean isInitialized = true;
			
			if(isInitialized == false)
			{
				messageIdentifier.close();
				messageIdentifier = null;
			}
		}
		return messageIdentifier;
	} 
	
	public byte[] geHandshakeMsg(byte[] rawData)
	{
		char headerArray[] = Constants.HANDSHAKE_HEADER_STRING.toCharArray();
		
		byte[] header = new byte[32];

		for(int i = 0; i< 18; i++)
		{
			header[i] = (byte)headerArray[i];	
		}
		
		for(int i = 18; i<31;i++)
		{
			header[i] = (byte)0;
			
		}
		
		header[31] = rawData[3];
		
		return header;
	}
	
	public byte[] getRequestMsg(int pieceIndex)
	{
		return null;
	}

	
	public byte[] getChokeMsg()
	{		
		ByteBuffer byteBuff = ByteBuffer.allocate(5);
		byteBuff.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuff.put(Constants.MESSAGE_CHOKE);
		byte[] chokeMessage = byteBuff.array();
		return chokeMessage;
		
	}
	
	
	public byte[] getUnchokeMsg()
	{
		ByteBuffer byteBuff = ByteBuffer.allocate(5);
		byteBuff.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuff.put(Constants.MESSAGE_UNCHOKE);
		byte[] unChokeMessage = byteBuff.array();
		return unChokeMessage;		
	}
	

	public byte[] getInterestMsg()
	{
		ByteBuffer byteBuff = ByteBuffer.allocate(5);
		byteBuff.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuff.put(Constants.MESSAGE_INTERESTED);
		byte[] interested = byteBuff.array();
		return interested;
	}
	
	public byte[] getNoInterestMsg()
	{
		ByteBuffer byteBuff = ByteBuffer.allocate(5);
		byteBuff.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuff.put(Constants.MESSAGE_NOT_INTERESTED);
		byte[] notInterested = byteBuff.array();
		return notInterested;

	}

	
	public byte[] getHaveMsg(byte[] payLoad)
	{
		ByteBuffer byteBuff = ByteBuffer.allocate(9);
		byteBuff.putInt(5);
		byteBuff.put(Constants.MESSAGE_HAVE);
		byteBuff.put(payLoad);
		byte[] have = byteBuff.array();
		return have;
		
	}	
	
	
	public byte[] getBitFieldMsg(byte[] payload)
	{
		int payloadLen = payload.length;
		ByteBuffer byteBuff = ByteBuffer.allocate(payloadLen+5);
		byteBuff.putInt(payloadLen+1);
		byteBuff.put(Constants.MESSAGE_BITFIELD);
		byteBuff.put(payload);
		byte[] bitFieldMessage = byteBuff.array();
		return bitFieldMessage;
	}

	
	public byte[] getRequestMsg(byte[] payLoad)
	{
		ByteBuffer byteBuff = ByteBuffer.allocate(9);
		byteBuff.putInt(5);
		byteBuff.put(Constants.MESSAGE_REQUEST);
		byteBuff.put(payLoad);
		byte[] requestMessage = byteBuff.array();
		return requestMessage;
	}
	
	
	public handshake checkHandShakeMsg(byte[] data)
	{
		return null;
	}
	
	
	public messageDefine checkPeer2PeerMsg(byte[] data)
	{ 
		return null;
	}
	
	
	public message checkMessage(byte[] data)
	{
		if( data== null || data.length < 5)
		{
			return null;
		}
		
		byte messageType = data[4];
		
		messageDefine message = messageDefine.createInstance();

		
		switch (messageType) 
		{
			case Constants.MESSAGE_CHOKE:
				message = messageDefine.createInstance();
				message.setMsgType(Constants.MESSAGE_CHOKE);
				message.setMsgLen(1);
				message.setData(null);
				return message;
				
			case Constants.MESSAGE_UNCHOKE:
				message = messageDefine.createInstance();
				message.setMsgType(Constants.MESSAGE_UNCHOKE);
				message.setMsgLen(1);
				message.setData(null);
				return message;
				
			case Constants.MESSAGE_INTERESTED:
				message = messageDefine.createInstance();
				message.setMsgType(Constants.MESSAGE_INTERESTED);
				message.setMsgLen(1);
				message.setData(null);
				return message;	
				
			case Constants.MESSAGE_NOT_INTERESTED:
				message = messageDefine.createInstance();
				message.setMsgType(Constants.MESSAGE_NOT_INTERESTED);
				message.setMsgLen(1);
				message.setData(null);
				return message;			
				
			case Constants.MESSAGE_HAVE:
				message = messageDefine.createInstance();
				message.setMsgLen(5);
				message.setMsgLen(Constants.MESSAGE_HAVE);
				message.setPieceIndex((int)data[8]);
				break;
				
			case Constants.MESSAGE_REQUEST:
				message = messageDefine.createInstance();
				message.setMsgLen(5);
				message.setMsgLen(Constants.MESSAGE_REQUEST);
				message.setPieceIndex((int)data[8]);
				break;
		}
		return null;
	}
}