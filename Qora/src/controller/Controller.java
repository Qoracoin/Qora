package controller;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import network.Network;
import network.Peer;
import network.message.BlockMessage;
import network.message.GetBlockMessage;
import network.message.GetSignaturesMessage;
import network.message.Message;
import network.message.MessageFactory;
import network.message.TransactionMessage;
import network.message.VersionMessage;

import org.mapdb.Fun.Tuple2;

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
import qora.crypto.Ed25519;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.payment.Payment;
import qora.transaction.Transaction;
import qora.voting.Poll;
import qora.voting.PollOption;
import qora.wallet.Wallet;
import settings.Settings;
import utils.ObserverMessage;
import utils.Pair;
import utils.SimpleFileVisitorForRecursiveFolderDeletion;
import api.ApiService;
import namewebserver.WebService;


import database.DBSet;
import database.SortableList;

public class Controller extends Observable {

	public static final int STATUS_NO_CONNECTIONS = 0;
	public static final int STATUS_SYNCHRONIZING = 1;
	public static final int STATUS_OKE = 2;
	
	private int status;
	private Network network;
	private ApiService rpcService;
	private WebService webService;
	private BlockChain blockChain;
	private BlockGenerator blockGenerator;
	private Wallet wallet;
	private Synchronizer synchronizer;
	private TransactionCreator transactionCreator;
	
	private Map<Peer, Integer> peerHeight;
	
	private static Controller instance;
	
	public Map<Peer, Integer> getPeerHeights()
	{
		return peerHeight;
	}
	
	public static Controller getInstance()
	{
		if(instance == null)
		{
			instance = new Controller();
		}
		
		return instance;
	}
	
	public int getStatus()
	{
		return this.status;
	}
	
