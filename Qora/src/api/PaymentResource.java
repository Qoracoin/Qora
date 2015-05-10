package api;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import qora.crypto.Crypto;
import utils.APIUtils;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

@Path("payment")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentResource 
{
	@POST
	@Consumes(MediaType.WILDCARD)
	public String createPayment(String x)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String amount = (String) jsonObject.get("amount");
			String fee = (String) jsonObject.get("fee");
			String sender = (String) jsonObject.get("sender");
			String recipient = (String) jsonObject.get("recipient");
			
			if(!Crypto.getInstance().isValidAddress(recipient))
			{
				//NAME PAYMENT
				Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(recipient);
				
				if(nameToAdress.getB() == NameResult.OK)
				{
					recipient = nameToAdress.getA().getAddress();
				}
			}			
			
			return APIUtils.processPayment(amount, fee, sender, recipient);
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

	
	
	
	
}
