package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import ntp.NTP;

import org.junit.Assert;
import org.junit.Test;

import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.assets.Order;
import qora.assets.Trade;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.GenesisTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import database.DBSet;

public class OrderTests 
{
	@Test
	public void validateSignatureOrderTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = CreateOrderTransaction.generateSignature(databaseSet, sender, 1l, 2l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE ORDER TRANSACTION
		Transaction orderTransaction = new CreateOrderTransaction(sender, 1l, 2l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF ORDER CREATION SIGNATURE IS VALID
		assertEquals(true, orderTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		orderTransaction = new CreateOrderTransaction(sender, 1l, 2l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF ORDER CREATION SIGNATURE IS INVALID
		assertEquals(false, orderTransaction.isSignatureValid());
	}
	
	@Test
	public void validateCreateOrderTransaction() 
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//ADD QORA ASSET
    	Asset qoraAsset = new Asset(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, true, new byte[64]);
    	dbSet.getAssetMap().set(0l, qoraAsset);
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount account = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset asset = new Asset(account, "a", "a", 50000l, false, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(account, asset, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), account.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CHECK VALID
		long timeStamp = System.currentTimeMillis();
		CreateOrderTransaction orderCreation = new CreateOrderTransaction(account, 1l, 0l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timeStamp, account.getLastReference(dbSet), new byte[64]);		
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(dbSet));
		
		//CREATE INVALID ORDER CREATION HAVE EQUALS WANT
		orderCreation = new CreateOrderTransaction(account, 1l, 1l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timeStamp, account.getLastReference(dbSet), new byte[64]);		
			
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.HAVE_EQUALS_WANT, orderCreation.isValid(dbSet));
		
		//CREATE INVALID ORDER CREATION NOT ENOUGH BALANCE
		orderCreation = new CreateOrderTransaction(account, 1l, 0l, BigDecimal.valueOf(50001).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timeStamp, account.getLastReference(dbSet), new byte[64]);		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.NO_BALANCE, orderCreation.isValid(dbSet));
		
		//CREATE INVALID ORDER CREATION INVALID AMOUNT
		orderCreation = new CreateOrderTransaction(account, 1l, 0l, BigDecimal.valueOf(50.01).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timeStamp, account.getLastReference(dbSet), new byte[64]);		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.INVALID_AMOUNT, orderCreation.isValid(dbSet));
		
