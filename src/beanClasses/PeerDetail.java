package beanClasses;

public class PeerDetail 
{	
	private String peerID;	
	private int peerPort;
	private boolean fileExists;
	private String peerAddress;

	
	public String getPeerID() 
	{
		return peerID;
	}

	public void setPeerID(String peerID)
	{
		this.peerID = peerID;
	}

	public int getPeerPort() 
	{
		return peerPort;
	}

	public void setPeerPort(int peerPort) 
	{
		this.peerPort = peerPort;
	}

	public boolean fileExist() 
	{
		return fileExists;
	}

	public void setFileAvailability(boolean isFile)
	{
		this.fileExists = isFile;
	}

	public String getPeerAddress() 
	{
		return peerAddress;
	}

	public void setPeerAddress(String peerAddress)
	{
		this.peerAddress = peerAddress;
	}
}