	public void start(boolean disableRpc, boolean disableWeb) throws Exception
	{
		//CHECK NETWORK PORT AVAILABLE
		if(!Network.isPortAvailable(Network.PORT))
		{
			throw new Exception("Network port " + Network.PORT + " already in use!");
		}
		
		//CHECK RPC PORT AVAILABLE
		if(!disableRpc)
        {
        	if(!Network.isPortAvailable(Settings.getInstance().getRpcPort()))
    		{
    			throw new Exception("Rpc port " + Settings.getInstance().getRpcPort() + " already in use!");
    		}
        }
		
		if(!disableWeb)
        {
        	if(!Network.isPortAvailable(Settings.getInstance().getWebPort()))
    		{
    			throw new Exception("Web port " + Settings.getInstance().getWebPort() + " already in use!");
    		}
        }
				
		//LOAD NATIVE LIBRARIES
		if(!Ed25519.load())
		{
			throw new Exception("Failed to load native libraries!");
		}
		
		this.peerHeight = new LinkedHashMap<Peer, Integer>(); //LINKED TO PRESERVE ORDER WHEN SYNCHRONIZING (PRIORITIZE SYNCHRONIZING FROM LONGEST CONNECTION ALIVE)
		this.status = STATUS_NO_CONNECTIONS;
		this.transactionCreator = new TransactionCreator();

		//OPENING DATABASES
		DBSet.getInstance();
		if(DBSet.getInstance().getBlockMap().isProcessing())
		{
			DBSet.getInstance().close();
			
			File dataDir = new File(Settings.getInstance().getDataDir());
			if(dataDir.exists())
			{
				//delete data folder
				java.nio.file.Files.walkFileTree(dataDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
				DBSet.reCreateDatabase();
				
			}
			
			if(DBSet.getInstance().getBlockMap().isProcessing())
			{
				throw new Exception("The application was not closed correctly! Delete the folder " + dataDir.getAbsolutePath() + " and start the application again.");
			}
		}
		
		
		//CREATE SYNCHRONIZOR
		this.synchronizer = new Synchronizer();
		
		//CREATE BLOCKCHAIN
        this.blockChain = new BlockChain();
         
        
        if(!disableRpc)
        {
        	this.rpcService = new ApiService();
        	this.rpcService.start();
        }
        
        if(!disableWeb)
        {
        	this.webService = new WebService();
        	this.webService.start();
        }
        
        
        //CREATE WALLET
        this.wallet = new Wallet(); 
        
        //CREATE BLOCKGENERATOR
        this.blockGenerator = new BlockGenerator();
        //START BLOCKGENERATOR
        this.blockGenerator.start();
        
        //CREATE NETWORK
      	this.network = new Network();
      	
		//CLOSE ON UNEXPECTED SHUTDOWN
      	Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				stopAll();
			}			
		});
      	
      	//REGISTER DATABASE OBSERVER
      	this.addObserver(DBSet.getInstance().getTransactionMap());
      	this.addObserver(DBSet.getInstance());
    }
	
	@Override
	public void addObserver(Observer o) 
	{
		//ADD OBSERVER TO SYNCHRONIZER
		//this.synchronizer.addObserver(o);
		DBSet.getInstance().getBlockMap().addObserver(o);
		
		//ADD OBSERVER TO BLOCKGENERATOR
		//this.blockGenerator.addObserver(o);
		DBSet.getInstance().getTransactionMap().addObserver(o);
			
		//ADD OBSERVER TO NAMESALES
		DBSet.getInstance().getNameExchangeMap().addObserver(o);
		
		//ADD OBSERVER TO POLLS
		DBSet.getInstance().getPollMap().addObserver(o);
		
		//ADD OBSERVER TO ASSETS
		DBSet.getInstance().getAssetMap().addObserver(o);
		
		//ADD OBSERVER TO ORDERS
		DBSet.getInstance().getOrderMap().addObserver(o);
				
		//ADD OBSERVER TO TRADES
		DBSet.getInstance().getTradeMap().addObserver(o);
		
		//ADD OBSERVER TO BALANCES
		DBSet.getInstance().getBalanceMap().addObserver(o);
		
		//ADD OBSERVER TO CONTROLLER
		super.addObserver(o);
		o.update(this, new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
	}
	
	@Override 
	public void deleteObserver(Observer o)
	{
		DBSet.getInstance().getBlockMap().deleteObserver(o);
		
		super.deleteObserver(o);
	}
	
	public void deleteWalletObserver(Observer o)
	{
		this.wallet.deleteObserver(o);
	}
	
	private boolean isStopping = false;

	public void stopAll()
	{
		//PREVENT MULTIPLE CALLS
		if(!this.isStopping)
		{
			this.isStopping = true;
			
			//STOP MESSAGE PROCESSOR
			Logger.getGlobal().info("Stopping message processor");
			this.network.stop();
			
			//STOP BLOCK PROCESSOR
			Logger.getGlobal().info("Stopping block processor");
			this.synchronizer.stop();

			//CLOSE DATABABASE
			Logger.getGlobal().info("Closing database");
			DBSet.getInstance().close();
			
			//CLOSE WALLET
			Logger.getGlobal().info("Closing wallet");
			this.wallet.close();
			
			//FORCE CLOSE
			System.exit(0);
		}
	}
	
	//NETWORK
	
	public List<Peer> getActivePeers()
	{
		//GET ACTIVE PEERS
		return this.network.getActiveConnections();
	}
	
	public void onConnect(Peer peer) {
		
		//GET HEIGHT
		int height = this.blockChain.getHeight();
		
		//SEND VERSION MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createVersionMessage(height));	
		
		if(this.status == STATUS_NO_CONNECTIONS)
		{
			//UPDATE STATUS
			this.status = STATUS_OKE;
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
		}
	}
	
	
	public void forgingStatusChanged(ForgingStatus status)
	{
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.FORGING_STATUS, status));
	}
	
	public void onDisconnect(Peer peer) 
	{		
		synchronized(this.peerHeight)
		{
			this.peerHeight.remove(peer);		
				
			if(this.peerHeight.size() == 0)
			{
				//UPDATE STATUS
				this.status = STATUS_NO_CONNECTIONS;
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
			}
		}
	}
	
	public void onError(Peer peer) 
	{
		this.onDisconnect(peer);
	}
	
	//SYNCHRONIZED DO NOT PROCESSS MESSAGES SIMULTANEOUSLY
	public void onMessage(Message message) 
	{
		Message response;
		Block block;
		
		synchronized(this)
		{
		switch(message.getType())
		{
		case Message.PING_TYPE:
			
			//CREATE PING
			response = MessageFactory.getInstance().createPingMessage();
			
			//SET ID
			response.setId(message.getId());
			
			//SEND BACK TO SENDER
			message.getSender().sendMessage(response);
			
			break;
		
		case Message.VERSION_TYPE:
			
			VersionMessage versionMessage = (VersionMessage) message;
			
			//ADD TO LIST
			synchronized(this.peerHeight)
			{
				this.peerHeight.put(versionMessage.getSender(), versionMessage.getHeight());
			}
			
			break;
			
		case Message.GET_SIGNATURES_TYPE:
			
			GetSignaturesMessage getHeadersMessage = (GetSignaturesMessage) message;
			
			//ASK SIGNATURES FROM BLOCKCHAIN
			List<byte[]> headers = this.blockChain.getSignatures(getHeadersMessage.getParent());
			
			//CREATE RESPONSE WITH SAME ID
			response = MessageFactory.getInstance().createHeadersMessage(headers);
			response.setId(message.getId());
			
			//SEND RESPONSE BACK WITH SAME ID
			message.getSender().sendMessage(response);
			
			break;
			
		case Message.GET_BLOCK_TYPE:
			
			GetBlockMessage getBlockMessage = (GetBlockMessage) message;
					
			//ASK BLOCK FROM BLOCKCHAIN
			block = this.blockChain.getBlock(getBlockMessage.getSignature());
			
			//CREATE RESPONSE WITH SAME ID
			response = MessageFactory.getInstance().createBlockMessage(block);
			response.setId(message.getId());
				
			//SEND RESPONSE BACK WITH SAME ID
			message.getSender().sendMessage(response);
				
			break;		
			
		case Message.BLOCK_TYPE:
			
			BlockMessage blockMessage = (BlockMessage) message;
					
			//ASK BLOCK FROM BLOCKCHAIN
			block = blockMessage.getBlock();
			
			//CHECK IF VALID
			if(this.blockChain.isNewBlockValid(block))
			{
				Logger.getGlobal().info("received new valid block");
				
				//PROCESS
				this.synchronizer.process(block);
				
				//BROADCAST
				List<Peer> excludes = new ArrayList<Peer>();
				excludes.add(message.getSender());
				this.network.broadcast(message, excludes);			
				
				//UPDATE ALL PEER HEIGHTS TO OUR HEIGHT
				/*synchronized(this.peerHeight)
				{					
					for(Peer peer: this.peerHeight.keySet())
					{
						this.peerHeight.put(peer, this.blockChain.getHeight());
					}
				}*/
			}
			else
			{
				synchronized(this.peerHeight)
				{
					//UPDATE SENDER HEIGHT + 1
					this.peerHeight.put(message.getSender(), blockMessage.getHeight());
				}
			}		
			
			break;		
			
		case Message.TRANSACTION_TYPE:
			
			TransactionMessage transactionMessage = (TransactionMessage) message;
			
			//GET TRANSACTION
			Transaction transaction = transactionMessage.getTransaction();
			
			//CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
			if(!transaction.isSignatureValid() || transaction.getType() == Transaction.GENESIS_TRANSACTION)
			{
				//DISHONEST PEER
				this.network.onError(message.getSender());
				
				return;
			}
			
			//CHECK IF TRANSACTION HAS MINIMUM FEE AND MINIMUM FEE PER BYTE
			if(transaction.hasMinimumFee() && transaction.hasMinimumFeePerByte())
			{				
				//ADD TO UNCONFIRMED TRANSACTIONS
				this.blockGenerator.addUnconfirmedTransaction(transaction);
					
				//NOTIFY OBSERVERS
				//this.setChanged();
				//this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, DatabaseSet.getInstance().getTransactionsDatabase().getTransactions()));
					
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_TRANSACTION_TYPE, transaction));
				
				//BROADCAST
				List<Peer> excludes = new ArrayList<Peer>();
				excludes.add(message.getSender());
				this.network.broadcast(message, excludes);			
			}
			
			break;
		}
		}
	}
	
	public void addActivePeersObserver(Observer o)
	{
		this.network.addObserver(o);
	}
	
	public void removeActivePeersObserver(Observer o)
	{
		this.network.deleteObserver(o);
	}

	private void broadcastBlock(Block newBlock) {
		
		//CREATE MESSAGE
		Message message = MessageFactory.getInstance().createBlockMessage(newBlock);
		
		//BROADCAST MESSAGE
		List<Peer> excludes = new ArrayList<Peer>();
		this.network.broadcast(message, excludes);
	}
	
	private void broadcastTransaction(Transaction transaction) {
		
		//CREATE MESSAGE
		Message message = MessageFactory.getInstance().createTransactionMessage(transaction);
		
		//BROADCAST MESSAGE
		List<Peer> excludes = new ArrayList<Peer>();
		this.network.broadcast(message, excludes);
	}
	
	//SYNCHRONIZE
	
	public boolean isUpToDate()
	{
		if(this.peerHeight.size() == 0)
		{
			return true;
		}
		
		int maxPeerHeight = this.getMaxPeerHeight();
		int chainHeight = this.blockChain.getHeight();
		return maxPeerHeight <= chainHeight;
	}
	
	public void update() 
	{
		//UPDATE STATUS
		this.status = STATUS_SYNCHRONIZING;
			
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
		
		Peer peer = null;
		try 
		{
			//WHILE NOT UPTODATE
			while(!this.isUpToDate())
			{						
				//START UPDATE FROM HIGHEST HEIGHT PEER
				peer = this.getMaxHeightPeer();
			
				//SYNCHRONIZE FROM PEER
				this.synchronizer.synchronize(peer);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			if(peer != null)
			{
				//DISHONEST PEER
				this.network.onError(peer);
			}
		}
		
		if(this.peerHeight.size() == 0)
		{
			//UPDATE STATUS
			this.status = STATUS_NO_CONNECTIONS;
				
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
		}
		else
		{
			//UPDATE STATUS
			this.status = STATUS_OKE;
				
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
		}
	}
	
	private Peer getMaxHeightPeer()
	{
		Peer highestPeer = null;
		int height = 0;
		
		try
		{
			synchronized(this.peerHeight)
			{
				for(Peer peer: this.peerHeight.keySet())
				{
					if(peer == null)
					{
						highestPeer = peer;
					}
					else
					{
						//IF HEIGHT IS BIGGER
						if(height < this.peerHeight.get(peer))
						{
							highestPeer = peer;
							height = this.peerHeight.get(peer);
						}
						
						//IF HEIGHT IS SAME
						if(height == this.peerHeight.get(peer))
						{
							//CHECK IF PING OF PEER IS BETTER
							if(peer.getPing() < highestPeer.getPing())
							{
								highestPeer = peer;
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			//PEER REMOVED WHILE ITERATING
		}	
		
		return highestPeer;
	}
	
	private int getMaxPeerHeight()
	{
		int height = 0;
		
		try
		{
			synchronized(this.peerHeight)
			{
				for(Peer peer: this.peerHeight.keySet())
				{
					if(height < this.peerHeight.get(peer))
					{
						height = this.peerHeight.get(peer);
					}
				}
			}
		}
		catch(Exception e)
		{
			//PEER REMOVED WHILE ITERATING
		}
		
		return height;
	}
	
	//WALLET
	
	public boolean doesWalletExists()
	{
		//CHECK IF WALLET EXISTS
		return this.wallet.exists();
	}
	
	public boolean createWallet(byte[] seed, String password, int amount)
	{
		//IF NEW WALLET CREADED
		return this.wallet.create(seed, password, amount, false);
	}

	public boolean recoverWallet(byte[] seed, String password, int amount) 
	{
		return this.wallet.create(seed, password, amount, true);	
	}
	
	public List<Account> getAccounts() {

		return this.wallet.getAccounts();
	}
	
	public List<PrivateKeyAccount> getPrivateKeyAccounts() {

		return this.wallet.getprivateKeyAccounts();
	}

	public String generateNewAccount() 
	{		
		return this.wallet.generateNewAccount();	
	}
	
	public PrivateKeyAccount getPrivateKeyAccountByAddress(String address) 
	{
		return this.wallet.getPrivateKeyAccount(address);
	}
	
	public Account getAccountByAddress(String address)
	{
		return this.wallet.getAccount(address);
	}
	
	public BigDecimal getUnconfirmedBalance(String address) 
	{
		return this.wallet.getUnconfirmedBalance(address);
	}
	
	public void addWalletListener(Observer o) 
	{
		this.wallet.addObserver(o);		
	}
	
	public String importAccountSeed(byte[] accountSeed)
	{
		return this.wallet.importAccountSeed(accountSeed);
	}
	
	public byte[] exportAccountSeed(String address)
	{
		return this.wallet.exportAccountSeed(address);
	}
	
	public byte[] exportSeed()
	{
		return this.wallet.exportSeed();
	}
	
	public boolean deleteAccount(PrivateKeyAccount account)
	{
		return this.wallet.deleteAccount(account);
	}
	
	public void synchronizeWallet()
	{
		this.wallet.synchronize();
	}
	
	public boolean isWalletUnlocked()
	{
		return this.wallet.isUnlocked();
	}
	
	public boolean lockWallet()
	{
		return this.wallet.lock();
	}
	
	public boolean unlockWallet(String password)
	{
		return this.wallet.unlock(password);
	}
	
	public List<Pair<Account, Transaction>> getLastTransactions(int limit)
	{
		return this.wallet.getLastTransactions(limit);
	}
	
	public Transaction getTransaction(byte[] signature) {
		
		//CHECK IF IN BLOCK
		Block block = DBSet.getInstance().getTransactionParentMap().getParent(signature);
		if(block != null)
		{
			return block.getTransaction(signature);
		}
		
		//CHECK IF IN TRANSACTION DATABASE
		return DBSet.getInstance().getTransactionMap().get(signature);
	}
	
	public List<Transaction> getLastTransactions(Account account, int limit)
	{
		return this.wallet.getLastTransactions(account, limit);
	}
	
	public List<Pair<Account, Block>> getLastBlocks()
	{
		return this.wallet.getLastBlocks();
	}
	
	public List<Block> getLastBlocks(Account account)
	{
		return this.wallet.getLastBlocks(account);
	}
	
	public List<Pair<Account, Name>> getNames()
	{
		return this.wallet.getNames();
	}
	
	public List<Name> getNames(Account account)
	{
		return this.wallet.getNames(account);
	}
	
	public List<Pair<Account, NameSale>> getNameSales()
	{
		return this.wallet.getNameSales();
	}
	
	public List<NameSale> getNameSales(Account account)
	{
		return this.wallet.getNameSales(account);
	}
	
	public List<NameSale> getAllNameSales()
	{
		return DBSet.getInstance().getNameExchangeMap().getNameSales();
	}
	
	public List<Pair<Account, Poll>> getPolls()
	{
		return this.wallet.getPolls();
	}
	
	public List<Poll> getPolls(Account account)
	{
		return this.wallet.getPolls(account);
	}
	
	public void addAssetFavorite(Asset asset) 
	{
		this.wallet.addAssetFavorite(asset);
	}
	
	public void removeAssetFavorite(Asset asset) 
	{
		this.wallet.removeAssetFavorite(asset);
	}
	
	public boolean isAssetFavorite(Asset asset) 
	{
		return this.wallet.isAssetFavorite(asset);
	}
	
	public Collection<Poll> getAllPolls()
	{
		return DBSet.getInstance().getPollMap().getValues();
	}
	
	public void onDatabaseCommit()
	{
		this.wallet.commit();
	}
	
    public ForgingStatus getForgingStatus()
    {
        return this.blockGenerator.getForgingStatus();
    } 
	
	//BLOCKCHAIN
	
	public int getHeight() 
	{
		return this.blockChain.getHeight();
	}
	
	public Block getLastBlock()
	{
		return this.blockChain.getLastBlock();
	}
	
	public Block getBlock(byte[] header) 
	{	
		return this.blockChain.getBlock(header);		
	}
	
	public Pair<Block, List<Transaction>> scanTransactions(Block block, int blockLimit, int transactionLimit, int type, int service, Account account) {
		return this.blockChain.scanTransactions(block, blockLimit, transactionLimit, type, service, account);
		
	}

	public long getNextBlockGeneratingBalance()
	{
		return BlockGenerator.getNextBlockGeneratingBalance(DBSet.getInstance(), DBSet.getInstance().getBlockMap().getLastBlock());
	}
	
	public long getNextBlockGeneratingBalance(Block parent)
	{
		return BlockGenerator.getNextBlockGeneratingBalance(DBSet.getInstance(), parent);
	}
	
	//FORGE
	
	public void newBlockGenerated(Block newBlock) {
		
		//ADD TO BLOCKCHAIN
		this.synchronizer.process(newBlock);
		
		//BROADCAST
		this.broadcastBlock(newBlock);		
	}
	
	public List<Transaction> getUnconfirmedTransactions()
	{
		return this.blockGenerator.getUnconfirmedTransactions();
	}
	
	//BALANCES
	
	public SortableList<Tuple2<String, Long>, BigDecimal> getBalances(long key)
	{
		return DBSet.getInstance().getBalanceMap().getBalancesSortableList(key);
	}
	
	public SortableList<Tuple2<String, Long>, BigDecimal> getBalances(Account account)
	{
		return DBSet.getInstance().getBalanceMap().getBalancesSortableList(account);
	}
	
	//NAMES
	
	public Name getName(String nameName)
	{
		return DBSet.getInstance().getNameMap().get(nameName);
	}

	public NameSale getNameSale(String nameName)
	{
		return DBSet.getInstance().getNameExchangeMap().getNameSale(nameName);
	}
	
	//POLLS
	
	public Poll getPoll(String name)
	{
		return DBSet.getInstance().getPollMap().get(name);
	}
	
	//ASSETS
	
	public Asset getQoraAsset()
	{
		return DBSet.getInstance().getAssetMap().get(0l);
	}
	
	public Asset getAsset(long key) 
	{
		return DBSet.getInstance().getAssetMap().get(key);
	}
	
	public SortableList<BigInteger, Order> getOrders(Asset have, Asset want)
	{
		return DBSet.getInstance().getOrderMap().getOrdersSortableList(have.getKey(), want.getKey());
	}
	
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(Asset have, Asset want)
	{
		return DBSet.getInstance().getTradeMap().getTradesSortableList(have.getKey(), want.getKey());
	}
	
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(Order order)
	{
		return DBSet.getInstance().getTradeMap().getTrades(order);
	}
	
	//TRANSACTIONS
	
	public void onTransactionCreate(Transaction transaction)
	{
		//ADD TO UNCONFIRMED TRANSACTIONS
		this.blockGenerator.addUnconfirmedTransaction(transaction);
		
		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, DBSet.getInstance().getTransactionMap().getValues()));
		
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_TRANSACTION_TYPE, transaction));
		
		//BROADCAST
		this.broadcastTransaction(transaction);
	}
	
	public Pair<Transaction, Integer> sendPayment(PrivateKeyAccount sender, Account recipient, BigDecimal amount, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{		
			return this.transactionCreator.createPayment(sender, recipient, amount, fee);
		}
	}
	
	public Pair<Transaction, Integer> registerName(PrivateKeyAccount registrant, Account owner, String name, String value, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{		
			return this.transactionCreator.createNameRegistration(registrant, new Name(owner, name, value), fee);
		}
	}
	
	public Pair<Transaction, Integer> updateName(PrivateKeyAccount owner, Account newOwner, String name, String value, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{		
			return this.transactionCreator.createNameUpdate(owner, new Name(newOwner, name, value), fee);
		}
	}
	
	public Pair<Transaction, Integer> sellName(PrivateKeyAccount owner, String name, BigDecimal amount, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createNameSale(owner, new NameSale(name, amount), fee);
		}
	}

	public Pair<Transaction, Integer> cancelSellName(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createCancelNameSale(owner, nameSale, fee);
		}
	}
	
	public Pair<Transaction, Integer> BuyName(PrivateKeyAccount buyer, NameSale nameSale, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createNamePurchase(buyer, nameSale, fee);
		}
	}

	public Pair<Transaction, Integer> createPoll(PrivateKeyAccount creator, String name, String description, List<String> options, BigDecimal fee) 
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			//CREATE POLL OPTIONS
			List<PollOption> pollOptions = new ArrayList<PollOption>();
			for(String option: options)
			{
				pollOptions.add(new PollOption(option));
			}
			
			//CREATE POLL
			Poll poll = new Poll(creator, name, description, pollOptions);
			
			return this.transactionCreator.createPollCreation(creator, poll, fee);
		}
	}

	public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, Poll poll, PollOption option, BigDecimal fee) 
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			//GET OPTION INDEX
			int optionIndex = poll.getOptions().indexOf(option);
			
			return this.transactionCreator.createPollVote(creator, poll.getName(), optionIndex, fee);
		}
	}
	
	public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, int service, byte[] data, BigDecimal fee) 
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createArbitraryTransaction(creator, service, data, fee);
		}
	}
	
	public Pair<Transaction, Integer> issueAsset(PrivateKeyAccount creator, String name, String description, long quantity, boolean divisible, BigDecimal fee) 
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createIssueAssetTransaction(creator, name, description, quantity, divisible, fee);
		}
	}
	
	public Pair<Transaction, Integer> createOrder(PrivateKeyAccount creator, Asset have, Asset want, BigDecimal amount, BigDecimal price, BigDecimal fee) 
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createOrderTransaction(creator, have, want, amount, price, fee);
		}
	}
	
	public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator, Order order, BigDecimal fee) 
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{
			return this.transactionCreator.createCancelOrderTransaction(creator, order, fee);
		}
	}
	
	public Pair<Transaction, Integer> transferAsset(PrivateKeyAccount sender, Account recipient, Asset asset, BigDecimal amount, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{		
			return this.transactionCreator.createAssetTransfer(sender, recipient, asset, amount, fee);
		}
	}
	
	public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount sender, List<Payment> payments, BigDecimal fee)
	{
		//CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized(this.transactionCreator)
		{		
			return this.transactionCreator.sendMultiPayment(sender, payments, fee);
		}
	}
}