		//CREATE INVALID ORDER CREATION WANT DOES NOT EXIST
		orderCreation = new CreateOrderTransaction(account, 1l, 4l, BigDecimal.valueOf(50).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timeStamp, account.getLastReference(dbSet), new byte[64]);		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.ASSET_DOES_NOT_EXIST, orderCreation.isValid(dbSet));
		
		//CREATE ORDER CREATION INVALID REFERENCE
		orderCreation = new CreateOrderTransaction(account, 1l, 0l, BigDecimal.valueOf(50).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timeStamp, new byte[]{1,2}, new byte[64]);		
			
		//CHECK IF  ORDER CREATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, orderCreation.isValid(dbSet));
				
		//CREATE  ORDER CREATION INVALID FEE
		orderCreation = new CreateOrderTransaction(account, 1l, 0l, BigDecimal.valueOf(50).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.ZERO.setScale(8), timeStamp, account.getLastReference(dbSet), new byte[64]);		
				
		//CHECK IF  ORDER CREATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, orderCreation.isValid(dbSet));
		
	}
	
	@Test
	public void parseCreateOrderTransaction() 
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = CreateOrderTransaction.generateSignature(databaseSet, sender, 1l, 2l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE ORDER TRANSACTION
		CreateOrderTransaction orderCreation = new CreateOrderTransaction(sender, 1l, 2l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CONVERT TO BYTES
		byte[] rawOrderCreation = orderCreation.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			CreateOrderTransaction parsedOrderCreation = (CreateOrderTransaction) TransactionFactory.getInstance().parse(rawOrderCreation);
			
			//CHECK INSTANCE
			assertEquals(true, parsedOrderCreation instanceof CreateOrderTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(orderCreation.getSignature(), parsedOrderCreation.getSignature()));
			
			//CHECK HAVE
			assertEquals(orderCreation.getOrder().getHave(), parsedOrderCreation.getOrder().getHave());	
			
			//CHECK WANT
			assertEquals(orderCreation.getOrder().getWant(), parsedOrderCreation.getOrder().getWant());	
				
			//CHECK AMOUNT
			assertEquals(0, orderCreation.getOrder().getAmount().compareTo(parsedOrderCreation.getOrder().getAmount()));	
			
			//CHECK PRICE
			assertEquals(0, orderCreation.getOrder().getPrice().compareTo(parsedOrderCreation.getOrder().getPrice()));
			
			//CHECK FEE
			assertEquals(orderCreation.getFee(), parsedOrderCreation.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(orderCreation.getReference(), parsedOrderCreation.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(orderCreation.getTimestamp(), parsedOrderCreation.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawOrderCreation = new byte[orderCreation.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawOrderCreation);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void testOrderProcessingNonDivisible()
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(accountA, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetA = new Asset(accountA, "a", "a", 50000l, false, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ASSET B
		seed = Crypto.getInstance().digest("testb".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountB = new PrivateKeyAccount(privateKey);
		
		transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetB = new Asset(accountB, "b", "b", 50000l, false, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountB.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.1).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		createOrderTransaction.process(dbSet);
		
		//CREATE ORDER TWO (SELLING 1000 B FOR A AT A PRICE OF 5)
		//GENERATES TRADE 100 B FOR 1000 A
		createOrderTransaction = new CreateOrderTransaction(accountB, 2l, 1l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(5).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{5, 6});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(100))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(1000))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		Order orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(dbSet).size());
		
		Trade trade = orderB.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[64])));
		Assert.assertEquals(0, trade.getAmount().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(0, trade.getPrice().compareTo(BigDecimal.valueOf(100)));
			
		//CREATE ORDER THREE (SELLING 24 A FOR B AT A PRICE OF 0.2)
		//GENERATES TRADE 20 A FOR 4 B
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(24).setScale(8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{1, 2});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(48976))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(104))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(1020))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(104)));
		Assert.assertEquals(false, orderB.isFulfilled());
		
		Order orderC = dbSet.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilled().compareTo(BigDecimal.valueOf(20)));
		Assert.assertEquals(false, orderC.isFulfilled());
		
		//CHECK TRADES
		Assert.assertEquals(1, orderC.getInitiatedTrades(dbSet).size());
		
		trade = orderC.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{1, 2})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getAmount().compareTo(BigDecimal.valueOf(4)));
		Assert.assertEquals(0, trade.getPrice().compareTo(BigDecimal.valueOf(20)));
	}
	
	@Test
	public void testOrderProcessingWantDivisible()
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(accountA, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetA = new Asset(accountA, "a", "a", 50000l, false, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ASSET B
		seed = Crypto.getInstance().digest("testb".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountB = new PrivateKeyAccount(privateKey);
		
		transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetB = new Asset(accountB, "b", "b", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountB.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.1).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		createOrderTransaction.process(dbSet);
		
		//CREATE ORDER TWO (SELLING 99.9 B FOR A AT A PRICE OF 5)
		//GENERATES TRADE 99,9 B FOR 999 A		
		createOrderTransaction = new CreateOrderTransaction(accountB, 2l, 1l, BigDecimal.valueOf(99.9).setScale(8), BigDecimal.valueOf(5).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{5, 6});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49900.1))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(99.9))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(999))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = dbSet.getOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(999)));
		Assert.assertEquals(false, orderA.isFulfilled());
		
		Order orderB = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(99.9)));
		Assert.assertEquals(true, orderB.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(dbSet).size());
		
		Trade trade = orderB.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[64])));
		Assert.assertEquals(0, trade.getAmount().compareTo(BigDecimal.valueOf(999)));
		Assert.assertEquals(0, trade.getPrice().compareTo(BigDecimal.valueOf(99.9)));
		
		//CREATE ORDER THREE (SELLING 99 A FOR B AT A PRICE OF 0.2)
		//GENERATED TRADE 99 A FOR 9.9 B
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(99).setScale(8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{1, 2});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(48901))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49900.1))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(99.9))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(999))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = dbSet.getOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(999)));
		Assert.assertEquals(false, orderA.isFulfilled());
		
		orderB = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(99.9)));
		Assert.assertEquals(true, orderB.isFulfilled());
		
		Order orderC = dbSet.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilled().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderC.isFulfilled());
		
		//CHECK TRADES
		Assert.assertEquals(0, orderC.getInitiatedTrades(dbSet).size());
	}
	
	@Test
	public void testOrderProcessingHaveDivisible()
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(accountA, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetA = new Asset(accountA, "a", "a", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ASSET B
		seed = Crypto.getInstance().digest("testb".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountB = new PrivateKeyAccount(privateKey);
		
		transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetB = new Asset(accountB, "b", "b", 50000l, false, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountB.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.1).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		createOrderTransaction.process(dbSet);
		
		//CREATE ORDER TWO (SELLING 200 B FOR A AT A PRICE OF 5)
		//GENERATES TRADE 100 B FOR 1000 A
		createOrderTransaction = new CreateOrderTransaction(accountB, 2l, 1l, BigDecimal.valueOf(200).setScale(8), BigDecimal.valueOf(5).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{5, 6});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49800))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(100))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(1000))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		Order orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(dbSet).size());
		
		Trade trade = orderB.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[64])));
		Assert.assertEquals(0, trade.getAmount().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(0, trade.getPrice().compareTo(BigDecimal.valueOf(100)));
		
		//CREATE ORDER THREE (SELLING 99 A FOR B AT A PRICE OF 0.2) (I CAN BUY AT INCREMENTS OF 1)
		//GENERATED TRADE 95 A for 19 B
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(99).setScale(8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{1, 2});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(48901))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49800))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(119))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(1095))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(119)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		Order orderC = dbSet.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilled().compareTo(BigDecimal.valueOf(95)));
		Assert.assertEquals(false, orderC.isFulfilled());
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(dbSet).size());
		
		trade = orderC.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{1, 2})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getAmount().compareTo(BigDecimal.valueOf(19)));
		Assert.assertEquals(0, trade.getPrice().compareTo(BigDecimal.valueOf(95)));
	}
	
	@Test
	public void testOrderProcessingDivisible()
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(accountA, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetA = new Asset(accountA, "a", "a", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ASSET B
		seed = Crypto.getInstance().digest("testb".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountB = new PrivateKeyAccount(privateKey);
		
		transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetB = new Asset(accountB, "b", "b", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountB.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.1).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		createOrderTransaction.process(dbSet);
		
		//CREATE ORDER TWO (SELLING 999 B FOR A AT A PRICE OF 5) (I CAN BUY AT INCREMENTS OF 0,00000010)
		//GENERATES TRADE 100 B FOR 1000 A			
		createOrderTransaction = new CreateOrderTransaction(accountB, 2l, 1l, BigDecimal.valueOf(999).setScale(8), BigDecimal.valueOf(5).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{5, 6});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49001))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(100))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(1000))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		Order orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(dbSet).size());
		
		Trade trade = orderB.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[64])));
		Assert.assertEquals(0, trade.getAmount().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(0, trade.getPrice().compareTo(BigDecimal.valueOf(100)));
		
		//CREATE ORDER THREE (SELLING 99.99999999 A FOR B AT A PRICE OF 0.2) (I CAN BUY AT INCREMENTS OF 0,00000001)
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, new BigDecimal(BigInteger.valueOf(9999999999L), 8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{1, 2});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(new BigDecimal("48900.00000001"))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49001))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(new BigDecimal("119.99999999"))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(new BigDecimal("1099.99999995"))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[64]));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(new BigDecimal("119.99999999")));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		Order orderC = dbSet.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilled().compareTo(new BigDecimal("99.99999995")));
		Assert.assertEquals(false, orderC.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(dbSet).size());
		
		trade = orderC.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{1, 2})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getAmount().compareTo(new BigDecimal("19.99999999")));
		Assert.assertEquals(0, trade.getPrice().compareTo(new BigDecimal("99.99999995")));
	}
	
	@Test
	public void testOrderProcessingMultipleOrders()
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(accountA, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetA = new Asset(accountA, "a", "a", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ASSET B
		seed = Crypto.getInstance().digest("testb".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountB = new PrivateKeyAccount(privateKey);
		
		transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetB = new Asset(accountB, "b", "b", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountB.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.1).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{5,6});
		createOrderTransaction.process(dbSet);
		
		//CREATE ORDER TWO (SELLING 1000 A FOR B AT A PRICE FOR 0.20)
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{1, 2});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(48000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(50000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(0))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(0))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = dbSet.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderA.isFulfilled());
		
		Order orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(0, orderB.getInitiatedTrades(dbSet).size());
		
		//CREATE ORDER THREE (SELLING 150 B FOR A AT A PRICE OF 5)
		createOrderTransaction = new CreateOrderTransaction(accountB, 2l, 1l, BigDecimal.valueOf(150).setScale(8), BigDecimal.valueOf(5).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[]{3, 4});
		createOrderTransaction.process(dbSet);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(48000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(49850))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, dbSet).compareTo(BigDecimal.valueOf(150))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, dbSet).compareTo(BigDecimal.valueOf(1250))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilled());
		
		orderB = dbSet.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, dbSet.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(250)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		Order orderC = dbSet.getCompletedOrderMap().get(new BigInteger(new byte[]{3, 4}));
		Assert.assertEquals(false, dbSet.getOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilled().compareTo(BigDecimal.valueOf(150)));
		Assert.assertEquals(true, orderC.isFulfilled());
		
		//CHECK TRADES
		Assert.assertEquals(0, orderA.getInitiatedTrades(dbSet).size());
		Assert.assertEquals(0, orderB.getInitiatedTrades(dbSet).size());
		Assert.assertEquals(2, orderC.getInitiatedTrades(dbSet).size());
		
		Trade trade = orderC.getInitiatedTrades(dbSet).get(1);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{3, 4})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[]{5, 6})));
		Assert.assertEquals(0, trade.getAmount().compareTo(new BigDecimal("1000")));
		Assert.assertEquals(0, trade.getPrice().compareTo(new BigDecimal("100")));
		
		trade = orderC.getInitiatedTrades(dbSet).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(new BigInteger(new byte[]{3, 4})));
		Assert.assertEquals(0, trade.getTarget().compareTo(new BigInteger(new byte[]{1, 2})));
		Assert.assertEquals(0, trade.getAmount().compareTo(new BigDecimal("250")));
		Assert.assertEquals(0, trade.getPrice().compareTo(new BigDecimal("50")));
	}
	
	@Test
	public void testOrderProcessingForks()
	{
		DBSet dbSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET A
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
		
		Transaction transaction = new GenesisTransaction(accountA, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetA = new Asset(accountA, "a", "a", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ASSET B
		seed = Crypto.getInstance().digest("testb".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount accountB = new PrivateKeyAccount(privateKey);
		
		transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(dbSet);
		
		//CREATE ASSET
		Asset assetB = new Asset(accountB, "b", "b", 50000l, true, new byte[64]);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountB.getLastReference(dbSet), new byte[64]);
		issueAssetTransaction.process(dbSet);
		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		DBSet fork1 = dbSet.fork();
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.1).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(fork1), new byte[]{5,6});
		createOrderTransaction.process(fork1);
		
		//CREATE ORDER TWO (SELLING 1000 A FOR B AT A PRICE FOR 0.20)
		DBSet fork2 = fork1.fork();
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(fork2), new byte[]{1, 2});
		createOrderTransaction.process(fork2);
		
		//CREATE ORDER THREE (SELLING 150 B FOR A AT A PRICE OF 5)
		DBSet fork3 = fork2.fork();
		createOrderTransaction = new CreateOrderTransaction(accountB, 2l, 1l, BigDecimal.valueOf(150).setScale(8), BigDecimal.valueOf(5).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(fork3), new byte[]{3, 4});
		createOrderTransaction.process(fork3);
		
		//ORPHAN ORDER THREE
		createOrderTransaction.orphan(fork3);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, fork3).compareTo(BigDecimal.valueOf(48000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, fork3).compareTo(BigDecimal.valueOf(50000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, fork3).compareTo(BigDecimal.valueOf(0))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, fork3).compareTo(BigDecimal.valueOf(0))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = fork3.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, fork3.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderA.isFulfilled());
		
		Order orderB = fork3.getOrderMap().get(new BigInteger(new byte[]{1, 2}));
		Assert.assertEquals(false, fork3.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilled().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderB.isFulfilled());	
		
		//CHECK TRADES
		Assert.assertEquals(0, orderB.getInitiatedTrades(fork3).size());
		
		//ORPHAN ORDER TWO
		createOrderTransaction = new CreateOrderTransaction(accountA, 1l, 2l, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(0.2).setScale(8), BigDecimal.ONE.setScale(8), System.currentTimeMillis(), accountA.getLastReference(fork2), new byte[]{1, 2});
		createOrderTransaction.orphan(fork2);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(1l, fork2).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(2l, fork2).compareTo(BigDecimal.valueOf(50000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(2l, fork2).compareTo(BigDecimal.valueOf(0))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(1l, fork2).compareTo(BigDecimal.valueOf(0))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = fork2.getOrderMap().get(new BigInteger(new byte[]{5, 6}));
		Assert.assertEquals(false, fork2.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilled().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderA.isFulfilled());
		
		Assert.assertEquals(false, fork2.getOrderMap().contains(new BigInteger(new byte[]{1, 2})));
		Assert.assertEquals(false, fork2.getCompletedOrderMap().contains(new BigInteger(new byte[]{1, 2})));
	}
}
