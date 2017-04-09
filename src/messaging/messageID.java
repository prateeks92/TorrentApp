package messaging;


import java.nio.ByteBuffer;
import peer2peer.*;
import property.*;
import beanClasses.*;

public class messageID 
{

	private static messageID manager;
	
	private messageID(){
		
	}
	
	public static messageID createIdentfier()
	{
		if(manager == null)
		{
			manager = new messageID();
			boolean isInitialized = true;
			
			if(isInitialized == false)
			{
				manager.close();
				manager = null;
			}
		}
		return manager;
	} 
	
	
	public void close()
	{
		
	}
	
	public byte[] geHandshakeMessage(byte[] rawData)
	{
		String headerString = Constants.HANDSHAKE_HEADER_STRING ;

		char array[] = headerString.toCharArray();
		
		byte[] headerMessage = new byte[32];

		for(int i = 0; i< 18; i++)
		{
			headerMessage[i] = (byte)array[i];	
		}
		
		for(int i = 18; i<31;i++)
		{
			headerMessage[i] = (byte)0;
			
		}
		
		headerMessage[31] = rawData[3];
		
		return headerMessage;
	}
	
	public byte[] getRequestMessage(int pieceIndex)
	{
		return null;
	}

	
	public byte[] getChokeMessage()
	{		
		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuffer.put(Constants.MESSAGE_CHOKE);
		byte[] chokeMessage = byteBuffer.array();
		return chokeMessage;
		
	}
	
	
	public byte[] getUnchokeMessage()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuffer.put(Constants.MESSAGE_UNCHOKE);
		byte[] unChokeMessage = byteBuffer.array();
		return unChokeMessage;		
	}
	

	public byte[] getInterestedMessage()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuffer.put(Constants.MESSAGE_INTERESTED);
		byte[] interested = byteBuffer.array();
		return interested;
	}
	
	public byte[] getNotInterestedMessage()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.putInt(Constants.EMPTY_MESSAGE_SIZE);
		byteBuffer.put(Constants.MESSAGE_NOT_INTERESTED);
		byte[] notInterested = byteBuffer.array();
		return notInterested;

	}

	
	public byte[] getHaveMessage(byte[] payLoad)
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(9);
		byteBuffer.putInt(5);
		byteBuffer.put(Constants.MESSAGE_HAVE);
		byteBuffer.put(payLoad);
		byte[] have = byteBuffer.array();
		return have;
		
	}	
	
	
	public byte[] getBitFieldMessage(byte[] payload)
	{
		int payloadSize = payload.length;
		ByteBuffer byteBuffer = ByteBuffer.allocate(payloadSize+5);
		byteBuffer.putInt(payloadSize+1);
		byteBuffer.put(Constants.MESSAGE_BITFIELD);
		byteBuffer.put(payload);
		byte[] bitFieldMessage = byteBuffer.array();
		return bitFieldMessage;
	}

	
	public byte[] getRequestMessage(byte[] payLoad)
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(9);
		byteBuffer.putInt(5);
		byteBuffer.put(Constants.MESSAGE_REQUEST);
		byteBuffer.put(payLoad);
		byte[] requestMessage = byteBuffer.array();
		return requestMessage;
	}
	
	
	public handshake parseHandShakeMessage(byte[] rawData)
	{
		return null;
	}
	
	
	public messageDefine parsePeer2PeerMessage(byte[] rawData)
	{ 
		return null;
	}
	
	
	public message parseMessage(byte[] rawData)
	{
		if( rawData== null || rawData.length < 5)
		{
			return null;
		}
		
		byte messageType = rawData[4];
		
		messageDefine message = messageDefine.createInstance();

		
		switch (messageType) 
		{
			case Constants.MESSAGE_CHOKE:
				message = messageDefine.createInstance();
				message.setMessgageType(Constants.MESSAGE_CHOKE);
				message.setMessageLength(1);
				message.setData(null);
				return message;
				
			case Constants.MESSAGE_UNCHOKE:
				message = messageDefine.createInstance();
				message.setMessgageType(Constants.MESSAGE_UNCHOKE);
				message.setMessageLength(1);
				message.setData(null);
				return message;
				
			case Constants.MESSAGE_INTERESTED:
				message = messageDefine.createInstance();
				message.setMessgageType(Constants.MESSAGE_INTERESTED);
				message.setMessageLength(1);
				message.setData(null);
				return message;	
				
			case Constants.MESSAGE_NOT_INTERESTED:
				message = messageDefine.createInstance();
				message.setMessgageType(Constants.MESSAGE_NOT_INTERESTED);
				message.setMessageLength(1);
				message.setData(null);
				return message;			
				
			case Constants.MESSAGE_HAVE:
				message = messageDefine.createInstance();
				message.setMessageLength(5);
				message.setMessageLength(Constants.MESSAGE_HAVE);
				message.setPieceIndex((int)rawData[8]);
				break;
				
			case Constants.MESSAGE_REQUEST:
				message = messageDefine.createInstance();
				message.setMessageLength(5);
				message.setMessageLength(Constants.MESSAGE_REQUEST);
				message.setPieceIndex((int)rawData[8]);
				break;
		}
		return null;
	}
}
