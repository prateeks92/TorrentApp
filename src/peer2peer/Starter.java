package peer2peer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import chokehandler.chokeUnchoke;
import chokehandler.unchoking;
import logging.*;
import messages.*;
import property.*;

public class Starter {

	public static String LOGGER_PREFIX = Starter.class.getSimpleName();

	public ArrayList<String> peerList = new ArrayList<String>();
	public MessageIdentifier messageIdentifier = null;
	public ChunkHandler msgHandler = null;
	
	public static Starter starter = null;

	public ArrayList<PeerHandle> neighborThreads = null;
	public chokeUnchoke chokeUnchokeHandler = null;

	public unchoking unchokingHandler = null;

	public Server peerServer;

	public logger logs = null;
	

	public PeerProperties propertyHandler = null;

	public boolean AllPeersConnected = false;

	public String getPeerID() 
	{
		return peerID;
	}

	public String peerID;

	ArrayList<String> chokedPeers = new ArrayList<String>();
	
	public static synchronized Starter setUpPeer(String peerID) 
	{
		if (starter == null) 
		{
			starter = new Starter();
			starter.peerID = peerID;
			boolean isInitialized=false;
			starter.propertyHandler = PeerProperties.createInstance();

			if (starter.propertyHandler == null)
			{
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.messageIdentifier = MessageIdentifier.createIdentfier();
			
			if (starter.messageIdentifier == null)
			{
				isInitialized = false;
				starter=null;
				return null;
			}

			if (PeerProperties.createInstance().getPeerInfoMap().get(peerID).checkIfTheFileExits() == false)
			{
				starter.msgHandler = ChunkHandler.createChunkHanlder(false, peerID);
			} 
			else 
			{
				starter.msgHandler = ChunkHandler.createChunkHanlder(true, peerID);
			}

			if (starter.msgHandler == null) 
			{
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.neighborThreads = new ArrayList<PeerHandle>();

			starter.logs = logger.getLogger(peerID);
			if (starter.logs == null)
			{
				System.out.println("Unable to Initialize logger object");	
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.peerServer = Server.createInstance(peerID, starter);

			starter.logs = logger.getLogger(peerID);

			isInitialized=true;

			if (isInitialized == false) 
			{
				starter = null;
			}
		}
		
		return starter;
	}

	public void startThread() 
	{
		new Thread(peerServer).start();
	
		connectPeers();

		chokeUnchokeHandler = chokeUnchoke.createInstance(this);
		
		int chokeUnchokeInterval = Integer.parseInt(ConfigTokens.returnPropertyValue(Constants.CHOKE_UNCHOKE_INTERVAL));
		chokeUnchokeHandler.start(0, chokeUnchokeInterval);

		unchokingHandler= unchoking.createInstance(this);
		int unchokeInterval = Integer.parseInt(ConfigTokens.returnPropertyValue(Constants.OPTIMISTIC_UNCHOKE_INTERVAL));
	
		unchokingHandler.task = unchokingHandler.task_scheduler.scheduleAtFixedRate(optimisticUnchokeManager, 10, optimisticUnchokeInterval, TimeUnit.SECONDS);
	}

	private void connectPeers() 
	{
		HashMap<String, PeerDetails> neighborPeers = propertyHandler.getPeerInfoMap();
		
		Set<String> peerIDList = neighborPeers.keySet();

		for (String neighborPeerID : peerIDList) 
		{		
			if (Integer.parseInt(neighborPeerID) < Integer.parseInt(peerID)) 
			{
				logger.info("Peer " + peerID + " makes a connection  to Peer [" + neighborPeerID + "]");
				
				PeerDetails details= neighborPeers.get(neighborPeerID);
				String neighborPeerHost = details.getHostAddress();
				int neighborPortNumber = details.getPortNumber();

				try 
				{
					Socket neighborPeerSocket = new Socket(neighborPeerHost, neighborPortNumber);

					PeerHandle neighborPeerHandler = PeerHandle.createPeerConnection(neighborPeerSocket, this);

					neighborPeerHandler.setPeerID(details.returnPeerId());

					neighborThreads.add(neighborPeerHandler);

					new Thread(neighborPeerHandler).start();
				}
				catch (UnknownHostException e) 
				{
					e.printStackTrace();
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		this.AllPeersConnected = true;
	}

	public boolean checkAllPeersFileDownloadComplete() 
	{
		System.out.println("check all peers file download"+ peerID);
		if (AllPeersConnected == false || peerServer.isPeerServerCompleted == false)
		{
			return false;
		}

		if (propertyHandler.getPeerInfoMap().size() == peerList.size())
		{
			System.out.println(peerList.size());
			chokeUnchokeHandler.task.cancel(true);
			unchokingHandler.task.cancel(true);
			logs.close();
			msgHandler.close();
			System.out.println("Exit");
			try{
			System.exit(0);
			}
			catch(Exception e) {
			System.out.println("ERROR");
			}
		}

		return false;
	}


	public synchronized MsgDetails getBitFieldMessage()
	{

		MsgDetails message = MsgDetails.createInstance();

		message.setHandler(msgHandler.returnBitFieldHandler());
	
		if (message.returnBitFieldHandler() == null)
		{
			
		}
		message.setMessgageType(Constants.MESSAGE_BITFIELD);

		return message;
	}

	public HashMap<String, Double> PeerDownloadSpeeds() 
	{
		HashMap<String, Double> peerSpeeds = new HashMap<String, Double>();

		for (PeerHandle peerHandler : neighborThreads)
		{
			peerSpeeds.put(peerHandler.peerID, peerHandler.getDownloadSpeed());
		}
		return peerSpeeds;
	}

	public void chokeThePeers(ArrayList<String> peerList)
	{
		chokedPeers = peerList;
		MsgDetails chokeMessage = MsgDetails.createInstance();
		chokeMessage.setMessgageType(Constants.MESSAGE_CHOKE);

		for (String peerToBeChoked : peerList) 
		{
			for (PeerHandle peerHandler : neighborThreads)
			{
				if (peerHandler.peerID.equals(peerToBeChoked)) 
				{
					if (peerHandler.isHandshakeACKReceived == true) 
					{	
						peerHandler.sendChokeMessage(chokeMessage);
					}
					break;
				}
			}
		}
	}

	
	public void unchokePeers(ArrayList<String> peerList)
	{
		MsgDetails unChokeMessage = MsgDetails.createInstance();
		unChokeMessage.setMessgageType(Constants.MESSAGE_UNCHOKE);
		
		for (String peerToUnchoke : peerList) 
		{
			for (PeerHandle peerHandler : neighborThreads) 
			{
				if (peerHandler.peerID.equals(peerToUnchoke))
				{
					if (peerHandler.isHandshakeACKReceived == true) 
					{
						peerHandler.sendUnchokeMessage(unChokeMessage);
					}
					break;
				}
			}
		}
	}

	
	public void unchokePeer(String peerToUnchoke) 
	{
		MsgDetails unChokeMessage = MsgDetails.createInstance();
		unChokeMessage.setMessgageType(Constants.UNCHOKE_MESSAGE);

		logger.info("Peer [" + peerID + "] has unchoked neighbor [" + peerToUnchoke + "]");
		
		for (PeerHandle peerHandler : neighborThreads) 
		{
			if (peerHandler.peerID.equals(peerToUnchoke))
			{
				if (peerHandler.isHandshakeACKReceived == true) 
				{
					peerHandler.sendUnchokeMessage(unChokeMessage);
				}
				break;
			}
		}
	}


	public synchronized void saveDownloadedPiece(MsgDetails pieceMessage, String sourcePeerID)
	{		
		msgHandler.writePieceToPeer(pieceMessage.getPieceIndex(), pieceMessage.getData());
		logger.info("Peer [" + starter.getPeerID() + "] has downloaded the piece [" + pieceMessage.getPieceIndex() + "] from [" + sourcePeerID + "]. Now the number of pieces it has is " + (msgHandler.returnBitFieldHandler().getSetbitCount()));
	}

	public int[] missingPieceIndex() 
	{
		return msgHandler.getMissingPieceNumberArray();
	}

	public MsgDetails getPieceMessage(int pieceIndex)
	{
		Chunk piece = msgHandler.getdata(pieceIndex);
		
		if (piece == null) 
		{
			return null;
		}
		else 
		{
			MsgDetails message = MsgDetails.createInstance();
			message.setMessgageType(Constants.MESSAGE_PIECE);
			message.setPieceIndex(pieceIndex);
			message.setData(piece);
			return message;
		}
	}


	public void sendHavePeiceMessage(int pieceIndex, String fromPeerID)
	{
		MsgDetails haveMessage = MsgDetails.createInstance();
		haveMessage.setPieceIndex(pieceIndex);
		haveMessage.setMessgageType(Constants.MESSAGE_HAVE);

		for (PeerHandle peerHandler : neighborThreads) {
			
			if (fromPeerID.equals(peerHandler.peerID) == false) {
				peerHandler.sendHaveMessage(haveMessage);
			}
		}
	}

	
	public void broadcastShutdown() {
		if (AllPeersConnected == false || peerServer.isPeerServerCompleted == false) {

			return;
		}

		MsgDetails shutdownMessage = MsgDetails.createInstance();

		shutdownMessage.setMessgageType(Constants.MESSAGE_SHUTDOWN);
		
		peerList.add(peerID);
	
		for (PeerHandle peerHandler : neighborThreads) 
		{	
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
	}

	
	public int numberOfPeersToBeConnected() {
		HashMap<String, PeerDetails> neighborPeerMap = propertyHandler.getPeerInfoMap();
		Set<String> peerIDList = neighborPeerMap.keySet();

		int numberOfPeersToEstablishConnection = 0;

		for (String neighborPeerID : peerIDList) 
		{	
			if (Integer.parseInt(neighborPeerID) > Integer.parseInt(peerID)) 
			{
				numberOfPeersToEstablishConnection++;
			}
		}

		return numberOfPeersToEstablishConnection;
	}

	public synchronized logger getLogger() 
	{
		return logs;
	}
}