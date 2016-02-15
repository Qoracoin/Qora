package api;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.payment.Payment;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;

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
			
			long lgAsset = 0L;
			if(jsonObject.containsKey("asset")) {
				lgAsset = ((Long) jsonObject.get("asset")).intValue();
			}
			
			Asset defaultAsset;

			try {
				defaultAsset = Controller.getInstance().getAsset(new Long(lgAsset));
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_ASSET_ID);
			}
			
			List<Payment> payments = MultiPaymentResource.jsonPaymentParser((JSONArray)jsonObject.get("payments"), defaultAsset);
			
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
			if(fee != null) {
				try
				{
					bdFee = new BigDecimal(fee);
					bdFee = bdFee.setScale(8);
				}
				catch(Exception e)
				{
					throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_FEE);
				}	
			} else {
				Pair<BigDecimal, Integer> recommendedFee = Controller.getInstance().calcRecommendedFeeForArbitraryTransaction(dataBytes, payments);
				
				bdFee = recommendedFee.getA().setScale(0, BigDecimal.ROUND_CEILING).setScale(8);
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
			Pair<Transaction, Integer> result = Controller.getInstance().createArbitraryTransaction(account, payments, service, dataBytes, bdFee);
				
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
		case Transaction.VALIDATE_OK:
			
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
		
		case Transaction.NEGATIVE_AMOUNT:	
		case Transaction.INVALID_AMOUNT:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);
			
		default:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_UNKNOWN);	
		}
	}
}
