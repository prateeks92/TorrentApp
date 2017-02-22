public class MsgDetails implements Message{
	
	private static final long serialVersionUID = 1L;
	private int messageLength;
	private int messgageType;
	private int pieceIndex;
	private Chunk data;
	private BitFieldHandler handler = null;
	public int messageNumber = 0;
	private static int messageCounter = 0;
	
	private MsgDetails(){
		
	}
	
	public static MsgDetails createInstance(){
		MsgDetails message = new MsgDetails();
		boolean isSuccessful = message.initialize();
		
		if(isSuccessful == false){
		
			message = null;
		}
		
		return message;
	}
	
	private boolean initialize(){
		messageCounter++;
		messageNumber = messageCounter;
		return true;
	}
	
	
	public byte[] getMessage(){
		return null;
	}

	public int returnMsgType() {

		return this.messgageType;
	}

	public int getLength() {

		return this.messageLength;
	}
	
	public int getMessgageType() {
		return messgageType;
	}

	public void setMessgageType(int messgageType) {
		this.messgageType = messgageType;
	}

	public int getPieceIndex() {
		return pieceIndex;
	}

	public void setPieceIndex(int pieceIndex) {
		this.pieceIndex = pieceIndex;
	}

	public Chunk getData() {
		return data;
	}

	public void setData(Chunk data) {
		this.data = data;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}

	public BitFieldHandler returnBitFieldHandler() {
		return handler;
	}

	public void setHandler(BitFieldHandler handler) {
		this.handler = handler;
	}

	public int getMessageIndex() {

		return messageNumber;
	}

	
	
}
