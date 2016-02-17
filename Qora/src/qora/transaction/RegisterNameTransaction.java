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

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class RegisterNameTransaction extends Transaction 
{
	private static final int REGISTRANT_LENGTH = 32;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + REGISTRANT_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount registrant;
	private Name name;
	
	public RegisterNameTransaction(PublicKeyAccount registrant, Name name, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(REGISTER_NAME_TRANSACTION, fee, timestamp, reference, signature);
		
		this.registrant = registrant;
		this.name = name;
	}

	//GETTERS/SETTERS
	
	public PublicKeyAccount getRegistrant()
	{
		return this.registrant;
	}
	
	public Name getName()
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
		
		//READ REGISTRANT
		byte[] registrantBytes = Arrays.copyOfRange(data, position, position + REGISTRANT_LENGTH);
		PublicKeyAccount registrant = new PublicKeyAccount(registrantBytes);
		position += REGISTRANT_LENGTH;
		
		//READ NAME
		Name name = Name.Parse(Arrays.copyOfRange(data, position, data.length));
		position += name.getDataLength();
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new RegisterNameTransaction(registrant, name, fee, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("registrant", this.registrant.getAddress());
		transaction.put("owner", this.name.getOwner().getAddress());
		transaction.put("name", this.name.getName());
		transaction.put("value", this.name.getValue());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(REGISTER_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE REGISTRANT
		data = Bytes.concat(data, this.registrant.getPublicKey());
		
		//WRITE NAME
		data = Bytes.concat(data , this.name.toBytes());
		
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
		return TYPE_LENGTH + BASE_LENGTH + this.name.getDataLength();
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(REGISTER_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE REGISTRANT
		data = Bytes.concat(data, this.registrant.getPublicKey());
		
		//WRITE NAME
		data = Bytes.concat(data , this.name.toBytes());
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
				
		return Crypto.getInstance().verify(this.registrant.getPublicKey(), this.signature, data);
	}
	
	@Override
	public int isValid(DBSet db) 
	{
		//CHECK NAME LENGTH
		int nameLength = this.name.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK IF LOWERCASE
		if(!this.name.getName().equals(this.name.getName().toLowerCase()))
		{
			return NAME_NOT_LOWER_CASE;
		}
		
		//CHECK VALUE LENGTH
		int valueLength = this.name.getValue().getBytes(StandardCharsets.UTF_8).length;
		if(valueLength > 4000 || valueLength < 1)
		{
			return INVALID_VALUE_LENGTH;
		}
		
		//CHECK OWNER
		if(!Crypto.getInstance().isValidAddress(this.name.getOwner().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK NAME NOT REGISTRED ALREADY
		if(db.getNameMap().contains(this.name))
		{
			return NAME_ALREADY_REGISTRED;
		}
		
		//CHECK IF REGISTRANT HAS ENOUGH MONEY
		if(this.registrant.getBalance(1, db).compareTo(this.fee) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.registrant.getLastReference(db), this.reference))
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
		this.registrant.setConfirmedBalance(this.registrant.getConfirmedBalance(db).subtract(this.fee), db);
								
		//UPDATE REFERENCE OF OWNER
		this.registrant.setLastReference(this.signature, db);
		
		//INSERT INTO DATABASE
		db.getNameMap().add(this.name);
	}


	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE OWNER
		this.registrant.setConfirmedBalance(this.registrant.getConfirmedBalance(db).add(this.fee), db);
										
		//UPDATE REFERENCE OF OWNER
		this.registrant.setLastReference(this.reference, db);
				
		//INSERT INTO DATABASE
		db.getNameMap().delete(this.name);		
	}

	@Override
	public PublicKeyAccount getCreator() 
	{
		return this.registrant;
	}


	@Override
	public List<Account> getInvolvedAccounts() 
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.registrant);
		accounts.add(this.name.getOwner());
		return accounts;
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.registrant.getAddress()) || address.equals(this.name.getOwner().getAddress()))
		{
			return true;
		}
		
		return false;
	}


	@Override
	public BigDecimal getAmount(Account account) 
	{
		if(account.getAddress().equals(this.registrant.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, Name name, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(REGISTER_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, creator.getLastReference(db));
		
		//WRITE REGISTRANT
		data = Bytes.concat(data, creator.getPublicKey());
		
		//WRITE NAME
		data = Bytes.concat(data , name.toBytes());
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
}
