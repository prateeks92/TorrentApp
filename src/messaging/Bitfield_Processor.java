package messaging;

import java.io.Serializable;

public class Bitfield_Processor implements Serializable{

	private boolean bitfieldArray[];
	
	private int size;
	
	public Bitfield_Processor(int numberOfPieces) {
		bitfieldArray = new boolean[numberOfPieces];
		size = numberOfPieces;
		
		for(int i = 0; i < size; i++)
		{
			bitfieldArray[i] = false;
		}		
	}

	public int getSize()
	{
		return size;
	}
	
	public boolean[] bitfieldArrayGetter() {
		return bitfieldArray;
	}

	
	public void bitfieldArraySetter(boolean[] bitfieldArray) {
		this.bitfieldArray = bitfieldArray;
	}
	
	public boolean getBitFieldOn(int num)
	{
		return bitfieldArray[num];
	}
	
	synchronized public void setBitFieldOn(int num, boolean val)
	{
		bitfieldArray[num] = val;
	}
	
	
	public void setAllBits(){
		for(int i=0 ; i<bitfieldArray.length ; i++){
			bitfieldArray[i] = true;
		}
	}
	
	public int getSetbitCount(){
		int counter = 0;
		for(int i = 0; i < this.bitfieldArray.length; i++){
			if(this.bitfieldArray[i]==true)
				counter++;
		}
		return counter;
	}
	
	public boolean FileDownloadCompleteCheck(){
		
		if(bitfieldArray==null || bitfieldArray.length==0){
			return false;
		}
		
		int i = 0;
		while(i < this.getSize())
		{
			if(bitfieldArray[i]!=true)
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
