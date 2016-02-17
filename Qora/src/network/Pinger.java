package network;

import database.DBSet;
import network.message.Message;
import network.message.MessageFactory;
import settings.Settings;

public class Pinger extends Thread
{
	private Peer peer;
	private boolean run;
	private long ping;
	
	public Pinger(Peer peer)
	{
		this.peer = peer;
		this.run = true;
		this.ping = Long.MAX_VALUE;
		
		this.start();
	}
	
	public long getPing()
	{
		return this.ping;
	}
	
	public void run()
	{
		while(this.run)
		{
			//CREATE PING
			Message pingMessage = MessageFactory.getInstance().createPingMessage();
			
			if(!this.run)
				break;
			
			//GET RESPONSE
			long start = System.currentTimeMillis();
			Message response = this.peer.getResponse(pingMessage);

			if(!this.run)
				break;

			//CHECK IF VALID PING
			if(response == null || response.getType() != Message.PING_TYPE)
			{
				//PING FAILES
				this.peer.onPingFail();
				
				//STOP PINGER
				this.run = false;
				return;
			}
			
			//UPDATE PING
			this.ping = System.currentTimeMillis() - start;
			this.peer.addPingCounter(); 
			
			if(!DBSet.getInstance().isStoped()){
				DBSet.getInstance().getPeerMap().addPeer(this.peer);
			}
			
			//SLEEP
			try 
			{
				Thread.sleep(Settings.getInstance().getPingInterval());
			} 
			catch (InterruptedException e)
			{
				//FAILED TO SLEEP
			}
		}
	}

	public void stopPing() 
	{
		try
		{
			this.run = false;
			this.interrupt();
			this.join();
		}
		catch(Exception e)
		{
			
		}
	}
}
