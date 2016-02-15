package utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import controller.Controller;
import gui.transaction.ArbitraryTransactionDetailsFrame;
import gui.transaction.MessageTransactionDetailsFrame;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.payment.Payment;
import qora.transaction.ArbitraryTransactionV3;
import qora.transaction.MessageTransaction;
import qora.transaction.Transaction;

public class txWindowTest {

	@Test
	public void windowTest() {
		
		new MessageTransactionDetailsFrame(
				(MessageTransaction) Controller.getInstance().getTransaction(Base58.decode("2kGG3Nmu2VNatZ8MAL1PF5r3VUZyY5FsbPve9G2zJ1UL1x3NHDU96VFWn2cXvqHnvdjvY2jt3kuGTkgabr2JQXAx"))
			);
		
		
		Account recipient1 = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		
		Account recipient2 = new Account("QbVq5kgfYY1kRh9EdLSQfR9XHxVy1fLstQ");		
		Account recipient3 = new Account("QcJCST3wT8t22jKM2FFDhL8zKiH8cuBjEB");		

		List<Payment> payments = new ArrayList<Payment>();
		payments.add(new Payment(recipient1, 61l, BigDecimal.valueOf(110).setScale(8)));
		payments.add(new Payment(recipient2, 61l, BigDecimal.valueOf(120).setScale(8)));
		payments.add(new Payment(recipient3, 61l, BigDecimal.valueOf(201).setScale(8)));
		
		byte[] seed = Crypto.getInstance().digest("test".getBytes());

		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		
		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				creator, payments, 111, data,
				BigDecimal.valueOf(1).setScale(8), 
				Transaction.getPOWFIX_RELEASE(),
				new byte[]{0},
				new byte[]{0}
				);
		
		new ArbitraryTransactionDetailsFrame(
				arbitraryTransactionV3
			);
	}
	
}
