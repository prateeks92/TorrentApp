public class HandshakeMessage implements Message{
	
	/** The peer id. */
	private String peerID;
	
	private static int countInstance;
	
	private int messageNumber;
	
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}

	private HandshakeMessage(){
		
	}
	
	public static HandshakeMessage createInstance(){
		HandshakeMessage handshakeMessage = new HandshakeMessage();
		boolean isSuccessful = true;
		countInstance++;
		if(isSuccessful==true)
			handshakeMessage.updateMessageNumber();
		if(isSuccessful == false){
			handshakeMessage = null;
		}
		return handshakeMessage;
	}
	private void updateMessageNumber()
	{
			messageNumber=countInstance;
	}
	
	//return type of message
	public int returnMsgType() {
		return Constants.HANDSHAKE_MESSAGE;
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getPeerID(){
		return peerID;
	}

	public int getMessageIndex() {
		return messageNumber;
	}

	public int getMessageLength() {
		// TODO Auto-generated method stub
		return 0;
	}
}
