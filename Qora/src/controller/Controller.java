package controller;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.Longs;

import api.ApiClient;
import api.ApiService;
import at.AT;
import database.DBSet;
import database.LocalDataMap;
import database.SortableList;
import gui.Gui;
import network.Network;
import network.Peer;
import network.message.BlockMessage;
import network.message.GetBlockMessage;
import network.message.GetSignaturesMessage;
import network.message.HeightMessage;
import network.message.Message;
import network.message.MessageFactory;
import network.message.TransactionMessage;
import network.message.VersionMessage;
import ntp.NTP;
import qora.BlockChain;
import qora.BlockGenerator;
import qora.BlockGenerator.ForgingStatus;
import qora.Synchronizer;
import qora.TransactionCreator;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.assets.Order;
import qora.assets.Trade;
import qora.block.Block;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.payment.Payment;
import qora.transaction.Transaction;
import qora.voting.Poll;
import qora.voting.PollOption;
import qora.wallet.Wallet;
import settings.Settings;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;
import utils.SimpleFileVisitorForRecursiveFolderDeletion;
import utils.SysTray;
import utils.UpdateUtil;
import webserver.WebService;

public class Controller extends Observable {

	private String version = "0.26.0 beta";
	private String buildTime = "2016-02-19 00:00:00 UTC";
	private long buildTimestamp;
	
	public static final String releaseVersion = "0.26.0";

//	TODO ENUM would be better here
	public static final int STATUS_NO_CONNECTIONS = 0;
	public static final int STATUS_SYNCHRONIZING = 1;
	public static final int STATUS_OK = 2;

	private boolean processingWalletSynchronize = false; 
	private int status;
	private Network network;
	private ApiService rpcService;
	private WebService webService;
	private BlockChain blockChain;
	private BlockGenerator blockGenerator;
	private Wallet wallet;
	private Synchronizer synchronizer;
	private TransactionCreator transactionCreator;
	private boolean needSync = false;
	private Timer timer = new Timer();
	private Timer timerPeerHeightUpdate = new Timer();
	private Random random = new SecureRandom();
	private byte[] foundMyselfID = new byte[128];
	private byte[] messageMagic;
	private long toOfflineTime; 
	
	private Map<Peer, Integer> peerHeight;

	private Map<Peer, Pair<String, Long>> peersVersions;

	private static Controller instance;

	public boolean isProcessingWalletSynchronize() {
		return processingWalletSynchronize;
	}
	
	public void setProcessingWalletSynchronize(boolean isPocessing) {
		this.processingWalletSynchronize = isPocessing;
	}
	
	public String getVersion() {
		return version;
	}

	public int getNetworkPort() {
		if(Settings.getInstance().isTestnet()) {
			return Network.TESTNET_PORT;
		} else {
			return Network.MAINNET_PORT;
		}
	}
	
	public String getBuildDateTimeString(){
		return DateTimeFormat.timestamptoString(this.getBuildTimestamp(), "yyyy-MM-dd HH:mm:ss z", "UTC");
	}
	
	public String getBuildDateString(){
		return DateTimeFormat.timestamptoString(this.getBuildTimestamp(), "yyyy-MM-dd", "UTC");
	}
	
