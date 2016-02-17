package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Crypto;
import qora.payment.Payment;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class MultiPaymentTransaction extends Transaction {

	private static final int REFERENCE_LENGTH = 64;
	private static final int SENDER_LENGTH = 32;
	private static final int PAYMENTS_SIZE_LENGTH = 4;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + SENDER_LENGTH + PAYMENTS_SIZE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount sender;
	private List<Payment> payments;
	
	public MultiPaymentTransaction(PublicKeyAccount sender, List<Payment> payments, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(MULTI_PAYMENT_TRANSACTION, fee, timestamp, reference, signature);
		
		this.sender = sender;
		this.payments = payments;
	}
	
	//GETTERS/SETTERS
	
	public Account getSender()
	{
		return this.sender;
	}
	
	public List<Payment> getPayments()
	{
		return this.payments;
	}
	
	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception{
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		int position = 0;
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ SENDER
		byte[] senderBytes = Arrays.copyOfRange(data, position, position + SENDER_LENGTH);
		PublicKeyAccount sender = new PublicKeyAccount(senderBytes);
		position += SENDER_LENGTH;
		
		//READ PAYMENTS SIZE
		byte[] paymentsLengthBytes = Arrays.copyOfRange(data, position, position + PAYMENTS_SIZE_LENGTH);
		int paymentsLength = Ints.fromByteArray(paymentsLengthBytes);
		position += PAYMENTS_SIZE_LENGTH;
		
		if(paymentsLength < 1 || paymentsLength > 400)
		{
			throw new Exception("Invalid payments length");
		}
		
		//READ PAYMENTS
		List<Payment> payments = new ArrayList<Payment>();
		for(int i=0; i<paymentsLength; i++)
		{
			Payment payment = Payment.parse(Arrays.copyOfRange(data, position, position + Payment.BASE_LENGTH));
			payments.add(payment);
			
			position += Payment.BASE_LENGTH;
		}
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new MultiPaymentTransaction(sender, payments, fee, timestamp, reference, signatureBytes);	
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD SENDER/PAYMENTS
		transaction.put("sender", this.sender.getAddress());
		
		JSONArray payments = new JSONArray();
		for(Payment payment: this.payments)
		{
			payments.add(payment.toJson());
		}
		transaction.put("payments", payments);
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(MULTI_PAYMENT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE SENDER
		data = Bytes.concat(data , this.sender.getPublicKey());
	
		//WRITE PAYMENTS SIZE
		int paymentsLength = this.payments.size();
		byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
		data = Bytes.concat(data, paymentsLengthBytes);
		
		//WRITE PAYMENTS
		for(Payment payment: this.payments)
		{
			data = Bytes.concat(data, payment.toBytes());
		}
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		//SIGNATURE
		data = Bytes.concat(data, this.signature);
		
		return data;
	}

	@Override
	public int getDataLength() 
	{
		int paymentsLength = 0;
		for(Payment payment: this.getPayments())
		{
			paymentsLength += payment.getDataLength();
		}
		
		return TYPE_LENGTH + BASE_LENGTH + paymentsLength;
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(MULTI_PAYMENT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE SENDER
		data = Bytes.concat(data , this.sender.getPublicKey());
	
		//WRITE PAYMENTS SIZE
		int paymentsLength = this.payments.size();
		byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
		data = Bytes.concat(data, paymentsLengthBytes);
		
		//WRITE PAYMENTS
		for(Payment payment: this.payments)
		{
			data = Bytes.concat(payment.toBytes());
		}
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
				
		return Crypto.getInstance().verify(this.sender.getPublicKey(), this.signature, data);
	}
	
	@Override
	public int isValid(DBSet db) 
	{
		//CHECK IF RELEASED
		if(NTP.getTime() < Transaction.getASSETS_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		
		//CHECK PAYMENTS SIZE
		if(this.payments.size() < 1 || this.payments.size() > 400)
		{
			return INVALID_PAYMENTS_LENGTH;
		}
		
		//REMOVE FEE
		DBSet fork = db.fork();
		this.sender.setConfirmedBalance(this.sender.getConfirmedBalance(fork).subtract(this.fee), fork);
		
		//ONLY AFTER POWFIX_RELEASE TO SAVE THE OLD NETWORK
		if(this.timestamp >= Transaction.getPOWFIX_RELEASE()) {
			//CHECK IF SENDER HAS ENOUGH QORA BALANCE
			if(this.sender.getConfirmedBalance(fork).compareTo(BigDecimal.ZERO) == -1)
			{
				return NO_BALANCE;
			}	
		}
		
		//CHECK PAYMENTS
		for(Payment payment: this.payments)
		{	
			//CHECK IF RECIPIENT IS VALID ADDRESS
			if(!Crypto.getInstance().isValidAddress(payment.getRecipient().getAddress()))
			{
				return INVALID_ADDRESS;
			}
			
			//CHECK IF AMOUNT IS POSITIVE
			if(payment.getAmount().compareTo(BigDecimal.ZERO) <= 0)
			{
				return NEGATIVE_AMOUNT;
			}
			
			//CHECK IF SENDER HAS ENOUGH ASSET BALANCE
			if(this.sender.getConfirmedBalance(payment.getAsset(), fork).compareTo(payment.getAmount()) == -1)
			{
				return NO_BALANCE;
			}
			
			//CHECK IF AMOUNT IS DIVISIBLE
			if(!db.getAssetMap().get(payment.getAsset()).isDivisible())
			{
				//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
				if(payment.getAmount().stripTrailingZeros().scale() > 0)
				{
					//AMOUNT HAS DECIMALS
					return INVALID_AMOUNT;
				}
			}
			
			//PROCESS PAYMENT IN FORK
			payment.process(this.sender, fork);
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.sender.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF FEE IS POSITIVE
		if(this.fee.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_FEE;
		}
		
		return VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db) 
	{
		//UPDATE SENDER
		this.sender.setConfirmedBalance(this.sender.getConfirmedBalance(db).subtract(this.fee), db);
						
		//UPDATE REFERENCE OF SENDER
		this.sender.setLastReference(this.signature, db);
		
		//PROCESS PAYMENTS
		for(Payment payment: this.payments)
		{
			payment.process(this.sender, db);
			
			//UPDATE REFERENCE OF RECIPIENT
			if(Arrays.equals(payment.getRecipient().getLastReference(db), new byte[0]))
			{
				payment.getRecipient().setLastReference(this.signature, db);
			}		
		}
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE SENDER
		this.sender.setConfirmedBalance(this.sender.getConfirmedBalance(db).add(this.fee), db);
						
		//UPDATE REFERENCE OF SENDER
		this.sender.setLastReference(this.reference, db);
		
		//ORPHAN PAYMENTS
		for(Payment payment: this.payments)
		{
			payment.orphan(this.sender, db);
								
			//UPDATE REFERENCE OF RECIPIENT
			if(Arrays.equals(payment.getRecipient().getLastReference(db), this.signature))
			{
				payment.getRecipient().removeReference(db);
			}
		}
	}

	//REST
	
	@Override
	public PublicKeyAccount getCreator()
	{
		return this.sender;
	}
	
	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.sender);
		
		for(Payment payment: this.payments)
		{
			accounts.add(payment.getRecipient());
		}
		
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		for(Account involved: this.getInvolvedAccounts())
		{
			if(address.equals(involved.getAddress()))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String address = account.getAddress();
		
		//IF SENDER
		if(address.equals(this.sender.getAddress()))
		{
			amount = amount.subtract(this.fee);
		}

		//CHECK PAYMENTS
		for(Payment payment: this.payments)
		{
			//IF QORA ASSET
			if(payment.getAsset() == BalanceMap.QORA_KEY)
			{
				//IF SENDER
				if(address.equals(this.sender.getAddress()))
				{
					amount = amount.subtract(payment.getAmount());
				}
				
				//IF RECIPIENT
				if(address.equals(payment.getRecipient().getAddress()))
				{
					amount = amount.add(payment.getAmount());
				}
			}
		}
		
		return amount;
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount sender, List<Payment> payments, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(MULTI_PAYMENT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, sender.getLastReference(db));
		
		//WRITE SENDER
		data = Bytes.concat(data , sender.getPublicKey());
		
		//WRITE PAYMENTS SIZE
		int paymentsLength = payments.size();
		byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
		data = Bytes.concat(data, paymentsLengthBytes);
		
		//WRITE PAYMENTS
		for(Payment payment: payments)
		{
			data = Bytes.concat(payment.toBytes());
		}
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		//SIGN
		return Crypto.getInstance().sign(sender, data);
	}
}
