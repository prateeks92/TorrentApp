package peer2peer;


import MsgDetails;
import PeerDetails;
import PeerHandle;
import Server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import logger.SimpleLogger;
import Messages.Chunk;
import Messages.ChunkHandler;
import Messages.MessageIdentifier;
import PropertyReader.ConfigTokens;
import PropertyReader.Configurations;
import PropertyReader.Constants;

/**
 * The Class starter.
 * 
 */
public class Starter {

	public static String LOGGER_PREFIX = Starter.class.getSimpleName();

	public ArrayList<String> peerList = new ArrayList<String>();
	public MessageIdentifier messageIdentifier = null;
	public ChunkHandler msgHandler = null;
	
	public static Starter starter = null;

	public ArrayList<PeerHandle> neighborThreads = null;
	public Server peerServer;

	public SimpleLogger logger = null;
	

	/** The peer configuration reader. */
	public Configurations propertyReader = null;

	public boolean AllPeerConnected = false;

	public String getPeerID() {
		return peerID;
	}

	/** The peer id. */
	public String peerID;
	

	public static synchronized Starter setUpPeer(String peerID) {
		if (starter == null) {
			starter = new Starter();
			starter.peerID = peerID;
			boolean isInitialized=false;
			starter.propertyReader = Configurations.createInstance();

			if (starter.propertyReader == null) {
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.messageIdentifier = MessageIdentifier.createIdentfier();
			if (starter.messageIdentifier == null) {
				isInitialized = false;
				starter=null;
				return null;
			}

			if (Configurations.createInstance().getPeerInfoMap().get(peerID).checkIfTheFileExits() == false) {
				starter.msgHandler = ChunkHandler.createChunkHanlder(false, peerID);
			} else {
				starter.msgHandler = ChunkHandler.createChunkHanlder(true, peerID);
			}

			if (starter.msgHandler == null) {
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.neighborThreads = new ArrayList<PeerHandle>();

			starter.logger = SimpleLogger.getLogger(peerID);
			if (starter.logger == null) {
				System.out.println("Unable to Initialize logger object");
				
				isInitialized = false;
				starter=null;
				return null;
			}

			starter.peerServer = Server.createInstance(peerID, starter);

			starter.logger = SimpleLogger.getLogger(peerID);

			isInitialized=true;

			if (isInitialized == false) {
				
				starter = null;
			}
		}
		return starter;
	}

	public void startThread() {
		//START THE THREAD AND INTIALIZE THE CHOKE UNCHOKE PROPERTIES
		
		new Thread(peerServer).start();
		
		//Now connect to all other Active Peers
		connectOtherPeers();
		}

	private void connectOtherPeers() {
		HashMap<String, PeerDetails> neighborPeerMap = propertyReader.getPeerInfoMap();
		Set<String> peerIDList = neighborPeerMap.keySet();

		for (String neighborPeerID : peerIDList) {
		
			if (Integer.parseInt(neighborPeerID) < Integer.parseInt(peerID)) {
				logger.info("Peer " + peerID + " makes a connection  to Peer [" + neighborPeerID + "]");
				
				PeerDetails details= neighborPeerMap.get(neighborPeerID);
				String neighborPeerHost = details.getHostAddress();
				int neighborPortNumber = details.getPortNumber();

				try {
					Socket neighborPeerSocket = new Socket(neighborPeerHost, neighborPortNumber);

					PeerHandle neighborPeerHandler = PeerHandle.createPeerConnection(neighborPeerSocket, this);

					neighborPeerHandler.setPeerID(details.returnPeerId());

					neighborThreads.add(neighborPeerHandler);

					new Thread(neighborPeerHandler).start();

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.AllPeerConnected = true;
		
	}

	
	public boolean checkAllPeersFileDownloadComplete() {

		System.out.println("check all peers file download"+ peerID);
		if (AllPeerConnected == false || peerServer.isPeerServerCompleted == false) {

			return false;
		}

		if (propertyReader.getPeerInfoMap().size() == peerList.size()) {
			System.out.println(peerList.size());
			logger.close();
			msgHandler.close();
			System.out.println("EXITT..!!");
			try{
			System.exit(0);
			}
			catch(Exception e) {
			System.out.println("ERROR");
			}
		}

		return false;
	}

	
	

	


	public synchronized MsgDetails getBitFieldMessage() {

		MsgDetails message = MsgDetails.createInstance();

		message.setHandler(msgHandler.returnBitFieldHandler());
		if (message.returnBitFieldHandler() == null) {
			
		}
		message.setMessgageType(Constants.BITFIELD_MESSAGE);

		return message;
	}

	public HashMap<String, Double> returnDownloadSpeedForPeers() {


		HashMap<String, Double> peerSpeedList = new HashMap<String, Double>();

		for (PeerHandle peerHandler : neighborThreads) {
			peerSpeedList.put(peerHandler.peerID, peerHandler.getDownloadSpeed());
		}
		return peerSpeedList;
	}

	

	public synchronized void saveDownloadedPiece(MsgDetails pieceMessage, String sourcePeerID) {		
		msgHandler.writePieceToPeer(pieceMessage.getPieceIndex(), pieceMessage.getData());
		logger.info("Peer [" + starter.getPeerID() + "] has downloaded the piece [" + pieceMessage.getPieceIndex() + "] from [" + sourcePeerID + "]. Now the number of pieces it has is " + (msgHandler.returnBitFieldHandler().getSetbitCount()));
	}

	public int[] getMissingPieceIndexArray() {
		return msgHandler.getMissingPieceNumberArray();
	}

	public MsgDetails getPieceMessage(int pieceIndex) {
		Chunk piece = msgHandler.getdata(pieceIndex);
		if (piece == null) {
			return null;
		} else {
			MsgDetails message = MsgDetails.createInstance();
			message.setMessgageType(Constants.PIECE_MESSAGE);
			message.setPieceIndex(pieceIndex);
			message.setData(piece);
			return message;
		}
	}


	public void sendHaveMessage(int pieceIndex, String fromPeerID) {
		MsgDetails haveMessage = MsgDetails.createInstance();
		haveMessage.setPieceIndex(pieceIndex);
		haveMessage.setMessgageType(Constants.HAVE_MESSAGE);

		for (PeerHandle peerHandler : neighborThreads) {
			
			if (fromPeerID.equals(peerHandler.peerID) == false) {
				peerHandler.sendHaveMessage(haveMessage);
			}
		}
	}

	public void broadcastShutdownMessage() {
		if (AllPeerConnected == false || peerServer.isPeerServerCompleted == false) {

			return;
		}

		MsgDetails shutdownMessage = MsgDetails.createInstance();

		shutdownMessage.setMessgageType(Constants.SHUTDOWN_MESSAGE);

		
		peerList.add(peerID);
	
		for (PeerHandle peerHandler : neighborThreads) {
			
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
	}

	public int getNumberOfPeersSupposedToBeConnected() {
		HashMap<String, PeerDetails> neighborPeerMap = propertyReader.getPeerInfoMap();
		Set<String> peerIDList = neighborPeerMap.keySet();

		int numberOfPeersSupposedToBeEstablishingConnection = 0;

		for (String neighborPeerID : peerIDList) {
			
			if (Integer.parseInt(neighborPeerID) > Integer.parseInt(peerID)) {
				numberOfPeersSupposedToBeEstablishingConnection++;
			}
		}

		return numberOfPeersSupposedToBeEstablishingConnection;
	}

	public synchronized SimpleLogger getLogger() {
		return logger;
	}
}