	public long getBuildTimestamp() {
	    if(this.buildTimestamp == 0) {
		    Date date = new Date();
		    URL resource = getClass().getResource(getClass().getSimpleName() + ".class");
		    if (resource != null) {
		        if (!resource.getProtocol().equals("file")) {
		        	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		        	try {
						date = (Date)formatter.parse(this.buildTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
		        }
		    }
		    this.buildTimestamp = date.getTime();
	    }
	    return this.buildTimestamp;
	}
	
	public byte[] getMessageMagic() {
		if(this.messageMagic == null) {
			long longTestNetStamp = Settings.getInstance().getGenesisStamp();
			if(Settings.getInstance().isTestnet()){
				byte[] seedTestNetStamp = Crypto.getInstance().digest(Longs.toByteArray(longTestNetStamp));
				this.messageMagic =  Arrays.copyOfRange(seedTestNetStamp, 0, Message.MAGIC_LENGTH);	
			} else {
				this.messageMagic = Message.MAINNET_MAGIC;
			}
		}
		return this.messageMagic;
	}
	
	public void statusInfo()
	{
		Logger.getGlobal().info(
			"STATUS OK\n" 
			+ "| Last Block Signature: " + Base58.encode(this.blockChain.getLastBlock().getSignature()) + "\n"
			+ "| Last Block Height: " + this.blockChain.getLastBlock().getHeight() + "\n"
			+ "| Last Block Time: " + DateTimeFormat.timestamptoString(this.blockChain.getLastBlock().getTimestamp()) + "\n"
			+ "| Last Block Found " + DateTimeFormat.timeAgo(this.blockChain.getLastBlock().getTimestamp()) + " ago."
			);
	}
	
	public byte[] getFoundMyselfID() {
		return this.foundMyselfID;
	}
	
	public int getWalletSyncHeight()
	{
		return this.wallet.getSyncHeight();
	}
	
	public void getSendMyHeightToPeer (Peer peer) {
	
		// GET HEIGHT
		int height = this.blockChain.getHeight();
				
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHeightMessage(
				height));
	}
	
	public Map<Peer, Integer> getPeerHeights() {
		return peerHeight;
	}
	
	public Integer getHeightOfPeer(Peer peer) {
		if(peerHeight!=null && peerHeight.containsKey(peer)){
			return peerHeight.get(peer);
		}
		else
		{
			return 0;
		}
	}
	
	public Map<Peer, Pair<String, Long>> getPeersVersions() {
		return peersVersions;
	}
	
	public Pair<String, Long> getVersionOfPeer(Peer peer) {
		if(peerHeight!=null && peersVersions.containsKey(peer)){
			return peersVersions.get(peer);
		}
		else
		{
			return new Pair<String, Long>("", 0l); 
			//\u22640.24.0
		}
	}

	public static Controller getInstance() {
		if (instance == null) {
			instance = new Controller();
		}

		return instance;
	}

	public int getStatus() {
		return this.status;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}
	
	public boolean isNeedSync() {
		return this.needSync;
	}
	
	public void start() throws Exception {
		
		this.toOfflineTime = NTP.getTime();
		
		this.foundMyselfID = new byte[128];
		this.random.nextBytes(this.foundMyselfID);
		
		// CHECK NETWORK PORT AVAILABLE
		if (!Network.isPortAvailable(Controller.getInstance().getNetworkPort())) {
			throw new Exception("Network port " + Controller.getInstance().getNetworkPort()
					+ " already in use!");
		}

		// CHECK RPC PORT AVAILABLE
		if (Settings.getInstance().isRpcEnabled()) {
			if (!Network.isPortAvailable(Settings.getInstance().getRpcPort())) {
				throw new Exception("Rpc port "
						+ Settings.getInstance().getRpcPort()
						+ " already in use!");
			}
		}

		// CHECK WEB PORT AVAILABLE
		if (Settings.getInstance().isWebEnabled()) {
			if (!Network.isPortAvailable(Settings.getInstance().getWebPort())) {
				System.out.println("Web port "
						+ Settings.getInstance().getWebPort()
						+ " already in use!");
			}
		}

		this.peerHeight = new LinkedHashMap<Peer, Integer>(); // LINKED TO
																// PRESERVE
																// ORDER WHEN
																// SYNCHRONIZING
																// (PRIORITIZE
																// SYNCHRONIZING
																// FROM LONGEST
																// CONNECTION
																// ALIVE)
		
		this.peersVersions = new LinkedHashMap<Peer, Pair<String, Long>>();
		
		this.status = STATUS_NO_CONNECTIONS;
		this.transactionCreator = new TransactionCreator();

		// OPENING DATABASES
		try {
			DBSet.getInstance();
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Error during startup detected trying to restore backup database...");
			reCreateDB();
		}

//		startFromScratchOnDemand();

		if (DBSet.getInstance().getBlockMap().isProcessing()) {
			try {
				DBSet.getInstance().close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			reCreateDB();
		}
		
		//CHECK IF DB NEEDS UPDATE

		if(DBSet.getInstance().getBlockMap().getLastBlockSignature() != null)
		{
			//CHECK IF NAME STORAGE NEEDS UPDATE
			if (DBSet.getInstance().getLocalDataMap().get("nsupdate") == null )
			{
				//FIRST NAME STORAGE UPDATE
				UpdateUtil.repopulateNameStorage( 70000 );
				DBSet.getInstance().getLocalDataMap().set("nsupdate", "1");
			}
			//CREATE TRANSACTIONS FINAL MAP
			if (DBSet.getInstance().getLocalDataMap().get("txfinalmap") == null )
			{
				//FIRST NAME STORAGE UPDATE
				UpdateUtil.repopulateTransactionFinalMap(  );
				DBSet.getInstance().getLocalDataMap().set("txfinalmap", "1");
			}
			
			if (DBSet.getInstance().getLocalDataMap().get("blogpostmap") == null ||  !DBSet.getInstance().getLocalDataMap().get("blogpostmap").equals("2"))
			{
				//recreate comment postmap
				UpdateUtil.repopulateCommentPostMap();
				DBSet.getInstance().getLocalDataMap().set("blogpostmap", "2");
			}
		} else {
			DBSet.getInstance().getLocalDataMap().set("nsupdate", "1");
			DBSet.getInstance().getLocalDataMap().set("txfinalmap", "1");
			DBSet.getInstance().getLocalDataMap().set("blogpostmap", "2");
		}
		
		// CREATE SYNCHRONIZOR
		this.synchronizer = new Synchronizer();

		// CREATE BLOCKCHAIN
		this.blockChain = new BlockChain();
		
		// START API SERVICE
		if (Settings.getInstance().isRpcEnabled()) {
			this.rpcService = new ApiService();
			this.rpcService.start();
		}

		// START WEB SERVICE
		if (Settings.getInstance().isWebEnabled()) {
			this.webService = new WebService();
			this.webService.start();
		}

		// CREATE WALLET
		this.wallet = new Wallet();

	    if(this.wallet.isWalletDatabaseExisting()){
	    	this.wallet.initiateAssetsFavorites();
	    }
	    
		if(Settings.getInstance().isTestnet() && this.wallet.isWalletDatabaseExisting() && this.wallet.getAccounts().size() > 0) {
			this.wallet.synchronize();	
		}
		
		// CREATE BLOCKGENERATOR
		this.blockGenerator = new BlockGenerator();
		// START BLOCKGENERATOR
		this.blockGenerator.start();

		// CREATE NETWORK
		this.network = new Network();

		// CLOSE ON UNEXPECTED SHUTDOWN
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopAll();
			}
		});
		
		
		//TIMER TO SEND HEIGHT TO NETWORK EVERY 5 MIN 
		
		this.timerPeerHeightUpdate.cancel();
		this.timerPeerHeightUpdate = new Timer();
		
		TimerTask action = new TimerTask() {
	        public void run() {
	        	if(Controller.getInstance().getStatus() == STATUS_OK)
	        	{
	        		if(Controller.getInstance().getActivePeers().size() > 0)
	        		{
	        			Peer peer = Controller.getInstance().getActivePeers().get(
	        				random.nextInt( Controller.getInstance().getActivePeers().size() )
	        				);
	        			if(peer != null){
	        				Controller.getInstance().getSendMyHeightToPeer(peer);
	        			}
	        		}
	        	}
	        }
		};
		
		this.timerPeerHeightUpdate.schedule(action, 5*60*1000, 5*60*1000);

