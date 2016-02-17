package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

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

public class VoteOnPollTransaction extends Transaction 
{
	private static final int CREATOR_LENGTH = 32;
	private static final int POLL_SIZE_LENGTH = 4;
	private static final int OPTION_SIZE_LENGTH = 4;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + POLL_SIZE_LENGTH + OPTION_SIZE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount creator;
	private String poll;
	public int option;
	
	public VoteOnPollTransaction(PublicKeyAccount creator, String poll, int option, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(VOTE_ON_POLL_TRANSACTION, fee, timestamp, reference, signature);
		
		this.creator = creator;
		this.poll = poll;
		this.option = option;
	}

	//GETTERS/SETTERS
	
	public String getPoll()
	{
		return this.poll;
	}
	
	public int getOption()
	{
		return this.option;
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
		
		//READ POLL SIZE
		byte[] pollLengthBytes = Arrays.copyOfRange(data, position, position + POLL_SIZE_LENGTH);
		int pollLength = Ints.fromByteArray(pollLengthBytes);
		position += POLL_SIZE_LENGTH;
				
		if(pollLength < 1 || pollLength > 400)
		{
			throw new Exception("Invalid poll length");
		}
		
		//READ POLL
		byte[] pollBytes = Arrays.copyOfRange(data, position, position + pollLength);
		String poll = new String(pollBytes, StandardCharsets.UTF_8);
		position += pollLength;
		
		//READ OPTION
		byte[] optionBytes = Arrays.copyOfRange(data, position, position + OPTION_SIZE_LENGTH);
		int option = Ints.fromByteArray(optionBytes);
		position += OPTION_SIZE_LENGTH;
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new VoteOnPollTransaction(creator, poll, option, fee, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DESCRIPTION/OPTIONS
		transaction.put("creator", this.creator.getAddress());
		transaction.put("poll", this.poll);
		transaction.put("option", this.option);
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(VOTE_ON_POLL_TRANSACTION);
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
		
		//WRITE POLL SIZE
		byte[] pollBytes = this.poll.getBytes(StandardCharsets.UTF_8);
		int pollLength = pollBytes.length;
		byte[] pollLengthBytes = Ints.toByteArray(pollLength);
		data = Bytes.concat(data, pollLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, pollBytes);
		
		//WRITE OPTION
		byte[] optionBytes = Ints.toByteArray(this.option);
		optionBytes = Bytes.ensureCapacity(optionBytes, OPTION_SIZE_LENGTH, 0);
		data = Bytes.concat(data, optionBytes);
		
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
		return TYPE_LENGTH + BASE_LENGTH + this.poll.getBytes(StandardCharsets.UTF_8).length;
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(VOTE_ON_POLL_TRANSACTION);
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
		
		//WRITE POLL SIZE
		byte[] pollBytes = this.poll.getBytes(StandardCharsets.UTF_8);
		int pollLength = pollBytes.length;
		byte[] pollLengthBytes = Ints.toByteArray(pollLength);
		data = Bytes.concat(data, pollLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, pollBytes);
		
		//WRITE OPTION
		byte[] optionBytes = Ints.toByteArray(this.option);
		optionBytes = Bytes.ensureCapacity(optionBytes, OPTION_SIZE_LENGTH, 0);
		data = Bytes.concat(data, optionBytes);
		
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
		
		//CHECK POLL LENGTH
		int pollLength = this.poll.getBytes(StandardCharsets.UTF_8).length;
		if(pollLength > 400 || pollLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK POLL LOWERCASE
		if(!this.poll.equals(this.poll.toLowerCase()))
		{
			return NAME_NOT_LOWER_CASE;
		}
		
		//CHECK POLL EXISTS
		if(!db.getPollMap().contains(this.poll))
		{
			return POLL_NO_EXISTS;
		}
		
		//CHECK OPTION EXISTS
		Poll poll = db.getPollMap().get(this.poll);
		if(poll.getOptions().size()-1 < this.option || this.option < 0)
		{
			return OPTION_NO_EXISTS;
		}
		
		//CHECK IF NOT VOTED ALREADY
		PollOption option = poll.getOptions().get(this.option);
		if(option.hasVoter(this.creator))
		{
			return ALREADY_VOTED_FOR_THAT_OPTION;
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
		
		//ADD VOTE TO POLL
		Poll poll = db.getPollMap().get(this.poll).copy();
		int previousOption = poll.addVoter(this.creator, this.option);
		db.getPollMap().add(poll);
		
		//CHECK IF WE HAD PREVIOUSLY VOTED
		if(previousOption != -1)
		{
			//ADD TO ORPHAN DATABASE
			db.getVoteOnPollDatabase().set(this, previousOption);
		}
	}


	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.fee), db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//DELETE VOTE FROM POLL
		Poll poll = db.getPollMap().get(this.poll).copy();
		poll.deleteVoter(this.creator, this.option);
		
		//RESTORE PREVIOUS VOTE
		int previousOption = db.getVoteOnPollDatabase().get(this);
		if(previousOption != -1)
		{
			poll.addVoter(this.creator, previousOption);
		}
		
		db.getPollMap().add(poll);
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
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, String poll, int option, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(VOTE_ON_POLL_TRANSACTION);
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
		
		//WRITE POLL SIZE
		byte[] pollBytes = poll.getBytes(StandardCharsets.UTF_8);
		int pollLength = pollBytes.length;
		byte[] pollLengthBytes = Ints.toByteArray(pollLength);
		data = Bytes.concat(data, pollLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, pollBytes);
		
		//WRITE OPTION
		byte[] optionBytes = Ints.toByteArray(option);
		optionBytes = Bytes.ensureCapacity(optionBytes, OPTION_SIZE_LENGTH, 0);
		data = Bytes.concat(data, optionBytes);
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
}
