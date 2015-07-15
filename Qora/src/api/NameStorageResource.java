package api;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;
import controller.Controller;
import database.DBSet;

@Path("namestorage")
@Produces(MediaType.APPLICATION_JSON)
public class NameStorageResource {

	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@POST
	@Path("/update/{name}")
	public String updateEntry(String x, @PathParam("name") String name
			) {
		try {

			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);

			// CHECK IF WALLET EXISTS
			if (!Controller.getInstance().doesWalletExists()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
			}

			// CHECK WALLET UNLOCKED
			if (!Controller.getInstance().isWalletUnlocked()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_LOCKED);
			}

			Name nameObj = DBSet.getInstance().getNameMap().get(name);
			
			if(nameObj == null)
			{
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NOT_REGISTERED);
			}
			
			String creator = nameObj.getOwner().getAddress();
			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(creator)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			// CHECK ACCOUNT IN WALLET
			if (Controller.getInstance().getAccountByAddress(creator) == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
			}



			// GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance()
					.getPrivateKeyAccountByAddress(creator);
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}


			jsonObject.put("name", name);
			String jsonString = jsonObject.toJSONString();
			byte[] bytes = jsonString.getBytes();
			BigDecimal fee = Controller.getInstance().calcRecommendedFeeForArbitraryTransaction(bytes).getA();
			APIUtils.askAPICallAllowed("POST namestorage/" + creator + "/" + name + "\n" + jsonString + "\nfee: " + fee.toPlainString(),
					request);


			// SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance()
					.createArbitraryTransaction(account, 10,
							bytes, fee);

			return ArbitraryTransactionsResource
					.checkArbitraryTransaction(result);

		} catch (NullPointerException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} catch (ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}

	}
	
}