

import java.io.Serializable;

public interface Message extends Serializable{
	public int returnMsgType();	
	public int getLength();
	public int getMessageIndex();
}
