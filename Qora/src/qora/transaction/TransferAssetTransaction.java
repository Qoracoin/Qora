package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class TransferAssetTransaction extends Transaction {

	private static final int REFERENCE_LENGTH = 64;
	private static final int SENDER_LENGTH = 32;
	private static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;
	private static final int KEY_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 12;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + SENDER_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount sender;
	private Account recipient;
	private BigDecimal amount;
	private long key;
	
	public TransferAssetTransaction(PublicKeyAccount sender, Account recipient, long key, BigDecimal amount, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(TRANSFER_ASSET_TRANSACTION, fee, timestamp, reference, signature);
		
		this.sender = sender;
		this.recipient = recipient;
		this.amount = amount;
		this.key = key;
	}
	
	//GETTERS/SETTERS
	
	public Account getSender()
	{
		return this.sender;
	}
	
	public Account getRecipient()
	{
		return this.recipient;
	}
	
	public BigDecimal getAmount() 
	{
		return this.amount;
	}
	
	public long getKey()
	{
		return this.key;
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
		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new TransferAssetTransaction(sender, recipient, key, amount, fee, timestamp, reference, signatureBytes);	
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD SENDER/RECIPIENT/AMOUNT/ASSET
		transaction.put("sender", this.sender.getAddress());
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("asset", this.key);
		transaction.put("amount", this.amount.toPlainString());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(TRANSFER_ASSET_TRANSACTION);
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
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		//SIGNATURE
		data = Bytes.concat(data, this.signature);
		
		return data;
	}

	@Override
	public int getDataLength() 
	{
		return TYPE_LENGTH + BASE_LENGTH;
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(TRANSFER_ASSET_TRANSACTION);
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
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
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
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//REMOVE FEE
		DBSet fork = db.fork();
		this.sender.setConfirmedBalance(this.sender.getConfirmedBalance(fork).subtract(this.fee), fork);

		//CHECK IF SENDER HAS ENOUGH ASSET BALANCE
		if(this.sender.getConfirmedBalance(this.key, fork).compareTo(this.amount) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF SENDER HAS ENOUGH QORA BALANCE
		if(this.sender.getConfirmedBalance(fork).compareTo(BigDecimal.ZERO) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF AMOUNT IS DIVISIBLE
		if(!db.getAssetMap().get(this.key).isDivisible())
		{
			//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if(this.getAmount().stripTrailingZeros().scale() > 0)
			{
				//AMOUNT HAS DECIMALS
				return INVALID_AMOUNT;
			}
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.sender.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
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
		this.sender.setConfirmedBalance(this.key, this.sender.getConfirmedBalance(this.key, db).subtract(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).add(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.sender.setLastReference(this.signature, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(this.key == BalanceMap.QORA_KEY)
		{
			if(Arrays.equals(this.recipient.getLastReference(db), new byte[0]))
			{
				this.recipient.setLastReference(this.signature, db);
			}
		}
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE SENDER
		this.sender.setConfirmedBalance(this.sender.getConfirmedBalance(db).add(this.fee), db);
		this.sender.setConfirmedBalance(this.key, this.sender.getConfirmedBalance(this.key, db).add(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).subtract(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.sender.setLastReference(this.reference, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(this.key == BalanceMap.QORA_KEY)
		{
			if(Arrays.equals(this.recipient.getLastReference(db), this.signature))
			{
				this.recipient.removeReference(db);
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
		return Arrays.asList(this.sender, this.recipient);
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(sender.getAddress()) || address.equals(recipient.getAddress()))
		{
			return true;
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

		//IF QORA ASSET
		if(this.key == BalanceMap.QORA_KEY)
		{
			//IF SENDER
			if(address.equals(this.sender.getAddress()))
			{
				amount = amount.subtract(this.amount);
			}
			
			//IF RECIPIENT
			if(address.equals(this.recipient.getAddress()))
			{
				amount = amount.add(this.amount);
			}
		}
		
		return amount;
	}
	
	public static byte[] generateSignature(PrivateKeyAccount sender, Account recipient, long key, BigDecimal amount, BigDecimal fee, long timestamp) 
	{
		return generateSignature(DBSet.getInstance(), sender, recipient, key, amount, fee, timestamp);
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount sender, Account recipient, long key, BigDecimal amount, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(TRANSFER_ASSET_TRANSACTION);
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
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(recipient.getAddress()));
		
		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		//SIGN
		return Crypto.getInstance().sign(sender, data);
	}
}
