

public class PeerDetails {
	
	private String peerID;
	
	private String myAddress;
	
	private int hostPortNo;
	
	private boolean checkIfFileExits;
	
	public String returnPeerId() {
		return peerID;
	}
	
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}
	
	public String getHostAddress() {
		return myAddress;
	}
	
	public void setHostAddress(String hostAddress) {
		this.myAddress = hostAddress;
	}
	
	public int getPortNumber() {
		return hostPortNo;
	}
	
	public void setPortNumber(int portNumber) {
		this.hostPortNo = portNumber;
	}
	
	public boolean checkIfTheFileExits() {
		return checkIfFileExits;
	}
	
	public void setFileExists(boolean isFileExists) {
		this.checkIfFileExits = isFileExists;
	}
}
