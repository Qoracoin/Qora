package api;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;
import controller.Controller;

@Path("namesales")
@Produces(MediaType.APPLICATION_JSON)
public class NameSalesResource 
{
	@Context
	HttpServletRequest request;
	
	@SuppressWarnings("unchecked")
	@GET
	public String getNameSales()
	{
		APIUtils.askAPICallAllowed("GET namesales", request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		List<Pair<Account, NameSale>> nameSales = Controller.getInstance().getNameSales();
		JSONArray array = new JSONArray();
		
		for(Pair<Account, NameSale> nameSale: nameSales)
		{
			array.add(nameSale.getB().toJson());
		}
		
		return array.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/address/{address}")	
	public String getNameSales(@PathParam("address") String address)
	{
		APIUtils.askAPICallAllowed("GET namesales/address/" + address, request);

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
		for(NameSale nameSale: Controller.getInstance().getNameSales(account))
		{
			array.add(nameSale.toJson());
		}
		
		return array.toJSONString();
	}
	
	@GET
	@Path("/{name}")	
	public static String getNameSale(@PathParam("name") String nameName)
	{	
		NameSale nameSale = Controller.getInstance().getNameSale(nameName);
				
		//CHECK IF NAME SALE EXISTS
		if(nameSale == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_SALE_NO_EXISTS);
		}
		
		return nameSale.toJson().toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/network")	
	@GET
	public String getAllNameSales()
	{
		List<NameSale> nameSales = Controller.getInstance().getAllNameSales();
		JSONArray array = new JSONArray();
		
		for(NameSale nameSale: nameSales)
		{
			array.add(nameSale.getKey());
		}
		
		return array.toJSONString();
	}
	
	@POST
	@Path("/{name}")
	@Consumes(MediaType.WILDCARD)
	public String createNameSale(String x, @PathParam("name") String nameName)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);		
			String amount = (String) jsonObject.get("amount");
			String fee = (String) jsonObject.get("fee");
			
			//PARSE AMOUNT
			BigDecimal bdAmount;
			try
			{
				bdAmount = new BigDecimal(amount);
				bdAmount = bdAmount.setScale(8);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);
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

			APIUtils.askAPICallAllowed("POST namesales/" + nameName + "\n"+x, request);

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

			//GET NAME
			Name name = Controller.getInstance().getName(nameName);
			if(name == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NO_EXISTS);
			}
			
			//GET OWNER
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(name.getOwner().getAddress());				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_OWNER);
			}
				
			//CREATE NAME SALE
			Pair<Transaction, Integer> result = Controller.getInstance().sellName(account, nameName, bdAmount, bdFee);
				
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				return result.getA().toJson().toJSONString();
			
			case Transaction.INVALID_NAME_LENGTH:
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);
					
			case Transaction.NAME_DOES_NOT_EXIST:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NO_EXISTS);	
				
			case Transaction.INVALID_ADDRESS:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);		
				
			case Transaction.INVALID_NAME_OWNER:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_OWNER);		
			
			case Transaction.NAME_ALREADY_FOR_SALE:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_ALREADY_FOR_SALE);	
				
			case Transaction.NEGATIVE_AMOUNT:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);				
				
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
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
	
	@DELETE
	@Path("/{name}/{fee}")
	@Consumes(MediaType.WILDCARD)
	public String cancelNameSale(@PathParam("name") String nameName, @PathParam("fee") String fee)
	{
		try
		{
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
			
			NameSale nameSale = Controller.getInstance().getNameSale(nameName);

			APIUtils.askAPICallAllowed("DELETE namesales/"+nameName+"/"+ fee, request );

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

			//CHECK IF NAME SALE EXISTS
			if(nameSale == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NO_EXISTS);
			}
			
			//GET OWNER
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(nameSale.getName().getOwner().getAddress());				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_OWNER);
			}
			
			//CREATE NAME SALE
			Pair<Transaction, Integer> result = Controller.getInstance().cancelSellName(account, nameSale, bdFee);
				
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				return result.getA().toJson().toJSONString();
			
			case Transaction.INVALID_NAME_LENGTH:
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);
					
			case Transaction.NAME_DOES_NOT_EXIST:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NO_EXISTS);	
				
			case Transaction.INVALID_ADDRESS:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);		
				
			case Transaction.INVALID_NAME_OWNER:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_OWNER);		
			
			case Transaction.NAME_NOT_FOR_SALE:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_SALE_NO_EXISTS);	
					
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
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
	
	@POST
	@Path("/buy/{name}")
	@Consumes(MediaType.WILDCARD)
	public String createNamePurchase(String x, @PathParam("name") String nameName)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);		
			String buyer = (String) jsonObject.get("buyer");
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

			APIUtils.askAPICallAllowed("POST namesales/buy/" + nameName + "\n" + x, request);

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

			NameSale nameSale = Controller.getInstance().getNameSale(nameName);
			
			//CHECK IF NAME SALE EXISTS
			if(nameSale == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NO_EXISTS);
			}
			
			//CHECK ADDRESS
			if(!Crypto.getInstance().isValidAddress(buyer))
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}
			
			//GET BUYER
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(buyer);				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_BUYER);
			}
			
			//CREATE NAME SALE
			Pair<Transaction, Integer> result = Controller.getInstance().BuyName(account, nameSale, bdFee);
				
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				return result.getA().toJson().toJSONString();
			
			case Transaction.INVALID_NAME_LENGTH:
					
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);
				
			case Transaction.NEGATIVE_AMOUNT:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);
				
			case Transaction.INVALID_AMOUNT:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);	
					
			case Transaction.NAME_DOES_NOT_EXIST:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_NO_EXISTS);	
							
			case Transaction.BUYER_ALREADY_OWNER: 	
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BUYER_ALREADY_OWNER);	
				
			case Transaction.NAME_NOT_FOR_SALE:
				
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NAME_SALE_NO_EXISTS);		
				
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
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
}
