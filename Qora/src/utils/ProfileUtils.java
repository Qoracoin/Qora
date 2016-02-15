package utils;

import org.json.simple.JSONObject;

import qora.web.NameStorageMap;
import database.DBSet;

public class ProfileUtils {
	
	
	public static JSONObject getBlogBlackWhiteList(String blogname)
	{
		JSONObject json = new JSONObject();
		NameStorageMap nameStorageMap = DBSet.getInstance().getNameStorageMap();
		
		if(nameStorageMap.get(blogname) != null)
		{
			addToJson(blogname, json, nameStorageMap, Qorakeys.BLOGWHITELIST);
			addToJson(blogname, json, nameStorageMap, Qorakeys.BLOGBLACKLIST);
		}
		
		
		return json;
		
	}
	
	public static JSONObject getProfile(String profilename)
	{
		JSONObject json = new JSONObject();
		NameStorageMap nameStorageMap = DBSet.getInstance().getNameStorageMap();
		
		if(nameStorageMap.get(profilename) != null)
		{
			addToJson(profilename, json, nameStorageMap, Qorakeys.BLOGTITLE);
			addToJson(profilename, json, nameStorageMap, Qorakeys.BLOGDESCRIPTION);
			addToJson(profilename, json, nameStorageMap, Qorakeys.BLOGENABLE);
			addToJson(profilename, json, nameStorageMap, Qorakeys.PROFILEENABLE);
			addToJson(profilename, json, nameStorageMap, Qorakeys.PROFILEAVATAR);
			addToJson(profilename, json, nameStorageMap, Qorakeys.PROFILEMAINGRAPHIC);
			addToJson(profilename, json, nameStorageMap, Qorakeys.PROFILEFOLLOW);
			addToJson(profilename, json, nameStorageMap, Qorakeys.PROFILELIKEPOSTS);
			addToJson(profilename, json, nameStorageMap, Qorakeys.BLOGBLOCKCOMMENTS);
		}
		
		
		return json;
		
	}

	@SuppressWarnings("unchecked")
	public static void addToJson(String profilename, JSONObject json,
			NameStorageMap nameStorageMap, Qorakeys key) {
		String value = nameStorageMap.getOpt(profilename, key.toString());
		if(value != null)
		{
			json.put(key.toString(), value);
		}
	}
	
}
