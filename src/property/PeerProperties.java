package property;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import beanClasses.PeerDetail;


public class PeerProperties {
	
public Map<String,PeerDetail> peerDetailMap = null;
	
private static PeerProperties  PeerConfiguration = null;

	
	public static PeerProperties createInstance(){
		if( PeerConfiguration == null){
			 PeerConfiguration = new  PeerProperties();
			 PeerConfiguration.populate();
		}
		return  PeerConfiguration;
	}
	
	public boolean populate(){		
		try {

			BufferedReader configFileReader =  new BufferedReader(new InputStreamReader(new FileInputStream(Constants.PEER_INFO_FILE)));
			
			peerDetailMap = new HashMap<String,PeerDetail>();
			
			String line = configFileReader.readLine();
			
			PeerDetail peerInfoInstance = null;
			while(line != null){
				peerInfoInstance = new PeerDetail();
				String tokens[] = line.trim().split(" ");
				peerInfoInstance.setPeerID(tokens[0]);
				peerInfoInstance.setPeerAddress(tokens[1]);
				peerInfoInstance.setPeerPort(Integer.parseInt(tokens[2]));
				
				if(tokens[3].equals("1")){
					peerInfoInstance.setFileAvailability(true);
				}else{
					peerInfoInstance.setFileAvailability(false);
				}
				
				peerDetailMap.put(tokens[0],peerInfoInstance);
				
				line = configFileReader.readLine();
			}			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return true;
	}
	
	public Map<String, PeerDetail> getPeerInfoMap() {
		return peerDetailMap;
	}

}
