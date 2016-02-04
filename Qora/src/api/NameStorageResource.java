package api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import controller.Controller;
import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.GZIP;
import utils.Pair;
import utils.StorageUtils;

@Path("namestorage")
@Produces(MediaType.APPLICATION_JSON)
public class NameStorageResource {

	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{name}/list")
	public String listNameStorage(@PathParam("name") String name) {

		Name nameObj = DBSet.getInstance().getNameMap().get(name);

		if (nameObj == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_NOT_REGISTERED);
		}

		Map<String, String> map = DBSet.getInstance().getNameStorageMap()
				.get(name);

		JSONObject json = new JSONObject();
		if (map != null) {
			Set<String> keySet = map.keySet();

			for (String key : keySet) {
				json.put(key, map.get(key));
			}
		}

		return json.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{name}/keys")
	public String listKeysNameStorage(@PathParam("name") String name) {

		Name nameObj = DBSet.getInstance().getNameMap().get(name);

		if (nameObj == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_NOT_REGISTERED);
		}

		Map<String, String> map = DBSet.getInstance().getNameStorageMap()
				.get(name);

		JSONArray json = new JSONArray();
		if (map != null) {
			Set<String> keySet = map.keySet();

			for (String key : keySet) {
				json.add(key);
			}
		}

		return json.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{name}/key/{key}")
	public String getNameStorageValue(@PathParam("name") String name,
			@PathParam("key") String key) {
		Name nameObj = DBSet.getInstance().getNameMap().get(name);

		if (nameObj == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_NOT_REGISTERED);
		}

		Map<String, String> map = DBSet.getInstance().getNameStorageMap()
				.get(name);

		JSONObject json = new JSONObject();
		if (map != null && map.containsKey(key)) {
			json.put(key, map.get(key));
		}

		return json.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/update/{name}")
	public String updateEntry(String x, @PathParam("name") String name) {
		try {
			APIUtils.disallowRemote(request);

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

			// CHECK WALLET IN SYNC
			if (Controller.getInstance().getStatus() != Controller.STATUS_OKE) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_NOT_IN_SYNC);
			}

			Name nameObj = DBSet.getInstance().getNameMap().get(name);
//			Controller.getInstance().getAccountByAddress(name)

