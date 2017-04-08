package property;

public class Constants {
	
	public static final int SENDER_SIZE_QUEUE = 100;
	
	public static final String CHOKE_UNCHOKE_INTERVAL = "UnchokingInterval";
	
	public static final String OPTIMISTIC_UNCHOKE_INTERVAL = "OptimisticUnchokingInterval";
	
	public static final String LOG_DIR = "PeerLogs";
	
	public static final String LOG_NAME_START = "log_peer_";

	public static final String HANDSHAKE_HEADER_STRING = "P2PFILESHARINGPROJ";
	
	public static final String LOGGER_NAME = "logger.name";
	
	public static final String CONFIG_FILE = "common.cfg";
	
	public static final String PEER_INFO_FILE = "PeerInfo.cfg";
		
	public static final String FILE_SIZE = "FileSize";
	
	public static final int EMPTY_MESSAGE_SIZE = 1;
	
	public static final String LOG_FILE_NAME = "logger.filename";
	
	public static final int MAX_MESSAGE_SIZE = 40000;
		
	public static final int RAW_DATA_SIZE = 1000;

	public static final byte MESSAGE_CHOKE = 0;	
	
	public static final byte MESSAGE_UNCHOKE = 1;
	
	public static final byte MESSAGE_INTERESTED = 2;
	
	public static final byte MESSAGE_NOT_INTERESTED = 3;
	
	public static final byte MESSAGE_HAVE = 4;
	
	public static final byte MESSAGE_BITFIELD = 5;
	
	public static final byte MESSAGE_REQUEST = 6;
	
	public static final byte MESSAGE_PIECE = 7;

	public static final byte MESSAGE_HANDSHAKE = 9;
	
	public static final byte MESSAGE_SHUTDOWN = 100;
	
	public static String getMessageName(int i){
		switch (i) {
		case Constants.MESSAGE_REQUEST:
			return "MESSAGE_REQUEST";
			
		case Constants.MESSAGE_HANDSHAKE:
			return "MESSAGE_HANDSHAKE";
		
		case Constants.MESSAGE_CHOKE:
			return "MESSAGE_CHOKE";
			
		case Constants.MESSAGE_UNCHOKE:
			return "MESSAGE_UNCHOKE";
			
		case Constants.MESSAGE_HAVE:
			return "MESSAGE_HAVE";
		
		case Constants.MESSAGE_BITFIELD:
			return "MESSAGE_BITFIELD";

		case Constants.MESSAGE_INTERESTED:
			return "MESSAGE_INTERESTED";
			
		case Constants.MESSAGE_NOT_INTERESTED:
			return "MESSAGE_NOT_INTERESTED";
			
		case Constants.MESSAGE_SHUTDOWN:
			return "MESSAGE_SHUTDOWN";
		
		case Constants.MESSAGE_PIECE:
			return "MESSAGE_PIECE";
		
		}
		return null;
	}

	
}
