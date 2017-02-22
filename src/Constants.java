

public class Constants {
	
	
	public static final int MAX_MESSAGE_SIZE = 40000;
	
	
	public static final int RAW_DATA_SIZE = 1000;
		
	
	public static final byte HANDSHAKE_MESSAGE = 9;
	
	public static final byte BITFIELD_MESSAGE = 5;
	
	
	public static final byte REQUEST_MESSAGE = 6;
	
	
	public static final byte PIECE_MESSAGE = 7;
	
	
	public static final byte INTERESTED_MESSAGE = 2;
	
	
	public static final byte NOT_INTERESTED_MESSAGE = 3;
	
	
	public static final byte HAVE_MESSAGE = 4;
	
	
	public static final byte CHOKE_MESSAGE = 0;
	
	public static final byte SHUTDOWN_MESSAGE = 100;
	
	
	public static final byte UNCHOKE_MESSAGE = 1;

	
	public static final String LOG_FILE_DIRECTORY_NAME = "PeerLogs";
	
	public static final String LOG_FILE_NAME_PREFIX = "log_peer_";

	/** The Constant HANDSHAKE_HEADER_STRING. */
	public static final String HANDSHAKE_HEADER_STRING = "P2PFILESHARINGPROJ";
	
	public static final int SIZE_OF_EMPTY_MESSAGE = 1;
	
	public static final String LOG_FILE_NAME = "logger.filename";
	
	public static final String LOGGER_NAME = "logger.name";
	
	public static final String CONFIGURATION_FILE = "common.cfg";
	
	public static final String PEER_INFO_FILE = "PeerInfo.cfg";
	
	public static final int SENDER_QUEUE_SIZE = 100;
	
	public static final String CHOKE_UNCHOKE_INTERVAL = "UnchokingInterval";
	
	public static final String OPTIMISTIC_UNCHOKE_INTERVAL = "OptimisticUnchokingInterval";
	
	public static final String FILE_SIZE = "FileSize";
	
	public static String getMessageName(int i){
		if(i == Constants.BITFIELD_MESSAGE){
			return "BITFIELD_MESSAGE";
		}
		
		if(i == Constants.HANDSHAKE_MESSAGE){
			return "HANDSHAKE_MESSAGE";
		}
		
		if(i == Constants.REQUEST_MESSAGE){
			return "REQUEST_MESSAGE";
		}
		
		
		if(i == Constants.CHOKE_MESSAGE){
			return "CHOKE_MESSAGE";
		}
		
		if(i == Constants.UNCHOKE_MESSAGE){
			return "UNCHOKE_MESSAGE";
		}
		
		if(i == Constants.HAVE_MESSAGE){
			return "HAVE_MESSAGE";
		}
		

		if(i == Constants.NOT_INTERESTED_MESSAGE){
			return "NOT_INTERESTED_MESSAGE";
		}
		if(i == Constants.INTERESTED_MESSAGE){
			return "INTERESTED_MESSAGE";
		}
		
		
		if(i == Constants.PIECE_MESSAGE){
			return "PIECE_MESSAGE";
		}
			
		if(i == Constants.SHUTDOWN_MESSAGE){
			return "SHUTDOWN_MESSAGE";
		}		
		return null;
	}
}
