package qora.wallet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import at.AT_Transaction;
import controller.Controller;
import database.DBSet;
import database.wallet.SecureWalletDatabase;
import database.wallet.WalletDatabase;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.assets.Order;
import qora.block.Block;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import utils.ObserverMessage;
import utils.Pair;

public class Wallet extends Observable implements Observer
{
	public static final int STATUS_UNLOCKED = 1;
	public static final int STATUS_LOCKED = 0;
	
	private WalletDatabase database;
	private SecureWalletDatabase secureDatabase;
	
	private int secondsToUnlock = -1;
	private Timer lockTimer = new Timer();
	
	//CONSTRUCTORS
	
	public Wallet()
	{
		//CHECK IF EXISTS
		if(this.exists())
		{
			//OPEN WALLET
			this.database = new WalletDatabase();
			
			//ADD OBSERVER
		    Controller.getInstance().addObserver(this);
		    DBSet.getInstance().getCompletedOrderMap().addObserver(this);
		}
	}
	
	//GETTERS/SETTERS
	
	public void setSecondsToUnlock(int seconds)
	{
		this.secondsToUnlock = seconds;
	}

	public int getVersion()
	{
		return this.database.getVersion();
	}
	
	public boolean isUnlocked()
	{
		return this.secureDatabase != null;
	}
	
	public List<Account> getAccounts()
	{
		return this.database.getAccountMap().getAccounts();
	}
	
	public boolean accountExists(String address)
	{
		return this.database.getAccountMap().exists(address);
	}
	
	public Account getAccount(String address)
	{
		return this.database.getAccountMap().getAccount(address);
	}
	
	public boolean isWalletDatabaseExisting()
	{
		return database != null;
	}
	
	public BigDecimal getUnconfirmedBalance(String address)
	{
		return this.database.getAccountMap().getUnconfirmedBalance(address);
	}
	
	public List<PrivateKeyAccount> getprivateKeyAccounts()
	{
		if(this.secureDatabase == null)
		{
			return new ArrayList<PrivateKeyAccount>();
		}
		
		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccounts();
	}
	
	public PrivateKeyAccount getPrivateKeyAccount(String address)
	{
		if(this.secureDatabase == null)
		{
			return null;
		}
		
		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccount(address);
	}
	
	public boolean exists()
	{
		return WalletDatabase.exists();
	}
	
	public List<Pair<Account, Transaction>> getLastTransactions(int limit)
	{
		if(!this.exists())
		{
			new ArrayList<Pair<Account, Transaction>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getTransactionMap().get(accounts, limit);
	}
	
	public List<Transaction> getLastTransactions(Account account, int limit)
	{
		if(!this.exists())
		{
			return new ArrayList<Transaction>();
		}

		return this.database.getTransactionMap().get(account, limit);
	}
	
	public List<Pair<Account, Block>> getLastBlocks()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Block>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getBlockMap().get(accounts);
	}

	public List<Block> getLastBlocks(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Block>();
		}

