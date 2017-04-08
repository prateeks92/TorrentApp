package peer2peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import logging.*;
import messaging.*;
import property.*;

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
	
	public MessageIdentifier messageIdentifier;

	public Starter threadController;
	
	public PeerMessageSender peerMessageSender;
	
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
		
		peerHandler.messageIdentifier = MessageIdentifier.createIdentfier();
		
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
		
		peerHandler.peerMessageSender = PeerMessageSender.createInstance(peerHandler.OutputDataStream,peerHandler);
		
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
				Message message = (Message)InputDataStream.readObject();
				
				byte returnType = message.returnMsgType();
				
				switch (returnType) 
				{
					case Constants.MESSAGE_HANDSHAKE:
						if(message instanceof HandshakeMessage)
						{						
							HandshakeMessage handshakeMessage = (HandshakeMessage)message;
							checkHandshakeMessage(handshakeMessage);
						}
						else
						{
							System.out.println("Message is not a Handshake Message");
						}
						break;
					
					case Constants.MESSAGE_REQUEST:
						MsgDetails peer2PeerMessage = (MsgDetails)message; 
						handleRequestMessage(peer2PeerMessage);
						break;
					
					case Constants.MESSAGE_BITFIELD:
						handleBFMessage((MsgDetails)message);
						break;
					
					case Constants.MESSAGE_CHOKE:
						MsgDetails peer2PeerMessage = (MsgDetails)message;
						peerChoked=true;
						break;
					
					case Constants.MESSAGE_HAVE:
						MsgDetails peer2PeerMessage = (MsgDetails)message;
						handleHaveMessage(peer2PeerMessage);
						break;
						
					case Constants.MESSAGE_INTERESTED:
						MsgDetails peer2PeerMessage = (MsgDetails)message;
						receiveInterestedMessage(peer2PeerMessage);
						break;
					
					case Constants.MESSAGE_NOT_INTERESTED:
						MsgDetails peer2PeerMessage = (MsgDetails)message;
						receiveNotInterestedMessage(peer2PeerMessage);
						break;
						
					case Constants.MESSAGE_PIECE:
						MsgDetails peer2PeerMessage = (MsgDetails)message;
						receivePieceMessage(peer2PeerMessage);
						break;
						
					case Constants.MESSAGE_UNCHOKE:
						MsgDetails peer2PeerMessage = (MsgDetails)message;
						peerChoked = false;
						logger.info("Peer ["+threadController.getPeerID()+"] is unchoked by ["+peerID+"]");
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
						MsgDetails peer2peerMessage = (MsgDetails)message;
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
			
			HandshakeMessage message = (HandshakeMessage)InputDataStream.readObject();
			
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
	
	
	private void receivePieceMessage(MsgDetails pieceMessage)
	{
		threadController.saveDownloadedPiece(pieceMessage, peerID);
		threadController.sendHaveMessage(pieceMessage.getPieceIndex(),peerID);
		
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

	
	private void handleBFMessage(MsgDetails peer2PeerMessage) 
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

	
	private void checkHandshakeMessage(HandshakeMessage handshakeMessage)
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
	

	private void handleRequestMessage(MsgDetails requestMessage)
	{
		if(choked == false)
		{
			MsgDetails pieceMessage = threadController.getPieceMessage(requestMessage.getPieceIndex());
			
			if(pieceMessage != null)
			{
				try
				{
					Thread.sleep(2000);
					peerMessageSender.sendMessage(pieceMessage);
				}
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void handleHaveMessage(MsgDetails haveMessage)
	{
		logger.info("Peer ["+threadController.getPeerID()+"] received the 'have' message from ["+peerID+"] for the piece"+haveMessage.getPieceIndex());
		
		try 
		{
			pieceRequester.messageQueue.put(haveMessage);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	private void receiveInterestedMessage(MsgDetails interestedMessage)
	{
		logger.info("Peer ["+threadController.getPeerID()+"] received the 'interested' message from ["+peerID+"]");
	}
	
	
	private void receiveNotInterestedMessage(MsgDetails message)
	{
		logger.info("Peer ["+threadController.getPeerID()+"] received the 'not interested' message from ["+peerID+"]");
	}
	
	
	synchronized boolean sendHandshakeMessage()
	{
		try 
		{
			HandshakeMessage message = HandshakeMessage.createInstance();
			message.setPeerID(threadController.getPeerID());
			peerMessageSender.sendMessage(message);
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
			MsgDetails message = threadController.getBitFieldMessage();
			peerMessageSender.sendMessage(message);
			Thread.sleep(2000);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void sendInterestedMessage(MsgDetails interestedMessage)
	{
		try
		{
			if(peerChoked == false)
			{
				peerMessageSender.sendMessage(interestedMessage);
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
	
	
	public void sendNotInterestedMessage(MsgDetails notInterestedMessage)
	{
		try 
		{
			peerMessageSender.sendMessage(notInterestedMessage);

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendRequestMessage(MsgDetails requestMessage)
	{
		try 
		{
			if(peerChoked == false)
			{
				peerMessageSender.sendMessage(requestMessage);
			}			
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendChokeMessage(MsgDetails chokeMessage) 
	{		
		try 
		{
			if(choked == false)
			{
				startTime = System.currentTimeMillis();
				dataSize = 0;

				setChoke(true);
				peerMessageSender.sendMessage(chokeMessage);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendUnchokeMessage(MsgDetails unchokeMessage)
	{
		try 
		{
			if(choked == true)
			{
				startTime = System.currentTimeMillis();
				dataSize = 0;

				setChoke(false);
				peerMessageSender.sendMessage(unchokeMessage);
			}
			
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendHaveMessage(MsgDetails haveMessage) 
	{
		try 
		{
			peerMessageSender.sendMessage(haveMessage);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendShutdownMessage(MsgDetails shutdownMessage)
	{
		try 
		{
			peerMessageSender.sendMessage(shutdownMessage);
		}
		catch (Exception e) 
		{
			// TODO: handle exception
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