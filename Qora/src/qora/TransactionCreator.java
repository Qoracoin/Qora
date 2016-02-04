package qora;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ntp.NTP;
import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.assets.Order;
import qora.block.Block;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.payment.Payment;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.ArbitraryTransactionV3;
import qora.transaction.MessageTransaction;
import qora.transaction.MessageTransactionV3;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.DeployATTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.MultiPaymentTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransferAssetTransaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import settings.Settings;
import utils.Pair;
import utils.TransactionTimestampComparator;
import database.DBSet;

public class TransactionCreator
{
	private DBSet fork;
	private Block lastBlock;
	
	private void checkUpdate()
	{
		//CHECK IF WE ALREADY HAVE A FORK
		if(this.lastBlock == null)
		{
			updateFork();
		}
		else
		{
			//CHECK IF WE NEED A NEW FORK
			if(!Arrays.equals(this.lastBlock.getSignature(), Controller.getInstance().getLastBlock().getSignature()))
			{
				updateFork();
			}
		}
	}
	
	private void updateFork()
	{
		//CREATE NEW FORK
		this.fork = DBSet.getInstance().fork();
		
		//UPDATE LAST BLOCK
		this.lastBlock = Controller.getInstance().getLastBlock();
			
		//SCAN UNCONFIRMED TRANSACTIONS FOR TRANSACTIONS WHERE ACCOUNT IS CREATOR OF
		List<Transaction> transactions = DBSet.getInstance().getTransactionMap().getTransactions();
		List<Transaction> accountTransactions = new ArrayList<Transaction>();
			
		for(Transaction transaction: transactions)
		{
			if(Controller.getInstance().getAccounts().contains(transaction.getCreator()))
			{
				accountTransactions.add(transaction);
			}
		}
			
		//SORT THEM BY TIMESTAMP
		Collections.sort(accountTransactions, new TransactionTimestampComparator());
			
		//VALIDATE AND PROCESS THOSE TRANSACTIONS IN FORK
		for(Transaction transaction: accountTransactions)
		{
			if(transaction.isValid(this.fork) == Transaction.VALIDATE_OKE && transaction.isSignatureValid())
			{
				transaction.process(this.fork);
			}
			else
			{
				//THE TRANSACTION BECAME INVALID LET 
				DBSet.getInstance().getTransactionMap().delete(transaction);
			}
		}
	}
	