		// REGISTER DATABASE OBSERVER
		this.addObserver(DBSet.getInstance().getTransactionMap());
		this.addObserver(DBSet.getInstance());
	}

	public void replaseAssetsFavorites() {
		if(this.wallet != null) {
			this.wallet.replaseAssetFavorite();
		}
	}
	
	public void reCreateDB() throws IOException, Exception {
		reCreateDB(true);
	}
	
	public void reCreateDB(boolean useDataBak) throws IOException, Exception {
		

		File dataDir = new File(Settings.getInstance().getDataDir());
		if (dataDir.exists()) {
			// delete data folder
			java.nio.file.Files.walkFileTree(dataDir.toPath(),
					new SimpleFileVisitorForRecursiveFolderDeletion());
			File dataBak = getDataBakDir(dataDir);
			if (useDataBak && dataBak.exists()
					&& Settings.getInstance().isCheckpointingEnabled()) {
				FileUtils.copyDirectory(dataBak, dataDir);
				System.out.println("restoring backup database");
				DBSet.reCreateDatabase();
				
			} else {
				DBSet.reCreateDatabase();
			}

		}

		if (DBSet.getInstance().getBlockMap().isProcessing()) {
			throw new Exception(
					"The application was not closed correctly! Delete the folder "
							+ dataDir.getAbsolutePath()
							+ " and start the application again.");
		}
	}

	public void startFromScratchOnDemand() throws IOException {
		String dataVersion = DBSet.getInstance().getLocalDataMap()
				.get(LocalDataMap.LOCAL_DATA_VERSION_KEY);

		if (dataVersion == null || !dataVersion.equals(releaseVersion)) {
			File dataDir = new File(Settings.getInstance().getDataDir());
			File dataBak = getDataBakDir(dataDir);
			DBSet.getInstance().close();

			if (dataDir.exists()) {
				// delete data folder
				java.nio.file.Files.walkFileTree(dataDir.toPath(),
						new SimpleFileVisitorForRecursiveFolderDeletion());

			}

			if (dataBak.exists()) {
				// delete data folder
				java.nio.file.Files.walkFileTree(dataBak.toPath(),
						new SimpleFileVisitorForRecursiveFolderDeletion());
			}
			DBSet.reCreateDatabase();

			DBSet.getInstance()
					.getLocalDataMap()
					.set(LocalDataMap.LOCAL_DATA_VERSION_KEY,
							Controller.releaseVersion);

		}
	}

	private File getDataBakDir(File dataDir) {
		return new File(dataDir.getParent(), "dataBak");
	}

	public void rpcServiceRestart() {
		this.rpcService.stop();

		// START API SERVICE
		if (Settings.getInstance().isRpcEnabled()) {
			this.rpcService = new ApiService();
			this.rpcService.start();
		}
	}

	public void webServiceRestart() {
		this.webService.stop();

		// START API SERVICE
		if (Settings.getInstance().isWebEnabled()) {
			this.webService = new WebService();
			this.webService.start();
		}
	}

	@Override
	public void addObserver(Observer o) {
		// ADD OBSERVER TO SYNCHRONIZER
		// this.synchronizer.addObserver(o);
		DBSet.getInstance().getBlockMap().addObserver(o);

		// ADD OBSERVER TO BLOCKGENERATOR
		// this.blockGenerator.addObserver(o);
		DBSet.getInstance().getTransactionMap().addObserver(o);

		// ADD OBSERVER TO NAMESALES
		DBSet.getInstance().getNameExchangeMap().addObserver(o);

		// ADD OBSERVER TO POLLS
		DBSet.getInstance().getPollMap().addObserver(o);

		// ADD OBSERVER TO ASSETS
		DBSet.getInstance().getAssetMap().addObserver(o);

		// ADD OBSERVER TO ORDERS
		DBSet.getInstance().getOrderMap().addObserver(o);

		// ADD OBSERVER TO TRADES
		DBSet.getInstance().getTradeMap().addObserver(o);

		// ADD OBSERVER TO BALANCES
		DBSet.getInstance().getBalanceMap().addObserver(o);

		// ADD OBSERVER TO ATMAP
		DBSet.getInstance().getATMap().addObserver(o);

		// ADD OBSERVER TO ATTRANSACTION MAP
		DBSet.getInstance().getATTransactionMap().addObserver(o);

		// ADD OBSERVER TO CONTROLLER
		super.addObserver(o);
		o.update(this, new ObserverMessage(ObserverMessage.NETWORK_STATUS,
				this.status));
	}

	@Override
	public void deleteObserver(Observer o) {
		DBSet.getInstance().getBlockMap().deleteObserver(o);

		super.deleteObserver(o);
	}

	public void deleteWalletObserver(Observer o) {
		this.wallet.deleteObserver(o);
	}

	private boolean isStopping = false;

	public void stopAll() {
		// PREVENT MULTIPLE CALLS
		if (!this.isStopping) {
			this.isStopping = true;

			// STOP MESSAGE PROCESSOR
			Logger.getGlobal().info("Stopping message processor");
			this.network.stop();

			// STOP BLOCK PROCESSOR
			Logger.getGlobal().info("Stopping block processor");
			this.synchronizer.stop();

			// CLOSE DATABABASE
			Logger.getGlobal().info("Closing database");
			DBSet.getInstance().close();

			// CLOSE WALLET
			Logger.getGlobal().info("Closing wallet");
			this.wallet.close();

			createDataCheckpoint();

			Logger.getGlobal().info("Closed.");
			// FORCE CLOSE
			System.exit(0);
		}
	}

	private void createDataCheckpoint() {
		if (!DBSet.getInstance().getBlockMap().isProcessing()
				&& Settings.getInstance().isCheckpointingEnabled()) {
			DBSet.getInstance().close();

			File dataDir = new File(Settings.getInstance().getDataDir());

			File dataBak = getDataBakDir(dataDir);

			if (dataDir.exists()) {
				if (dataBak.exists()) {
					try {
						Files.walkFileTree(
								dataBak.toPath(),
								new SimpleFileVisitorForRecursiveFolderDeletion());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					FileUtils.copyDirectory(dataDir, dataBak);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

	}

	// NETWORK

	public List<Peer> getActivePeers() {
		// GET ACTIVE PEERS
		return this.network.getActiveConnections();
	}

	public void walletStatusUpdate(int height) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.WALLET_SYNC_STATUS, height));
	}
		
	public long getToOfflineTime() {
		return this.toOfflineTime;
	}
	
	public void setToOfflineTime(long time) {
		this.toOfflineTime = time;
	}
		
	public void onConnect(Peer peer) {

		if(DBSet.getInstance().isStoped())
			return;
		
		// GET HEIGHT
		int height = this.blockChain.getHeight();

		if(NTP.getTime() >= Transaction.getPOWFIX_RELEASE())
		{
			// SEND FOUNDMYSELF MESSAGE
			peer.sendMessage( MessageFactory.getInstance().createFindMyselfMessage( 
				Controller.getInstance().getFoundMyselfID() 
				));

			// SEND VERSION MESSAGE
			peer.sendMessage( MessageFactory.getInstance().createVersionMessage( 
				Controller.getInstance().getVersion(),
				this.getBuildTimestamp() ));
		}
		
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHeightMessage(
				height));
		
		if (this.status == STATUS_NO_CONNECTIONS) {
			// UPDATE STATUS
			this.status = STATUS_OK;

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, this.status));
			
			this.actionAfterConnect();
		}
	}

	public void actionAfterConnect() 
	{
		this.timer.cancel();
		this.timer = new Timer();

		TimerTask action = new TimerTask() {
	        public void run() {
	        	
	        	if(Controller.getInstance().getStatus() == STATUS_OK)
		        {
	    			Controller.getInstance().statusInfo();

		        	Controller.getInstance().setToOfflineTime(0L);
		        	
			       	if(Controller.getInstance().isNeedSync() && !Controller.getInstance().isProcessingWalletSynchronize())
			       	{
			       		Controller.getInstance().synchronizeWallet();
			       	}
		        }
	        }
		};
			
		this.timer.schedule(action, Settings.getInstance().getConnectionTimeout());
	}
	
	
	public void forgingStatusChanged(ForgingStatus status) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.FORGING_STATUS, status));
	}

	public void onDisconnect(Peer peer) {
		synchronized (this.peerHeight) {
			
			this.peerHeight.remove(peer);
			
			this.peersVersions.remove(peer);
			
			if (this.peerHeight.size() == 0) {
				
				if(this.getToOfflineTime() == 0L) {
					//SET START OFFLINE TIME
					this.setToOfflineTime(NTP.getTime());
				}
				
				// UPDATE STATUS
				this.status = STATUS_NO_CONNECTIONS;

				
				// NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(
						ObserverMessage.NETWORK_STATUS, this.status));
			}
		}
	}

	public void onError(Peer peer) {
		this.onDisconnect(peer);
	}

	// SYNCHRONIZED DO NOT PROCESSS MESSAGES SIMULTANEOUSLY
	public void onMessage(Message message) {
		Message response;
		Block block;

		synchronized (this) {
			switch (message.getType()) {
			case Message.PING_TYPE:

				// CREATE PING
				response = MessageFactory.getInstance().createPingMessage();

				// SET ID
				response.setId(message.getId());

				// SEND BACK TO SENDER
				message.getSender().sendMessage(response);

				break;

			case Message.HEIGHT_TYPE:

				HeightMessage heightMessage = (HeightMessage) message;

				// ADD TO LIST
				synchronized (this.peerHeight) {
					this.peerHeight.put(heightMessage.getSender(),
							heightMessage.getHeight());
				}

				break;

			case Message.GET_SIGNATURES_TYPE:

				GetSignaturesMessage getHeadersMessage = (GetSignaturesMessage) message;

				// ASK SIGNATURES FROM BLOCKCHAIN
				List<byte[]> headers = this.blockChain
						.getSignatures(getHeadersMessage.getParent());

				// CREATE RESPONSE WITH SAME ID
				response = MessageFactory.getInstance().createHeadersMessage(
						headers);
				response.setId(message.getId());

				// SEND RESPONSE BACK WITH SAME ID
				message.getSender().sendMessage(response);

				break;

			case Message.GET_BLOCK_TYPE:

				GetBlockMessage getBlockMessage = (GetBlockMessage) message;

				// ASK BLOCK FROM BLOCKCHAIN
				block = this.blockChain
						.getBlock(getBlockMessage.getSignature());

				// CREATE RESPONSE WITH SAME ID
				response = MessageFactory.getInstance().createBlockMessage(
						block);
				response.setId(message.getId());

				// SEND RESPONSE BACK WITH SAME ID
				message.getSender().sendMessage(response);

				break;

			case Message.BLOCK_TYPE:

				BlockMessage blockMessage = (BlockMessage) message;

				// ASK BLOCK FROM BLOCKCHAIN
				block = blockMessage.getBlock();

				boolean isNewBlockValid = this.blockChain.isNewBlockValid(block);
				
				if(isNewBlockValid)	{
					synchronized (this.peerHeight) {
						this.peerHeight.put(message.getSender(),
								blockMessage.getHeight());
					}
				}
					
				if(this.isProcessingWalletSynchronize()) {
					
					break;
				}
				
				// CHECK IF VALID
				if (isNewBlockValid
						&& this.synchronizer.process(block)) {
					Logger.getGlobal().info("received new valid block");

					// PROCESS
					// this.synchronizer.process(block);

					// BROADCAST
					List<Peer> excludes = new ArrayList<Peer>();
					excludes.add(message.getSender());
					this.network.broadcast(message, excludes);

					// UPDATE ALL PEER HEIGHTS TO OUR HEIGHT
					/*
					 * synchronized(this.peerHeight) { for(Peer peer:
					 * this.peerHeight.keySet()) { this.peerHeight.put(peer,
					 * this.blockChain.getHeight()); } }
					 */
				} 

				break;

			case Message.TRANSACTION_TYPE:

				TransactionMessage transactionMessage = (TransactionMessage) message;

				// GET TRANSACTION
				Transaction transaction = transactionMessage.getTransaction();

				// CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
				if (!transaction.isSignatureValid()
						|| transaction.getType() == Transaction.GENESIS_TRANSACTION) {
					// DISHONEST PEER
					this.network.onError(message.getSender(), "invalid transaction signature");

					return;
				}

				// CHECK IF TRANSACTION HAS MINIMUM FEE AND MINIMUM FEE PER BYTE
				// AND UNCONFIRMED
				if (transaction.hasMinimumFee()
						&& transaction.hasMinimumFeePerByte()
						&& !DBSet.getInstance().getTransactionParentMap()
								.contains(transaction.getSignature())) {
					// ADD TO UNCONFIRMED TRANSACTIONS
					this.blockGenerator.addUnconfirmedTransaction(transaction);

					// NOTIFY OBSERVERS
					// this.setChanged();
					// this.notifyObservers(new
					// ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE,
					// DatabaseSet.getInstance().getTransactionsDatabase().getTransactions()));

					this.setChanged();
					this.notifyObservers(new ObserverMessage(
							ObserverMessage.ADD_TRANSACTION_TYPE, transaction));

					// BROADCAST
					List<Peer> excludes = new ArrayList<Peer>();
					excludes.add(message.getSender());
					this.network.broadcast(message, excludes);
				}

				break;
				
			case Message.VERSION_TYPE:

				VersionMessage versionMessage = (VersionMessage) message;

				// ADD TO LIST
				synchronized (this.peersVersions) {
					this.peersVersions.put(versionMessage.getSender(),
							new Pair<String, Long>(versionMessage.getStrVersion(), versionMessage.getBuildDateTime()) );
				}

				break;
			}
		}
	}

	public void addActivePeersObserver(Observer o) {
		this.network.addObserver(o);
	}

	public void removeActivePeersObserver(Observer o) {
		this.network.deleteObserver(o);
	}

	private void broadcastBlock(Block newBlock) {

		// CREATE MESSAGE
		Message message = MessageFactory.getInstance().createBlockMessage(
				newBlock);

		// BROADCAST MESSAGE
		List<Peer> excludes = new ArrayList<Peer>();
		this.network.broadcast(message, excludes);
	}

	private void broadcastTransaction(Transaction transaction) {

		if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {
			// CREATE MESSAGE
			Message message = MessageFactory.getInstance()
					.createTransactionMessage(transaction);

			// BROADCAST MESSAGE
			List<Peer> excludes = new ArrayList<Peer>();
			this.network.broadcast(message, excludes);
		}
	}

	// SYNCHRONIZE

	public boolean isUpToDate() {
		if (this.peerHeight.size() == 0) {
			return true;
		}

		int maxPeerHeight = this.getMaxPeerHeight();
		int chainHeight = this.blockChain.getHeight();
		return maxPeerHeight <= chainHeight;
	}
	
	public boolean isNSUpToDate() {
		return !Settings.getInstance().updateNameStorage();
	}

	public void update() {
		// UPDATE STATUS
		this.status = STATUS_SYNCHRONIZING;

		// NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.NETWORK_STATUS, this.status));
		
		Peer peer = null;
		try {
			// WHILE NOT UPTODATE
			while (!this.isUpToDate()) {
				// START UPDATE FROM HIGHEST HEIGHT PEER
				peer = this.getMaxHeightPeer();

				// SYNCHRONIZE FROM PEER
				this.synchronizer.synchronize(peer);
			}
		} catch (Exception e) {
			e.printStackTrace();

			if (peer != null) {
				// DISHONEST PEER
				this.network.onError(peer, e.getMessage());
			}
		}

		if (this.peerHeight.size() == 0) {
			// UPDATE STATUS
			this.status = STATUS_NO_CONNECTIONS;

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, this.status));
		} else {
			// UPDATE STATUS
			this.status = STATUS_OK;

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, this.status));
			
			Controller.getInstance().statusInfo();
		}
	}

	private Peer getMaxHeightPeer() {
		Peer highestPeer = null;
		int height = 0;

		try {
			synchronized (this.peerHeight) {
				for (Peer peer : this.peerHeight.keySet()) {
					if (highestPeer == null && peer != null) {
						highestPeer = peer;
					} else {
						// IF HEIGHT IS BIGGER
						if (height < this.peerHeight.get(peer)) {
							highestPeer = peer;
							height = this.peerHeight.get(peer);
						}

						// IF HEIGHT IS SAME
						if (height == this.peerHeight.get(peer)) {
							// CHECK IF PING OF PEER IS BETTER
							if (peer.getPing() < highestPeer.getPing()) {
								highestPeer = peer;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// PEER REMOVED WHILE ITERATING
		}

		return highestPeer;
	}

	public int getMaxPeerHeight() {
		int height = 0;

		try {
			synchronized (this.peerHeight) {
				for (Peer peer : this.peerHeight.keySet()) {
					if (height < this.peerHeight.get(peer)) {
						height = this.peerHeight.get(peer);
					}
				}
			}
		} catch (Exception e) {
			// PEER REMOVED WHILE ITERATING
		}

		return height;
	}

	// WALLET

	public boolean doesWalletExists() {
		// CHECK IF WALLET EXISTS
		return this.wallet.exists();
	}

	public boolean doesWalletDatabaseExists() {
		return wallet != null && this.wallet.isWalletDatabaseExisting();
	}

	public boolean createWallet(byte[] seed, String password, int amount) {
		// IF NEW WALLET CREADED
		return this.wallet.create(seed, password, amount, false);
	}
	
	public boolean recoverWallet(byte[] seed, String password, int amount) {
		if(this.wallet.create(seed, password, amount, false))
		{
			Logger.getGlobal().info("Wallet needs to synchronize!");
			this.actionAfterConnect();
			this.setNeedSync(true);

			return true;
		}
		else
			return false;
	}

	public List<Account> getAccounts() {

		return this.wallet.getAccounts();
	}

	public List<PrivateKeyAccount> getPrivateKeyAccounts() {

		return this.wallet.getprivateKeyAccounts();
	}

	public String generateNewAccount() {
		return this.wallet.generateNewAccount();
	}

	public PrivateKeyAccount getPrivateKeyAccountByAddress(String address) {
		if(this.doesWalletExists()) {
			return this.wallet.getPrivateKeyAccount(address);
		} else {
			return null;
		}
	}

	public Account getAccountByAddress(String address) {
		if(this.doesWalletExists()) {
			return this.wallet.getAccount(address);
		} else {
			return null;
		}
	}

	public BigDecimal getUnconfirmedBalance(String address) {
		return this.wallet.getUnconfirmedBalance(address);
	}

	public void addWalletListener(Observer o) {
		this.wallet.addObserver(o);
	}

	public String importAccountSeed(byte[] accountSeed) {
		return this.wallet.importAccountSeed(accountSeed);
	}

	public byte[] exportAccountSeed(String address) {
		return this.wallet.exportAccountSeed(address);
	}

	public byte[] exportSeed() {
		return this.wallet.exportSeed();
	}

	public boolean deleteAccount(PrivateKeyAccount account) {
		return this.wallet.deleteAccount(account);
	}

	public void synchronizeWallet() {
		this.wallet.synchronize();
	}

	public boolean isWalletUnlocked() {
		return this.wallet.isUnlocked();
	}

	public int checkAPICallAllowed(String json, HttpServletRequest request)
			throws Exception {
		int result = 0;

		if (request != null) {
			Enumeration<String> headers = request
					.getHeaders(ApiClient.APICALLKEY);
			String uuid = null;
			if (headers.hasMoreElements()) {
				uuid = headers.nextElement();
				if (ApiClient.isAllowedDebugWindowCall(uuid)) {
					return ApiClient.SELF_CALL;
				}
			}
		}

		if (!GraphicsEnvironment.isHeadless() &&  (Settings.getInstance().isGuiEnabled() || Settings.getInstance().isSysTrayEnabled()) ) {
			Gui gui = Gui.getInstance();
			SysTray.getInstance().sendMessage("INCOMING API CALL",
					"An API call needs authorization!", MessageType.WARNING);
			Object[] options = { "Yes", "No" };

			 StringBuilder sb = new StringBuilder("Permission Request: ");
	            sb.append("Do you want to authorize the following API call?\n\n"
						+ json);
	            JTextArea jta = new JTextArea(sb.toString());
	            jta.setLineWrap(true);
	            jta.setEditable(false);
	            JScrollPane jsp = new JScrollPane(jta){
	                /**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
	                public Dimension getPreferredSize() {
	                    return new Dimension(480, 200);
	                }
	            };

			gui.bringtoFront();
			
			result = JOptionPane
					.showOptionDialog(gui,
							jsp, "INCOMING API CALL",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[1]);
		}

		return result;
	}

	public boolean lockWallet() {
		return this.wallet.lock();
	}

	public boolean unlockWallet(String password) {
		return this.wallet.unlock(password);
	}

	public void setSecondsToUnlock(int seconds) {
		this.wallet.setSecondsToUnlock(seconds);
	}

	public List<Pair<Account, Transaction>> getLastTransactions(int limit) {
		return this.wallet.getLastTransactions(limit);
	}

	public Transaction getTransaction(byte[] signature) {

		return getTransaction(signature, DBSet.getInstance());
	}
	
	public Transaction getTransaction(byte[] signature, DBSet database) {
		
		// CHECK IF IN BLOCK
		Block block = database.getTransactionParentMap()
				.getParent(signature);
		if (block != null) {
			return block.getTransaction(signature);
		}
		
		// CHECK IF IN TRANSACTION DATABASE
		return database.getTransactionMap().get(signature);
	}

	public List<Transaction> getLastTransactions(Account account, int limit) {
		return this.wallet.getLastTransactions(account, limit);
	}

	public List<Pair<Account, Block>> getLastBlocks() {
		return this.wallet.getLastBlocks();
	}

	public List<Block> getLastBlocks(Account account) {
		return this.wallet.getLastBlocks(account);
	}

	public List<Pair<Account, Name>> getNames() {
		return this.wallet.getNames();
	}

	public List<Name> getNamesAsList() {
		List<Pair<Account, Name>> names = this.wallet.getNames();
		List<Name> result = new ArrayList<>();
		for (Pair<Account, Name> pair : names) {
			result.add(pair.getB());
		}

		return result;

	}

	public List<String> getNamesAsListAsString() {
		List<Name> namesAsList = getNamesAsList();
		List<String> results = new ArrayList<String>();
		for (Name name : namesAsList) {
			results.add(name.getName());
		}
		return results;

	}

	public List<Name> getNames(Account account) {
		return this.wallet.getNames(account);
	}

	public List<Pair<Account, NameSale>> getNameSales() {
		return this.wallet.getNameSales();
	}

	public List<NameSale> getNameSales(Account account) {
		return this.wallet.getNameSales(account);
	}

	public List<NameSale> getAllNameSales() {
		return DBSet.getInstance().getNameExchangeMap().getNameSales();
	}

	public List<Pair<Account, Poll>> getPolls() {
		return this.wallet.getPolls();
	}

	public List<Poll> getPolls(Account account) {
		return this.wallet.getPolls(account);
	}

	public void addAssetFavorite(Asset asset) {
		this.wallet.addAssetFavorite(asset);
	}

	public void removeAssetFavorite(Asset asset) {
		this.wallet.removeAssetFavorite(asset);
	}

	public boolean isAssetFavorite(Asset asset) {
		return this.wallet.isAssetFavorite(asset);
	}

	public Collection<Poll> getAllPolls() {
		return DBSet.getInstance().getPollMap().getValues();
	}

	public Collection<Asset> getAllAssets() {
		return DBSet.getInstance().getAssetMap().getValues();
	}

	public void onDatabaseCommit() {
		this.wallet.commit();
	}

	public ForgingStatus getForgingStatus() {
		return this.blockGenerator.getForgingStatus();
	}

	// BLOCKCHAIN

	public int getHeight() {
		return this.blockChain.getHeight();
	}

	public Block getLastBlock() {
		return this.blockChain.getLastBlock();
	}
	
	public byte[] getWalletLastBlockSign() {
		return this.wallet.getLastBlockSignature();
	}
	
	public Block getBlock(byte[] header) {
		return this.blockChain.getBlock(header);
	}

	public Pair<Block, List<Transaction>> scanTransactions(Block block,
			int blockLimit, int transactionLimit, int type, int service,
			Account account) {
		return this.blockChain.scanTransactions(block, blockLimit,
				transactionLimit, type, service, account);

	}

	public long getNextBlockGeneratingBalance() {
		return BlockGenerator.getNextBlockGeneratingBalance(
				DBSet.getInstance(), DBSet.getInstance().getBlockMap()
						.getLastBlock());
	}

	public long getNextBlockGeneratingBalance(Block parent) {
		return BlockGenerator.getNextBlockGeneratingBalance(
				DBSet.getInstance(), parent);
	}

	// FORGE

	public void newBlockGenerated(Block newBlock) {

		this.synchronizer.process(newBlock);

		// BROADCAST
		this.broadcastBlock(newBlock);
	}

	public List<Transaction> getUnconfirmedTransactions() {
		return this.blockGenerator.getUnconfirmedTransactions();
	}

	// BALANCES

	public SortableList<Tuple2<String, Long>, BigDecimal> getBalances(long key) {
		return DBSet.getInstance().getBalanceMap().getBalancesSortableList(key);
	}

	public SortableList<Tuple2<String, Long>, BigDecimal> getBalances(
			Account account) {
		return DBSet.getInstance().getBalanceMap()
				.getBalancesSortableList(account);
	}

	// NAMES

	public Name getName(String nameName) {
		return DBSet.getInstance().getNameMap().get(nameName);
	}

	public NameSale getNameSale(String nameName) {
		return DBSet.getInstance().getNameExchangeMap().getNameSale(nameName);
	}

	// POLLS

	public Poll getPoll(String name) {
		return DBSet.getInstance().getPollMap().get(name);
	}

	// ASSETS

	public Asset getQoraAsset() {
		return DBSet.getInstance().getAssetMap().get(0l);
	}

	public Asset getAsset(long key) {
		return DBSet.getInstance().getAssetMap().get(key);
	}

	public SortableList<BigInteger, Order> getOrders(Asset have, Asset want) {
		return this.getOrders(have, want, false);
	}

	public SortableList<BigInteger, Order> getOrders(Asset have, Asset want, boolean filter) {
		return DBSet.getInstance().getOrderMap()
				.getOrdersSortableList(have.getKey(), want.getKey(), filter);
	}
	
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(
			Asset have, Asset want) {
		return DBSet.getInstance().getTradeMap()
				.getTradesSortableList(have.getKey(), want.getKey());
	}

	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(
			Order order) {
		return DBSet.getInstance().getTradeMap().getTrades(order);
	}

	// ATs

	public SortableList<String, AT> getAcctATs(String type, boolean initiators) {
		return DBSet.getInstance().getATMap().getAcctATs(type, initiators);
	}

	// TRANSACTIONS

	public void onTransactionCreate(Transaction transaction) {
		// ADD TO UNCONFIRMED TRANSACTIONS
		this.blockGenerator.addUnconfirmedTransaction(transaction);

		// NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.LIST_TRANSACTION_TYPE, DBSet.getInstance()
						.getTransactionMap().getValues()));

		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.ADD_TRANSACTION_TYPE, transaction));

		// BROADCAST
		this.broadcastTransaction(transaction);
	}

	public Pair<Transaction, Integer> sendPayment(PrivateKeyAccount sender,
			Account recipient, BigDecimal amount, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createPayment(sender, recipient,
					amount, fee);
		}
	}

	public Pair<Transaction, Integer> registerName(
			PrivateKeyAccount registrant, Account owner, String name,
			String value, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNameRegistration(registrant,
					new Name(owner, name, value), fee);
		}
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameRegistration(
			String name, String value) {
		return this.transactionCreator
				.calcRecommendedFeeForNameRegistration(new Name(new Account(
						"QLpLzqs4DW1FNJByeJ63qaqw3eAYCxfkjR"), name, value)); // FOR
																				// GENESIS
																				// ADDRESS
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameUpdate(
			String name, String value) {
		return this.transactionCreator
				.calcRecommendedFeeForNameUpdate(new Name(new Account(
						"QLpLzqs4DW1FNJByeJ63qaqw3eAYCxfkjR"), name, value)); // FOR
																				// GENESIS
																				// ADDRESS
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForPoll(String name,
			String description, List<String> options) {
		// CREATE ONLY ONE TRANSACTION AT A TIME

		// CREATE POLL OPTIONS
		List<PollOption> pollOptions = new ArrayList<PollOption>();
		for (String option : options) {
			pollOptions.add(new PollOption(option));
		}

		// CREATE POLL
		Poll poll = new Poll(new Account("QLpLzqs4DW1FNJByeJ63qaqw3eAYCxfkjR"),
				name, description, pollOptions);

		return this.transactionCreator.calcRecommendedFeeForPollCreation(poll);
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForArbitraryTransaction(
			byte[] data, List<Payment> payments) {
		
		if(payments == null) {
			payments = new ArrayList<Payment>();
		}
		
		return this.transactionCreator
				.calcRecommendedFeeForArbitraryTransaction(data, payments);
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForMessage(byte[] message) {
		return this.transactionCreator.calcRecommendedFeeForMessage(message);
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForPayment() {
		return this.transactionCreator.calcRecommendedFeeForPayment();
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForAssetTransfer() {
		return this.transactionCreator.calcRecommendedFeeForAssetTransfer();
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForOrderTransaction() {
		return this.transactionCreator.calcRecommendedFeeForOrderTransaction();
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForCancelOrderTransaction() {
		return this.transactionCreator
				.calcRecommendedFeeForCancelOrderTransaction();
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameSale(String name) {
		return this.transactionCreator
				.calcRecommendedFeeForNameSale(new NameSale(name,
						Transaction.MINIMUM_FEE));
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForNamePurchase(
			String name) {
		return this.transactionCreator
				.calcRecommendedFeeForNamePurchase(new NameSale(name,
						Transaction.MINIMUM_FEE));
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForCancelNameSale(
			String name) {
		return this.transactionCreator
				.calcRecommendedFeeForCancelNameSale(new NameSale(name,
						Transaction.MINIMUM_FEE));
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForPollVote(
			String pollName) {
		return this.transactionCreator.calcRecommendedFeeForPollVote(pollName);
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForIssueAssetTransaction(
			String name, String description) {
		return this.transactionCreator
				.calcRecommendedFeeForIssueAssetTransaction(name, description);
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForMultiPayment(
			List<Payment> payments) {
		return this.transactionCreator
				.calcRecommendedFeeForMultiPayment(payments);
	}

	public Pair<BigDecimal, Integer> calcRecommendedFeeForDeployATTransaction(
			String name, String description, String type, String tags,
			byte[] creationBytes) {
		return this.transactionCreator
				.calcRecommendedFeeForDeployATTransaction(name, description,
						type, tags, creationBytes);
	}

	public Pair<Transaction, Integer> updateName(PrivateKeyAccount owner,
			Account newOwner, String name, String value, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNameUpdate(owner, new Name(
					newOwner, name, value), fee);
		}
	}

	public Pair<Transaction, Integer> sellName(PrivateKeyAccount owner,
			String name, BigDecimal amount, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNameSale(owner, new NameSale(
					name, amount), fee);
		}
	}

	public Pair<Transaction, Integer> cancelSellName(PrivateKeyAccount owner,
			NameSale nameSale, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createCancelNameSale(owner,
					nameSale, fee);
		}
	}

	public Pair<Transaction, Integer> BuyName(PrivateKeyAccount buyer,
			NameSale nameSale, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNamePurchase(buyer, nameSale,
					fee);
		}
	}

	public Pair<Transaction, Integer> createPoll(PrivateKeyAccount creator,
			String name, String description, List<String> options,
			BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			// CREATE POLL OPTIONS
			List<PollOption> pollOptions = new ArrayList<PollOption>();
			for (String option : options) {
				pollOptions.add(new PollOption(option));
			}

			// CREATE POLL
			Poll poll = new Poll(creator, name, description, pollOptions);

			return this.transactionCreator.createPollCreation(creator, poll,
					fee);
		}
	}

	public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator,
			Poll poll, PollOption option, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			// GET OPTION INDEX
			int optionIndex = poll.getOptions().indexOf(option);

			return this.transactionCreator.createPollVote(creator,
					poll.getName(), optionIndex, fee);
		}
	}

	public Pair<Transaction, Integer> createArbitraryTransaction(
			PrivateKeyAccount creator, List<Payment> payments, int service, byte[] data, BigDecimal fee) {
		
		if(payments == null) {
			payments = new ArrayList<Payment>();
		}
		
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createArbitraryTransaction(creator, payments,
					service, data, fee);
		}
	}

	public Pair<Transaction, Integer> createTransactionFromRaw(
			byte[] rawData) {
		
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createTransactionFromRaw(rawData);
		}
	}
	
	public Pair<Transaction, Integer> issueAsset(PrivateKeyAccount creator,
			String name, String description, long quantity, boolean divisible,
			BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueAssetTransaction(creator,
					name, description, quantity, divisible, fee);
		}
	}

	public Pair<Transaction, Integer> createOrder(PrivateKeyAccount creator,
			Asset have, Asset want, BigDecimal amount, BigDecimal price,
			BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createOrderTransaction(creator,
					have, want, amount, price, fee);
		}
	}

	public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator,
			Order order, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createCancelOrderTransaction(
					creator, order, fee);
		}
	}

	public Pair<Transaction, Integer> transferAsset(PrivateKeyAccount sender,
			Account recipient, Asset asset, BigDecimal amount, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createAssetTransfer(sender,
					recipient, asset, amount, fee);
		}
	}

	public Pair<Transaction, Integer> deployAT(PrivateKeyAccount creator,
			String name, String description, String type, String tags,
			byte[] creationBytes, BigDecimal quantity, BigDecimal fee) {

		synchronized (this.transactionCreator) {
			return this.transactionCreator.deployATTransaction(creator, name,
					description, type, tags, creationBytes, quantity, fee);
		}
	}

	public Pair<Transaction, Integer> sendMultiPayment(
			PrivateKeyAccount sender, List<Payment> payments, BigDecimal fee) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.sendMultiPayment(sender, payments,
					fee);
		}
	}

	public Pair<Transaction, Integer> sendMessage(PrivateKeyAccount sender,
			Account recipient, long key, BigDecimal amount, BigDecimal fee,
			byte[] isText, byte[] message, byte[] encryptMessage) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createMessage(sender, recipient,
					key, amount, fee, message, isText, encryptMessage);
		}

	}

	public Block getBlockByHeight(int parseInt) {
		byte[] b = DBSet.getInstance().getHeightMap().getBlockByHeight(parseInt);
		return DBSet.getInstance().getBlockMap().get(b);
	}

	public byte[] getPublicKeyByAddress(String address) {

		if (!Crypto.getInstance().isValidAddress(address)) {
			return null;
		}

		// CHECK ACCOUNT IN OWN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);
		if (account != null) {
			if (Controller.getInstance().isWalletUnlocked()) {
				return Controller.getInstance()
						.getPrivateKeyAccountByAddress(address).getPublicKey();
			}
		}

		if (!DBSet.getInstance().getReferenceMap().contains(address)) {
			return null;
		}

		Transaction transaction = Controller.getInstance().getTransaction(
				DBSet.getInstance().getReferenceMap().get(address));

		if (transaction == null) {
			return null;
		}

		return transaction.getCreator().getPublicKey();
	}
}
