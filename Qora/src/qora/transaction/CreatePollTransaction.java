package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import qora.voting.Poll;
import qora.voting.PollOption;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class CreatePollTransaction extends Transaction 
{
	private static final int CREATOR_LENGTH = 32;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount creator;
	private Poll poll;
	
	public CreatePollTransaction(PublicKeyAccount creator, Poll poll, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(CREATE_POLL_TRANSACTION, fee, timestamp, reference, signature);
		
		this.creator = creator;
		this.poll = poll;
	}

	//GETTERS/SETTERS
	
	public Poll getPoll()
	{
		return this.poll;
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
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ POLL
		Poll poll = Poll.parse(Arrays.copyOfRange(data, position, data.length));
		position += poll.getDataLength();
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CreatePollTransaction(creator, poll, fee, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DESCRIPTION/OPTIONS
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.poll.getName());
		transaction.put("description", this.poll.getDescription());
		
		JSONArray options = new JSONArray();
		for(PollOption option: this.poll.getOptions())
		{
			options.add(option.getName());
		}
		
		transaction.put("options", options);
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CREATE_POLL_TRANSACTION);
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
		
		//WRITE POLL
		data = Bytes.concat(data , this.poll.toBytes());
		
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
		return TYPE_LENGTH + BASE_LENGTH + this.poll.getDataLength();
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
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE POLL
		data = Bytes.concat(data , this.poll.toBytes());
		
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
		//CHECK IF RELEASED
		if(NTP.getTime() < Transaction.getVOTING_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		
		//CHECK POLL NAME LENGTH
		int nameLength = this.poll.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK POLL NAME LOWERCASE
		if(!this.poll.getName().equals(this.poll.getName().toLowerCase()))
		{
			return NAME_NOT_LOWER_CASE;
		}
		
		//CHECK POLL DESCRIPTION LENGTH
		int descriptionLength = this.poll.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000 || descriptionLength < 1)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
		
		//CHECK POLL DOES NOT EXIST ALREADY
		if(db.getPollMap().contains(this.poll))
		{
			return POLL_ALREADY_CREATED;
		}
		
		//CHECK IF POLL DOES NOT CONTAIN ANY VOTERS
		if(this.poll.hasVotes())
		{
			return POLL_ALREADY_HAS_VOTES;
		}
		
		//CHECK POLL CREATOR VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.poll.getCreator().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK OPTIONS LENGTH
		int optionsLength = poll.getOptions().size();
		if(optionsLength > 100 || optionsLength < 1)
		{
			return INVALID_OPTIONS_LENGTH;
		}
		
		//CHECK OPTIONS
		List<String> options = new ArrayList<String>();
		for(PollOption option: this.poll.getOptions())
		{
			//CHECK OPTION LENGTH
			int optionLength = option.getName().getBytes(StandardCharsets.UTF_8).length;
			if(optionLength > 400 || optionLength < 1)
			{
				return INVALID_OPTION_LENGTH;
			}
			
			//CHECK OPTION UNIQUE
			if(options.contains(option.getName()))
			{
				return DUPLICATE_OPTION;
			}
			
			options.add(option.getName());
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getBalance(1, db).compareTo(this.fee) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
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
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.fee), db);
								
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
		
		//INSERT INTO DATABASE
		db.getPollMap().add(this.poll);
	}


	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.fee), db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//DELETE FROM DATABASE
		db.getPollMap().delete(this.poll);		
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
		accounts.add(this.poll.getCreator());
		return accounts;
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()) || address.equals(this.poll.getCreator().getAddress()))
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
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, Poll poll, BigDecimal fee, long timestamp) 
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
		
		//WRITE CREATOR
		data = Bytes.concat(data, creator.getPublicKey());
		
		//WRITE POLL
		data = Bytes.concat(data , poll.toBytes());
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
}