	public Pair<Transaction, Integer> createPayment(PrivateKeyAccount sender, Account recipient, BigDecimal amount, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = PaymentTransaction.generateSignature(this.fork, sender, recipient, amount, fee, time);
		
		//CREATE PAYMENT
		PaymentTransaction payment = new PaymentTransaction(new PublicKeyAccount(sender.getPublicKey()), recipient, amount, fee, time, sender.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(payment);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForPayment() 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount sender = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE PAYMENT
		PaymentTransaction payment = new PaymentTransaction(sender, sender, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, time, signature, signature);
					
		return new Pair(payment.calcRecommendedFee(), payment.getDataLength());
	}
	
	public Pair<Transaction, Integer> createNameRegistration(PrivateKeyAccount registrant, Name name, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = RegisterNameTransaction.generateSignature(this.fork, registrant, name, fee, time);
		
		//CREATE NAME REGISTRATION
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(registrant, name, fee, time, registrant.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(nameRegistration);
	}
	 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameRegistration(Name name) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount registrant = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE NAME UPDATE
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(registrant, name, Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(nameRegistration.calcRecommendedFee(), nameRegistration.getDataLength());
	}
	
	public Pair<Transaction, Integer> createNameUpdate(PrivateKeyAccount owner, Name name, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = UpdateNameTransaction.generateSignature(this.fork, owner, name, fee, time);
		
		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(owner, name, fee, time, owner.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(nameUpdate);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameUpdate(Name name) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount owner = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(owner, name, Transaction.MINIMUM_FEE, time, signature, signature);
				
		return new Pair(nameUpdate.calcRecommendedFee(), nameUpdate.getDataLength());
	}
	
	public Pair<Transaction, Integer> createNameSale(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
				
		//CREATE SIGNATURE
		byte[] signature = SellNameTransaction.generateSignature(this.fork, owner, nameSale, fee, time);
				
		//CREATE NAME SALE
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(owner, nameSale, fee, time, owner.getLastReference(this.fork), signature);
				
		//VALIDATE AND PROCESS
		return this.afterCreate(nameSaleTransaction);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameSale(NameSale nameSale) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount owner = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE NAME SALE
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(owner, nameSale, Transaction.MINIMUM_FEE, time, signature, signature);
					
		return new Pair(nameSaleTransaction.calcRecommendedFee(), nameSaleTransaction.getDataLength());
	}
	
	public Pair<Transaction, Integer> createCancelNameSale(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
				
		//CREATE SIGNATURE
		byte[] signature = CancelSellNameTransaction.generateSignature(this.fork, owner, nameSale.getKey(), fee, time);
				
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(owner, nameSale.getKey(), fee, time, owner.getLastReference(this.fork), signature);
				
		//VALIDATE AND PROCESS
		return this.afterCreate(cancelNameSaleTransaction);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForCancelNameSale(NameSale nameSale) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount owner = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(owner, nameSale.getKey(), Transaction.MINIMUM_FEE, time, signature, signature);
				
		return new Pair(cancelNameSaleTransaction.calcRecommendedFee(), cancelNameSaleTransaction.getDataLength());
	}
	
	public Pair<Transaction, Integer> createNamePurchase(PrivateKeyAccount buyer, NameSale nameSale, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
				
		//CREATE SIGNATURE
		byte[] signature = BuyNameTransaction.generateSignature(this.fork, buyer, nameSale, nameSale.getName().getOwner(), fee, time);
				
		//CREATE NAME PURCHASE
		BuyNameTransaction namePurchase = new BuyNameTransaction(buyer, nameSale, nameSale.getName().getOwner(), fee, time, buyer.getLastReference(this.fork), signature);
				
		//VALIDATE AND PROCESS
		return this.afterCreate(namePurchase);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForNamePurchase(NameSale nameSale) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount buyer = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE NAME UPDATE
		BuyNameTransaction namePurchase = new BuyNameTransaction(buyer, nameSale, nameSale.getName().getOwner(), Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(namePurchase.calcRecommendedFee(), namePurchase.getDataLength());
	}
	
	public Pair<Transaction, Integer> createPollCreation(PrivateKeyAccount creator, Poll poll, BigDecimal fee) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
						
		//TIME
		long time = NTP.getTime();
						
		//CREATE SIGNATURE
		byte[] signature = CreatePollTransaction.generateSignature(this.fork, creator, poll, fee, time);
					
		//CREATE POLL CREATION
		CreatePollTransaction pollCreation = new CreatePollTransaction(creator, poll, fee, time, creator.getLastReference(this.fork), signature);
						
		//VALIDATE AND PROCESS
		return this.afterCreate(pollCreation);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForPollCreation(Poll poll) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE NAME UPDATE
		CreatePollTransaction pollCreation = new CreatePollTransaction(creator, poll, Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(pollCreation.calcRecommendedFee(), pollCreation.getDataLength());
	}

	public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, String poll, int optionIndex, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
						
		//TIME
		long time = NTP.getTime();
						
		//CREATE SIGNATURE
		byte[] signature = VoteOnPollTransaction.generateSignature(this.fork, creator, poll, optionIndex, fee, time);
					
		//CREATE POLL VOTE
		VoteOnPollTransaction pollVote = new VoteOnPollTransaction(creator, poll, optionIndex, fee, time, creator.getLastReference(this.fork), signature);
						
		//VALIDATE AND PROCESS
		return this.afterCreate(pollVote);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForPollVote(String poll) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE VOTE
		VoteOnPollTransaction pollVote = new VoteOnPollTransaction(creator, poll, 0, Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(pollVote.calcRecommendedFee(), pollVote.getDataLength());
	}
	
	public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, List<Payment> payments, int service, byte[] data, BigDecimal fee) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
			
		Transaction arbitraryTransaction;
		
		//TIME
		long time = NTP.getTime();
		
		if(time < Transaction.POWFIX_RELEASE)
		{
			//CREATE SIGNATURE
			byte[] signature = ArbitraryTransaction.generateSignature(this.fork, creator, service, data, fee, time);
							
			//CREATE ARBITRARY TRANSACTION V1
			arbitraryTransaction = new ArbitraryTransaction(creator, service, data, fee, time, creator.getLastReference(this.fork), signature);
		}
		else
		{
			//CREATE SIGNATURE
			byte[] signature = ArbitraryTransactionV3.generateSignature(this.fork, creator, payments, service, data, fee, time);
							
			//CREATE ARBITRARY TRANSACTION V3
			arbitraryTransaction = new ArbitraryTransactionV3(creator, payments, service, data, fee, time, creator.getLastReference(this.fork), signature);
		}
		
		//VALIDATE AND PROCESS
		return this.afterCreate(arbitraryTransaction);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForArbitraryTransaction(byte[] data, List<Payment> payments) 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		Transaction arbitraryTransaction;
		
		if(time < Transaction.POWFIX_RELEASE)
		{
			//CREATE ARBITRARY TRANSACTION V1
			arbitraryTransaction = new ArbitraryTransaction(creator, 0, data, Transaction.MINIMUM_FEE, time, signature, signature);
		}
		else
		{
			//CREATE ARBITRARY TRANSACTION V3
			arbitraryTransaction = new ArbitraryTransactionV3(creator, payments, 0, data, Transaction.MINIMUM_FEE, time, signature, signature);			
		}
		
		return new Pair(arbitraryTransaction.calcRecommendedFee(), arbitraryTransaction.getDataLength());
	}
	
	public Pair<Transaction, Integer> createIssueAssetTransaction(PrivateKeyAccount creator, String name, String description, long quantity, boolean divisible, BigDecimal fee) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		Asset asset = new Asset(creator, name, description, quantity, divisible, new byte[64]);
		byte[] signature = IssueAssetTransaction.generateSignature(this.fork, creator, asset, fee, time);
							
		//CREATE ISSUE ASSET TRANSACTION
		asset = new Asset(creator, name, description, quantity, divisible, signature);
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(creator, asset, fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(issueAssetTransaction);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForIssueAssetTransaction(String name, String description) 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE ARBITRARY TRANSACTION
		Asset asset = new Asset(creator, name, description, 10000, true, signature);
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(creator, asset, Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(issueAssetTransaction.calcRecommendedFee(), issueAssetTransaction.getDataLength());
	}
	
	public Pair<Transaction, Integer> createOrderTransaction(PrivateKeyAccount creator, Asset have, Asset want, BigDecimal amount, BigDecimal price, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = CreateOrderTransaction.generateSignature(this.fork, creator, have.getKey(), want.getKey(), amount, price, fee, time);
							
		//CREATE ORDER TRANSACTION
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(creator, have.getKey(), want.getKey(), amount, price, fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(createOrderTransaction);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForOrderTransaction() 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE ORDER TRANSACTION
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(creator, 0, 0, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(createOrderTransaction.calcRecommendedFee(), createOrderTransaction.getDataLength());
	}
	
	public Pair<Transaction, Integer> createCancelOrderTransaction(PrivateKeyAccount creator, Order order, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = CancelOrderTransaction.generateSignature(this.fork, creator, order.getId(), fee, time);
							
		//CREATE PRDER TRANSACTION
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, order.getId(), fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(cancelOrderTransaction);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForCancelOrderTransaction() 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE TRANSACTION
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, BigInteger.ONE, Transaction.MINIMUM_FEE, time, signature, signature);
		
		return new Pair(cancelOrderTransaction.calcRecommendedFee(), cancelOrderTransaction.getDataLength());
	}

	
	public Pair<Transaction, Integer> createAssetTransfer(PrivateKeyAccount sender, Account recipient, Asset asset, BigDecimal amount, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = TransferAssetTransaction.generateSignature(this.fork, sender, recipient, asset.getKey(), amount, fee, time);
		
		//CREATE ASSET TRANSFER
		TransferAssetTransaction assetTransfer = new TransferAssetTransaction(sender, recipient, asset.getKey(), amount, fee, time, sender.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(assetTransfer);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForAssetTransfer() 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount sender = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE ASSET TRANSFER
		TransferAssetTransaction assetTransfer = new TransferAssetTransaction(sender, sender, 0l, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, time, signature, signature);
				
		return new Pair(assetTransfer.calcRecommendedFee(), assetTransfer.getDataLength());
	}
	
	public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount sender, List<Payment> payments, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = MultiPaymentTransaction.generateSignature(this.fork, sender, payments, fee, time);
		
		//CREATE MULTI PAYMENTS
		MultiPaymentTransaction multiPayment = new MultiPaymentTransaction(sender, payments, fee, time, sender.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(multiPayment);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForMultiPayment(List<Payment> payments) 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE MULTI PAYMENTS
		MultiPaymentTransaction multiPayment = new MultiPaymentTransaction(creator, payments, Transaction.MINIMUM_FEE, time, signature, signature);
				
		return new Pair(multiPayment.calcRecommendedFee(), multiPayment.getDataLength());
	}
	
	public Pair<Transaction, Integer> deployATTransaction(PrivateKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal amount, BigDecimal fee )
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = DeployATTransaction.generateSignature(this.fork, creator, name, description, creationBytes, amount, fee, time);
		
		//DEPLOY AT
		DeployATTransaction deployAT = new DeployATTransaction(creator, name, description, type, tags, creationBytes, amount, fee, time, creator.getLastReference(this.fork), signature);
		
		return this.afterCreate(deployAT);
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForDeployATTransaction(String name, String description, String type, String tags, byte[] creationBytes) 
	{	
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//DEPLOY AT
		DeployATTransaction deployAT = new DeployATTransaction(creator, name, description, type, tags, creationBytes, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, time, signature, signature);
				
		return new Pair(deployAT.calcRecommendedFee(), deployAT.getDataLength());
	}
	
	public Pair<Transaction, Integer> createMessage(PrivateKeyAccount sender,
			Account recipient, long key, BigDecimal amount, BigDecimal fee, byte[] isText,
			byte[] message, byte[] encryptMessage) {
		
		this.checkUpdate();
		
		Transaction messageTx;

		long timestamp = NTP.getTime();
		
		if(timestamp < Transaction.POWFIX_RELEASE)
		{
			//CREATE MESSAGE TRANSACTION V1
			byte[] signature = MessageTransaction.generateSignature(this.fork, sender, recipient, amount, fee, message, isText, encryptMessage, timestamp);
			messageTx = new MessageTransaction(sender, recipient, amount, fee, message, isText, encryptMessage, timestamp, sender.getLastReference(this.fork), signature );
		}
		else
		{
			//CREATE MESSAGE TRANSACTION V3
			byte[] signature = MessageTransactionV3.generateSignature(this.fork, sender, recipient, key, amount, fee, message, isText, encryptMessage, timestamp);
			messageTx = new MessageTransactionV3(sender, recipient, key, amount, fee, message, isText, encryptMessage, timestamp, sender.getLastReference(this.fork), signature );
		}
			
		return afterCreate(messageTx);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForMessage(byte[] message) 
	{
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount sender = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		Transaction messageTx;
		
		long timestamp = NTP.getTime();
		
		if(timestamp < Transaction.POWFIX_RELEASE)
		{
			//CREATE MESSAGE TRANSACTION V1
			messageTx = new MessageTransaction(sender, sender, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, message, new byte[1], new byte[1], time, signature, signature );
		}
		else
		{
			//CREATE MESSAGE TRANSACTION V3
			messageTx = new MessageTransactionV3(sender, sender, 0l, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, message, new byte[1], new byte[1], time, signature, signature );
		}
			
		return new Pair(messageTx.calcRecommendedFee(), messageTx.getDataLength());
	}
	
	private Pair<Transaction, Integer> afterCreate(Transaction transaction)
	{
		//CHECK IF PAYMENT VALID
		int valid = transaction.isValid(this.fork);
		
		if(valid == Transaction.VALIDATE_OKE)
		{
			//CHECK IF FEE BELOW MINIMUM
			if(!Settings.getInstance().isAllowFeeLessRequired() && !transaction.hasMinimumFeePerByte())
			{
				valid = Transaction.FEE_LESS_REQUIRED;
			}
			else
			{
				//PROCESS IN FORK
				transaction.process(this.fork);
						
				//CONTROLLER ONTRANSACTION
				Controller.getInstance().onTransactionCreate(transaction);
			}
		}
				
		//RETURN
		return new Pair<Transaction, Integer>(transaction, valid);
	}
}
