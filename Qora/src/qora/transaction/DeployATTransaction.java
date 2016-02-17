package qora.transaction;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import utils.Converter;
import at.AT;
import at.AT_Constants;
import at.AT_Controller;
import at.AT_Exception;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class DeployATTransaction extends Transaction
{

	private static final int CREATOR_LENGTH = 32;
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int DESCRIPTION_SIZE_LENGTH = 4;
	private static final int TYPE_SIZE_LENGTH = 4;
	private static final int TAGS_SIZE_LENGTH = 4;
	private static final int CREATION_BYTES_SIZE_LENGTH = 4;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + NAME_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH + TYPE_SIZE_LENGTH + TAGS_SIZE_LENGTH + CREATION_BYTES_SIZE_LENGTH + AMOUNT_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;


	private PublicKeyAccount creator;
	private String name;
	private String description;
	private String type;
	private String tags;
	private BigDecimal amount;
	private byte[] creationBytes;

	public DeployATTransaction(PublicKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal quantity, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(DEPLOY_AT_TRANSACTION, fee, timestamp, reference, signature);

		this.creator = creator;
		this.name = name;
		this.description = description;
		this.creationBytes = creationBytes;
		this.type = type;
		this.tags = tags;
		this.amount = quantity;
	}

	//PARSE/CONVERT

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

		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;

		if(nameLength < 1 || nameLength > AT_Constants.NAME_MAX_LENGTH)
		{
			throw new Exception("Invalid name length");
		}

		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;

		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;

		if(descriptionLength < 1 || descriptionLength > AT_Constants.DESC_MAX_LENGTH)
		{
			throw new Exception("Invalid description length");
		}

		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;

		//READ TYPE
		byte[] typeLengthBytes = Arrays.copyOfRange(data, position, position + TYPE_SIZE_LENGTH);
		int typeLength = Ints.fromByteArray(typeLengthBytes);
		position += TYPE_SIZE_LENGTH;

		if(typeLength < 1 || typeLength > AT_Constants.TYPE_MAX_LENGTH)
		{
			throw new Exception("Invalid type length");
		}

		byte[] typeBytes = Arrays.copyOfRange(data, position, position + typeLength);
		String type = new String(typeBytes, StandardCharsets.UTF_8);
		position += typeLength;

		//READ TAGS
		byte[] tagsLengthBytes = Arrays.copyOfRange(data, position, position + TAGS_SIZE_LENGTH);
		int tagsLength = Ints.fromByteArray(tagsLengthBytes);
		position += TAGS_SIZE_LENGTH;

		if(tagsLength < 1 || tagsLength > AT_Constants.TAGS_MAX_LENGTH)
		{
			throw new Exception("Invalid tags length");
		}

		byte[] tagsBytes = Arrays.copyOfRange(data, position, position + tagsLength);
		String tags = new String(tagsBytes, StandardCharsets.UTF_8);
		position += tagsLength;

		//READ CREATIONBYTES
		byte[] creationLengthBytes = Arrays.copyOfRange(data, position, position + CREATION_BYTES_SIZE_LENGTH);
		int creationLength = Ints.fromByteArray(creationLengthBytes);
		position +=  CREATION_BYTES_SIZE_LENGTH;

		if(creationLength < 1 || creationLength > AT_Constants.CREATION_BYTES_MAX_LENGTH) //TODO SEE WHAT IS BEST
		{
			throw new Exception("Invalid creation bytes length");
		}

		byte[] creationBytes = Arrays.copyOfRange(data, position, position + creationLength);
		position += creationLength;

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

		return new DeployATTransaction(creator, name, description, type, tags, creationBytes, amount, fee, timestamp, reference, signatureBytes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson()
	{
		JSONObject transaction = this.getJsonBase();
		
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.name);
		transaction.put("description", this.description);
		transaction.put("atType", this.type);
		transaction.put("tags", this.tags);
		transaction.put("creationBytes", Converter.toHex(this.creationBytes));
		transaction.put("amount", this.amount.toPlainString());
		
		return transaction;
	}

	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(DEPLOY_AT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);

		//WRITE NAME
		data = Bytes.concat(data, nameBytes);

		//WRITE DESCRIPTION SIZE
		byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
		int descriptionLength = descriptionBytes.length;
		byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
		data = Bytes.concat(data, descriptionLengthBytes);

		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);

		//WRITE TYPE SIZE
		byte[] typeATBytes = this.type.getBytes(StandardCharsets.UTF_8);
		int typeLength = typeATBytes.length;
		byte[] typeLengthBytes = Ints.toByteArray(typeLength);
		data = Bytes.concat(data, typeLengthBytes);

		//WRITE TYPE
		data = Bytes.concat(data, typeATBytes);

		//WRITE TAGS SIZE
		byte[] tagsBytes = this.tags.getBytes(StandardCharsets.UTF_8);
		int tagsLength = tagsBytes.length;
		byte[] tagsLengthBytes = Ints.toByteArray(tagsLength);
		data = Bytes.concat(data, tagsLengthBytes);

		//WRITE TAGS
		data = Bytes.concat(data, tagsBytes);

		//WRITE CREATIONBYTES SIZE
		int creationBytesLength = this.creationBytes.length;
		byte[] creationLengthBytes = Ints.toByteArray(creationBytesLength);
		data = Bytes.concat(data, creationLengthBytes);

		//WRITE DESCRIPTION
		data = Bytes.concat(data, this.creationBytes);

		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fillAmount = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fillAmount, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
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
		return TYPE_LENGTH + BASE_LENGTH +  
				this.name.getBytes(StandardCharsets.UTF_8).length + 
				this.description.getBytes(StandardCharsets.UTF_8).length + 
				this.type.getBytes(StandardCharsets.UTF_8).length + 
				this.tags.getBytes(StandardCharsets.UTF_8).length + 
				this.creationBytes.length;	
	}

	//VALIDATE

	@Override
	public boolean isSignatureValid() 
	{
		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(DEPLOY_AT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);

		//WRITE NAME
		data = Bytes.concat(data, nameBytes);

		//WRITE DESCRIPTION SIZE
		byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
		int descriptionLength = descriptionBytes.length;
		byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
		data = Bytes.concat(data, descriptionLengthBytes);

		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);

		//WRITE CREATIONBYTES SIZE
		int creationBytesLength = this.creationBytes.length;
		byte[] creationLengthBytes = Ints.toByteArray(creationBytesLength);
		data = Bytes.concat(data, creationLengthBytes);

		//WRITE DESCRIPTION
		data = Bytes.concat(data, this.creationBytes);

		//WRITE FEE
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fillAmount = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fillAmount, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		return Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data);
	}

	@Override
	public int isValid(DBSet db)
	{
		return isValid(db, 0);
	}

	public int isValid(DBSet db, Integer forkHeight) 
	{
		//CHECK IF RELEASED
		if(db.getBlockMap().getLastBlock().getHeight(db) + 1 < Transaction.getAT_BLOCK_HEIGHT_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		
		//CHECK NAME LENGTH
		int nameLength = this.name.getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > AT_Constants.NAME_MAX_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}

		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.description.getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > AT_Constants.DESC_MAX_LENGTH || descriptionLength < 1)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
		
		int typeLength = this.type.getBytes(StandardCharsets.UTF_8).length;
		if(typeLength > AT_Constants.TYPE_MAX_LENGTH || typeLength < 1)
		{
			return INVALID_TYPE_LENGTH;
		}
		
		int tagsLength = this.tags.getBytes(StandardCharsets.UTF_8).length;
		if(tagsLength > AT_Constants.TYPE_MAX_LENGTH || tagsLength < 1)
		{
			return INVALID_TAGS_LENGTH;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getBalance(1, db).compareTo(this.amount.add(this.fee)) == -1)
		{
			return NO_BALANCE;
		}

		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}

		//CHECK IF FEE IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECK IF FEE IS POSITIVE
		if(this.fee.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_FEE;
		}
		
		//CHECK IF CREATIONBYTES VALID
		try 
		{
			int height = db.getBlockMap().getLastBlock().getHeight(db) + 1;
			byte[] balanceBytes = this.getFee().unscaledValue().toByteArray();
			byte[] fill = new byte[8 - balanceBytes.length];
			balanceBytes = Bytes.concat(fill, balanceBytes);

			long lFee = Longs.fromByteArray(balanceBytes);
			int returnCode = AT_Controller.checkCreationBytes(this.creationBytes, this.type, lFee , height, forkHeight, db);
			if ( returnCode != 0 )
			{
				return returnCode + AT_ERROR;
			}
			
			String atId = Crypto.getInstance().getATAddress( getBytesForAddress( db ) );
			if ( db.getATMap().getAT(atId)!=null)
			{
				return 12 + AT_ERROR;
			}
			
		}
		catch(AT_Exception e) 
		{
			//TODO CAN BE CHANGED TO HANDLE THE ERRORS BETTER
			return INVALID_CREATION_BYTES;
		}

		return VALIDATE_OK;

	}

	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db) 
	{
		//UPDATE ISSUER
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.amount.add(this.fee)), db);

		//UPDATE REFERENCE OF ISSUER
		this.creator.setLastReference(this.signature, db);

		//CREATE AT ID = ADDRESS
		String atId = Crypto.getInstance().getATAddress( getBytesForAddress( db ) );

		Account atAccount = new Account(atId);
	
		atAccount.setConfirmedBalance( this.amount , db );
		
		//UPDATE REFERENCE OF RECIPIENT
		if(Arrays.equals(atAccount.getLastReference(db), new byte[0]))
		{
			atAccount.setLastReference(this.signature, db);
		}
		
		//CREATE AT - public key or address? Is that the correct height?
		AT at = new AT( Base58.decode( atId ), Base58.decode(this.creator.getAddress()) , this.name , this.description, this.type, this.tags , this.creationBytes , db.getBlockMap().getLastBlock().getHeight(db) + 1 );

		//INSERT INTO DATABASE
		db.getATMap().add(at);
		db.getATStateMap().addOrUpdate(at.getCreationBlockHeight(), at.getId(), at.getState());

	}

	public byte[] getBytesForAddress( DBSet db )
	{
		byte[] name = StringUtil.getUtf8Bytes(this.name );
		byte[] desc = StringUtil.getUtf8Bytes(this.description.replaceAll("\\s", "") );
		ByteBuffer bf = ByteBuffer.allocate( name.length + desc.length + this.creator.getPublicKey().length + this.creationBytes.length + 4 );
		bf.order( ByteOrder.LITTLE_ENDIAN );

		bf.put( name );
		bf.put( desc );
		bf.put( this.creator.getPublicKey() );
		bf.put( this.creationBytes );
		bf.putInt( db.getBlockMap().getLastBlock().getHeight(db) + 1 );
		return bf.array().clone();
	}
	
	public Account getATaccount()
	{
		byte[] name = StringUtil.getUtf8Bytes(this.name );
		byte[] desc = StringUtil.getUtf8Bytes(this.description.replaceAll("\\s", "") );
		ByteBuffer bf = ByteBuffer.allocate( name.length + desc.length + this.creator.getPublicKey().length + this.creationBytes.length + 4 );
		bf.order( ByteOrder.LITTLE_ENDIAN );

		bf.put( name );
		bf.put( desc );
		bf.put( this.creator.getPublicKey() );
		bf.put( this.creationBytes );
		bf.putInt( getParent().getHeight() );

		String atId = Crypto.getInstance().getATAddress( bf.array().clone() );

		return new Account(atId);
	}

	@Override
	public void orphan(DBSet db) {
		//UPDATE ISSUER
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.amount.add(this.fee)), db);

		//UPDATE REFERENCE OF ISSUER
		this.creator.setLastReference(this.reference, db);
		
		String atId = Crypto.getInstance().getATAddress( getBytesForAddress( db ) );
		
		Account atAccount = new Account(atId);
		
		//UPDATE RECIPIENT
		atAccount.setConfirmedBalance(atAccount.getConfirmedBalance(db).subtract(this.amount), db);
			
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.reference, db);
				
		///UPDATE REFERENCE OF RECIPIENT
		if(Arrays.equals(atAccount.getLastReference(db), this.signature))
		{
			atAccount.removeReference(db);
		}	
		
	}

	//REST

	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	@Override
	public PublicKeyAccount getCreator() 
	{
		return this.creator;
	}

	@Override
	public List<Account> getInvolvedAccounts() 
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.creator);
		return accounts;
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();

		if(address.equals(this.creator.getAddress()))
		{
			return true;
		}

		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		if(account.getAddress().equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.amount.add(this.fee));
		}

		return BigDecimal.ZERO;
	}

	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, String name, String description, byte[] creationBytes, BigDecimal amount, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(DEPLOY_AT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, creator.getLastReference(db));

		//WRITE CREATOR
		data = Bytes.concat(data, creator.getPublicKey());

		//WRITE NAME SIZE
		byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);

		//WRITE NAME
		data = Bytes.concat(data, nameBytes);

		//WRITE DESCRIPTION SIZE
		byte[] descriptionBytes = description.getBytes(StandardCharsets.UTF_8);
		int descriptionLength = descriptionBytes.length;
		byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
		data = Bytes.concat(data, descriptionLengthBytes);

		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);

		//WRITE CREATIONBYTES SIZE
		int creationBytesLength = creationBytes.length;
		byte[] creationLengthBytes = Ints.toByteArray(creationBytesLength);
		data = Bytes.concat(data, creationLengthBytes);

		//WRITE CREATIONBYTES
		data = Bytes.concat(data, creationBytes);

		//WRITE FEE
		byte[] amountBytes = amount.unscaledValue().toByteArray();
		byte[] fillAmount = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fillAmount, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		return Crypto.getInstance().sign(creator, data);
	}
	
}
