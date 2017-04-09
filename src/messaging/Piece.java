package messaging;

import java.io.Serializable;

public class Piece implements Serializable{
	private byte[] data;
	int size;
	
	public Piece(int size)
	{
		this.size = size;
	}

	public byte[] get_Data() {
		return data;
	}

	public void set_Data(byte[] data) {
		this.data = data;
	}
	
	public int get_Size(){
		if(data == null){
			return -1;
		}else{
			return data.length;
		}		
	}
}
