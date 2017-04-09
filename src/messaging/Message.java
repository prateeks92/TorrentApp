package messaging;

import java.io.Serializable;

public interface Message extends Serializable{

	public int getMessageType();
	public int returnLength();
	public int returnIndexOfMessage();
}
