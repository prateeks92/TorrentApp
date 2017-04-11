package peerProcess;

import java.util.Scanner;

import peer2peer.Starter;

public class peerProcess {
	public static void main(String args[]) throws Exception{	
//		Scanner sc = new Scanner(System.in);
		Starter controller = Starter.setUpPeer(args[0]);//sc.next());
		controller.startThread();
	}
}
