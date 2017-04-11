package peer2peer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import chokehandler.chokeUnchoke;
import chokehandler.unchoking;
import logging.*;
import messaging.*;
import property.*;
import peer2peer.*;
import beanClasses.*;


public class Starter {

	public static String LOGGER_PREFIX = Starter.class.getSimpleName();

	public ArrayList<String> peerList = new ArrayList<String>();
	public messageID messageIdentifier = null;
	public Piece_Processor msgHandler = null;
	
	public static Starter starter = null;

	public ArrayList<Peer> neighborThreads = null;
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

	public ArrayList<String> chokedPeers = new ArrayList<String>();
	
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

			starter.messageIdentifier = messageID.createIdentfier();
			
			if (starter.messageIdentifier == null)
			{
				isInitialized = false;
				starter=null;
				return null;
			}

			if (PeerProperties.createInstance().getPeerInfoMap().get(peerID).fileExist() == false)
			{
				starter.msgHandler = Piece_Processor.createPieceHanlder(false, peerID);
			} 
			else 
			{
				starter.msgHandler = Piece_Processor.createPieceHanlder(true, peerID);
			}

			if (starter.msgHandler == null) 
			{
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.neighborThreads = new ArrayList<Peer>();

			starter.logs = logger.getLogger(peerID);
			if (starter.logs == null)
			{
				System.out.println("logger initialization error");	
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.peerServer = Server.initialize(peerID, starter);

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
		
		int chokeUnchokeInterval = Integer.parseInt(PeerPropertyTokens.returnPropertyValue(Constants.CHOKE_UNCHOKE_INTERVAL));
		chokeUnchokeHandler.start(0, chokeUnchokeInterval);

		unchokingHandler= unchoking.createInstance(this);
		int unchokeInterval = Integer.parseInt(PeerPropertyTokens.returnPropertyValue(Constants.OPTIMISTIC_UNCHOKE_INTERVAL));
	
		unchokingHandler.task = unchokingHandler.task_scheduler.scheduleAtFixedRate(unchokingHandler, 10, unchokeInterval, TimeUnit.SECONDS);
	}

	private void connectPeers() 
	{		
		Set<String> peerIDList = propertyHandler.getPeerInfoMap().keySet();

		for (String neighborPeerID : peerIDList) 
		{		
			if (Integer.parseInt(neighborPeerID) < Integer.parseInt(peerID)) 
			{
				logs.info("Peer with peerID " + peerID + " is making a connection to PeerID [" + neighborPeerID + "]");
				
				PeerDetail details= propertyHandler.getPeerInfoMap().get(neighborPeerID);
				String neighborPeerHost = details.getPeerAddress();
				int neighborPortNumber = details.getPeerPort();

				try 
				{
					Socket neighborPeerSocket = new Socket(neighborPeerHost, neighborPortNumber);

					Peer neighborPeerHandler = Peer.createPeerConnection(neighborPeerSocket, this);

					neighborPeerHandler.setPeerID(details.getPeerID());

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
		System.out.println("Now Checking All Peers for Download with PeerID "+ peerID);
		if (AllPeersConnected == false || peerServer.isServComplete == false)
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


	public synchronized messageDefine getBitFieldMessage()
	{

		messageDefine message = messageDefine.createInstance();

		message.setHandler(msgHandler.returnBitFieldProcessor());
	
		if (message.returnBitFieldHandler() == null)
		{
			
		}
		message.setMessgageType(Constants.MESSAGE_BITFIELD);

		return message;
	}

	public HashMap<String, Double> PeerDownloadSpeeds() 
	{
		HashMap<String, Double> peerSpeeds = new HashMap<String, Double>();

		for (Peer peerHandler : neighborThreads)
		{
			peerSpeeds.put(peerHandler.peerID, peerHandler.getDownloadSpeed());
		}
		return peerSpeeds;
	}
	
public int numberOfPeersToBeConnected() {
		
		Set<String> peerIDs= propertyHandler.getPeerInfoMap().keySet();

		int countPeer = 0;

		for (String neighborPeerID : peerIDs) 
		{	
			if (Integer.valueOf(neighborPeerID) > Integer.valueOf(peerID)) 
			{
				countPeer++;
			}
		}

		return countPeer;
	}

	public synchronized logger getLogger() 
	{
		return logs;
	}
	
	public int[] missingPieceIndex() 
	{
		return msgHandler.arrayMissingPieceNumberGetter();
	}

	public messageDefine getPieceMessage(int pieceIndex)
	{
		pieceDetails piece = msgHandler.getdata(pieceIndex);
		
		if (piece == null) 
		{
			return null;
		}
		else 
		{
			messageDefine message = messageDefine.createInstance();
			message.setMessgageType(Constants.MESSAGE_PIECE);
			message.setPieceIndex(pieceIndex);
			message.setData(piece);
			return message;
		}
	}

	public void chokeThePeers(ArrayList<String> peerList)
	{
		chokedPeers = peerList;
		messageDefine chokeMessage = messageDefine.createInstance();
		chokeMessage.setMessgageType(Constants.MESSAGE_CHOKE);

		for (String peerToBeChoked : peerList) 
		{
			for (Peer peerHandler : neighborThreads)
			{
				if (peerHandler.peerID.equals(peerToBeChoked)) 
				{
					if (peerHandler.handshakeACKReceived == true) 
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
		messageDefine unChokeMessage = messageDefine.createInstance();
		unChokeMessage.setMessgageType(Constants.MESSAGE_UNCHOKE);
		
		for (String peerToUnchoke : peerList) 
		{
			for (Peer peerHandler : neighborThreads) 
			{
				if (peerHandler.peerID.equals(peerToUnchoke))
				{
					if (peerHandler.handshakeACKReceived == true) 
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
		messageDefine unChokeMessage = messageDefine.createInstance();
		unChokeMessage.setMessgageType(Constants.MESSAGE_UNCHOKE);

		logs.info("Peer [" + peerID + "] has unchoked neighbor [" + peerToUnchoke + "]");
		
		for (Peer peerHandler : neighborThreads) 
		{
			if (peerHandler.peerID.equals(peerToUnchoke))
			{
				if (peerHandler.handshakeACKReceived == true) 
				{
					peerHandler.sendUnchokeMessage(unChokeMessage);
				}
				break;
			}
		}
	}


	public synchronized void saveDownloadedPiece(messageDefine pieceMessage, String sourcePeerID)
	{		
		msgHandler.PeerPieceWriter(pieceMessage.getPieceIndex(), pieceMessage.getData());
		logs.info("Peer [" + starter.getPeerID() + "] has downloaded the piece [" + pieceMessage.getPieceIndex() + "] from [" + sourcePeerID + "]. Now the number of pieces it has is " + (msgHandler.returnBitFieldProcessor().getSetbitCount()));
	}


	public void sendHavePeiceMessage(int pieceIndex, String fromPeerID)
	{
		messageDefine haveMessage = messageDefine.createInstance();
		haveMessage.setPieceIndex(pieceIndex);
		haveMessage.setMessgageType(Constants.MESSAGE_HAVE);

		for (Peer peerHandler : neighborThreads) {
			
			if (fromPeerID.equals(peerHandler.peerID) != true) {
				peerHandler.sendHaveMessage(haveMessage);
			}
		}
	}

	
	public void sendShutdownSignal() 
	{
		if (AllPeersConnected == false || peerServer.isServComplete == false) {

			return;
		}

		messageDefine shutdownMessage = messageDefine.createInstance();

		shutdownMessage.setMessgageType(Constants.MESSAGE_SHUTDOWN);
	
		for (Peer peerHandler : neighborThreads) 
		{	
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
		peerList.add(peerID);
	}
}