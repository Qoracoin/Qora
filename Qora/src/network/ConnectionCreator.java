package network;

import java.util.List;

import lang.Lang;
import network.message.Message;
import network.message.MessageFactory;
import network.message.PeersMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import settings.Settings;

public class ConnectionCreator extends Thread {

	private ConnectionCallback callback;
	private boolean isRun;

	private static final Logger LOGGER = LogManager.getLogger(ConnectionCreator.class);

	public ConnectionCreator(ConnectionCallback callback) {
		this.callback = callback;
	}

	public void run() {
		Thread.currentThread().setName("ConnCreator");

		this.isRun = true;

		while (isRun) {
			try {
				int maxReceivePeers = Settings.getInstance().getMaxReceivePeers();

				// CHECK IF WE NEED NEW CONNECTIONS
				if (this.isRun && Settings.getInstance().getMinConnections() >= callback.getActiveConnections().size()) {
					// GET LIST OF KNOWN PEERS
					List<Peer> knownPeers = PeerManager.getInstance().getKnownPeers();

					int knownPeersCounter = 0;

					// ITERATE knownPeers
					for (Peer peer : knownPeers) {
						knownPeersCounter++;

						// CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if (!this.isRun || callback.getActiveConnections().size() >= Settings.getInstance().getMaxConnections())
							break;

						// CHECK IF THAT PEER IS NOT BLACKLISTED
						if (PeerManager.getInstance().isBlacklisted(peer))
							continue;

						// CHECK IF ALREADY CONNECTED TO PEER
						if (callback.isConnectedTo(peer.getAddress()))
							continue;

						// Check peer's address is not loopback, localhost, one of ours, etc.
						if (Network.isHostLocalAddress(peer.getAddress()))
							continue;

						// CONNECT
						LOGGER.info(Lang.getInstance()
								.translate("Connecting to known peer %peer% :: %knownPeersCounter% / %allKnownPeers% :: Connections: %activeConnections%")
								.replace("%peer%", peer.getAddress().getHostAddress())
								.replace("%knownPeersCounter%", String.valueOf(knownPeersCounter))
								.replace("%allKnownPeers%", String.valueOf(knownPeers.size()))
								.replace("%activeConnections%", String.valueOf(callback.getActiveConnections().size())));
						peer.connect(callback);
					}
				}

				// CHECK IF WE STILL NEED NEW CONNECTIONS
				if (this.isRun && Settings.getInstance().getMinConnections() >= callback.getActiveConnections().size()) {
					// OLD SCHOOL ITERATE activeConnections
					// avoids Exception when adding new elements
					for (int i = 0; i < callback.getActiveConnections().size(); i++) {
						Peer peer = callback.getActiveConnections().get(i);

						// CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if (!this.isRun || callback.getActiveConnections().size() >= Settings.getInstance().getMaxConnections())
							break;

						// ASK PEER FOR PEERS
						Message getPeersMessage = MessageFactory.getInstance().createGetPeersMessage();
						PeersMessage peersMessage = (PeersMessage) peer.getResponse(getPeersMessage);
						if (peersMessage == null)
							continue;

						int foreignPeersCounter = 0;
						// FOR ALL THE RECEIVED PEERS
						for (Peer newPeer : peersMessage.getPeers()) {
							// CHECK IF WE ALREADY HAVE MAX CONNECTIONS
							if (!this.isRun || callback.getActiveConnections().size() >= Settings.getInstance().getMaxConnections())
								break;

							// We only process a maximum number of proposed peers
							if (foreignPeersCounter >= maxReceivePeers)
								break;

							foreignPeersCounter++;

							// CHECK IF THAT PEER IS NOT BLACKLISTED
							if (PeerManager.getInstance().isBlacklisted(newPeer))
								continue;

							// CHECK IF CONNECTED
							if (callback.isConnectedTo(newPeer))
								continue;

							// Check peer's address is not loopback, localhost, one of ours, etc.
							if (Network.isHostLocalAddress(newPeer.getAddress()))
								continue;

							// Don't connect to "bad" peers (unless settings say otherwise)
							if (!Settings.getInstance().isTryingConnectToBadPeers() && newPeer.isBad())
								continue;

							int maxReceivePeersForPrint = (maxReceivePeers > peersMessage.getPeers().size()) ? peersMessage.getPeers().size() : maxReceivePeers;

							LOGGER.info(Lang.getInstance()
									.translate("Connecting to peer %newpeer% proposed by %peer% :: %foreignPeersCounter% / %maxReceivePeersForPrint% / %allReceivePeers% :: Connections: %activeConnections%")
									.replace("%newpeer%", newPeer.getAddress().getHostAddress())
									.replace("%peer%", peer.getAddress().getHostAddress())
									.replace("%foreignPeersCounter%", String.valueOf(foreignPeersCounter))
									.replace("%maxReceivePeersForPrint%", String.valueOf(maxReceivePeersForPrint))
									.replace("%allReceivePeers%", String.valueOf(peersMessage.getPeers().size()))
									.replace("%activeConnections%", String.valueOf(callback.getActiveConnections().size())));
							// CONNECT
							newPeer.connect(callback);
						}
					}
				}

				// SLEEP
				Thread.sleep(60 * 1000);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);

				LOGGER.info(Lang.getInstance().translate("Error creating new connection"));
			}
		}
	}

	public void halt() {
		this.isRun = false;
	}

}
