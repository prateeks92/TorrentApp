import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Server implements Runnable{
	
	public static String LOGGER_PREFIX = Server.class.getSimpleName();
	String peerID;
	
	
	public Configurations configHandler = null;
	
	
	public static Server servr = null;
	
	
	
	
	public boolean isPeerServerCompleted = false;
	
	public SimpleLogger simpleLogger = null;
	
   public ServerSocket serverSocket = null;

	
	public String ID;
	public Starter controller;
	public static Server createInstance(String id, Starter controller){
		if(servr == null){
			
			servr = new Server();
			servr.ID = id;
			servr.controller = controller;
			
			boolean isInitialized = servr.initialize(controller);
			if(isInitialized == false){
				
				servr = null;
			}
		}
		return servr;
	}
	
	public boolean initialize(Starter controller){
		
		simpleLogger = controller.getLogger();
		
		configHandler = Configurations.createInstance();
		
		if(configHandler == null){
			return false;
		}
		
		return true;
	}
	
	public void run() {
		HashMap<String,PeerDetails> peerDetailsMap = configHandler.getPeerInfoMap();
		PeerDetails serverPeerInfo = peerDetailsMap.get(ID);
		
		int peerServerPortNumber = serverPeerInfo.getPortNumber();
		
		try {
			
			serverSocket = new ServerSocket(peerServerPortNumber);
			
			int numberOfPeersSupposedToBeConnected = controller.getNumberOfPeersSupposedToBeConnected();
			
			for(int i=0 ; i<numberOfPeersSupposedToBeConnected ; i++){
				
				Socket neighborPeerSocket = serverSocket.accept();
				
				PeerHandle neighborPeerHandler = PeerHandle.createPeerConnection(neighborPeerSocket, controller);
				
				controller.neighborThreads.add(neighborPeerHandler);
				//(neighborPeerHandler);
				
				new Thread(neighborPeerHandler).start();
			}					
			
			isPeerServerCompleted = true;
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}



}
