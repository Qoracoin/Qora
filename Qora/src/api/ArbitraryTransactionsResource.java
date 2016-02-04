package api;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.PrivateKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;
import controller.Controller;

@Path("arbitrarytransactions")
@Produces(MediaType.APPLICATION_JSON)
public class ArbitraryTransactionsResource 
{
	
	@Context
	HttpServletRequest request;
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public String createArbitraryTransaction(String x)
	{
		try
		{
			APIUtils.askAPICallAllowed("POST arbitrarytransactions\n" + x, request );

			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			int service = ((Long) jsonObject.get("service")).intValue();
			String data = (String) jsonObject.get("data");
			String fee = (String) jsonObject.get("fee");
			String creator = (String) jsonObject.get("creator");
			
			//PARSE DATA
			byte[] dataBytes;
			try
			{
				dataBytes = Base58.decode(data);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_DATA);
			}
				
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
				
			//CHECK ADDRESS
			if(!Crypto.getInstance().isValidAddress(creator))
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}
				
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
				
			//SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance().createArbitraryTransaction(account, null, service, dataBytes, bdFee);
				
			return checkArbitraryTransaction(result);
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
			//e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}

	public static String checkArbitraryTransaction(Pair<Transaction, Integer> result) {
		switch(result.getB())
		{
		case Transaction.VALIDATE_OKE:
			
			return result.getA().toJson().toJSONString();
			
		case Transaction.NOT_YET_RELEASED:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NOT_YET_RELEASED);			
		
		case Transaction.INVALID_DATA_LENGTH:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_DATA_LENGTH);	

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
}
