package utils;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.junit.Test;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.payment.Payment;
import qora.transaction.ArbitraryTransactionV3;
import qora.transaction.GenesisTransaction;
import qora.transaction.MessageTransactionV3;
import qora.transaction.Transaction;


public class TransactionV3Tests {

	@Test
	public void validateMessageTransactionV3() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//ADD QORA ASSET
		Asset qoraAsset = new Asset(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, true, new byte[64]);
		databaseSet.getAssetMap().set(0l, qoraAsset);
    	
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		Account recipient = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		creator.setConfirmedBalance(61l, BigDecimal.valueOf(100).setScale(8), databaseSet);
		
		byte[] signature = MessageTransactionV3.generateSignature(
				databaseSet, 
				creator, recipient, 61l, //	ATFunding
				BigDecimal.valueOf(10).setScale(8), 
				BigDecimal.valueOf(1).setScale(8), 
				data, 
				new byte[] { 1 },
				new byte[] { 0 },
				timestamp
				);
		
		MessageTransactionV3 messageTransactionV3 = new MessageTransactionV3(
				creator, recipient, 61l, //	ATFunding 
				BigDecimal.valueOf(10).setScale(8), 
				BigDecimal.valueOf(1).setScale(8), 
				data, 
				new byte[] { 1 },
				new byte[] { 0 },
				timestamp,
				creator.getLastReference(databaseSet),
				signature
				);
		
