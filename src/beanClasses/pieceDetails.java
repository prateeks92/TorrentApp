package beanClasses;

import java.io.Serializable;


public class pieceDetails implements Serializable
{
	private byte[] pieceData;
	int size;
	
	public pieceDetails(int size)
	{
		this.size = size;
	}

	public byte[] getData() 
	{
		return pieceData;
	}

	public void setData(byte[] data) 
	{
		this.pieceData = data;
	}
	
	public int getSize()
	{
		if(pieceData == null)
		{
			return -1;
		}
		else
		{
			return pieceData.length;
		}		
	}
}