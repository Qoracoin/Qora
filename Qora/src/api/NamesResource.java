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
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.GZIP;
import utils.Pair;
import utils.Qorakeys;
import controller.Controller;

@Path("names")
@Produces(MediaType.APPLICATION_JSON)
public class NamesResource {

	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@GET
	public String getNames() {
		APIUtils.askAPICallAllowed("GET names", request);

		// CHECK IF WALLET EXISTS
		if (!Controller.getInstance().doesWalletExists()) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		List<Pair<Account, Name>> names = Controller.getInstance().getNames();
		JSONArray array = new JSONArray();

		for (Pair<Account, Name> name : names) {
			array.add(name.getB().toJson());
		}

		return array.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/address/{address}")
	public String getNames(@PathParam("address") String address) {
		APIUtils.askAPICallAllowed("GET names/address/" + address, request);

		// CHECK IF WALLET EXISTS
		if (!Controller.getInstance().doesWalletExists()) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		// CHECK ADDRESS
		if (!Crypto.getInstance().isValidAddress(address)) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_ADDRESS);
		}

		// CHECK ACCOUNT IN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);
		if (account == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}

		JSONArray array = new JSONArray();
		for (Name name : Controller.getInstance().getNames(account)) {
			array.add(name.toJson());
		}

		return array.toJSONString();
	}

	@GET
	@Path("/{name}")
	public static String getName(@PathParam("name") String nameName) {
		Name name = Controller.getInstance().getName(nameName);

		// CHECK IF NAME EXISTS
		if (name == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_NO_EXISTS);
		}

		return name.toJson().toJSONString();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	public String createName(String x) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String registrant = (String) jsonObject.get("registrant");
			String name = (String) jsonObject.get("name");
			String value = (String) jsonObject.get("value");

			// PARSE FEE
			BigDecimal bdFee;
			try {
				bdFee = new BigDecimal(fee);
				bdFee = bdFee.setScale(8);
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);
			}

			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(registrant)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			APIUtils.askAPICallAllowed("POST names " + x, request);

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

			// GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance()
					.getPrivateKeyAccountByAddress(registrant);
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			// CREATE NAME
			Pair<Transaction, Integer> result = Controller.getInstance()
					.registerName(account, account, name, value, bdFee);

			switch (result.getB()) {
			case Transaction.VALIDATE_OKE:

				return result.getA().toJson().toJSONString();

			case Transaction.NAME_NOT_LOWER_CASE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NOT_LOWER_CASE);

			case Transaction.INVALID_NAME_LENGTH:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);

			case Transaction.INVALID_VALUE_LENGTH:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_VALUE_LENGTH);

			case Transaction.NAME_ALREADY_REGISTRED:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_ALREADY_EXISTS);

			case Transaction.NEGATIVE_FEE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);

			case Transaction.NO_BALANCE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NO_BALANCE);

			default:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_UNKNOWN);
			}
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

	@SuppressWarnings("unchecked")
	@DELETE
	@Path("/key/{name}")
	public String removeKey(String x, @PathParam("name") String nameName) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String key = (String) jsonObject.get("key");

			// keys are always lowercase!
			key = key.toLowerCase();

			// PARSE FEE
			BigDecimal bdFee;
			try {
				bdFee = new BigDecimal(fee);
				bdFee = bdFee.setScale(8);
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);
			}

			APIUtils.askAPICallAllowed("DELETE names/key/" + nameName + "\n"
				                           + x, request);

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


			// GET NAME
			Name name = Controller.getInstance().getName(nameName);
			if (name == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NO_EXISTS);
			}

			// GET ACCOUNT
			PrivateKeyAccount account = Controller
					.getInstance()
					.getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_NAME_OWNER);
			}

			String oldValue = GZIP.webDecompress(name.getValue());
			JSONObject resultJson = null;
			try {
				resultJson = (JSONObject) JSONValue.parse(oldValue);
			} catch (Exception e) {
				// looks like no json
			}

			if (resultJson != null) {
				if (!resultJson.containsKey(key)) {

					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_KEY_NOT_EXISTS);

				} else {

					
					if(resultJson.size() == 1)
					{
						//THERE MUST BE at least one KEY always, so we convert it to the defaultkey
						String oldVal = (String) resultJson.get(key);
						resultJson.put(Qorakeys.DEFAULT.toString(), oldVal);
					}
					
					resultJson.remove(key);
					
				}

			} else {

				//SPECIAL CASE USER TRIES TO DELETE LAST KEY
				if(key.equals(Qorakeys.DEFAULT.toString()))
				{
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_LAST_KEY_IS_DEFAULT_KEY_ERROR);
				}
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_KEY_NOT_EXISTS);

			}

			String resultString = resultJson.toJSONString();

			// UPDATE NAME
			Pair<Transaction, Integer> result = Controller.getInstance()
					.updateName(account, account, nameName,
							GZIP.compress(resultString), bdFee);

			return checkNameTransaction(result);

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

	@SuppressWarnings("unchecked")
	@POST
	@Path("/key/{name}")
	public String updateKey(String x, @PathParam("name") String nameName) {
		try {

			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String key = (String) jsonObject.get("key");
			String value = (String) jsonObject.get("value");
			String updateString = (String) jsonObject.get("update");

			// keys are always lowercase!
			key = key.toLowerCase();

			boolean update = true;

			if (updateString != null) {
				if (updateString.equalsIgnoreCase("true")
						|| updateString.equalsIgnoreCase("false")) {
					update = Boolean.parseBoolean(updateString);
				} else {
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_INVALID_UPDATE_VALUE);
				}
			}

			// PARSE FEE
			BigDecimal bdFee;
			try {
				bdFee = new BigDecimal(fee);
				bdFee = bdFee.setScale(8);
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);
			}

			APIUtils.askAPICallAllowed("POST names/key/" + nameName + "\n" + x,
				request);

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

			// GET NAME
			Name name = Controller.getInstance().getName(nameName);
			if (name == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NO_EXISTS);
			}

			// GET ACCOUNT
			PrivateKeyAccount account = Controller
					.getInstance()
					.getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_NAME_OWNER);
			}

			String oldValue = GZIP.webDecompress(name.getValue());
			JSONObject resultJson = null;
			try {
				resultJson = (JSONObject) JSONValue.parse(oldValue);
			} catch (Exception e) {
				// looks like no json
			}

			if (resultJson != null) {
				if (resultJson.containsKey(key)) {
					if (!update) {
						throw ApiErrorFactory.getInstance().createError(
								ApiErrorFactory.ERROR_KEY_ALREADY_EXISTS);
					}

				}
				resultJson.put(key, value);

			} else {
				// CONVERTING TO CORRECT FORMAT
				resultJson = new JSONObject();
				resultJson.put(Qorakeys.DEFAULT.toString(), oldValue);
				resultJson.put(key, value);
			}

			String resultString = resultJson.toJSONString();

			// UPDATE NAME
			Pair<Transaction, Integer> result = Controller.getInstance()
					.updateName(account, account, nameName,
							GZIP.compress(resultString), bdFee);

			return checkNameTransaction(result);

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

	@POST
	@Path("/{name}")
	@Consumes(MediaType.WILDCARD)
	public String updateName(String x, @PathParam("name") String nameName) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String newOwner = (String) jsonObject.get("newowner");
			String newValue = (String) jsonObject.get("newvalue");

			// PARSE FEE
			BigDecimal bdFee;
			try {
				bdFee = new BigDecimal(fee);
				bdFee = bdFee.setScale(8);
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);
			}

			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(newOwner)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			APIUtils.askAPICallAllowed("POST names/" + nameName + "\n" + x,
				request);

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

			// GET NAME
			Name name = Controller.getInstance().getName(nameName);
			if (name == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NO_EXISTS);
			}

			// GET ACCOUNT
			PrivateKeyAccount account = Controller
					.getInstance()
					.getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_NAME_OWNER);
			}

			// UPDATE NAME
			Pair<Transaction, Integer> result = Controller.getInstance()
					.updateName(account, new Account(newOwner), nameName,
							newValue, bdFee);

			return checkNameTransaction(result);
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

	public String checkNameTransaction(Pair<Transaction, Integer> result) {
		switch (result.getB()) {
		case Transaction.VALIDATE_OKE:

			return result.getA().toJson().toJSONString();

		case Transaction.INVALID_NAME_LENGTH:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);

		case Transaction.INVALID_VALUE_LENGTH:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_VALUE_LENGTH);

		case Transaction.NAME_DOES_NOT_EXIST:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_NO_EXISTS);

		case Transaction.NAME_ALREADY_FOR_SALE:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_ALREADY_FOR_SALE);

		case Transaction.NEGATIVE_FEE:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_FEE);

		case Transaction.FEE_LESS_REQUIRED:
			
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_FEE_LESS_REQUIRED);

		case Transaction.NO_BALANCE:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NO_BALANCE);

		default:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_UNKNOWN);
		}
	}
}
