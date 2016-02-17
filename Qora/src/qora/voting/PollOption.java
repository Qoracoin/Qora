package qora.voting;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import qora.account.Account;
import qora.crypto.Base58;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class PollOption {
	
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int VOTERS_SIZE_LENGTH = 4;
	private static final int VOTER_LENGTH = 25;
	
	private String name;
	private List<Account> voters;
	
	public PollOption(String name)
	{
		this.name = name;
		this.voters = new ArrayList<Account>();
	}
	
	public PollOption(String name, List<Account> voters)
	{
		this.name = name;
		this.voters = voters;
	}
	
	//GETTERS/SETTERS
	
	public String getName()
	{
		return this.name;
	}
	
	public List<Account> getVoters()
	{
		return this.voters;
	}
	
	public boolean hasVoter(Account account) 
	{
		for(Account voter: this.voters)
		{
			if(voter.getAddress().equals(account.getAddress()))
			{
				return true;
			}
		}
		
		return false;
	}

	public void removeVoter(Account account) 
	{
		Account remove = null;
		for(Account voter: this.voters)
		{
			if(voter.getAddress().equals(account.getAddress()))
			{
				remove = voter;
			}
		}
		
		if(remove != null)
		{
			this.voters.remove(remove);
		}
	}	
	
	public void addVoter(Account account)
	{
		this.voters.add(account);
	}
	
	public BigDecimal getVotes()
	{
		return getVotes(0);
	}

	public BigDecimal getVotes(long assetKey)
	{
		BigDecimal votes = BigDecimal.ZERO.setScale(8);
		
		for(Account voter: this.voters)
		{
			votes = votes.add(voter.getConfirmedBalance(assetKey));
		}
		
		return votes;
	}
	
	//PARSE
	
	public static PollOption parse(byte[] data) throws Exception
	{
		int position = 0;
		
		//READ NAME SIZE
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
				
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
		
		//READ NAME
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
		
		//READ VOTERS SIZE
		byte[] votersLengthBytes = Arrays.copyOfRange(data, position, position + VOTERS_SIZE_LENGTH);
		int votersLength = Ints.fromByteArray(votersLengthBytes);
		position += VOTERS_SIZE_LENGTH;
		
		//READ VOTERS
		List<Account> voters = new ArrayList<Account>();
		for(int i=0; i<votersLength; i++)
		{
			byte[] rawAddress = Arrays.copyOfRange(data, position, position + VOTER_LENGTH);
			String address = Base58.encode(rawAddress);
			voters.add(new Account(address));
			position += VOTER_LENGTH;
		}
		
		return new PollOption(name, voters);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() 
	{
		JSONObject pollOption = new JSONObject();
								
		//ADD NAME/TOTAL VOTES/VOTERS
		pollOption.put("name", this.getName());
		pollOption.put("votes", this.getVotes().toPlainString());
		
		JSONArray voters = new JSONArray();
		for(Account voter: this.voters)
		{
			voters.add(voter.getAddress());
		}
		
		pollOption.put("voters", voters);
		
		return pollOption;	
	}
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE VOTERS SIZE
		int votersLength = this.voters.size();
		byte[] votersLengthBytes = Ints.toByteArray(votersLength);
		data = Bytes.concat(data, votersLengthBytes);
			
		//WRITE VOTERS
		for(Account voter: this.voters)
		{
			data = Bytes.concat(data, Base58.decode(voter.getAddress()));
		}
		
		return data;
	}
	
	public int getDataLength() 
	{
		return NAME_SIZE_LENGTH + this.name.getBytes(StandardCharsets.UTF_8).length + VOTERS_SIZE_LENGTH + (this.voters.size() * VOTER_LENGTH);
	}
	
	//REST
	
	@Override
	public String toString()
	{
		return this.name + " - " + this.getVotes().toPlainString();
	}
	
	public String toString(long assetKey)
	{
		return this.name + " - " + this.getVotes(assetKey).toPlainString();
	}
}