		if( messageTransactionV3.getTimestamp() < Transaction.POWFIX_RELEASE || databaseSet.getBlockMap().getLastBlock().getHeight(databaseSet) < Transaction.MESSAGE_BLOCK_HEIGHT_RELEASE)
		{
			assertEquals(messageTransactionV3.isValid(databaseSet), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(messageTransactionV3.isValid(databaseSet), Transaction.VALIDATE_OKE);
		}
		
		messageTransactionV3.process(databaseSet);
		
		assertEquals(BigDecimal.valueOf(999).setScale(8), creator.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(90).setScale(8), creator.getConfirmedBalance(61l, databaseSet));
		assertEquals(BigDecimal.valueOf(10).setScale(8), recipient.getConfirmedBalance(61l, databaseSet));
		
		byte[] rawMessageTransactionV3 = messageTransactionV3.toBytes();
		
		MessageTransactionV3 messageTransactionV3_2 = null;
		try {
			messageTransactionV3_2 = (MessageTransactionV3) MessageTransactionV3.Parse(Arrays.copyOfRange(rawMessageTransactionV3, 4, rawMessageTransactionV3.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(new String(messageTransactionV3.getData()), new String(messageTransactionV3_2.getData()));
		assertEquals(messageTransactionV3.getCreator(), messageTransactionV3_2.getCreator());
		assertEquals(messageTransactionV3.getRecipient(), messageTransactionV3_2.getRecipient());
		assertEquals(messageTransactionV3.getKey(), messageTransactionV3_2.getKey());
		assertEquals(messageTransactionV3.getAmount(), messageTransactionV3_2.getAmount());
		assertEquals(messageTransactionV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
		assertEquals(messageTransactionV3.isText(), messageTransactionV3_2.isText());
		
		assertEquals(messageTransactionV3.isSignatureValid(), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(), true);		
	}
	
	
	@Test
	public void validateArbitraryTransactionV3() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//ADD QORA ASSET
		Asset qoraAsset = new Asset(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, true, new byte[64]);
		Asset aTFundingAsset = new Asset(new GenesisBlock().getGenerator(), "ATFunding", "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into Qora.", 250000000L, true, new byte[64]);
		databaseSet.getAssetMap().set(0l, qoraAsset);
		databaseSet.getAssetMap().set(61l, aTFundingAsset);
    	
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		Account recipient1 = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		
		Account recipient2 = new Account("QbVq5kgfYY1kRh9EdLSQfR9XHxVy1fLstQ");		
		Account recipient3 = new Account("QcJCST3wT8t22jKM2FFDhL8zKiH8cuBjEB");		

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		creator.setConfirmedBalance(61l, BigDecimal.valueOf(1000).setScale(8), databaseSet);
		
		List<Payment> payments = new ArrayList<Payment>();
		payments.add(new Payment(recipient1, 61l, BigDecimal.valueOf(110).setScale(8)));
		payments.add(new Payment(recipient2, 61l, BigDecimal.valueOf(120).setScale(8)));
		payments.add(new Payment(recipient3, 61l, BigDecimal.valueOf(201).setScale(8)));
		
		byte[] signature = ArbitraryTransactionV3.generateSignature(
				databaseSet, 
				creator, payments, 111, data,
				BigDecimal.valueOf(1).setScale(8), 
				timestamp
				);
		
		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				creator, payments, 111, data,
				BigDecimal.valueOf(1).setScale(8), 
				timestamp,
				creator.getLastReference(databaseSet),
				signature
				);
		
		if (NTP.getTime() < Transaction.ARBITRARY_TRANSACTIONS_RELEASE || arbitraryTransactionV3.getTimestamp() < Transaction.POWFIX_RELEASE)
		{
			assertEquals(arbitraryTransactionV3.isValid(databaseSet), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(arbitraryTransactionV3.isValid(databaseSet), Transaction.VALIDATE_OKE);
		}
		
		arbitraryTransactionV3.process(databaseSet);
		
		assertEquals(BigDecimal.valueOf(999).setScale(8), creator.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(1000-110-120-201).setScale(8), creator.getConfirmedBalance(61l, databaseSet));
		assertEquals(BigDecimal.valueOf(110).setScale(8), recipient1.getConfirmedBalance(61l, databaseSet));
		assertEquals(BigDecimal.valueOf(120).setScale(8), recipient2.getConfirmedBalance(61l, databaseSet));
		assertEquals(BigDecimal.valueOf(201).setScale(8), recipient3.getConfirmedBalance(61l, databaseSet));
		
		byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes();
		
		ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
		try {
			arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 4, rawArbitraryTransactionV3.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));
		assertEquals(	arbitraryTransactionV3.getPayments().get(0).toJson().toJSONString(), 
						arbitraryTransactionV3_2.getPayments().get(0).toJson().toJSONString());
		assertEquals(	arbitraryTransactionV3.getPayments().get(1).toJson().toJSONString(), 
						arbitraryTransactionV3_2.getPayments().get(1).toJson().toJSONString());
		assertEquals(	arbitraryTransactionV3.getPayments().get(2).toJson().toJSONString(), 
						arbitraryTransactionV3_2.getPayments().get(2).toJson().toJSONString());
		assertEquals( 	arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());  

		assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
		assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

		assertEquals(arbitraryTransactionV3.isSignatureValid(), true);
		assertEquals(arbitraryTransactionV3_2.isSignatureValid(), true);		
	}	
	
	@Test
	public void validateArbitraryTransactionV3withoutPayments() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//ADD QORA ASSET
		Asset qoraAsset = new Asset(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, true, new byte[64]);
		Asset aTFundingAsset = new Asset(new GenesisBlock().getGenerator(), "ATFunding", "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into Qora.", 250000000L, true, new byte[64]);
		databaseSet.getAssetMap().set(0l, qoraAsset);
		databaseSet.getAssetMap().set(61l, aTFundingAsset);
    	
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		
		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		creator.setConfirmedBalance(61l, BigDecimal.valueOf(1000).setScale(8), databaseSet);
		
		List<Payment> payments = new ArrayList<Payment>();
		
		byte[] signature = ArbitraryTransactionV3.generateSignature(
				databaseSet, 
				creator, payments, 111, data,
				BigDecimal.valueOf(1).setScale(8), 
				timestamp
				);
		
		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				creator, payments, 111, data,
				BigDecimal.valueOf(1).setScale(8), 
				timestamp,
				creator.getLastReference(databaseSet),
				signature
				);
		
		if (NTP.getTime() < Transaction.ARBITRARY_TRANSACTIONS_RELEASE || arbitraryTransactionV3.getTimestamp() < Transaction.POWFIX_RELEASE)
		{
			assertEquals(arbitraryTransactionV3.isValid(databaseSet), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(arbitraryTransactionV3.isValid(databaseSet), Transaction.VALIDATE_OKE);
		}
		
		arbitraryTransactionV3.process(databaseSet);
		
		assertEquals(BigDecimal.valueOf(999).setScale(8), creator.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(1000).setScale(8), creator.getConfirmedBalance(61l, databaseSet));

		
		byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes();
		
		ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
		try {
			arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 4, rawArbitraryTransactionV3.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));

		assertEquals( 	arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());  

		assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
		assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

		assertEquals(arbitraryTransactionV3.isSignatureValid(), true);
		assertEquals(arbitraryTransactionV3_2.isSignatureValid(), true);		
	}	
	
}
