package messaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import beanClasses.pieceDetails;
import messaging.Piece_Processor;
import property.PeerPropertyTokens;
import property.Constants;

public class Piece_Processor {
	
	public static final String LOGGER_PREFIX = Piece_Processor.class.getSimpleName();

	private static Piece_Processor pieceProcessor;	
	int pieceSize;
	int piecesCount ;
	RandomAccessFile outputStream;
	FileInputStream inputStream;
	private static beanClasses.bitField bitField ;
	private Piece_Processor(){
		
	}
		
	
	synchronized public static Piece_Processor createPieceHanlder(boolean doesFileExist, String peer_ID){
		if(pieceProcessor == null){
			pieceProcessor = new Piece_Processor();
			boolean initializationSuccessChecker = pieceProcessor.initChunkProp(doesFileExist,peer_ID);
			if(initializationSuccessChecker == false){
				pieceProcessor = null;
			}
	
		}
		return pieceProcessor;
	}
	
	public boolean initChunkProp(boolean isFileExists, String peerID){
		
		if(PeerPropertyTokens.returnPropertyValue("PieceSize")!=null)
			pieceSize = Integer.parseInt(PeerPropertyTokens.returnPropertyValue("PieceSize"));
		
			if(PeerPropertyTokens.returnPropertyValue("FileSize")!= null)
			piecesCount = (int) Math.ceil(Integer.parseInt(PeerPropertyTokens.returnPropertyValue("FileSize")) / (pieceSize*1.0)) ;
		

		try
		{
			bitField = new beanClasses.bitField(piecesCount);
			
			if(isFileExists){
				bitField.setBits();
			}
			
			String outputFileName = new String();			
			outputFileName = PeerPropertyTokens.returnPropertyValue("FileName");
			
			String directoryName = "peer_"+peerID;
			File directory = new File(directoryName);
			
			if(isFileExists == false){

				directory.mkdir();

			}
			
			outputFileName = directory.getAbsolutePath()+"/"+outputFileName;
			
			File outFile = new File(outputFileName);
			if(outFile.exists() == true){
		
			}
			
			outputStream = new RandomAccessFile(outputFileName,"rw");
			
			outputStream.setLength(Integer.parseInt(PeerPropertyTokens.returnPropertyValue(Constants.FILE_SIZE)));
			
			return true;
			
		}
		catch(Exception e)
		{
		  e.printStackTrace();
		  return false;
		}	
	}
	
	synchronized public void close(){
		//close outputfilestream
		try {
			if(outputStream!= null){
				outputStream.close();
			}
			
			if(inputStream != null){
				inputStream.close();
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
				
	}
	
	synchronized public pieceDetails getdata(int num){
		
		pieceDetails readPiece = new pieceDetails(pieceSize);
		if(bitField.getBitFieldOn(num))
		{
			//have to read this piece from my own output file.
			try{
				byte[] bytesRead = new byte[pieceSize];
				outputStream.seek(num*pieceSize);
				int data_Size = outputStream.read(bytesRead);
				
				if(data_Size != pieceSize){
					byte[] newBytesRead = new byte[data_Size];
					for(int i=0 ; i<data_Size ; i++){
						newBytesRead[i] = bytesRead[i];
					}
					readPiece.setData(bytesRead);
				}else{
					readPiece.setData(bytesRead);
				}
				
				return readPiece;
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return null;
			}
		}
		else {
			
			return null;
		}
	}
	
	synchronized public boolean PeerPieceWriter(int num,pieceDetails piece){
		
		if(!bitField.getBitFieldOn(num))
		{
			try {
				
				outputStream.seek(num*pieceSize);
				outputStream.write(piece.getData());
				
				bitField.setBitFieldOn(num, true);
				
				return true;
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			
			return false;
		}
		
	}
	
	synchronized public int[] arrayMissingPieceNumberGetter(){
	
		int i = 0, j = 0;
		
		while( i < bitField.getSize())
		{
			if(bitField.getBitFieldOn(i) == false)
			{				
				j++;
			}
			i++;											
		}
	
		int[] missing = new int[j];
		j = 0;
		i = 0;
		while( i < bitField.getSize())
		{
			if(bitField.getBitFieldOn(i) == false)
			{				
				missing[j] = i;
				j++;
			}
			i++;											
		}				
		return missing;

	}
	
	synchronized public int[] arrayAvaialablePieceNumberGetter(){

		int i = 0, j = 0; 		
		int[] available = new int[piecesCount];
		while( i < bitField.getSize())
		{
			if(bitField.getBitFieldOn(i) == true)
			{
				available[j] = i;
				j++;
			}
			i++;								
		}
		return available;
	}
	
	synchronized public boolean fileDownloadCompletionCheck(){
		return bitField.checkIfFileDownloadComplete();
	}
	
	public beanClasses.bitField returnBitFieldProcessor(){
		return bitField;
	}

}