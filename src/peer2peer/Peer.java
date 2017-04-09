package peer2peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import logging.*;
import messaging.*;
import property.*;
import beanClasses.*;
public class Peer implements Runnable
{	
	public static final String LOGGER_PREFIX = Peer.class.getSimpleName();
	
	public String peerID;

	private Socket neighborSocket;
	
	private ObjectInputStream InputDataStream;
	
	public ObjectOutputStream OutputDataStream;
	
	public boolean choked;
	
	public boolean peerChoked;
	
	public boolean handshakeACKReceived = false;
	
	public boolean handShakeMessageSent = false;
	
	public boolean chunkRequestStarted = false;
	
	public messageID messageIdentifier;

	public Starter threadController;
	
	public MessageSenderPeer peerMessageSender;
	
	public requestPiece pieceRequester;
	
	public boolean pieceMessageForLastMessageReceived = true;
	
	public long startTime;
	
	public int dataSize;
	
	public logger logs = null;
	
	private Peer()
	{
		
	}
	
	synchronized public static Peer createPeerConnection(Socket socket, Starter controller)
	{
		Peer peerHandler = new Peer();
		
		peerHandler.neighborSocket = socket;
		peerHandler.threadController = controller;
		
		boolean isInitialized = false;

		if(peerHandler.neighborSocket == null)
		{
			peerHandler.close();
			peerHandler = null;
			return null;
		}
		
		try 
		{	
			peerHandler.OutputDataStream = new ObjectOutputStream(peerHandler.neighborSocket.getOutputStream());
			peerHandler.InputDataStream = new ObjectInputStream(peerHandler.neighborSocket.getInputStream());			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			peerHandler.close();
			peerHandler = null;
			return null;
		}
		
		peerHandler.messageIdentifier = messageID.createIdentfier();
		
		if(peerHandler.messageIdentifier == null)
		{
			peerHandler.close();
			return null;
		}
		
		if(controller == null)
		{
			peerHandler.close();
			return null;
		}
		
		peerHandler.peerMessageSender = MessageSenderPeer.instanceCreate(peerHandler.OutputDataStream,peerHandler);
		
		if(peerHandler.peerMessageSender == null)
		{
			peerHandler.close();
			return null;
		}
		
		new Thread(peerHandler.peerMessageSender).start();

		peerHandler.pieceRequester = requestPiece.createInstance(controller, peerHandler);

		peerHandler.logs = controller.getLogger();
		isInitialized=true;
		
		if(isInitialized == false){
			peerHandler.close();
			peerHandler = null;
		}
		return peerHandler;
	}
	
	
	synchronized public void close()
	{
		try
		{
			if(InputDataStream != null)
			{
				InputDataStream.close();
			}			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}
	
	public void run()
	{
		byte[] rawData = new byte[Constants.RAW_DATA_SIZE];
		ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_MESSAGE_SIZE);
		
		if(peerID != null)
		{
			sendHandshakeMessage();
		}

		try 
		{	
			while(true)
			{
				message message = (message)InputDataStream.readObject();
				
				int returnType = message.returnMsgType();
				
				messageDefine peer2PeerMessage = (messageDefine)message;
				
				switch (returnType) 
				{
					case Constants.MESSAGE_HANDSHAKE:
						if(message instanceof handshake)
						{						
							handshake handshakeMessage = (handshake)message;
							checkHandshakeMessage(handshakeMessage);
						}
						else
						{
							System.out.println("Message is not a Handshake Message");
						}
						break;
					
					case Constants.MESSAGE_REQUEST:
						peer2PeerMessage = (messageDefine)message; 
						handleRequestMessage(peer2PeerMessage);
						break;
					
					case Constants.MESSAGE_BITFIELD:
						handleBFMessage((messageDefine)message);
						break;
					
					case Constants.MESSAGE_CHOKE:
						peer2PeerMessage = (messageDefine)message;
						peerChoked=true;
						break;
					
					case Constants.MESSAGE_HAVE:
						peer2PeerMessage = (messageDefine)message;
						handleHaveMessage(peer2PeerMessage);
						break;
						
					case Constants.MESSAGE_INTERESTED:
						peer2PeerMessage = (messageDefine)message;
						receiveInterestedMessage(peer2PeerMessage);
						break;
					
					case Constants.MESSAGE_NOT_INTERESTED:
						peer2PeerMessage = (messageDefine)message;
						receiveNotInterestedMessage(peer2PeerMessage);
						break;
						
					case Constants.MESSAGE_PIECE:
						peer2PeerMessage = (messageDefine)message;
						receivePieceMessage(peer2PeerMessage);
						break;
						
					case Constants.MESSAGE_UNCHOKE:
						peer2PeerMessage = (messageDefine)message;
						peerChoked = false;
						logs.info("Peer ["+threadController.getPeerID()+"] is unchoked by ["+peerID+"]");
						try 
						{
							pieceRequester.messageQueue.put(peer2PeerMessage);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						break;
					
					case Constants.MESSAGE_SHUTDOWN:
						messageDefine peer2peerMessage = (messageDefine)message;
						threadController.peerList.add(peerID);
						break;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public synchronized boolean sendHandshakeBitField()
	{
		try 
		{
			
			handshake message = (handshake)InputDataStream.readObject();
			
			peerID = message.getPeerID();

			Thread.sleep(4000);
			
			checkHandshakeMessage(message);	

			return true;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 

		return false;
	}
	
	
	private void receivePieceMessage(messageDefine pieceMessage)
	{
		threadController.saveDownloadedPiece(pieceMessage, peerID);
		threadController.sendHavePeiceMessage(pieceMessage.getPieceIndex(),peerID);
		
		dataSize += pieceMessage.getData().getSize();
		
		setPieceMessageForPreviousMessageReceived(true);

		try 
		{
			pieceRequester.messageQueue.put(pieceMessage);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

	
	private void handleBFMessage(messageDefine peer2PeerMessage) 
	{	
		try 
		{
			pieceRequester.messageQueue.put(peer2PeerMessage);
			
			if(handshakeACKReceived == true && handShakeMessageSent == true && chunkRequestStarted == false)
			{
				new Thread(pieceRequester).start();
				startTime = System.currentTimeMillis();
				dataSize = 0;
				setChunkRequestedStarted(true) ;
			}
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

	
	private void checkHandshakeMessage(handshake handshakeMessage)
	{	
		peerID = handshakeMessage.getPeerID();
		sendBitField();
		
		if(handShakeMessageSent == false)
		{
			logs.info("Peer "+threadController.getPeerID()+" is connected from Peer "+peerID+".");
			sendHandshakeMessage();
		}
		
		handshakeACKReceived = true;
		
		if(handshakeACKReceived == true && handShakeMessageSent == true && chunkRequestStarted == false)
		{
			new Thread(pieceRequester).start();
			startTime = System.currentTimeMillis();
			dataSize = 0;
			setChunkRequestedStarted(true);
		}
	}
	

	private void handleRequestMessage(messageDefine requestMessage)
	{
		if(choked == false)
		{
			messageDefine pieceMessage = threadController.getPieceMessage(requestMessage.getPieceIndex());
			
			if(pieceMessage != null)
			{
				try
				{
					Thread.sleep(2000);
					peerMessageSender.messageSend(pieceMessage);
				}
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void handleHaveMessage(messageDefine haveMessage)
	{
		logs.info("Peer ["+threadController.getPeerID()+"] received the 'have' message from ["+peerID+"] for the piece"+haveMessage.getPieceIndex());
		
		try 
		{
			pieceRequester.messageQueue.put(haveMessage);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	private void receiveInterestedMessage(messageDefine interestedMessage)
	{
		logs.info("Peer ["+threadController.getPeerID()+"] received the 'interested' message from ["+peerID+"]");
	}
	
	
	private void receiveNotInterestedMessage(messageDefine message)
	{
		logs.info("Peer ["+threadController.getPeerID()+"] received the 'not interested' message from ["+peerID+"]");
	}
	
	
	synchronized boolean sendHandshakeMessage()
	{
		try 
		{
			handshake message = handshake.createInstance();
			message.setPeerID(threadController.getPeerID());
			peerMessageSender.messageSend(message);
			handShakeMessageSent = true;
			return true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return false;
	}
	
	
	synchronized boolean sendBitField()
	{
		try 
		{			
			messageDefine message = threadController.getBitFieldMessage();
			peerMessageSender.messageSend(message);
			Thread.sleep(2000);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void sendInterestedMessage(messageDefine interestedMessage)
	{
		try
		{
			if(peerChoked == false)
			{
				peerMessageSender.messageSend(interestedMessage);
			}
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public boolean checkIfDownloadComplete()
	{
		if(chunkRequestStarted == true)
		{
			return pieceRequester.checkIfNeighbourDownloadFile();
		}
		else
		{
			return false;
		}
	}
	
	
	public void sendNotInterestedMessage(messageDefine notInterestedMessage)
	{
		try 
		{
			peerMessageSender.messageSend(notInterestedMessage);

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendRequestMessage(messageDefine requestMessage)
	{
		try 
		{
			if(peerChoked == false)
			{
				peerMessageSender.messageSend(requestMessage);
			}			
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendChokeMessage(messageDefine chokeMessage) 
	{		
		try 
		{
			if(choked == false)
			{
				startTime = System.currentTimeMillis();
				dataSize = 0;

				setChoke(true);
				peerMessageSender.messageSend(chokeMessage);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendUnchokeMessage(messageDefine unchokeMessage)
	{
		try 
		{
			if(choked == true)
			{
				startTime = System.currentTimeMillis();
				dataSize = 0;

				setChoke(false);
				peerMessageSender.messageSend(unchokeMessage);
			}
			
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendHaveMessage(messageDefine haveMessage) 
	{
		try 
		{
			peerMessageSender.messageSend(haveMessage);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendShutdownMessage(messageDefine shutdownMessage)
	{
		try 
		{
			peerMessageSender.messageSend(shutdownMessage);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	

	private void setChoke(boolean message)
	{
		choked = message;
	}
	
	
	synchronized public void setPeerID(String peerID) 
	{
		this.peerID = peerID;	
	}
	
	
	public boolean isPieceMessageForPreviousMessageReceived() 
	{
		return pieceMessageForLastMessageReceived;
	}

	
	public void setPieceMessageForPreviousMessageReceived(boolean isPieceMessageForPreviousMessageReceived)
	{
		this.pieceMessageForLastMessageReceived = isPieceMessageForPreviousMessageReceived;
	}
	
	
	public double getDownloadSpeed()
	{
		long timePeriod = System.currentTimeMillis() - startTime;
		if(timePeriod != 0)
		{
			return ((dataSize * 1.0) / (timePeriod * 1.0) );
		}
		else
		{
			return 0;
		} 
	}
	
	
	public void setHandshakeMessageReceived(boolean isHandshakeACKReceived) 
	{
		this.handshakeACKReceived = isHandshakeACKReceived;
	}

	
	public synchronized void setChunkRequestedStarted(boolean isChunkRequestedStarted) 
	{
		this.chunkRequestStarted = isChunkRequestedStarted;
	}	
}