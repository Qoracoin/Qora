package network;

import java.util.List;
import java.util.logging.Logger;

import network.message.Message;
import network.message.MessageFactory;
import network.message.PeersMessage;
import settings.Settings;

public class ConnectionCreator extends Thread {

	private ConnectionCallback callback;
	private boolean isRun;
	
	public ConnectionCreator(ConnectionCallback callback)
	{
		this.callback = callback;
	}
	
	public void run()
	{
		this.isRun = true;

		while(isRun)
		{
			try
			{	
				int maxReceivePeers = Settings.getInstance().getMaxReceivePeers();
				
				//CHECK IF WE NEED NEW CONNECTIONS
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActiveConnections().size())
				{			
					//GET LIST OF KNOWN PEERS
					List<Peer> knownPeers = PeerManager.getInstance().getKnownPeers();
					
					int knownPeersCounter = 0;
										
					//ITERATE knownPeers
					for(Peer peer: knownPeers)
					{
						knownPeersCounter ++;
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if(this.isRun && Settings.getInstance().getMaxConnections() > callback.getActiveConnections().size())
						{
							//CHECK IF ALREADY CONNECTED TO PEER
							if(!callback.isConnectedTo(peer.getAddress()))
							{							
								//CHECK IF SOCKET IS NOT LOCALHOST
								if(true)
								//if(!peer.getAddress().isSiteLocalAddress() && !peer.getAddress().isLoopbackAddress() && !peer.getAddress().isAnyLocalAddress())
								{
									//CONNECT
									Logger.getGlobal().info(
											"Connecting to known peer " + peer.getAddress().getHostAddress() 
											+ " :: " + knownPeersCounter + " / " + knownPeers.size() 
											+ " :: Connections: " + callback.getActiveConnections().size());
								
									peer.connect(callback);
								}
							}
						}
					}
				}
				
				//CHECK IF WE STILL NEED NEW CONNECTIONS
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActiveConnections().size())
				{
					//OLD SCHOOL ITERATE activeConnections
					//avoids Exception when adding new elements
					for(int i=0; i<callback.getActiveConnections().size(); i++)
					{
						Peer peer = callback.getActiveConnections().get(i);
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if(this.isRun && Settings.getInstance().getMaxConnections() > callback.getActiveConnections().size())
						{
								//ASK PEER FOR PEERS
								Message getPeersMessage = MessageFactory.getInstance().createGetPeersMessage();
								PeersMessage peersMessage = (PeersMessage) peer.getResponse(getPeersMessage);
								if(peersMessage != null)
								{
									int foreignPeersCounter = 0;
									//FOR ALL THE RECEIVED PEERS
									
									for(Peer newPeer: peersMessage.getPeers())
									{		
										//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
										if(this.isRun && Settings.getInstance().getMaxConnections() > callback.getActiveConnections().size())
										{
											if(foreignPeersCounter >= maxReceivePeers) {
												break;
											}
	
											foreignPeersCounter ++;
											
											//CHECK IF THAT PEER IS NOT BLACKLISTED
											if(!PeerManager.getInstance().isBlacklisted(newPeer))
											{
												//CHECK IF CONNECTED
												if(!callback.isConnectedTo(newPeer))
												{
													//CHECK IF SOCKET IS NOT LOCALHOST
													if(!newPeer.getAddress().isSiteLocalAddress() && !newPeer.getAddress().isLoopbackAddress() && !newPeer.getAddress().isAnyLocalAddress())
													{
														if(Settings.getInstance().isTryingConnectToBadPeers() || !newPeer.isBad())
														{
															int maxReceivePeersForPrint = (maxReceivePeers > peersMessage.getPeers().size()) ? peersMessage.getPeers().size() : maxReceivePeers;  
															
															Logger.getGlobal().info(
																"Connecting to peer " + newPeer.getAddress().getHostAddress() + " proposed by " + peer.getAddress().getHostAddress() 
																+ " :: " + foreignPeersCounter + " / " + maxReceivePeersForPrint + " / " + peersMessage.getPeers().size() 
																+ " :: Connections: " + callback.getActiveConnections().size());
														
															//CONNECT
															newPeer.connect(callback);
														}
													}
												}
											}
										}
									}									
								}					
							
						}
					}
				}			
				//SLEEP
				Thread.sleep(60 * 1000);	
	
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
				Logger.getGlobal().info("Error creating new connection");			
			}					
		}
	}
	
	public void halt()
	{
		this.isRun = false;
	}
}
