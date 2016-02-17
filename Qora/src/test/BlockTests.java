package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;

import ntp.NTP;

import org.junit.Test;

import database.DBSet;
import qora.BlockGenerator;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.block.Block;
import qora.block.BlockFactory;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.transaction.GenesisTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.Transaction;

public class BlockTests
{
	@Test
	public void validateSignatureGenesisBlock()
	{
		Block genesisBlock = new GenesisBlock();
		
		//CHECK IF SIGNATURE VALID
		assertEquals(true, genesisBlock.isSignatureValid());
	}
	
	@Test
	public void validateGenesisBlock()
	{
		//CREATE EMPTY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE GENESIS BLOCK
		Block genesisBlock = new GenesisBlock();
		
		//CHECK IF VALID
		assertEquals(true, genesisBlock.isValid(databaseSet));
		
		//ADD INVALID GENESIS TRANSACTION
		Transaction transaction = new GenesisTransaction(new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g"), BigDecimal.valueOf(-1000).setScale(8), NTP.getTime());
		genesisBlock.addTransaction(transaction);
		
		//CHECK IF INVALID
		assertEquals(false, genesisBlock.isValid(databaseSet));
		
		//CREATE NEW BLOCK
		genesisBlock = new GenesisBlock();
		
		//CHECK IF VALID
		assertEquals(true, genesisBlock.isValid(databaseSet));
		
		//PROCESS
		genesisBlock.process(databaseSet);
		
		//CHECK IF INVALID
		assertEquals(false, genesisBlock.isValid(databaseSet));
	}
	
	@Test
	public void parseGenesisBlock()
	{
		//CREATE VALID BLOCK
		Block genesisBlock = new GenesisBlock();
				
		//CONVERT TO BYTES
		byte[] rawBlock = genesisBlock.toBytes();
				
		try 
		{	
			//PARSE FROM BYTES
			Block parsedBlock = BlockFactory.getInstance().parse(rawBlock);
					
			//CHECK INSTANCE
			assertEquals(true, parsedBlock instanceof GenesisBlock);
					
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisBlock.getSignature(), parsedBlock.getSignature()));
					
			//CHECK GENERATOR
			assertEquals(genesisBlock.getGenerator().getAddress(), parsedBlock.getGenerator().getAddress());	
					
			//CHECK BASE TARGET
			assertEquals(genesisBlock.getGeneratingBalance(), parsedBlock.getGeneratingBalance());	
			
			//CHECK FEE
			assertEquals(genesisBlock.getTotalFee(), parsedBlock.getTotalFee());	
					
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisBlock.getReference(), parsedBlock.getReference()));	
					
			//CHECK TIMESTAMP
			assertEquals(genesisBlock.getTimestamp(), parsedBlock.getTimestamp());

			//CHECK TRANSACTION COUNT
			assertEquals(genesisBlock.getTransactionCount(), parsedBlock.getTransactionCount());
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
				
		//PARSE TRANSACTION FROM WRONG BYTES
		rawBlock = new byte[50];
		
		try 
		{	
			//PARSE FROM BYTES
			BlockFactory.getInstance().parse(rawBlock);
					
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void validateSignatureBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK IF SIGNATURE VALID
		assertEquals(true, newBlock.isSignatureValid());
		
		//INVALID TRANSACTION SIGNATURE
		transactionsSignature = new byte[64];
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());
		
		//INVALID GENERATOR SIGNATURE
		newBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), newBlock.getTimestamp(), newBlock.getGeneratingBalance(), generator, new byte[32]);
		transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
		newBlock.setTransactionsSignature(transactionsSignature);
		
		///CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());
		
		//VALID TRANSACTION SIGNATURE
		newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);	
		
		//ADD TRANSACTION
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = newBlock.getTimestamp();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(databaseSet), signature);
		newBlock.addTransaction(payment);
		
		//ADD TRANSACTION SIGNATURE
		transactionsSignature = blockGenerator.calculateTransactionsSignature(newBlock, generator);
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID TRANSACTION SIGNATURE
		assertEquals(true, newBlock.isSignatureValid());	
		
		//INVALID TRANSACTION SIGNATURE
		newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);	
		
		//ADD TRANSACTION
		payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(200).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(databaseSet), signature);
		newBlock.addTransaction(payment);
				
		//ADD TRANSACTION SIGNATURE
		transactionsSignature = blockGenerator.calculateTransactionsSignature(newBlock, generator);
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK INVALID TRANSACTION SIGNATURE
		assertEquals(false, newBlock.isSignatureValid());	
	}
	
	@Test
	public void validateBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
								
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
						
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
				
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK IF VALID
		assertEquals(true, newBlock.isValid(databaseSet));
		
		//CHANGE REFERENCE
		Block invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), new byte[128], newBlock.getTimestamp(), newBlock.getGeneratingBalance(), newBlock.getGenerator(), newBlock.getGeneratorSignature());
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//CHANGE TIMESTAMP
		invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), 1L, newBlock.getGeneratingBalance(), newBlock.getGenerator(), newBlock.getGeneratorSignature());
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//CHANGE BASETARGET
		invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), newBlock.getTimestamp(), 1L, newBlock.getGenerator(), newBlock.getGeneratorSignature());
				
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//ADD INVALID TRANSACTION
		invalidBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = newBlock.getTimestamp();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, generator, recipient, BigDecimal.valueOf(-100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(-100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(databaseSet), signature);
		invalidBlock.addTransaction(payment);		
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//ADD GENESIS TRANSACTION
		invalidBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);	
		transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), newBlock.getTimestamp());
		invalidBlock.addTransaction(transaction);	
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
	}
	
	@Test
	public void parseBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
										
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
						
		//FORK
		DBSet fork = databaseSet.fork();
				
		//GENERATE PAYMENT 1
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = block.getTimestamp();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment1 = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(databaseSet), signature);
		payment1.process(fork);
		block.addTransaction(payment1);	
				
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("XLPYYfxKEiDcybCkFA7jXcxSdePMMoyZLt");
		signature = PaymentTransaction.generateSignature(fork, generator, recipient2, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment2 = new PaymentTransaction(generator, recipient2, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(fork), signature);
		block.addTransaction(payment2);	
						
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = Crypto.getInstance().sign(generator, block.getGeneratorSignature());
		block.setTransactionsSignature(transactionsSignature);
				
		//CONVERT TO BYTES
		byte[] rawBlock = block.toBytes();
				
		try 
		{	
			//PARSE FROM BYTES
			Block parsedBlock = BlockFactory.getInstance().parse(rawBlock);
					
			//CHECK INSTANCE
			assertEquals(false, parsedBlock instanceof GenesisBlock);
					
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(block.getSignature(), parsedBlock.getSignature()));
					
			//CHECK GENERATOR
			assertEquals(block.getGenerator().getAddress(), parsedBlock.getGenerator().getAddress());	
					
			//CHECK BASE TARGET
			assertEquals(block.getGeneratingBalance(), parsedBlock.getGeneratingBalance());	
			
			//CHECK FEE
			assertEquals(block.getTotalFee(), parsedBlock.getTotalFee());	
					
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(block.getReference(), parsedBlock.getReference()));	
					
			//CHECK TIMESTAMP
			assertEquals(block.getTimestamp(), parsedBlock.getTimestamp());		
			
			//CHECK TRANSACTIONS COUNT
			assertEquals(block.getTransactionCount(), parsedBlock.getTransactionCount());		
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
				
		//PARSE TRANSACTION FROM WRONG BYTES
		rawBlock = new byte[50];
		
		try 
		{	
			//PARSE FROM BYTES
			BlockFactory.getInstance().parse(rawBlock);
					
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}			
	}
	
	@Test
	public void processBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
										
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
										
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
												
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//FORK
		DBSet fork = databaseSet.fork();
		
		//GENERATE PAYMENT 1
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = block.getTimestamp();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment1 = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(databaseSet), signature);
		payment1.process(fork);
		block.addTransaction(payment1);	
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("XLPYYfxKEiDcybCkFA7jXcxSdePMMoyZLt");
		signature = PaymentTransaction.generateSignature(fork, generator, recipient2, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment2 = new PaymentTransaction(generator, recipient2, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(fork), signature);
		block.addTransaction(payment2);	
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = blockGenerator.calculateTransactionsSignature(block, generator);
		block.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		assertEquals(true, block.isValid(databaseSet));
		
		//PROCESS BLOCK
		block.process(databaseSet);
		
		//CHECK BALANCE GENERATOR
		assertEquals(true, generator.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(800)) == 0);
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals(true, Arrays.equals(generator.getLastReference(databaseSet), payment2.getSignature()));
		
		//CHECK BALANCE RECIPIENT
		assertEquals(true, recipient.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(1100)) == 0);
		
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(recipient.getLastReference(databaseSet), payment1.getSignature()));
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(true, recipient2.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(100)) == 0);
				
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient2.getLastReference(databaseSet), payment2.getSignature()));
		
		//CHECK TOTAL FEE
		assertEquals(true, block.getTotalFee().compareTo(BigDecimal.valueOf(2)) == 0);
		
		//CHECK TOTAL TRANSACTIONS
		assertEquals(2, block.getTransactionCount());
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(block.getSignature(), databaseSet.getBlockMap().getLastBlock().getSignature()));
	}
	
	@Test
	public void orphanBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
										
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
										
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
												
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//FORK
		DBSet fork = databaseSet.fork();
		
		//GENERATE PAYMENT 1
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = block.getTimestamp();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment1 = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(databaseSet), signature);
		payment1.process(fork);
		block.addTransaction(payment1);	
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("XLPYYfxKEiDcybCkFA7jXcxSdePMMoyZLt");
		signature = PaymentTransaction.generateSignature(fork, generator, recipient2, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment2 = new PaymentTransaction(generator, recipient2, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(fork), signature);
		block.addTransaction(payment2);	
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = blockGenerator.calculateTransactionsSignature(block, generator);
		block.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		assertEquals(true, block.isValid(databaseSet));
		
		//PROCESS BLOCK
		block.process(databaseSet);
		
		//ORPHAN BLOCK
		block.orphan(databaseSet);
		
		//CHECK BALANCE GENERATOR
		assertEquals(true, generator.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(1000)) == 0);
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals(true, Arrays.equals(generator.getLastReference(databaseSet), transaction.getSignature()));
		
		//CHECK BALANCE RECIPIENT
		assertEquals(true, recipient.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(1000)) == 0);
		
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(recipient.getLastReference(databaseSet), payment1.getSignature()));
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(true, recipient2.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(0)) == 0);
				
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient2.getLastReference(databaseSet), new byte[0]));
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(genesisBlock.getSignature(), databaseSet.getBlockMap().getLastBlock().getSignature()));
	}
}
