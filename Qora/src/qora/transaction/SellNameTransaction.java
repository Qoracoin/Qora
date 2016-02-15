package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Crypto;
import qora.naming.NameSale;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class SellNameTransaction extends Transaction 
{
	private static final int OWNER_LENGTH = 32;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + OWNER_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount owner;
	private NameSale nameSale;
	
	public SellNameTransaction(PublicKeyAccount owner, NameSale nameSale, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(SELL_NAME_TRANSACTION, fee, timestamp, reference, signature);

		this.owner = owner;
		this.nameSale = nameSale;
	}
	
	//GETTERS/SETTERS
	
	public PublicKeyAccount getOwner()
	{
		return this.owner;
	}
	
	public NameSale getNameSale()
	{
		return this.nameSale;
	}
	
	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	
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
		
		//READ OWNER
		byte[] registrantBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
		PublicKeyAccount owner = new PublicKeyAccount(registrantBytes);
		position += OWNER_LENGTH;
		
		//READ NAMESALE
		NameSale nameSale = NameSale.Parse(Arrays.copyOfRange(data, position, data.length));
		position += nameSale.getDataLength();
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new SellNameTransaction(owner, nameSale, fee, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
						
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("owner", this.owner.getAddress());
		transaction.put("name", this.nameSale.getKey());
		transaction.put("amount", this.nameSale.getAmount().toPlainString());
						
		return transaction;	
	}

	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(SELL_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE OWNER
		data = Bytes.concat(data, this.owner.getPublicKey());
		
		//WRITE NAMESALE
		data = Bytes.concat(data, this.nameSale.toBytes());
		
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
		return TYPE_LENGTH + BASE_LENGTH + this.nameSale.getDataLength();
	}
	
	//VALIDATE

	@Override
	public boolean isSignatureValid() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(SELL_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE OWNER
		data = Bytes.concat(data, this.owner.getPublicKey());
		
		//WRITE NAMESALE
		data = Bytes.concat(data, this.nameSale.toBytes());
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		return Crypto.getInstance().verify(this.owner.getPublicKey(), this.signature, data);
	}

	@Override
	public int isValid(DBSet db) 
	{
		//CHECK NAME LENGTH
		int nameLength = this.nameSale.getKey().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK IF NAME EXISTS
		if(!db.getNameMap().contains(this.nameSale.getName(db)))
		{
			return NAME_DOES_NOT_EXIST;
		}
				
		//CHECK OWNER
		if(!Crypto.getInstance().isValidAddress(this.nameSale.getName(db).getOwner().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF OWNER IS OWNER
		if(!db.getNameMap().get(this.nameSale.getKey()).getOwner().getAddress().equals(this.owner.getAddress()))
		{
			return INVALID_NAME_OWNER;
		}
		
		//CHECK IF NOT FOR SALE ALREADY
		if(db.getNameExchangeMap().contains(this.nameSale))
		{
			return NAME_ALREADY_FOR_SALE;
		}
		
		//CHECK IF AMOUNT POSITIVE
		if(this.nameSale.getAmount().compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECK IF AMOUNT POSSIBLE
		if(this.nameSale.getAmount().compareTo(BigDecimal.valueOf(10000000000l)) > 0)
		{
			return INVALID_AMOUNT;
		}
				
		//CHECK IF OWNER HAS ENOUGH MONEY
		if(this.owner.getBalance(1, db).compareTo(this.fee) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.owner.getLastReference(db), this.reference))
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
		//UPDATE OWNER
		this.owner.setConfirmedBalance(this.owner.getConfirmedBalance(db).subtract(this.fee), db);
										
		//UPDATE REFERENCE OF OWNER
		this.owner.setLastReference(this.signature, db);
				
		//INSERT INTO DATABASE
		db.getNameExchangeMap().add(this.nameSale);
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE OWNER
		this.owner.setConfirmedBalance(this.owner.getConfirmedBalance(db).add(this.fee), db);
										
		//UPDATE REFERENCE OF OWNER
		this.owner.setLastReference(this.reference, db);
		
		//DELETE FORM DATABASE
		db.getNameExchangeMap().delete(this.nameSale);
		
	}

	@Override
	public PublicKeyAccount getCreator() 
	{
		return this.owner;
	}

	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.owner);
		return accounts;
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.owner.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.owner.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, NameSale nameSale, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(SELL_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, creator.getLastReference(db));
		
		//WRITE OWNER
		data = Bytes.concat(data, creator.getPublicKey());
		
		//WRITE NAMESALE
		data = Bytes.concat(data, nameSale.toBytes());
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}

}
