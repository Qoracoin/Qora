package network;

import java.net.InetAddress;
import java.util.List;

import network.message.Message;

public interface ConnectionCallback {

	void onConnect(Peer peer);
	void onDisconnect(Peer peer);
	void onError(Peer peer, String error);
	boolean isConnectedTo(InetAddress address);
	boolean isConnectedTo(Peer peer);
	List<Peer> getActiveConnections();
	void onMessage(Message message);
	
}
