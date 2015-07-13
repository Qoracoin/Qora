package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;

import database.DBSet;
import qora.account.PublicKeyAccount;
import qora.naming.Name;
import qora.web.NameStorageMap;
import qora.web.OrphanNameStorageMap;

public class StorageUtils {

	public static final String ADD_COMPLETE_KEY = "addcomplete";
	public static final String REMOVE_COMPLETE_KEY = "removecomplete";
	public static final String ADD_LIST_KEY = "addlist";
	public static final String REMOVE_LIST_KEY = "removelist";

	@SuppressWarnings("unchecked")
	public static JSONObject getStorageJsonObject(
			List<Pair<String, String>> addCompleteKeys,
			List<String> removeCompleteKeys,
			List<Pair<String, String>> addListKeys,
			List<Pair<String, String>> removeListKeys) {
		JSONObject json = new JSONObject();

		addListPairtoJson(addCompleteKeys, json, ADD_COMPLETE_KEY);

		if (removeCompleteKeys != null && removeCompleteKeys.size() > 0) {
			JSONObject jsonRemoveComplete = new JSONObject();

			for (String key : removeCompleteKeys) {
				jsonRemoveComplete.put(key, "");
			}

			json.put(REMOVE_COMPLETE_KEY, jsonRemoveComplete.toString());
		}

		addListPairtoJson(addListKeys, json, ADD_LIST_KEY);

		addListPairtoJson(removeListKeys, json, REMOVE_LIST_KEY);

		return json;

	}

	@SuppressWarnings("unchecked")
	public static void addListPairtoJson(
			List<Pair<String, String>> addListKeys, JSONObject json, String key) {
		if (addListKeys != null && addListKeys.size() > 0) {
			JSONObject innerJsonObject = new JSONObject();

			for (Pair<String, String> pair : addListKeys) {
				innerJsonObject.put(pair.getA(), pair.getB());
			}

			json.put(key, innerJsonObject.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public static void processUpdate(byte[] data, byte[] signature, PublicKeyAccount creator) {
		String string = new String(data);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(string);

		if (jsonObject != null) {

			String name = (String) jsonObject.get("name");

			if (name != null) {

				
				Name nameObj = DBSet.getInstance().getNameMap().get(name);
				
				if(nameObj == null)
				{
					return;
				}
				
				if(!nameObj.getOwner().getAddress().equals(creator.getAddress()))
				{
//					creator is not the owner of the name
					return;
				}
				
				
				NameStorageMap nameStorageMap = DBSet.getInstance()
						.getNameStorageMap();
				OrphanNameStorageMap orphanNameStorageMap = DBSet.getInstance()
						.getOrphanNameStorageMap();

				Set<String> allKeysForOrphanSaving = getAllKeysForOrphanSaving(jsonObject);

				// SAVE OLD VALUES FOR ORPHANING
				for (String keyForOrphaning : allKeysForOrphanSaving) {
					orphanNameStorageMap.add(new Tuple2<byte[], String>(
							signature, name), keyForOrphaning, nameStorageMap
							.getOpt(name, keyForOrphaning));
				}

				String addJson = (String) jsonObject.get(ADD_COMPLETE_KEY);
				if (addJson != null) {
					JSONObject addCompleteResults = (JSONObject) JSONValue
							.parse(addJson);

					Set<String> keys = addCompleteResults.keySet();

					for (String key : keys) {
						nameStorageMap.add(name, key,
								"" + addCompleteResults.get(key));
					}

				}

				String removeJson = (String) jsonObject
						.get(REMOVE_COMPLETE_KEY);
				if (removeJson != null) {

					JSONObject removeCompleteResults = (JSONObject) JSONValue
							.parse(removeJson);

					Set<String> keys = removeCompleteResults.keySet();

					for (String key : keys) {

						nameStorageMap.remove(name, key);
					}
				}

				String addJsonList = (String) jsonObject.get(ADD_LIST_KEY);
				if (addJsonList != null) {

					JSONObject addListKey = (JSONObject) JSONValue
							.parse(addJsonList);

					Set<String> keys = addListKey.keySet();

					for (String key : keys) {
						List<String> entriesToAdd = new ArrayList<>(
								Arrays.asList(StringUtils.split(
										"" + addListKey.get(key), ";")));
						nameStorageMap.addListEntries(name, key, entriesToAdd);
					}
				}

				String removeJsonList = (String) jsonObject
						.get(REMOVE_LIST_KEY);
				if (removeJsonList != null) {

					JSONObject removeListKey = (JSONObject) JSONValue
							.parse(removeJsonList);

					Set<String> keys = removeListKey.keySet();

					for (String key : keys) {
						List<String> entriesToAdd = new ArrayList<>(
								Arrays.asList(StringUtils.split(""
										+ removeListKey.get(key), ";")));
						nameStorageMap.removeListEntries(name, key,
								entriesToAdd);
					}
				}

			}

		}

	}

	private static Set<String> getAllKeysForOrphanSaving(JSONObject jsonObject) {
		Set<String> results = new HashSet<>();
		getKeys(jsonObject, results, ADD_COMPLETE_KEY);
		getKeys(jsonObject, results, ADD_LIST_KEY);
		getKeys(jsonObject, results, REMOVE_COMPLETE_KEY);
		getKeys(jsonObject, results, REMOVE_LIST_KEY);

		return results;
	}

	private static void getKeys(JSONObject jsonObject, Set<String> results,
			String mainKey) {
		String addJson = (String) jsonObject.get(mainKey);
		if (addJson != null) {
			JSONObject addCompleteResults = (JSONObject) JSONValue
					.parse(addJson);

			@SuppressWarnings("unchecked")
			Set<String> keys = addCompleteResults.keySet();

			results.addAll(keys);

		}
	}
	

	public static void processOrphan(byte[] data, byte[] signature) {

		String string = new String(data);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(string);

		if (jsonObject != null) {

			String name = (String) jsonObject.get("name");

			if (name != null) {
				
				Tuple2<byte[], String> tupleKeyToProcess = new Tuple2<byte[], String>(signature, name);
				Map<String, String> orphanMapForTx = DBSet.getInstance().getOrphanNameStorageMap().get(tupleKeyToProcess);
				
				if(orphanMapForTx != null)
				{
					NameStorageMap nameStorageMap = DBSet.getInstance().getNameStorageMap();
					Set<String> keySet = orphanMapForTx.keySet();
					
					for (String key : keySet) {
						Map<String, String> valueMapForName = nameStorageMap.get(name);
						if(valueMapForName != null)
						{
							String value = orphanMapForTx.get(key);
							if(value != null)
							{
								nameStorageMap.add(name, key, value);
							}else
							{
								nameStorageMap.remove(name, key);
							}
						}
					}
					
					DBSet.getInstance().getOrphanNameStorageMap().delete(tupleKeyToProcess);
					
					
				}
				
				
			}
		}
	}

}
