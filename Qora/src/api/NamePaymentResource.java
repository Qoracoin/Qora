package api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import utils.APIUtils;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;

@Path("namepayment")
@Produces(MediaType.APPLICATION_JSON)
public class NamePaymentResource {

	
	@Context
	HttpServletRequest request;
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public String namePayment(String x)
	{
		try
		{				
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String assetKey = (String) jsonObject.get("asset");
			String amount = (String) jsonObject.get("amount");
			String fee = (String) jsonObject.get("fee");
			String sender = (String) jsonObject.get("sender");
			String nameName = (String) jsonObject.get("recipient");	
			
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(nameName);
			if(nameToAdress.getB() == NameResult.OK)
			{
				String recipient = nameToAdress.getA().getAddress();
				return APIUtils.processPayment(assetKey, amount, fee, sender, recipient, x, request);
			}else
			{
				return APIUtils.processPayment(assetKey, amount, fee, sender, nameName, x, request);
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
	
	
}
