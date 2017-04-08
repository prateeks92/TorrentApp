package peer2peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import beanClasses.PeerDetail;

import property.PeerProperties;
import logging.logger;

public class Server implements Runnable{
	
	public static Server serverObj = null;
	public boolean isServComplete = false;
	public logger logger = null;
	public ServerSocket serverSocket = null;
	String peerID;	
	public PeerProperties peerPropHandler = null;
	public String ID;
	public Starter starter;
	
	public static Server initialize(String id, Starter controller){
		if(serverObj == null){
			
			serverObj = new Server();
			serverObj.ID = id;
			serverObj.starter = controller;
			
			boolean isInitialized = serverObj.peerPropInit(controller);
			if(isInitialized == false){
				
				serverObj = null;
			}
		}
		return serverObj;
	}
	
	public void run() {
		Map<String,PeerDetail> peerMap = peerPropHandler.getPeerInfoMap();
		PeerDetail serverPeerInfo = peerMap.get(ID);
		
		int peerServerPortNumber = serverPeerInfo.getPeerPort();
		
		try {
			
			serverSocket = new ServerSocket(peerServerPortNumber);
			
			int numberOfPeersSupposedToBeConnected = starter.getNumberOfPeersSupposedToBeConnected();
			
			for(int i=0 ; i<numberOfPeersSupposedToBeConnected ; i++){
				
				Socket neighborPeerSocket = serverSocket.accept();
				
				PeerHandle neighborPeerHandler = PeerHandle.createPeerConnection(neighborPeerSocket, starter);
				
				starter.neighborThreads.add(neighborPeerHandler);
				//(neighborPeerHandler);
				
				new Thread(neighborPeerHandler).start();
			}					
			
			isServComplete = true;
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	
	public boolean peerPropInit (Starter controller){
		
		logger = starter.getLogger();		
		peerPropHandler = PeerProperties.createInstance();
		if(peerPropHandler == null){
			return false;
		}	
		return true;
	}
	




}
