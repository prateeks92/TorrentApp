import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import peer2peer.Starter;

public class PeerHandle implements Runnable{
	
	
	public static final String LOGGER_PREFIX = PeerHandle.class.getSimpleName();
	
	public String peerID;
	

	private Socket neighborSocket;
	
	
	private ObjectInputStream InputDataStream;
	
	
	public ObjectOutputStream OutputDataStream;
	
	public boolean isChoked;
	
	public boolean isPeerChoked;
	
	public boolean isHandshakeACKReceived = false;
	
	public boolean isHandShakeMessageSent = false;
	
	public boolean isChunkRequestedStarted = false;
	public MessageIdentifier messageIdentifier;

	public Starter threadController;
	
	public PeerMessageSender peerMessageSender;
	
	public ChunkRequester chunkRequester;
	
	public boolean isPieceMessageForPreviousMessageReceived = true;
	
	public long downloadStartTime;
	
	public int downloadDataSize;
	
	public SimpleLogger logger = null;
	
	private PeerHandle(){
		
	}
	synchronized public static PeerHandle createPeerConnection(Socket socket, Starter controller){
		PeerHandle peerHandler = new PeerHandle();
		
		peerHandler.neighborSocket = socket;
		peerHandler.threadController = controller;
		
		boolean isInitialized = false;
		//peerHandler.initializeDataStreams(controller);
		if(peerHandler.neighborSocket == null){
			
			peerHandler.close();
			peerHandler = null;
			return null;
		}
		
		try {
			
			peerHandler.OutputDataStream = new ObjectOutputStream(peerHandler.neighborSocket.getOutputStream());
			peerHandler.InputDataStream = new ObjectInputStream(peerHandler.neighborSocket.getInputStream());			
		} catch (IOException e) {
			e.printStackTrace();
			peerHandler.close();
			peerHandler = null;
			return null;
		}
		
		peerHandler.messageIdentifier = MessageIdentifier.createIdentfier();
		
		if(peerHandler.messageIdentifier == null){
			peerHandler.close();
			return null;
		}
		
		if(controller == null){
			peerHandler.close();
			return null;
		}
		
		peerHandler.peerMessageSender = PeerMessageSender.createInstance(peerHandler.OutputDataStream,peerHandler);
		
		if(peerHandler.peerMessageSender == null){
			peerHandler.close();
			return null;
		}
		
		new Thread(peerHandler.peerMessageSender).start();

		peerHandler.chunkRequester = ChunkRequester.createInstance(controller, peerHandler);

		peerHandler.logger = controller.getLogger();
		isInitialized=true;
		
		if(isInitialized == false){
			peerHandler.close();
			peerHandler = null;
		}
		return peerHandler;
	}
	
	
	synchronized public void close(){
		try {
			if(InputDataStream != null){
				InputDataStream.close();
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void run(){
		byte[] rawData = new byte[Constants.RAW_DATA_SIZE];
		ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_MESSAGE_SIZE);
		
		if(peerID != null){
			sendHandshakeMessage();
		}

		
		try {
			
			while(true){
	// CONTINUE READING THE INCOMING MESSAGES ON THIS CONECTION			
				Message message = (Message)InputDataStream.readObject();
				
				
				if(message.returnMsgType() == Constants.HANDSHAKE_MESSAGE){					
					if(message instanceof HandshakeMessage){						
						HandshakeMessage handshakeMessage = (HandshakeMessage)message;
						checkHandshakeMessage(handshakeMessage);
					}else{
						// send some invalid data
						System.out.println("Not a Handshake message");
					}
				}else if(message.returnMsgType() == Constants.REQUEST_MESSAGE){					
					MsgDetails peer2PeerMessage = (MsgDetails)message; 
					handleRequestMessage(peer2PeerMessage);
				}else if(message.returnMsgType() == Constants.BITFIELD_MESSAGE){					
					handleBFMessage((MsgDetails)message);
				}else if(message.returnMsgType() == Constants.CHOKE_MESSAGE){
					MsgDetails peer2PeerMessage = (MsgDetails)message;
					isPeerChoked=true;
				}else if(message.returnMsgType() == Constants.HAVE_MESSAGE){
					MsgDetails peer2PeerMessage = (MsgDetails)message;
					handleHaveMessage(peer2PeerMessage);
				}else if(message.returnMsgType() == Constants.INTERESTED_MESSAGE){
					MsgDetails peer2PeerMessage = (MsgDetails)message;
					receiveInterestedMessage(peer2PeerMessage);
				}else if(message.returnMsgType() == Constants.NOT_INTERESTED_MESSAGE){
					MsgDetails peer2PeerMessage = (MsgDetails)message;
					receiveNotInterestedMessage(peer2PeerMessage);
				}else if(message.returnMsgType() == Constants.PIECE_MESSAGE){
					MsgDetails peer2PeerMessage = (MsgDetails)message;
					
					receivePieceMessage(peer2PeerMessage);
				}else if(message.returnMsgType() == Constants.UNCHOKE_MESSAGE){
					MsgDetails peer2PeerMessage = (MsgDetails)message;
			
					isPeerChoked = false;

					logger.info("Peer ["+threadController.getPeerID()+"] is unchoked by ["+peerID+"]");
					
					try {
						chunkRequester.messageQueue.put(peer2PeerMessage);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(message.returnMsgType() == Constants.SHUTDOWN_MESSAGE){
					MsgDetails peer2peerMessage = (MsgDetails)message;
					
					threadController.peerList.add(peerID);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized boolean sendHandshakeandBitField(){
		try {
			
			HandshakeMessage message = (HandshakeMessage)InputDataStream.readObject();
			
			peerID = message.getPeerID();

			Thread.sleep(4000);
			
			checkHandshakeMessage(message);
			
			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return false;
	}
	
	private void receivePieceMessage(MsgDetails pieceMessage) {	
		
		threadController.saveDownloadedPiece(pieceMessage, peerID);
		threadController.sendHaveMessage(pieceMessage.getPieceIndex(),peerID);
		
		downloadDataSize += pieceMessage.getData().getSize();
		
		setPieceMessageForPreviousMessageReceived(true);

		try {			
			chunkRequester.messageQueue.put(pieceMessage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleBFMessage(MsgDetails peer2PeerMessage) {
		
		try {
			chunkRequester.messageQueue.put(peer2PeerMessage);
			
			if(isHandshakeACKReceived == true && isHandShakeMessageSent == true && isChunkRequestedStarted == false){

				new Thread(chunkRequester).start();
				//START MEASURING THE DOWNLOAD TIME FOR THE FILE
				downloadStartTime = System.currentTimeMillis();
				downloadDataSize = 0;

				setChunkRequestedStarted(true) ;
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkHandshakeMessage(HandshakeMessage handshakeMessage){
		
		peerID = handshakeMessage.getPeerID();
		sendBitField();
		
		if(isHandShakeMessageSent == false){
			logger.info("Peer "+threadController.getPeerID()+" is connected from Peer "+peerID+".");
			sendHandshakeMessage();
		}
		
		isHandshakeACKReceived = true;		
		
		if(isHandshakeACKReceived == true && isHandShakeMessageSent == true && isChunkRequestedStarted == false){

			new Thread(chunkRequester).start();
			//START MEASURING THE DOWNLOAD TIME FOR THE FILE
			downloadStartTime = System.currentTimeMillis();
			downloadDataSize = 0;

			
			setChunkRequestedStarted(true);
		}
	}
	
	private void handleRequestMessage(MsgDetails requestMessage){

		if(isChoked == false){
			MsgDetails pieceMessage = threadController.getPieceMessage(requestMessage.getPieceIndex());
			
			if(pieceMessage != null){
				try {
					Thread.sleep(2000);

					peerMessageSender.sendMessage(pieceMessage);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void handleHaveMessage(MsgDetails haveMessage){
		logger.info("Peer ["+threadController.getPeerID()+"] received the 'have' message from ["+peerID+"] for the piece"+haveMessage.getPieceIndex());
		
		try {
			chunkRequester.messageQueue.put(haveMessage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void receiveInterestedMessage(MsgDetails interestedMessage){
		logger.info("Peer ["+threadController.getPeerID()+"] received the 'interested' message from ["+peerID+"]");

	}
	
	private void receiveNotInterestedMessage(MsgDetails message){
		logger.info("Peer ["+threadController.getPeerID()+"] received the 'not interested' message from ["+peerID+"]");

	}
	synchronized boolean sendHandshakeMessage(){
		try {
			HandshakeMessage message = HandshakeMessage.createInstance();
			message.setPeerID(threadController.getPeerID());
			peerMessageSender.sendMessage(message);
			isHandShakeMessageSent = true;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	synchronized boolean sendBitField(){
		try {		
			
			MsgDetails message = threadController.getBitFieldMessage();
			
			peerMessageSender.sendMessage(message);

			Thread.sleep(2000);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	

	public void sendInterestedMessage(MsgDetails interestedMessage){
		try {
			if(isPeerChoked == false){
				peerMessageSender.sendMessage(interestedMessage);
			}			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean checkIfDownloadComplete()
	{
		if(isChunkRequestedStarted == true){
			return chunkRequester.checkIfNeighbourDownloadFile();
		}else{
			return false;
		}
	}
	public void sendNotInterestedMessage(MsgDetails notInterestedMessage){

		try {
			peerMessageSender.sendMessage(notInterestedMessage);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestMessage(MsgDetails requestMessage){
		try {
			if(isPeerChoked == false){
				peerMessageSender.sendMessage(requestMessage);
			}			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void sendHaveMessage(MsgDetails haveMessage) {
		try {
			peerMessageSender.sendMessage(haveMessage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendShutdownMessage(MsgDetails shutdownMessage)
	{

		try {
			peerMessageSender.sendMessage(shutdownMessage);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	

	private void setChoke(boolean message)
	{
		isChoked = message;
	}
	

	
	synchronized public void setPeerID(String peerID) {
		this.peerID = peerID;
		
	}
	
	public boolean isPieceMessageForPreviousMessageReceived() {
		return isPieceMessageForPreviousMessageReceived;
	}

	public void setPieceMessageForPreviousMessageReceived(
			boolean isPieceMessageForPreviousMessageReceived) {
		this.isPieceMessageForPreviousMessageReceived = isPieceMessageForPreviousMessageReceived;
	}
	
	public double getDownloadSpeed(){
		long timePeriod = System.currentTimeMillis() - downloadStartTime;
		if(timePeriod != 0){
			return ((downloadDataSize * 1.0) / (timePeriod * 1.0) );
		}else{
			return 0;
		} 
	}
	


	public void setHandshakeMessageReceived(boolean isHandshakeACKReceived) {
		this.isHandshakeACKReceived = isHandshakeACKReceived;
	}



	public synchronized void setChunkRequestedStarted(boolean isChunkRequestedStarted) {
		this.isChunkRequestedStarted = isChunkRequestedStarted;
	}

	
	
}
