package messaging;

import java.util.*;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import beanClasses.*;
import messaging.*;
import property.*;
import peer2peer.*;

public class requestPiece implements Runnable 
{	
	private static String LOGGER_PREFIX = requestPiece.class.getSimpleName();	
	public BlockingQueue<messageDefine> msgQ;
	private boolean shutDown = false;
	private Starter threadController;
	private Peer peerHandler;
	private bitField bitFieldHandler = null;
	int [] pieces = new int[1000];	
	
	
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
		
		requestSender.msgQ = new ArrayBlockingQueue<messageDefine>(Constants.SENDER_SIZE_QUEUE);

		int pieceSize = Integer.parseInt(PeerPropertyTokens.returnPropertyValue("PieceSize"));
		int nPieces = (int) Math.ceil(Integer.parseInt(PeerPropertyTokens.returnPropertyValue("FileSize")) / (pieceSize*1.0));
	
		requestSender.bitFieldHandler = new bitField(nPieces);
		
		requestSender.threadController = controller;
		requestSender.peerHandler = peerHandler;
		
		return requestSender;
	}
	
	
	public int arbitPieceNum()
	{
		bitField peerBitFieldHandler = threadController.getBitFieldMessage().returnBitFieldHandler();
		int count = 0;

		for(int i=0 ; i<bitFieldHandler.getSize() && count<pieces.length ; i++)
		{
			if(peerBitFieldHandler.getBitFieldOn(i) == false && bitFieldHandler.getBitFieldOn(i) == true)
			{
				pieces[count] = i;
				count++;
			}
		}
		
		if(count != 0)
		{
			Random random = new Random();
			return pieces[random.nextInt(count)];
		}
		else
		{
			return -1;
		}	
	}
	
	
	public void run() 
	{	
		while(shutDown == false)
		{
			try 
			{				
				messageDefine message = msgQ.take();
				System.out.println(LOGGER_PREFIX+": Message Received : "+Constants.getMessageName(message.returnMsgType()));
				
				messageDefine requestMsg = messageDefine.createInstance();
				requestMsg.setMsgType(Constants.MESSAGE_REQUEST);
				
				messageDefine interestMsg = messageDefine.createInstance();
				interestMsg.setMsgType(Constants.MESSAGE_INTERESTED);
				
				if(message.returnMsgType() == Constants.MESSAGE_BITFIELD)
				{
					bitFieldHandler = message.returnBitFieldHandler();
					
					int missingPieces = arbitPieceNum();
					
					if(missingPieces == -1)
					{
						messageDefine noInterestMsg = messageDefine.createInstance();
						noInterestMsg.setMsgType(Constants.MESSAGE_NOT_INTERESTED);
						peerHandler.sendNoInterestMsg(noInterestMsg);
					}
					else
					{
						interestMsg.setPieceIndex(missingPieces);
						peerHandler.sendInterestMsg(interestMsg);
						
						requestMsg.setPieceIndex(missingPieces);
						peerHandler.sendRequestMsg(requestMsg);
					}									
				}
				
				if(message.returnMsgType() == Constants.MESSAGE_HAVE)
				{
					int pieceNum = message.getPieceIndex();
					
					try 
					{
						bitFieldHandler.setBitFieldOn(pieceNum, true);
					}
					catch (Exception e)
					{
						System.out.println(LOGGER_PREFIX+"["+peerHandler.peerID+"]: Error : NULL POINTER for piece Index"+pieceNum +" ... "+bitFieldHandler);
						e.printStackTrace();
					}
					
					int missingPieceIndex = arbitPieceNum();

					if(missingPieceIndex == -1)
					{
						messageDefine noInterestMsg = messageDefine.createInstance();
						noInterestMsg.setMsgType(Constants.MESSAGE_NOT_INTERESTED);
						peerHandler.sendNoInterestMsg(noInterestMsg);
					}
					else
					{
						if(peerHandler.lastPieceMessageReceived() == true)
						{
							peerHandler.setLastPieceMessageReceived(false);
							interestMsg.setPieceIndex(missingPieceIndex);
							peerHandler.sendInterestMsg(interestMsg);
							
							requestMsg.setPieceIndex(missingPieceIndex);
							peerHandler.sendRequestMsg(requestMsg);
						}	
					}									
				}
				
				if(message.returnMsgType() == Constants.MESSAGE_PIECE)
				{					
					int numMissingPiece = arbitPieceNum();

					if(numMissingPiece != -1)
					{
						if(peerHandler.lastPieceMessageReceived() == true)
						{
							peerHandler.setLastPieceMessageReceived(false);
							interestMsg.setPieceIndex(numMissingPiece);
							peerHandler.sendInterestMsg(interestMsg);
							
							requestMsg.setPieceIndex(numMissingPiece);
							peerHandler.sendRequestMsg(requestMsg);
						}						
					}									
				}
				
				if(message.returnMsgType() == Constants.MESSAGE_UNCHOKE)
				{
					int numMissingPiece = arbitPieceNum();

					peerHandler.setLastPieceMessageReceived(false);
					
					if(numMissingPiece != -1)
					{
						interestMsg.setPieceIndex(numMissingPiece);
						peerHandler.sendInterestMsg(interestMsg);
						
						requestMsg.setPieceIndex(numMissingPiece);
						peerHandler.sendRequestMsg(requestMsg);
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
	
	
	public boolean checkNeighbourDownloadComplete()
	{
		if(bitFieldHandler != null && bitFieldHandler.checkIfFileDownloadComplete() == true)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}