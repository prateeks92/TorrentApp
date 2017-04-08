package beanClasses;

import java.io.Serializable;

public interface message extends Serializable
{
	public int returnMsgType();	
	public int getLength();
	public int getMessageIndex();
}