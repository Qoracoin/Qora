package api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import qora.voting.Poll;
import qora.voting.PollOption;
import utils.APIUtils;
import utils.Pair;
import controller.Controller;

@Path("polls")
@Produces(MediaType.APPLICATION_JSON)
public class PollsResource 
{
	@Context
	HttpServletRequest request;

	@POST
	@Consumes(MediaType.WILDCARD)
	public String createPoll(String x)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String creator = (String) jsonObject.get("creator");
			String name = (String) jsonObject.get("name");
			String description = (String) jsonObject.get("description");
			JSONArray optionsJSON = (JSONArray) jsonObject.get("options");
			String fee = (String) jsonObject.get("fee");
			
			//PARSE FEE
			BigDecimal bdFee;
			try
			{
				bdFee = new BigDecimal(fee);
				bdFee = bdFee.setScale(8);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_FEE);
			}	
			
			//PARSE OPTIONS
			List<String> options = new ArrayList<String>();
			try
			{
				for(int i=0; i<optionsJSON.size(); i++)
				{
					String option = (String) optionsJSON.get(i);
					options.add(option);
				}
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
			}
				
			//CHECK CREATOR
			if(!Crypto.getInstance().isValidAddress(creator))
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			APIUtils.askAPICallAllowed("POST polls " + x, request);

			//CHECK IF WALLET EXISTS
			if(!Controller.getInstance().doesWalletExists())
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
			}
						
			//CHECK WALLET UNLOCKED
			if(!Controller.getInstance().isWalletUnlocked())
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
			}

			//GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(creator);				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}
				
			//CREATE POLL
			Pair<Transaction, Integer> result = Controller.getInstance().createPoll(account, name, description, options, bdFee);
				
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				return result.getA().toJson().toJSONString();
			
			case Transaction.NOT_YET_RELEASED:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NOT_YET_RELEASED);		
				
			case Transaction.NAME_NOT_LOWER_CASE:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NOT_LOWER_CASE);	
				
			case Transaction.INVALID_NAME_LENGTH:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);	
			
			case Transaction.INVALID_VALUE_LENGTH:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_VALUE_LENGTH);	
				
			case Transaction.POLL_ALREADY_CREATED:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_POLL_ALREADY_EXISTS);		
				
			case Transaction.INVALID_ADDRESS:
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
					
			case Transaction.INVALID_OPTIONS_LENGTH:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_OPTIONS_LENGTH);	
				
			case Transaction.INVALID_OPTION_LENGTH:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_OPTION_LENGTH);	
				
			case Transaction.DUPLICATE_OPTION:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_DUPLICATE_OPTION);		
			
			case Transaction.NEGATIVE_FEE:
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_FEE);
				
			case Transaction.FEE_LESS_REQUIRED:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_FEE_LESS_REQUIRED);
					
			case Transaction.NO_BALANCE:	
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NO_BALANCE);
			
			default:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_UNKNOWN);	
			}
		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			//e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
	
	@POST
	@Path("/vote/{name}")
	@Consumes(MediaType.WILDCARD)
	public String createPollVote(String x, @PathParam("name") String name)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String voter = (String) jsonObject.get("voter");
			String option = (String) jsonObject.get("option");
			String fee = (String) jsonObject.get("fee");
			
			//PARSE FEE
			BigDecimal bdFee;
			try
			{
				bdFee = new BigDecimal(fee);
				bdFee = bdFee.setScale(8);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_FEE);
			}	
			
			//CHECK VOTER
			if(!Crypto.getInstance().isValidAddress(voter))
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			APIUtils.askAPICallAllowed("POST polls/vote/" + name + "\n"+x, request);

			//CHECK IF WALLET EXISTS
			if(!Controller.getInstance().doesWalletExists())
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
			}
						
			//CHECK WALLET UNLOCKED
			if(!Controller.getInstance().isWalletUnlocked())
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
			}

			//GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(voter);				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}
			
			//GET POLL
			Poll poll = Controller.getInstance().getPoll(name);
			if(poll == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_POLL_NO_EXISTS);
			}
			
			//GET OPTION
			PollOption pollOption = poll.getOption(option);
			if(pollOption == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_POLL_OPTION_NO_EXISTS);
			}
				
			//CREATE POLL
			Pair<Transaction, Integer> result = Controller.getInstance().createPollVote(account, poll, pollOption, bdFee);
				
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				return result.getA().toJson().toJSONString();
			
			case Transaction.NOT_YET_RELEASED:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NOT_YET_RELEASED);		
				
			case Transaction.NAME_NOT_LOWER_CASE:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NOT_LOWER_CASE);	
				
			case Transaction.INVALID_NAME_LENGTH:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);	
			
			case Transaction.POLL_NO_EXISTS:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_POLL_NO_EXISTS);
				
			case Transaction.OPTION_NO_EXISTS:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_POLL_OPTION_NO_EXISTS);	
				
			case Transaction.ALREADY_VOTED_FOR_THAT_OPTION:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_ALREADY_VOTED_FOR_THAT_OPTION);	
				
			case Transaction.NEGATIVE_FEE:
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_FEE);
				
			case Transaction.FEE_LESS_REQUIRED:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_FEE_LESS_REQUIRED);
					
			case Transaction.NO_BALANCE:	
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NO_BALANCE);
			
			default:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_UNKNOWN);	
			}
		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			//e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	public String getPolls()
	{
		APIUtils.askAPICallAllowed("GET polls", request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		List<Pair<Account, Poll>> polls = Controller.getInstance().getPolls();
		JSONArray array = new JSONArray();
		
		for(Pair<Account, Poll> poll: polls)
		{
			array.add(poll.getB().toJson());
		}
		
		return array.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/address/{address}")	
	public String getPolls(@PathParam("address") String address)
	{
		APIUtils.askAPICallAllowed("GET polls/address/" + address, request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
				
		//CHECK ADDRESS
		if(!Crypto.getInstance().isValidAddress(address))
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
		}
				
		//CHECK ACCOUNT IN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);	
		if(account == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}
		
		JSONArray array = new JSONArray();
		for(Poll poll: Controller.getInstance().getPolls(account))
		{
			array.add(poll.toJson());
		}
		
		return array.toJSONString();
	}
	
	@GET
	@Path("/{name}")	
	public String getPoll(@PathParam("name") String name)
	{	
		Poll poll = Controller.getInstance().getPoll(name);
				
		//CHECK IF NAME EXISTS
		if(poll == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_POLL_NO_EXISTS);
		}
		
		return poll.toJson().toJSONString();
	}

	@SuppressWarnings("unchecked")
	@Path("/network")	
	@GET
	public String getAllPolls()
	{
		Collection<Poll> polls = Controller.getInstance().getAllPolls();
		JSONArray array = new JSONArray();
		
		for(Poll poll: polls)
		{
			array.add(poll.getName());
		}
		
		return array.toJSONString();
	}
	
}
