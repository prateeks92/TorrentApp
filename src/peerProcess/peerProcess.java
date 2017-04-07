package peerProcess;

import peer2peer.Starter;

public class peerProcess {
	public static void main(String args[]) throws Exception{		
		Starter controller = Starter.setUpPeer(args[0]);
		controller.startThread();
	}
}
