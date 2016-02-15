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
import qora.naming.Name;
import qora.naming.NameSale;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class CancelSellNameTransaction extends Transaction
{
	private static final int OWNER_LENGTH = 32;
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + OWNER_LENGTH + NAME_SIZE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;
	
	private PublicKeyAccount owner;
	private String name;
	
	public CancelSellNameTransaction(PublicKeyAccount owner, String name, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(CANCEL_SELL_NAME_TRANSACTION, fee, timestamp, reference, signature);
		
		this.owner = owner;
		this.name = name;
	}
	
	//GETTERS/SETTERS
	
	public PublicKeyAccount getOwner()
	{
		return this.owner;
	}
	
	public String getName()
	{
		return this.name;
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
		
		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
						
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
						
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CancelSellNameTransaction(owner, name, fee, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
								
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("owner", this.owner.getAddress());
		transaction.put("name", this.name);
								
		return transaction;	
	}

	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_SELL_NAME_TRANSACTION);
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
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
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
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		
		return TYPE_LENGTH + BASE_LENGTH + nameLength;
	}
	
	//VALIDATE

	@Override
	public boolean isSignatureValid() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_SELL_NAME_TRANSACTION);
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
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
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
		int nameLength = this.name.getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK IF NAME EXISTS
		Name name = db.getNameMap().get(this.name);
		if(name == null)
		{
			return NAME_DOES_NOT_EXIST;
		}
		
		//CHECK OWNER
		if(!Crypto.getInstance().isValidAddress(this.owner.getAddress()))
		{
			return INVALID_ADDRESS;
		}
				
		//CHECK IF OWNER IS OWNER
		if(!name.getOwner().getAddress().equals(this.owner.getAddress()))
		{
			return INVALID_NAME_OWNER;
		}
		
		//CHECK IF NAME FOR SALE ALREADY
		if(!db.getNameExchangeMap().contains(this.name))
		{
			return NAME_NOT_FOR_SALE;
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
				
		//SET ORPHAN DATA
		NameSale nameSale = db.getNameExchangeMap().getNameSale(this.name);
		db.getCancelSellNameMap().set(this, nameSale.getAmount());
		
		//DELETE FROM DATABASE
		db.getNameExchangeMap().delete(this.name);
		
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE OWNER
		this.owner.setConfirmedBalance(this.owner.getConfirmedBalance(db).add(this.fee), db);
												
		//UPDATE REFERENCE OF OWNER
		this.owner.setLastReference(this.reference, db);
				
		//ADD TO DATABASE
		BigDecimal amount = db.getCancelSellNameMap().get(this);
		NameSale nameSale = new NameSale(this.name, amount);
		db.getNameExchangeMap().add(nameSale);	
		
		//DELETE ORPHAN DATA
		db.getCancelSellNameMap().delete(this);
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
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, String name, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_SELL_NAME_TRANSACTION);
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
		
		//WRITE NAME SIZE
		byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
}
