package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;

import controller.Controller;
import database.DBSet;
import qora.account.PublicKeyAccount;
import qora.naming.Name;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.web.NameStorageMap;
import qora.web.OrphanNameStorageHelperMap;
import qora.web.OrphanNameStorageMap;

public class StorageUtils {

	// REPLACES CURRENT VALUE
	public static final String ADD_COMPLETE_KEY = "addcomplete";
	// REMOVES CURRENT VALUE (COMPLETE KEY FROM STORAGE)
	public static final String REMOVE_COMPLETE_KEY = "removecomplete";
	// ADD VALUE TO A LIST IF NOT IN LIST SEPERATOR ";"
	public static final String ADD_LIST_KEY = "addlist";
	// REMOVE VALUE FROM LIST IF VALUE THERE SEPERATOR ";"
	public static final String REMOVE_LIST_KEY = "removelist";
	// ADD TO CURRENT VALUE WITHOUT SEPERATOR
	public static final String ADD_KEY = "add";
	// ADD PATCH TO CURRENT VALUE
	public static final String PATCH_KEY = "patch";


	@SuppressWarnings("unchecked")
	public static JSONObject getStorageJsonObject(
			List<Pair<String, String>> addCompleteKeys,
			List<String> removeCompleteKeys,
			List<Pair<String, String>> addListKeys,
			List<Pair<String, String>> removeListKeys,
			List<Pair<String, String>> addWithoutSeperator, List<Pair<String, String>> addPatch) {
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

		addListPairtoJson(addWithoutSeperator, json, ADD_KEY);
		
		addListPairtoJson(addPatch, json, PATCH_KEY);

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

	public static void processUpdate(byte[] data, byte[] signature,
			PublicKeyAccount creator, DBSet db) {

			String string = new String(data, Charsets.UTF_8 );
			
			string = GZIP.webDecompress(string);

			JSONObject jsonObject = (JSONObject) JSONValue.parse(string);

			if (jsonObject != null) {

				String name = (String) jsonObject.get("name");

				OrphanNameStorageHelperMap orphanNameStorageHelperMap = db.getOrphanNameStorageHelperMap();
				List<byte[]> list = orphanNameStorageHelperMap.get(name);
				if(list == null || !ByteArrayUtils.contains(list, signature))
				{
					if (name != null) {
						
						Name nameObj = db.getNameMap().get(name);
						
						if (nameObj == null) {
							
							//addressstorage?
							if(!name.equals(creator.getAddress()))
							{
								//they don't match do nothing!
								return;
							}
							
						}else
						{
							
							if (!nameObj.getOwner().getAddress()
									.equals(creator.getAddress())) {
								// creator is not the owner of the name
								return;
							}
						}
						
						
						NameStorageMap nameStorageMap = db
								.getNameStorageMap();
						OrphanNameStorageMap orphanNameStorageMap = db.getOrphanNameStorageMap();
						
						Set<String> allKeysForOrphanSaving = getAllKeysForOrphanSaving(jsonObject);
						
						// SAVE OLD VALUES FOR ORPHANING
						for (String keyForOrphaning : allKeysForOrphanSaving) {
							orphanNameStorageMap.add(signature, keyForOrphaning,
									nameStorageMap.getOpt(name, keyForOrphaning));
						}
						
						db.getOrphanNameStorageHelperMap()
						.add(name, signature);
						
						addTxChangesToStorage(jsonObject, name, nameStorageMap,
								null);
						
					}
				}
				

			}


	}

	@SuppressWarnings("unchecked")
	public static void addTxChangesToStorage(JSONObject jsonObject,
			String name, NameStorageMap nameStorageMap,
			Set<String> onlyTheseKeysOpt) {
		String addCompleteJson = (String) jsonObject.get(ADD_COMPLETE_KEY);
		if (addCompleteJson != null) {
			JSONObject addCompleteResults = (JSONObject) JSONValue
					.parse(addCompleteJson);

			Set<String> keys = addCompleteResults.keySet();

			for (String key : keys) {

				if (onlyTheseKeysOpt == null || onlyTheseKeysOpt.contains(key)) {
					nameStorageMap.add(name, key,
							"" + addCompleteResults.get(key));
				}
			}

		}

		String removeJson = (String) jsonObject.get(REMOVE_COMPLETE_KEY);
		if (removeJson != null) {

			JSONObject removeCompleteResults = (JSONObject) JSONValue
					.parse(removeJson);

			Set<String> keys = removeCompleteResults.keySet();

			for (String key : keys) {

				if (onlyTheseKeysOpt == null || onlyTheseKeysOpt.contains(key)) {
					nameStorageMap.remove(name, key);
				}
			}
		}

		String addJsonList = (String) jsonObject.get(ADD_LIST_KEY);
		if (addJsonList != null) {

			JSONObject addListKey = (JSONObject) JSONValue.parse(addJsonList);

			Set<String> keys = addListKey.keySet();

			for (String key : keys) {
				if (onlyTheseKeysOpt == null || onlyTheseKeysOpt.contains(key)) {
					List<String> entriesToAdd = new ArrayList<>(
							Arrays.asList(StringUtils.split(
									"" + addListKey.get(key), ";")));
					nameStorageMap.addListEntries(name, key, entriesToAdd);
				}
			}
		}

		String removeJsonList = (String) jsonObject.get(REMOVE_LIST_KEY);
		if (removeJsonList != null) {

			JSONObject removeListKey = (JSONObject) JSONValue
					.parse(removeJsonList);

			Set<String> keys = removeListKey.keySet();

			for (String key : keys) {
				if (onlyTheseKeysOpt == null || onlyTheseKeysOpt.contains(key)) {
					List<String> entriesToAdd = new ArrayList<>(
							Arrays.asList(StringUtils.split(
									"" + removeListKey.get(key), ";")));
					nameStorageMap.removeListEntries(name, key, entriesToAdd);
				}
			}
		}

		String addJson = (String) jsonObject.get(ADD_KEY);
		if (addJson != null) {

			JSONObject addJsonKey = (JSONObject) JSONValue.parse(addJson);

			Set<String> keys = addJsonKey.keySet();

			for (String key : keys) {

				if (onlyTheseKeysOpt == null || onlyTheseKeysOpt.contains(key)) {
					String oldValueOpt = nameStorageMap.getOpt(name, key);
					oldValueOpt = oldValueOpt == null ? "" : oldValueOpt;
					nameStorageMap.add(name, key,
							oldValueOpt + "" + addJsonKey.get(key));
				}
			}
		}
		
		String patchJson = (String) jsonObject.get(PATCH_KEY);
		if (patchJson != null) {
			
			JSONObject patchJsonKey = (JSONObject) JSONValue.parse(patchJson);
			
			Set<String> keys = patchJsonKey.keySet();
			
			for (String key : keys) {
				
				if (onlyTheseKeysOpt == null || onlyTheseKeysOpt.contains(key)) {
					String oldValueOpt = nameStorageMap.getOpt(name, key);
					
					oldValueOpt = oldValueOpt == null ? "" : oldValueOpt;
					try {
						nameStorageMap.add(name, key,
								DiffHelper.patch(oldValueOpt, (String) patchJsonKey.get(key)));
					} catch (Throwable e) {
						Logger.getGlobal().info("Invalid patch!");
						Logger.getGlobal().log(Level.FINE, "Invalid patch!", e);
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
		getKeys(jsonObject, results, ADD_KEY);
		getKeys(jsonObject, results, PATCH_KEY);

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

	public static void processOrphan(byte[] data, byte[] signature, DBSet db) {

		String string = new String(data, Charsets.UTF_8 );
		
		string = GZIP.webDecompress(string);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(string);

		if (jsonObject != null) {

			String name = (String) jsonObject.get("name");

			if (name != null) {

				Map<String, String> orphanMapForTx = db
						.getOrphanNameStorageMap().get(signature);

				if (orphanMapForTx != null) {
					
					//RESTORING SNAPSHOT FOR ALL CHANGED KEYS 
					NameStorageMap nameStorageMap = db
							.getNameStorageMap();
					Set<String> keySet = orphanMapForTx.keySet();

					for (String key : keySet) {
						Map<String, String> valueMapForName = nameStorageMap
								.get(name);
						if (valueMapForName != null) {
							String value = orphanMapForTx.get(key);
							if (value != null) {
								nameStorageMap.add(name, key, value);
							} else {
								nameStorageMap.remove(name, key);
							}
						}
					}

					List<byte[]> listOfSignaturesForName = db
							.getOrphanNameStorageHelperMap().get(name);
					int indexOf =  ByteArrayUtils.indexOf(listOfSignaturesForName, signature);
					indexOf++;
					
					OrphanNameStorageMap orphanNameStorageMap = db.getOrphanNameStorageMap();
					// REDO ALL FOLLOWING TXS FOR THIS NAME (THIS TIME
					// SELECTIVE)
					for (int i = indexOf; i < listOfSignaturesForName.size(); i++) {
						
						byte[] signatureofFollowingTx = listOfSignaturesForName.get(i);
						Transaction transaction = Controller.getInstance().getTransaction(signatureofFollowingTx, db);
						byte[] dataOfFollowingTx = ((ArbitraryTransaction) transaction).getData();
						
						String dataOfFollowingTxSting = new String(dataOfFollowingTx,  Charsets.UTF_8 );
						
						JSONObject jsonObjectOfFollowingTx = (JSONObject) JSONValue.parse(dataOfFollowingTxSting);
						
						Set<String> allKeysForOrphanSaving = getAllKeysForOrphanSaving(jsonObjectOfFollowingTx);
						
						//ALL KEYS THAT THEY HAVE IN COMMON
						Set<String> keysToSaveSnapshot = new HashSet<String>();
						for (String key : keySet) {
							if(allKeysForOrphanSaving.contains(key))
							{
								keysToSaveSnapshot.add(key);
							}
						}
						
						
						// SAVE OLD VALUES FOR ORPHANING
						for (String keyForOrphaning : keysToSaveSnapshot) {
							orphanNameStorageMap.add(signatureofFollowingTx, keyForOrphaning,
									nameStorageMap.getOpt(name, keyForOrphaning));
						}
						
						
						addTxChangesToStorage(jsonObjectOfFollowingTx, name, nameStorageMap, keySet);
					}

					db.getOrphanNameStorageMap()
							.delete(signature);
					
					db
					.getOrphanNameStorageHelperMap().remove(name, signature);

				}

			}
		}
	}


	

}
