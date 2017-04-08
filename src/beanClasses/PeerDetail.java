package beanClasses;

public class PeerDetail {
	
	private String peerID;
	
	private int peerPort;
	
	private boolean isFileExist;
	
	private String peerAddress;

	public String getPeerID() {
		return peerID;
	}

	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public void setPeerPort(int peerPort) {
		this.peerPort = peerPort;
	}

	public boolean isFileExist() {
		return isFileExist;
	}

	public void setFileExist(boolean isFileExist) {
		this.isFileExist = isFileExist;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public void setPeerAddress(String peerAddress) {
		this.peerAddress = peerAddress;
	}

	
}
