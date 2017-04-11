package beanClasses;

import java.io.Serializable;

public class bitField implements Serializable
{
	private boolean ArrBitField[];
	private int size;
	
	public bitField(int numPieces)
	{
		ArrBitField = new boolean[numPieces];
		size = numPieces;
		
		for(int i = 0; i < size; i++)
		{
			ArrBitField[i] = false;
		}		
	}

	
	public int getSize()
	{
		return size;
	}
	
	
	public boolean[] getBitfieldVector() 
	{
		return ArrBitField;
	}
	
	
	public void setBitfieldVector(boolean[] ArrBitField) 
	{
		this.ArrBitField = ArrBitField;
	}
	
	
	public boolean getBitFieldOn(int number)
	{
		return ArrBitField[number];
	}
	
	
	synchronized public void setBitFieldOn(int number, boolean value)
	{
		ArrBitField[number] = value;
	}
	
	
	public void setBits()
	{
		for(int i=0 ; i<ArrBitField.length ; i++)
		{
			ArrBitField[i] = true;
		}
	}
	
	
	public int getSetbitCount()
	{
		int counter = 0;
		for(int i = 0; i < this.ArrBitField.length; i++){
			if(this.ArrBitField[i]==true)
				counter++;
		}
		return counter;
	}
	
	
	public boolean checkIfFileDownloadComplete()
	{	
		if(ArrBitField==null || ArrBitField.length==0)
		{
			return false;
		}
		
		int i = 0;
		while(i < this.getSize())
		{
			if(ArrBitField[i]!=true)
			{
				return false;
			}
			else
			{
				i++;
			}
		}
		return true;
	}
}