package qora.voting;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import database.DBSet;
import qora.account.Account;
import qora.crypto.Base58;
import utils.Pair;

public class Poll 
{
	private static final int CREATOR_LENGTH = 25;
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int DESCRIPTION_SIZE_LENGTH = 4;
	private static final int OPTIONS_SIZE_LENGTH = 4;
	
	private Account creator;
	private String name;
	private String description;
	private List<PollOption> options;
	
	public Poll(Account creator, String name, String description, List<PollOption> options)
	{
		this.creator = creator;
		this.name = name;
		this.description = description;
		this.options = options;
	}
	
	//GETTERS/SETTERS

	public Account getCreator() {
		return this.creator;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}
	
	public List<PollOption> getOptions() {
		return this.options;
	}
	
	public boolean isConfirmed()
	{
		return DBSet.getInstance().getPollMap().contains(this);
	}
	
	public boolean hasVotes()
	{
		for(PollOption option: this.options)
		{
			if(option.getVoters().size() > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public BigDecimal getTotalVotes() 
	{
		return getTotalVotes(0);
	}
	
	public BigDecimal getTotalVotes(long assetKey) 
	{
		BigDecimal votes = BigDecimal.ZERO.setScale(8);
		
		for(PollOption option: this.options)
		{
			votes = votes.add(option.getVotes(assetKey));
		}
		
		return votes;
	}
	
	public List<Pair<Account, PollOption>> getVotes() 
	{
		List<Pair<Account, PollOption>> votes = new ArrayList<Pair<Account, PollOption>>();
		
		for(PollOption option: this.options)
		{
			for(Account voter: option.getVoters())
			{
				Pair<Account, PollOption> vote = new Pair<Account, PollOption>(voter, option);
				votes.add(vote);
			}
		}
		
		return votes;
	}
	
	public List<Pair<Account, PollOption>> getVotes(List<Account> accounts)
	{
		List<Pair<Account, PollOption>> votes = new ArrayList<Pair<Account, PollOption>>();
		
		for(PollOption option: this.options)
		{
			for(Account voter: option.getVoters())
			{
				if(accounts.contains(voter))
				{
					Pair<Account, PollOption> vote = new Pair<Account, PollOption>(voter, option);
					votes.add(vote);
				}
			}
		}
		
		return votes;
	}
	
	public PollOption getOption(String option)
	{
		for(PollOption pollOption: this.options)
		{
			if(pollOption.getName().equals(option))
			{
				return pollOption;
			}
		}
		
		return null;
	}
	
	//PARSE
	
	public static Poll parse(byte[] data) throws Exception
	{
		int position = 0;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
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
		
		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
				
		if(descriptionLength < 1 || descriptionLength > 4000)
		{
			throw new Exception("Invalid description length");
		}
				
		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;
		
		//READ OPTIONS SIZE
		byte[] optionsLengthBytes = Arrays.copyOfRange(data, position, position + OPTIONS_SIZE_LENGTH);
		int optionsLength = Ints.fromByteArray(optionsLengthBytes);
		position += OPTIONS_SIZE_LENGTH;
		
		if(optionsLength < 1 || optionsLength > 100)
		{
			throw new Exception("Invalid options length");
		}
		
		//READ OPTIONS
		List<PollOption> options = new ArrayList<PollOption>();
		for(int i=0; i<optionsLength; i++)
		{
			PollOption option = PollOption.parse(Arrays.copyOfRange(data, position, data.length));
			position += option.getDataLength();
			options.add(option);
		}
		
		return new Poll(creator, name, description, options);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject poll = new JSONObject();
										
		//ADD NAME/DESCRIPTIONS/OPTIONS
		poll.put("creator", this.getCreator().getAddress());
		poll.put("name", this.getName());
		poll.put("description", this.getDescription());
		
		
		JSONArray jsonOptions = new JSONArray();
		for(PollOption option: this.options)
		{
			jsonOptions.add(option.toJson());
		}
		poll.put("options", jsonOptions);
										
		return poll;	
	}	
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE CREATOR
		try
		{
			data = Bytes.concat(data , Base58.decode(this.creator.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE DESCRIPTION SIZE
		byte[] valueBytes = this.description.getBytes(StandardCharsets.UTF_8);
		int valueLength = valueBytes.length;
		byte[] valueLengthBytes = Ints.toByteArray(valueLength);
		data = Bytes.concat(data, valueLengthBytes);
				
		//WRITE DESCRIPTION
		data = Bytes.concat(data, valueBytes);
		
		//WRITE OPTIONS SIZE
		byte[] optionsLengthBytes = Ints.toByteArray(this.options.size());
		data = Bytes.concat(data, optionsLengthBytes);
				
		//WRITE OPTIONS
		for(PollOption option: this.options)
		{
			data = Bytes.concat(data, option.toBytes());
		}
		
		return data;
	}
	
	public int getDataLength() 
	{
		int length = CREATOR_LENGTH + NAME_SIZE_LENGTH + this.name.getBytes(StandardCharsets.UTF_8).length + DESCRIPTION_SIZE_LENGTH + this.description.getBytes(StandardCharsets.UTF_8).length + OPTIONS_SIZE_LENGTH;
		
		for(PollOption option: this.options)
		{
			length += option.getDataLength();
		}
		
		return length;
	}

	public int addVoter(Account voter, int optionIndex) 
	{
		//CHECK IF WE HAD A PREVIOUS VOTE IN THIS POLL
		int previousOption = -1;
		for(PollOption option: this.options)
		{
			if(option.hasVoter(voter))
			{
				previousOption = this.options.indexOf(option);
			}
		}
		
		if(previousOption != -1)
		{
			//REMOVE VOTE
			this.options.get(previousOption).removeVoter(voter);
		}
		
		//ADD NEW VOTE
		this.options.get(optionIndex).addVoter(voter);
		
		return previousOption;
	}

	public void deleteVoter(Account voter, int optionIndex) 
	{
		this.options.get(optionIndex).removeVoter(voter);
	}
	
	//COPY
	
	public Poll copy()
	{
		try
		{
			byte[] bytes = this.toBytes();
			return parse(bytes);
		}
		catch(Exception e)
		{
			return null;
		}
	}
}
