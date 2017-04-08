package property;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class PeerProperties {
public LinkedHashMap<String,PeerDetail> peerDetailsMap = null;
	
	private static Configurations  PeerConfiguration = null;
	
	private Configurations(){
		
	}
	
	public static Configurations createInstance(){
		if( PeerConfiguration == null){
			 PeerConfiguration = new  Configurations();
			 PeerConfiguration.initialize();
		}
		return  PeerConfiguration;
	}
	
	public boolean initialize(){
		
		
		try {

			BufferedReader configFileReader =  new BufferedReader(new InputStreamReader(new FileInputStream(Constants.PEER_INFO_FILE)));
			
			peerDetailsMap = new LinkedHashMap<String,PeerDetail>();
			
			String line = configFileReader.readLine();
			
			PeerDetail peerInfoInstance = null;
			while(line != null){
				peerInfoInstance = new PeerDetail();
				String tokens[] = line.trim().split(" ");
				peerInfoInstance.setPeerID(tokens[0]);
				peerInfoInstance.setHostAddress(tokens[1]);
				peerInfoInstance.setPortNumber(Integer.parseInt(tokens[2]));
				
				if(tokens[3].equals("1")){
					peerInfoInstance.setFileExists(true);
				}else{
					peerInfoInstance.setFileExists(false);
				}
				
				peerDetailsMap.put(tokens[0],peerInfoInstance);
				
				line = configFileReader.readLine();
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return true;
	}
	
	public HashMap<String, PeerDetail> getPeerInfoMap() {
		return peerDetailsMap;
	}

}