		return this.database.getBlockMap().get(account);
	}
		
	public List<Pair<Account, Name>> getNames()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Name>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameMap().get(accounts);
	}
	
	public List<Name> getNames(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Name>();
		}

		return this.database.getNameMap().get(account);
	}
	
	public List<Pair<Account, NameSale>> getNameSales()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, NameSale>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameSaleMap().get(accounts);
	}
	
	public List<NameSale> getNameSales(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<NameSale>();
		}

		return this.database.getNameSaleMap().get(account);
	}
	
	public List<Pair<Account, Poll>> getPolls()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Poll>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getPollMap().get(accounts);
	}
	
	public List<Poll> getPolls(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Poll>();
		}

		return this.database.getPollMap().get(account);
	}
	
	public void addAssetFavorite(Asset asset)
	{
		if(!this.exists())
		{
			return;
		}
		
		this.database.getAssetFavoritesSet().add(asset.getKey());
	}
	
	public void removeAssetFavorite(Asset asset)
	{
		if(!this.exists())
		{
			return;
		}
		
		this.database.getAssetFavoritesSet().delete(asset.getKey());
	}
	
	public boolean isAssetFavorite(Asset asset)
	{
		if(!this.exists())
		{
			return false;
		}
		
		return this.database.getAssetFavoritesSet().contains(asset.getKey());
	}
	
	//CREATE
	
	public boolean create(byte[] seed, String password, int depth, boolean synchronize)
	{
		//OPEN WALLET
		WalletDatabase database = new WalletDatabase();
		
	    //OPEN SECURE WALLET
		SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
	    
	    //CREATE
	    return this.create(database, secureDatabase, seed, depth, synchronize);
	}
	
	public boolean create(WalletDatabase database, SecureWalletDatabase secureDatabase, byte[] seed, int depth, boolean synchronize)
	{
		//CREATE WALLET
		this.database = database;
	    
	    //CREATE SECURE WALLET
	    this.secureDatabase = secureDatabase;
	    
	    //ADD VERSION
	    this.database.setVersion(1);
	    
	    //ADD SEED
	    this.secureDatabase.setSeed(seed);
	    
	    //ADD NONCE
	    this.secureDatabase.setNonce(0);
	    
	    //CREATE ACCOUNTS
	    for(int i=1; i<=depth; i++)
	    {
	    	this.generateNewAccount();
	    }
	    
	    //SCAN TRANSACTIONS
	    if(synchronize)
	    {
	    	this.synchronize();
	    }
	    
	    //COMMIT
	    this.commit();
	    
	    //ADD OBSERVER
	    Controller.getInstance().addObserver(this);
	    DBSet.getInstance().getCompletedOrderMap().addObserver(this);
	    
	    return true;
	}
	
	public String generateNewAccount() 
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return "";
		}
	    
	    //READ SEED
	    byte[] seed = this.secureDatabase.getSeed();
	    
	    //READ NONCE
	    int nonce = this.secureDatabase.getAndIncrementNonce();
	    
	    //GENERATE ACCOUNT SEED
	    byte[] accountSeed = this.generateAccountSeed(seed, nonce);
	    PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
	    
	    //CHECK IF ACCOUNT ALREADY EXISTS
	    if(!this.accountExists(account.getAddress()))
	    {	    
	    	//ADD TO DATABASE
		    this.secureDatabase.getAccountSeedMap().add(account);
		    this.database.getAccountMap().add(account);
		    Logger.getGlobal().info("Added account #" + nonce);
		    
		    this.secureDatabase.commit();
		    this.database.commit();
		    
		    //NOTIFY
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
	    }
	    
	    return account.getAddress();
	}
	
	private byte[] generateAccountSeed(byte[] seed, int nonce) 
	{		
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);		
	}
	
	//DELETE
	
	public boolean deleteAccount(PrivateKeyAccount account)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return false;
		}
		
		//DELETE FROM DATABASE
		this.database.delete(account);
		this.secureDatabase.delete(account);
		
		//NOTIFY
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_ACCOUNT_TYPE, account));
	    
	    //RETURN
	    return true;
	}
	
	//SYNCRHONIZE
	
	public void synchronize()
	{
		List<Account> accounts = this.getAccounts();
		
		//RESET MAPS
		this.database.getTransactionMap().reset();
		this.database.getBlockMap().reset();
		this.database.getNameMap().reset();
		this.database.getNameSaleMap().reset();
		this.database.getPollMap().reset();
		this.database.getAssetMap().reset();
		this.database.getOrderMap().reset();
		Logger.getGlobal().info("Resetted maps");
		
		//REPROCESS BLOCKS
		Block block = new GenesisBlock();
		this.database.setLastBlockSignature(new byte[]{1,1,1,1,1,1,1,1});
		
		try{
			Controller.getInstance().setNeedSync(false);
			Controller.getInstance().isProcessSynchronize = true;
		
			do
			{
				//UPDATE
				this.update(this, new ObserverMessage(ObserverMessage.ADD_BLOCK_TYPE, block));
				
				if(block.getHeight() % 2000 == 0) 
				{
					Controller.getInstance().walletStatusUpdate(block.getHeight());
					
					//Gui.getInstance().
					
					Logger.getGlobal().info("Synchronize wallet: " + block.getHeight());
					this.database.commit();
				}
				
				//LOAD NEXT
				block = block.getChild();
			}
			while(block != null);
			
		}finally{
			Controller.getInstance().isProcessSynchronize = false;
			this.database.commit();
		}
		
		
		//RESET UNCONFIRMED BALANCE
		synchronized(accounts)
		{
			for(Account account: accounts)
			{
				this.database.getAccountMap().update(account, account.getConfirmedBalance());
			}
		}
		Logger.getGlobal().info("Resetted balances");

		Controller.getInstance().walletStatusUpdate(-1);
		
		//SET LAST BLOCK
		
		/*//SCAN TRANSACTIONS
		Map<Account, List<Transaction>> transactions;
		synchronized(accounts)
		{
			transactions = Controller.getInstance().scanTransactions(accounts);
		}
		
		//DELETE TRANSACTIONS
		this.database.getTransactionMap().deleteAll(accounts);
		
		//ADD TRANSACTIONS
		this.database.getTransactionMap().addAll(transactions);
	    	
		//TODO SCAN UNCONFIRMED TRANSACTIONS    
	    	    
	    //SCAN BLOCKS
	    Map<Account, List<Block>> blocks;
	    synchronized(accounts)
		{
	    	blocks = Controller.getInstance().scanBlocks(accounts);
		}
	    
	    //DELETE BLOCKS
	  	this.database.getBlockMap().deleteAll(accounts);
	  	
	  	//ADD BLOCKS
	  	this.database.getBlockMap().addAll(blocks);
	    
	    //SCAN NAMES
	    Map<Account, List<Name>> names;
	    synchronized(accounts)
		{
	    	names = Controller.getInstance().scanNames(accounts);
		}
	    
	    //DELETE NAMES
	  	this.database.getNameMap().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameMap().addAll(names);
	  	
	  	//TODO SCAN UNCONFIRMED NAMES
	    
	  	//SCAN NAMESALES
	    Map<Account, List<NameSale>> nameSales;
	    synchronized(accounts)
		{
	    	nameSales = Controller.getInstance().scanNameSales(accounts);
		}
	    
	    //DELETE NAMESALES
	  	this.database.getNameSaleMap().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameSaleMap().addAll(nameSales);
	  	
	  	//SCAN POLLS
	  	Map<Account, List<Poll>> polls;
	  	synchronized(accounts)
	  	{
	  		polls = Controller.getInstance().scanPolls(accounts);
	  	}
	  	
	  	//DELETE POLLS
	  	this.database.getPollMap().deleteAll(accounts);
	  	
	  	//ADD POLLS
	  	this.database.getPollMap().addAll(polls);
	  	
	  	//SCAN ASSETS
		Map<Account, List<Asset>> assets;
	  	synchronized(accounts)
	  	{
	  		assets = Controller.getInstance().scanAssets(accounts);
	  	}
	  	
	  	//DELETE ASSETS
	  	this.database.getAssetMap().deleteAll(accounts);
	  	
	  	//ADD ASSETS
	  	this.database.getAssetMap().addAll(assets);

	  	//SCAN ORDERS
	  	Map<Account, List<Order>> orders;
	  	synchronized(accounts)
	  	{
	  		orders = Controller.getInstance().scanOrders(accounts);
	  	}
	  	
	  	//DELETE ASSETS
	  	this.database.getOrderMap().deleteAll(accounts);
	  	
	  	//ADD ASSETS
	  	this.database.getOrderMap().addAll(orders);
	  	
	  	//SET LAST BLOCK
	  	this.database.setLastBlockSignature(Controller.getInstance().getLastBlock().getSignature());*/
	}
	
	//UNLOCK
	
	public boolean unlock(String password)
	{
		if(this.isUnlocked())
		{
			return false;
		}
		
		//TRY TO UNLOCK
		try
		{
			SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
			return this.unlock(secureDatabase);
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public boolean unlock(SecureWalletDatabase secureDatabase)
	{
		this.secureDatabase = secureDatabase;
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_UNLOCKED));
		
		if(this.secondsToUnlock > 0)
		{
			this.lockTimer.cancel(); 
			this.lockTimer = new Timer();
			
			TimerTask action = new TimerTask() {
		        public void run() {
		            lock();
		        }
		    };
		    
		    this.lockTimer.schedule(action, this.secondsToUnlock*1000);
		}
		return true;
	}
	
	public boolean lock()
	{
		if(!this.isUnlocked())
		{
			return false;
		}
		
		//CLOSE
		this.secureDatabase.close();
		this.secureDatabase = null;
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_LOCKED));
		
		this.secondsToUnlock = -1;
		this.lockTimer.cancel(); 
		
		//LOCK SUCCESSFULL
		return true;
	}

	//IMPORT/EXPORT
	
	public String importAccountSeed(byte[] accountSeed)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return "";
		}
		
		//CHECK LENGTH
		if(accountSeed.length != 32)
		{
			return "";
		}
		
		//CREATE ACCOUNT
		PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
		
		//CHECK IF ACCOUNT ALREADY EXISTS
	    if(!this.accountExists(account.getAddress()))
	    {	
	    	//ADD TO DATABASE
		    this.secureDatabase.getAccountSeedMap().add(account);
		    this.database.getAccountMap().add(account);
		    
		    //SYNCHRONIZE
		    this.synchronize();
		    
		    //NOTIFY
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
		    
		    //RETURN
		    return account.getAddress();
	    }
	    
	    return "";
	}

	public byte[] exportAccountSeed(String address)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return null;
		}
		
		PrivateKeyAccount account = this.getPrivateKeyAccount(address);
		
		if(account == null)
		{
			return null;
		}
		
		return account.getSeed();	
	}
	
	public byte[] exportSeed()
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return null;
		}
		
		return this.secureDatabase.getSeed();
	}
	

	//OBSERVER
	
	@Override
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		
		//REGISTER ON ACCOUNTS
		this.database.getAccountMap().addObserver(o);
		
		//REGISTER ON TRANSACTIONS
		this.database.getTransactionMap().addObserver(o);
		
		//REGISTER ON BLOCKS
		this.database.getBlockMap().addObserver(o);
		
		//REGISTER ON NAMES
		this.database.getNameMap().addObserver(o);
		
		//REGISTER ON NAME SALES
		this.database.getNameSaleMap().addObserver(o);
		
		//REGISTER ON POLLS
		this.database.getPollMap().addObserver(o);
		
		//REGISTER ON ASSETS
		this.database.getAssetMap().addObserver(o);

		//REGISTER ON ORDERS
		this.database.getOrderMap().addObserver(o);
		
		//REGISTER ON ASSET FAVORITES
		this.database.getAssetFavoritesSet().addObserver(o);
		
		//SEND STATUS
		int status = STATUS_LOCKED;
		if(this.isUnlocked())
		{
			status = STATUS_UNLOCKED;
		}
		
		o.update(this, new ObserverMessage(ObserverMessage.WALLET_STATUS, status));
	}

	private void processTransaction(Transaction transaction)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					//ADD TO ACCOUNT TRANSACTIONS
					if(!this.database.getTransactionMap().add(account, transaction))
					{					
						//UPDATE UNCONFIRMED BALANCE
						BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).add(transaction.getAmount(account));
						this.database.getAccountMap().update(account, unconfirmedBalance);
					}
				}
			}
		}
	}
	
	private void processATTransaction( Tuple2< Tuple2< Integer, Integer >, AT_Transaction > atTx )
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(atTx.b.getRecipient().equalsIgnoreCase( account.getAddress() ))
				{				
						BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).add( BigDecimal.valueOf(atTx.b.getAmount(),8));
						this.database.getAccountMap().update(account, unconfirmedBalance);
					
				}
			}
		}
	}
	
	private void orphanTransaction(Transaction transaction)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		///FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					//DELETE FROM ACCOUNT TRANSACTIONS
					this.database.getTransactionMap().delete(account, transaction);
					
					//UPDATE UNCONFIRMED BALANCE
					BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).subtract(transaction.getAmount(account));
					this.database.getAccountMap().update(account, unconfirmedBalance);
				}
			}
		}
	}
	
	private void orphanATTransaction(Tuple2<Tuple2<Integer,Integer>, AT_Transaction> atTx)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(atTx.b.getRecipient().equalsIgnoreCase( account.getAddress() ))
				{				
						BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).subtract( BigDecimal.valueOf(atTx.b.getAmount(),8));
						this.database.getAccountMap().update(account, unconfirmedBalance);
					
				}
			}
		}
	}

	private void processBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE NEED TO RESYNC
		byte[] lastBlockSignature = this.database.getLastBlockSignature();
		if(lastBlockSignature == null || !Arrays.equals(lastBlockSignature, block.getReference()))
		{
			Logger.getGlobal().info("Wallet not synchronized with current blockchain: synchronizing wallet.");
			this.synchronize();
		}
		
		//SET AS LAST BLOCK
		this.database.setLastBlockSignature(block.getSignature());
			
		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(block.getGenerator().getAddress()))
		{
			//ADD BLOCK
			this.database.getBlockMap().add(block);
				
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(block.getGenerator().getAddress()).add(block.getTotalFee());
			this.database.getAccountMap().update(block.getGenerator(), unconfirmedBalance);
		}
	}

	private void orphanBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(block.getGenerator().getAddress()))
		{
			//DELETE BLOCK
			this.database.getBlockMap().delete(block);
			
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(block.getGenerator().getAddress()).subtract(block.getTotalFee());
			this.database.getAccountMap().update(block.getGenerator(), unconfirmedBalance);
		}
	}
	
	private void processNameRegistration(RegisterNameTransaction nameRegistration)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameRegistration.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().add(nameRegistration.getName());
		}
	}
	
	private void orphanNameRegistration(RegisterNameTransaction nameRegistration)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameRegistration.getName().getOwner().getAddress()))
		{
			//DELETE NAME
			this.database.getNameMap().delete(nameRegistration.getName());
		}
	}
	
	private void processPollCreation(CreatePollTransaction pollCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(pollCreation.getPoll().getCreator().getAddress()))
		{
			//ADD POLL
			this.database.getPollMap().add(pollCreation.getPoll());
		}
	}
	
	private void orphanPollCreation(CreatePollTransaction pollCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(pollCreation.getPoll().getCreator().getAddress()))
		{
			//DELETE POLL
			this.database.getPollMap().delete(pollCreation.getPoll());
		}
	}

	private void processPollVote(VoteOnPollTransaction pollVote)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		Poll poll = DBSet.getInstance().getPollMap().get(pollVote.getPoll());
		if(this.accountExists(poll.getCreator().getAddress()))
		{
			//UPDATE POLL
			this.database.getPollMap().add(poll);
		}
	}
	
	private void orphanPollVote(VoteOnPollTransaction pollVote)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE OWNER
		Poll poll = DBSet.getInstance().getPollMap().get(pollVote.getPoll());
		if(this.accountExists(poll.getCreator().getAddress()))
		{
			//UPDATE POLL
			this.database.getPollMap().add(poll);
		}
	}	
	
	private void processNameUpdate(UpdateNameTransaction nameUpdate)
	{
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameUpdate.getOwner().getAddress()))
		{
			//CHECK IF OWNER CHANGED
			if(!nameUpdate.getOwner().getAddress().equals(nameUpdate.getName().getOwner().getAddress()))
			{
				//DELETE PREVIOUS NAME
				Name name = DBSet.getInstance().getUpdateNameMap().get(nameUpdate);
				this.database.getNameMap().delete(nameUpdate.getOwner(), name);
			}
		}
		
		//CHECK IF WE ARE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().add(nameUpdate.getName());
		}
	}
	
	private void orphanNameUpdate(UpdateNameTransaction nameUpdate)
	{
		//CHECK IF WE WERE OWNER
		if(this.accountExists(nameUpdate.getOwner().getAddress()))
		{
			//CHECK IF OWNER WAS CHANGED
			if(!nameUpdate.getOwner().getAddress().equals(nameUpdate.getName().getOwner().getAddress()))
			{
				//ADD PREVIOUS  NAME
				Name name = DBSet.getInstance().getNameMap().get(nameUpdate.getName().getName());
				this.database.getNameMap().add(name);

			}
		}
		
		//CHECK IF WE WERE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().delete(nameUpdate.getName());
		}
	}
	
	private void processNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//ADD TO DATABASE
			this.database.getNameSaleMap().add(nameSaleTransaction.getNameSale());
		}
	}
	
	private void orphanNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			this.database.getNameSaleMap().delete(nameSaleTransaction.getNameSale());
		}
	}

	private void processCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			BigDecimal amount = DBSet.getInstance().getCancelSellNameMap().get(cancelNameSaleTransaction);
			NameSale nameSale = new NameSale(cancelNameSaleTransaction.getName(), amount);
			this.database.getNameSaleMap().delete(nameSale);
		}
	}
	
	private void orphanCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getOwner().getAddress()))
		{
			//ADD TO DATABASE
			NameSale nameSale = DBSet.getInstance().getNameExchangeMap().getNameSale(cancelNameSaleTransaction.getName());
			this.database.getNameSaleMap().add(nameSale);
		}
	}
	
	private void processNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE ARE BUYER
		if(this.accountExists(namePurchase.getBuyer().getAddress()))
		{
			//ADD NAME
			Name name = DBSet.getInstance().getNameMap().get(namePurchase.getNameSale().getKey());
			this.database.getNameMap().add(name);
		}
		
		//CHECK IF WE ARE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//DELETE NAMESALE
			this.database.getNameSaleMap().delete(seller, namePurchase.getNameSale());
			
			//DELETE NAME
			Name name = DBSet.getInstance().getNameMap().get(namePurchase.getNameSale().getKey());
			name.setOwner(seller);
			this.database.getNameMap().delete(seller, name);
		}
	}
	
	private void orphanNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE WERE BUYER
		if(this.accountExists(namePurchase.getBuyer().getAddress()))
		{
			//DELETE NAME
			Name name = namePurchase.getNameSale().getName();
			name.setOwner(namePurchase.getBuyer());
			this.database.getNameMap().delete(namePurchase.getBuyer(), name);
		}
		
		//CHECK IF WE WERE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//ADD NAMESALE
			this.database.getNameSaleMap().add(namePurchase.getNameSale());
			
			//ADD NAME
			Name name = namePurchase.getNameSale().getName();
			this.database.getNameMap().add(name);
		}
	}
	
	private void processAssetIssue(IssueAssetTransaction assetIssue)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(assetIssue.getAsset().getOwner().getAddress()))
		{
			//ADD POLL
			this.database.getAssetMap().add(assetIssue.getAsset());
		}
	}
	
	private void orphanAssetIssue(IssueAssetTransaction assetIssue)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(assetIssue.getAsset().getOwner().getAddress()))
		{
			//DELETE ASSET
			this.database.getAssetMap().delete(assetIssue.getAsset());
		}
	}
	
	private void processOrderCreation(CreateOrderTransaction orderCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//ADD ORDER
		this.addOrder(orderCreation.getOrder());
	}
	
	private void addOrder(Order order)
	{
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(order.getCreator().getAddress()))
		{
			//ADD ORDER
			this.database.getOrderMap().add(order);
		}
	}
	
	private void orphanOrderCreation(CreateOrderTransaction orderCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(orderCreation.getOrder().getCreator().getAddress()))
		{
			//DELETE ORDER
			//this.database.getOrderMap().delete(orderCreation.getOrder());
		}
	}
	
	private void processOrderCancel(CancelOrderTransaction orderCancel)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(orderCancel.getCreator().getAddress()))
		{
			//DELETE ORDER
			this.database.getOrderMap().delete(new Tuple2<String, BigInteger>(orderCancel.getCreator().getAddress(), orderCancel.getOrder()));
		}
	}
	
	private void orphanOrderCancel(CancelOrderTransaction orderCancel)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(orderCancel.getCreator().getAddress()))
		{
			//DELETE ORDER
			Order order = DBSet.getInstance().getOrderMap().get(orderCancel.getOrder());
			this.database.getOrderMap().add(order);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) 
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE)
		{
			Block block = (Block) message.getValue();
				
			//CHECK BLOCK
			this.processBlock(block);
				
			//CHECK TRANSACTIONS
			for(Transaction transaction: block.getTransactions())
			{
				this.processTransaction(transaction);
				
				//SKIP PAYMENT TRANSACTIONS
				if (transaction instanceof PaymentTransaction)
				{
					continue;
				}
				
				//CHECK IF NAME REGISTRATION
				else if(transaction instanceof RegisterNameTransaction)
				{
					this.processNameRegistration((RegisterNameTransaction) transaction);
				}
				
				//CHECK IF NAME UPDATE
				else if(transaction instanceof UpdateNameTransaction)
				{
					this.processNameUpdate((UpdateNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				else if(transaction instanceof SellNameTransaction)
				{
					this.processNameSale((SellNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				else if(transaction instanceof CancelSellNameTransaction)
				{
					this.processCancelNameSale((CancelSellNameTransaction) transaction);
				}
				
				//CHECK IF NAME PURCHASE
				else if(transaction instanceof BuyNameTransaction)
				{
					this.processNamePurchase((BuyNameTransaction) transaction);
				}
				
				//CHECK IF POLL CREATION
				else if(transaction instanceof CreatePollTransaction)
				{
					this.processPollCreation((CreatePollTransaction) transaction);
				}
				
				//CHECK IF POLL VOTE
				else if(transaction instanceof VoteOnPollTransaction)
				{
					this.processPollVote((VoteOnPollTransaction) transaction);
				}
				
				//CHECK IF ASSET ISSUE
				else if(transaction instanceof IssueAssetTransaction)
				{
					this.processAssetIssue((IssueAssetTransaction) transaction);
				}
				
				//CHECK IF ORDER CREATION
				/*if(transaction instanceof CreateOrderTransaction)
				{
					this.processOrderCreation((CreateOrderTransaction) transaction);
				}*/
				
				//CHECK IF ORDER CANCEL
				else if(transaction instanceof CancelOrderTransaction)
				{
					this.processOrderCancel((CancelOrderTransaction) transaction);
				}
			}
		}
		
		else if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{	
			Transaction transaction = (Transaction) message.getValue();
				
			this.processTransaction(transaction);
			
			//CHECK IF PAYMENT
			if (transaction instanceof PaymentTransaction)
			{
				
			}
			
			//CHECK IF NAME REGISTRATION
			else if(transaction instanceof RegisterNameTransaction)
			{
				this.processNameRegistration((RegisterNameTransaction) transaction);
			}
			
			//CHECK IF POLL CREATION
			else if(transaction instanceof CreatePollTransaction)
			{
				this.processPollCreation((CreatePollTransaction) transaction);
			}
			
			//CHECK IF ASSET ISSUE
			else if(transaction instanceof IssueAssetTransaction)
			{
				this.processAssetIssue((IssueAssetTransaction) transaction);
			}
			
			//CHECK IF ORDER CREATION
			else if(transaction instanceof CreateOrderTransaction)
			{
				this.processOrderCreation((CreateOrderTransaction) transaction);
			}
		}
		
		else if(message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE)
		{
			Block block = (Block) message.getValue();
				
			//CHECK BLOCK
			this.orphanBlock(block);
				
			//CHECK TRANSACTIONS
			for(Transaction transaction: block.getTransactions())
			{
				this.orphanTransaction(transaction);
				
				//CHECK IF NAME REGISTRATION
				if(transaction instanceof RegisterNameTransaction)
				{
					this.orphanNameRegistration((RegisterNameTransaction) transaction);
				}
				
				//CHECK IF NAME UPDATE
				else if(transaction instanceof UpdateNameTransaction)
				{
					this.orphanNameUpdate((UpdateNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				else if(transaction instanceof SellNameTransaction)
				{
					this.orphanNameSale((SellNameTransaction) transaction);
				}
				
				//CHECK IF CANCEL NAME SALE
				else if(transaction instanceof CancelSellNameTransaction)
				{
					this.orphanCancelNameSale((CancelSellNameTransaction) transaction);
				}
				
				//CHECK IF CANCEL NAME SALE
				else if(transaction instanceof BuyNameTransaction)
				{
					this.orphanNamePurchase((BuyNameTransaction) transaction);
				}
				
				//CHECK IF POLL CREATION
				else if(transaction instanceof CreatePollTransaction)
				{
					this.orphanPollCreation((CreatePollTransaction) transaction);
				}
				
				//CHECK IF POLL VOTE
				else if(transaction instanceof VoteOnPollTransaction)
				{
					this.orphanPollVote((VoteOnPollTransaction) transaction);
				}
				
				//CHECK IF ASSET ISSUE
				else if(transaction instanceof IssueAssetTransaction)
				{
					this.orphanAssetIssue((IssueAssetTransaction) transaction);
				}
				
				//CHECK IF ORDER CREATION
				else if(transaction instanceof CreateOrderTransaction)
				{
					this.orphanOrderCreation((CreateOrderTransaction) transaction);
				}
				
				//CHECK IF ORDER CANCEL
				else if(transaction instanceof CancelOrderTransaction)
				{
					this.orphanOrderCancel((CancelOrderTransaction) transaction);
				}
			}
		}
		
		else if(message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{	
			Transaction transaction = (Transaction) message.getValue();
				
			this.orphanTransaction(transaction);
				
			//CHECK IF PAYMENT
			if (transaction instanceof PaymentTransaction)
			{
				
			}
			//CHECK IF NAME REGISTRATION
			else if(transaction instanceof RegisterNameTransaction)
			{
				this.orphanNameRegistration((RegisterNameTransaction) transaction);
			}
			
			//CHECK IF POLL CREATION
			else if(transaction instanceof CreatePollTransaction)
			{
				this.orphanPollCreation((CreatePollTransaction) transaction);
			}
			
			//CHECK IF ASSET ISSUE
			else if(transaction instanceof IssueAssetTransaction)
			{
				this.orphanAssetIssue((IssueAssetTransaction) transaction);
			}
			
			//CHECK IF ORDER CREATION
			else if(transaction instanceof CreateOrderTransaction)
			{
				this.orphanOrderCreation((CreateOrderTransaction) transaction);
			}
		}
		
		else if (message.getType() == ObserverMessage.ADD_AT_TX_TYPE)
		{
			this.processATTransaction( (Tuple2<Tuple2<Integer, Integer>, AT_Transaction>) message.getValue() );
		}
		
		else if (message.getType() == ObserverMessage.REMOVE_AT_TX)
		{
			this.orphanATTransaction( (Tuple2<Tuple2<Integer, Integer>, AT_Transaction>) message.getValue() );
		}
		
		//ADD ORDER
		else if(message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE)
		{
			this.addOrder((Order) message.getValue());
		}
	}

	//CLOSE
	
	public void close()
	{
		if(this.database != null)
		{
			this.database.close();
		}
		
		if(this.secureDatabase != null)
		{
			this.secureDatabase.close();
		}
	}

	public void commit() 
	{
		if(this.database != null)
		{
			this.database.commit();
		}
		
		if(this.secureDatabase != null)
		{
			this.secureDatabase.commit();
		}
		
	}	
	
	public byte[] getLastBlockSignature()
	{
		return this.database.getLastBlockSignature();
	}
}
