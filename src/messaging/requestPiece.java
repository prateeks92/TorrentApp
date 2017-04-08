package messaging;

import java.util.*;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import beanClasses.bitField;
import messaging.*;
import property.*;
import peer2peer.*;

public class requestPiece implements Runnable 
{	
	private static String LOGGER_PREFIX = requestPiece.class.getSimpleName();
	
	public BlockingQueue<MsgDetails> messageQueue;
	
	private boolean isShutDown = false;
	
	private Starter threadController;
	private Peer peerHandler;
	
	private bitField neighborPeerBitFieldhandler = null;
	
	int [] pieceIndexArray = new int[1000];	
	
	private requestPiece()
	{
	}
	
	
	public static requestPiece createInstance(Starter controller, Peer peerHandler)
	{		
		if(controller == null || peerHandler == null)
		{
			return null;
		}
		
		requestPiece requestSender = new requestPiece();
		
		requestSender.messageQueue = new ArrayBlockingQueue<MsgDetails>(Constants.SENDER_SIZE_QUEUE);

		int pieceSize = Integer.parseInt(PeerPropertyTokens.returnPropertyValue("PieceSize"));
		int nPieces = (int) Math.ceil(Integer.parseInt(PeerPropertyTokens.returnPropertyValue("FileSize")) / (pieceSize*1.0));
	
		requestSender.neighborPeerBitFieldhandler = new bitField(nPieces);
		
		requestSender.threadController = controller;
		requestSender.peerHandler = peerHandler;
		
		return requestSender;
	}
	
	
	public int getRandomPieceNo()
	{
		bitField thisPeerBitFieldhandler = threadController.getBitFieldMessage().returnBitFieldHandler();
		int count = 0;

		for(int i=0 ; i<neighborPeerBitFieldhandler.getSize() && count<pieceIndexArray.length ; i++)
		{
			if(thisPeerBitFieldhandler.getBitFieldOn(i) == false && neighborPeerBitFieldhandler.getBitFieldOn(i) == true)
			{
				pieceIndexArray[count] = i;
				count++;
			}
		}
		
		if(count != 0)
		{
			Random random = new Random();
			int index = random.nextInt(count);
			return pieceIndexArray[index];
		}
		else
		{
			return -1;
		}	
	}
	
	
	public void run() 
	{	
		while(isShutDown == false)
		{
			try 
			{				
				MsgDetails message = messageQueue.take();
				System.out.println(LOGGER_PREFIX+": Received Message: "+Constants.getMessageName(message.returnMsgType()));
				
				MsgDetails requestMessage = MsgDetails.createInstance();
				requestMessage.setMessgageType(Constants.MESSAGE_REQUEST);
				
				MsgDetails interestedMessage = MsgDetails.createInstance();
				interestedMessage.setMessgageType(Constants.MESSAGE_INTERESTED);
				
				if(message.returnMsgType() == Constants.MESSAGE_BITFIELD)
				{
					neighborPeerBitFieldhandler = message.returnBitFieldHandler();
					
					int missingPieceIndex = getRandomPieceNo();
					
					if(missingPieceIndex == -1)
					{
						MsgDetails notInterestedMessage = MsgDetails.createInstance();
						notInterestedMessage.setMessgageType(Constants.MESSAGE_NOT_INTERESTED);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					}
					else
					{
						interestedMessage.setPieceIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);
						
						requestMessage.setPieceIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}									
				}
				
				if(message.returnMsgType() == Constants.MESSAGE_HAVE)
				{
					int pieceIndex = message.getPieceIndex();
					
					try 
					{
						neighborPeerBitFieldhandler.setBitFieldOn(pieceIndex, true);
					}
					catch (Exception e)
					{
						System.out.println(LOGGER_PREFIX+"["+peerHandler.peerID+"]: NULL POINTER EXCEPTION for piece Index"+pieceIndex +" ... "+neighborPeerBitFieldhandler);
						e.printStackTrace();
					}
					
					int missingPieceIndex = getRandomPieceNo();

					if(missingPieceIndex == -1)
					{
						MsgDetails notInterestedMessage = MsgDetails.createInstance();
						notInterestedMessage.setMessgageType(Constants.MESSAGE_NOT_INTERESTED);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					}
					else
					{
						if(peerHandler.isPieceMessageForPreviousMessageReceived() == true)
						{
							peerHandler.setPieceMessageForPreviousMessageReceived(false);
							interestedMessage.setPieceIndex(missingPieceIndex);
							peerHandler.sendInterestedMessage(interestedMessage);
							
							requestMessage.setPieceIndex(missingPieceIndex);
							peerHandler.sendRequestMessage(requestMessage);
						}	
					}									
				}
				
				if(message.returnMsgType() == Constants.MESSAGE_PIECE)
				{					
					int missingPieceIndex = getRandomPieceNo();

					if(missingPieceIndex == -1)
					{
						// do nothing 
					}
					else
					{
						if(peerHandler.isPieceMessageForPreviousMessageReceived() == true)
						{
							peerHandler.setPieceMessageForPreviousMessageReceived(false);
							interestedMessage.setPieceIndex(missingPieceIndex);
							peerHandler.sendInterestedMessage(interestedMessage);
							
							requestMessage.setPieceIndex(missingPieceIndex);
							peerHandler.sendRequestMessage(requestMessage);
						}						
					}									
				}
				
				if(message.returnMsgType() == Constants.MESSAGE_UNCHOKE)
				{
					int missingPieceIndex = getRandomPieceNo();

					peerHandler.setPieceMessageForPreviousMessageReceived(false);
					
					if(missingPieceIndex == -1)
					{
						// do nothing 
					}
					else
					{
						interestedMessage.setPieceIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);
						
						requestMessage.setPieceIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}									
				}
				
				
			}
			catch (Exception e) 
			{				
				e.printStackTrace();
				break;
			}
		}
	}
	
	
	public boolean checkIfNeighbourDownloadFile()
	{
		if(neighborPeerBitFieldhandler != null && neighborPeerBitFieldhandler.checkIfFileDownloadComplete() == true)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}