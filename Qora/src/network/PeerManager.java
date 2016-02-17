package network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import database.DBSet;
import settings.Settings;

public class PeerManager {

	private static PeerManager instance;
	
	public static PeerManager getInstance()
	{
		if(instance == null)
		{
			instance = new PeerManager();
		}
		
		return instance;
	}
	
	private PeerManager()
	{
		
	}
	
	public List<Peer> getBestPeers()
	{
		return DBSet.getInstance().getPeerMap().getBestPeers(Settings.getInstance().getMaxSentPeers(), false);
	}
	
	
	public List<Peer> getKnownPeers()
	{
		List<Peer> knownPeers = new ArrayList<Peer>();
		//ASK DATABASE FOR A LIST OF PEERS
		if(!DBSet.getInstance().isStoped()){
			knownPeers = DBSet.getInstance().getPeerMap().getBestPeers(Settings.getInstance().getMaxReceivePeers(), true);
		}
		
		//RETURN
		return knownPeers;
	}
	
	public void addPeer(Peer peer)
	{
		//ADD TO DATABASE
		if(!DBSet.getInstance().isStoped()){
			DBSet.getInstance().getPeerMap().addPeer(peer);
		}
	}
	
	public void blacklistPeer(Peer peer)
	{
		DBSet.getInstance().getPeerMap().blacklistPeer(peer);
	}
	
	public boolean isBlacklisted(InetAddress address)
	{
		if(!DBSet.getInstance().isStoped()){
			return DBSet.getInstance().getPeerMap().isBlacklisted(address);
		}else{
			return true;
		}
	}
	
	public boolean isBlacklisted(Peer peer)
	{
		return DBSet.getInstance().getPeerMap().isBlacklisted(peer.getAddress());
	}
}
