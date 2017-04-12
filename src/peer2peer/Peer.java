package peer2peer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
	public messageID messageIdentifier;
	public Starter threadController;
	public MessageSenderPeer peerMessageSender;
	public requestPiece pieceRequester;
	public boolean choked;
	public boolean peerChoked;
	public boolean handshakeACKReceived = false;
	public boolean handShakeMessageSent = false;
	public boolean chunkRequestStarted = false;
	public boolean pieceMessageForLastMessageReceived = true;
	public long startTime;
	public int dataSize;
	public logger logs = null;
	
	private Peer()
	{
		
	}
	
	synchronized public static Peer newConnection(Socket socket, Starter controller)
	{
		Peer newPeer = new Peer();
		
		newPeer.neighborSocket = socket;
		newPeer.threadController = controller;
		
		boolean isInit = false;

		if(newPeer.neighborSocket == null)
		{
			newPeer.close();
			newPeer = null;
			return null;
		}
		
		try 
		{	
			newPeer.OutputDataStream = new ObjectOutputStream(newPeer.neighborSocket.getOutputStream());
			newPeer.InputDataStream = new ObjectInputStream(newPeer.neighborSocket.getInputStream());			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			newPeer.close();
			newPeer = null;
			return null;
		}
		
		newPeer.messageIdentifier = messageID.createIdentfier();
		
		if(newPeer.messageIdentifier == null)
		{
			newPeer.close();
			return null;
		}
		
		if(controller == null)
		{
			newPeer.close();
			return null;
		}
		
		newPeer.peerMessageSender = MessageSenderPeer.instanceCreate(newPeer.OutputDataStream,newPeer);
		
		if(newPeer.peerMessageSender == null)
		{
			newPeer.close();
			return null;
		}
		
		new Thread(newPeer.peerMessageSender).start();

		newPeer.pieceRequester = requestPiece.createInstance(controller, newPeer);

		newPeer.logs = controller.getLogger();
		isInit=true;
		
		if(isInit == false){
			newPeer.close();
			newPeer = null;
		}
		return newPeer;
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
		byte[] data = new byte[Constants.RAW_DATA_SIZE];
		ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_MESSAGE_SIZE);
		
		if(peerID != null)
		{
			sendHandshakeMsg();
		}

		try 
		{	
			while(true)
			{
				message message = (message)InputDataStream.readObject();
				
				int returnType = message.returnMsgType();
				
				messageDefine peer2PeerMsg;
				
				switch (returnType) 
				{
					case Constants.MESSAGE_HANDSHAKE:
						if(message instanceof handshake)
						{						
							handshake handshakeMessage = (handshake)message;
							checkHandshakeMsg(handshakeMessage);
						}
						else
						{
							System.out.println("Message is not a Handshake Message");
						}
						break;
					
					case Constants.MESSAGE_REQUEST:
						peer2PeerMsg = (messageDefine)message; 
						handleRequestMsg(peer2PeerMsg);
						break;
					
					case Constants.MESSAGE_BITFIELD:
						handleP2PMsg((messageDefine)message);
						break;
					
					case Constants.MESSAGE_CHOKE:
						peer2PeerMsg = (messageDefine)message;
						peerChoked=true;
						logs.info("Peer ["+threadController.getPeerID()+"] is choked by ["+peerID+"]");
						break;
					
					case Constants.MESSAGE_HAVE:
						peer2PeerMsg = (messageDefine)message;
						handleHaveMsg(peer2PeerMsg);
						break;
						
					case Constants.MESSAGE_INTERESTED:
						peer2PeerMsg = (messageDefine)message;
						receiveInterestMsg(peer2PeerMsg);
						break;
					
					case Constants.MESSAGE_NOT_INTERESTED:
						peer2PeerMsg = (messageDefine)message;
						receiveNoInterestMsg(peer2PeerMsg);
						break;
						
					case Constants.MESSAGE_PIECE:
						peer2PeerMsg = (messageDefine)message;
						getPieceMsg(peer2PeerMsg);
						break;
						
					case Constants.MESSAGE_UNCHOKE:
						peer2PeerMsg = (messageDefine)message;
						peerChoked = false;
						logs.info("Peer ["+threadController.getPeerID()+"] is unchoked by ["+peerID+"]");
						try 
						{
							pieceRequester.msgQ.put(peer2PeerMsg);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						break;
					
					case Constants.MESSAGE_SHUTDOWN:
						peer2PeerMsg = (messageDefine)message;
						threadController.peerList.add(peerID);
						break;
				}
			}
		}
		catch(SocketException e){
			System.out.println("Connection Reset.!!");
		}
		catch (EOFException e) {
			System.out.println("Peer "+peerID+" Disconnected.!!");
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
	
	
	public synchronized boolean sendBitFieldForHandshake()
	{
		try 
		{
			handshake msg = (handshake)InputDataStream.readObject();
			
			peerID = msg.getPeerID();

			Thread.sleep(4000);
			
			checkHandshakeMsg(msg);	

			return true;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 

		return false;
	}
	
	
	private void getPieceMsg(messageDefine pieceMsg)
	{
		threadController.saveDownloadedPiece(pieceMsg, peerID);
		threadController.sendHavePeiceMessage(pieceMsg.getPieceIndex(),peerID);
		
		dataSize += pieceMsg.getData().getSize();
		
		setLastPieceMessageReceived(true);

		try 
		{
			pieceRequester.msgQ.put(pieceMsg);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

	
	private void handleP2PMsg(messageDefine peer2PeerMsg) 
	{	
		try 
		{
			pieceRequester.msgQ.put(peer2PeerMsg);
			
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

	
	private void checkHandshakeMsg(handshake handshakeMsg)
	{	
		peerID = handshakeMsg.getPeerID();
		sendBitField();
		
		if(handShakeMessageSent == false)
		{
			logs.info("Peer "+threadController.getPeerID()+" is now connected to Peer "+peerID+".");
			sendHandshakeMsg();
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
	

	private void handleRequestMsg(messageDefine requestMsg)
	{
		if(choked == false)
		{
			messageDefine pieceMessage = threadController.getPieceMessage(requestMsg.getPieceIndex());
			
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
	
	
	private void handleHaveMsg(messageDefine haveMsg)
	{
		logs.info("Peer ["+threadController.getPeerID()+"] received 'HAVE' message from ["+peerID+"] for piece: "+haveMsg.getPieceIndex());
		
		try 
		{
			pieceRequester.msgQ.put(haveMsg);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	private void receiveInterestMsg(messageDefine interestedMessage)
	{
		logs.info("Peer ["+threadController.getPeerID()+"] received 'INTERESTED' message from ["+peerID+"]");
	}
	
	
	private void receiveNoInterestMsg(messageDefine message)
	{
		logs.info("Peer ["+threadController.getPeerID()+"] received 'NOT INTERESTED' message from ["+peerID+"]");
	}
	
	
	synchronized boolean sendHandshakeMsg()
	{
		try 
		{
			handshake message = handshake.createNewInstance();
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
	
	
	public void sendInterestMsg(messageDefine interestedMessage)
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
	
	
	public boolean checkDownload()
	{
		if(chunkRequestStarted == true)
		{
			return pieceRequester.checkNeighbourDownloadComplete();
		}
		else
		{
			return false;
		}
	}
	
	
	public void sendNoInterestMsg(messageDefine notInterestedMessage)
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
	
	
	public void sendRequestMsg(messageDefine requestMessage)
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
	
	
	public void sendChokeMsg(messageDefine chokeMessage) 
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
	
	
	public void sendUnchokeMsg(messageDefine unchokeMessage)
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
	
	
	public void sendHaveMsg(messageDefine haveMsg) 
	{
		try 
		{
			peerMessageSender.messageSend(haveMsg);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendShutDownMsg(messageDefine shutdownMsg)
	{
		try 
		{
			peerMessageSender.messageSend(shutdownMsg);
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
	
	
	public boolean lastPieceMessageReceived() 
	{
		return pieceMessageForLastMessageReceived;
	}

	
	public void setLastPieceMessageReceived(boolean isPieceMessageForPreviousMessageReceived)
	{
		this.pieceMessageForLastMessageReceived = isPieceMessageForPreviousMessageReceived;
	}
	
	
	public double getDownSpeed()
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