			String creator;
			if (nameObj == null) {
				
				//check if addressstorage
				Account accountByAddress = Controller.getInstance().getAccountByAddress(name);
				
				if(accountByAddress == null)
				{
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_NAME_NOT_REGISTERED);
				}
				
				
					creator = name;
				
			}else
			{
				creator = nameObj.getOwner().getAddress();
			}

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

			String compressedJsonString = GZIP.compress(jsonString);

			if (compressedJsonString.length() < jsonString.length()) {
				jsonString = compressedJsonString;
			}

			byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
			List<String> askApicalls = new ArrayList<String>();	
			List<String> decompressedValue = new ArrayList<String>();
			JSONObject jsonObjectForCheck = (JSONObject) JSONValue.parse(x);
			// IF VALUE TOO LARGE FOR ONE ARB TX AND WE ONLY HAVE ADDCOMPLETE
			// WITH ONE KEY
			if (bytes.length > 4000
					&& jsonObjectForCheck.containsKey(StorageUtils.ADD_COMPLETE_KEY)
					&& jsonObjectForCheck.keySet().size() == 1) {
				JSONObject innerJsonObject = (JSONObject) JSONValue.parse((String) jsonObjectForCheck.get(StorageUtils.ADD_COMPLETE_KEY));
				if (innerJsonObject.keySet().size() == 1) {
					// Starting Multi TX

					String key = (String) innerJsonObject.keySet().iterator()
							.next();
					String value = (String) innerJsonObject.get(key);

					Iterable<String> chunks = Splitter.fixedLength(3500).split(
							value);
					List<String> arbTxs = Lists.newArrayList(chunks);

					BigDecimal completeFee = BigDecimal.ZERO;
					List<Pair<byte[], BigDecimal>> allTxPairs = new ArrayList<>();

					boolean isFirst = true;
					for (String valueString : arbTxs) {
						Pair<String, String> keypair = new Pair<String, String>(
								key, valueString);
						JSONObject storageJsonObject;
						if (isFirst) {
							storageJsonObject = StorageUtils
									.getStorageJsonObject(
											Collections.singletonList(keypair),
											null, null, null, null, null);
							isFirst = false;
						} else {
							storageJsonObject = StorageUtils
									.getStorageJsonObject(null, null, null,
											null,
											Collections.singletonList(keypair), null);
						}
						storageJsonObject.put("name", name);

						String jsonStringForMultipleTx = storageJsonObject
								.toJSONString();

						String compressedjsonStringForMultipleTx = GZIP
								.compress(jsonStringForMultipleTx);

						if (compressedjsonStringForMultipleTx.length() < jsonStringForMultipleTx
								.length()) {
							jsonStringForMultipleTx = compressedjsonStringForMultipleTx;
						}

						byte[] resultbyteArray = jsonStringForMultipleTx
								.getBytes(StandardCharsets.UTF_8);
						BigDecimal currentFee = Controller
								.getInstance()
								.calcRecommendedFeeForArbitraryTransaction(
										resultbyteArray, null).getA();

						completeFee = completeFee.add(currentFee);

						allTxPairs.add(new Pair<>(resultbyteArray, currentFee));

						String decompressed = GZIP.webDecompress(jsonStringForMultipleTx);
						askApicalls.add("POST namestorage/update/" + name
								+ "\n"
								+ decompressed
								+ "\nfee: " + currentFee.toPlainString());
						decompressedValue.add(decompressed);
					}

					if (account.getBalance(1, DBSet.getInstance()).compareTo(
							completeFee) == -1) {
						throw ApiErrorFactory.getInstance().createError(
								ApiErrorFactory.ERROR_NO_BALANCE);
					}
					
					if(allTxPairs.size() > ApiErrorFactory.BATCH_TX_AMOUNT)
					{
						throw ApiErrorFactory.getInstance().createError(
								ApiErrorFactory.ERROR_TX_AMOUNT);
					}
					
					//recalculating qora amount
					BigDecimal newCompleteFee = BigDecimal.ZERO;
					BigDecimal oldAmount = BigDecimal.ZERO;
					List<Pair<byte[], BigDecimal>> newPairs = new ArrayList<Pair<byte[],BigDecimal>>();
					for (Pair<byte[], BigDecimal> pair : allTxPairs) {
						if(oldAmount.equals(BigDecimal.ZERO))
						{
							oldAmount = pair.getB();
							newCompleteFee = oldAmount;
							newPairs.add(pair);
							continue;
						}
						
						BigDecimal newAmount = oldAmount.multiply(new BigDecimal(1.15));
						newAmount = newAmount.setScale(0, BigDecimal.ROUND_UP).setScale(8); 
						pair.setB(newAmount);
						newPairs.add(pair);
						
						oldAmount = newAmount;
						
						newCompleteFee= newCompleteFee.add(newAmount);
						
						
					}
					
					String apicalls = "";
					for (int i = 0; i < newPairs.size(); i++) {
						apicalls +=	"POST namestorage/update/" + name
								+ "\n"
								+ decompressedValue.get(i)
								+ "\nfee: " + newPairs.get(i).getB().toPlainString()+"\n";
					}

					String basicInfo = "Because of the size of the data this call will create "
							+ allTxPairs.size()
							+ " transactions.\nAll Arbitrary Transactions will cost: "
							+ newCompleteFee.toPlainString() + " Qora.\nDetails:\n\n";

//					basicInfo += StringUtils.join(askApicalls, "\n");
					basicInfo += apicalls;

					APIUtils.askAPICallAllowed(basicInfo, request);

					Pair<Transaction, Integer> result;
					String results = "";
					for (Pair<byte[], BigDecimal> pair : newPairs) {
						result = Controller.getInstance()
								.createArbitraryTransaction(account, null, 10,
										pair.getA(), pair.getB());

						results += ArbitraryTransactionsResource
								.checkArbitraryTransaction(result) + "\n";
					}

					return results;

				}
			}
			BigDecimal fee = Controller.getInstance()
					.calcRecommendedFeeForArbitraryTransaction(bytes, null).getA();
			APIUtils.askAPICallAllowed(
					"POST namestorage/update/" + name + "\n"
							+ GZIP.webDecompress(jsonString) + "\nfee: "
							+ fee.toPlainString(), request);

			// SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance()
					.createArbitraryTransaction(account, null, 10, bytes, fee);

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
