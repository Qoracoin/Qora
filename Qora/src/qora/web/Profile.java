package qora.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.simple.JSONObject;

import qora.naming.Name;
import utils.KeyVariation;
import utils.Pair;
import utils.ProfileUtils;
import utils.Qorakeys;
import utils.StorageUtils;
import api.NameStorageResource;
import controller.Controller;
import database.DBSet;

@SuppressWarnings("unchecked")
public class Profile {

	public static boolean isAllowedProfileName(String name) {
		// RULES FOR PROFILES
		if (name == null || name.length() < 3 || name.contains(";")
				|| name.endsWith(" ") || name.startsWith(" ")) {
			return false;
		}

		return true;
	}

	private final BlogBlackWhiteList blogBlackWhiteList;
	private JSONObject jsonRepresenation;
	private Name name;
	private List<Name> followerCache = null;

	public static Profile getProfileOpt(String name) {
		Profile result = null;
		if (name != null) {
			Name nameObj = DBSet.getInstance().getNameMap().get(name);
			result = Profile.getProfileOpt(nameObj);
		}

		return result;
	}

	public static Profile getProfileOpt(Name name) {

		if (name == null || !isAllowedProfileName(name.getName())) {
			return null;
		}
		Name nameReloaded = DBSet.getInstance().getNameMap()
				.get(name.getName());
		if (nameReloaded == null) {
			return null;
		}
		return new Profile(nameReloaded);
	}

	private Profile(Name name) {
		this.name = name;
		blogBlackWhiteList = BlogBlackWhiteList.getBlogBlackWhiteList(name
				.toString());
		jsonRepresenation = ProfileUtils.getProfile(name.getName());
	}

	public List<Name> getFollower() {

		if (followerCache != null) {
			return followerCache;
		}

		List<Name> results = new ArrayList<>();
		Collection<Name> values = DBSet.getInstance().getNameMap().getValues();

		for (Name name : values) {
			Profile profileOpt = Profile.getProfileOpt(name);
			// FOLLOWING ONLY WITH ENABLED PROFILE
			if (profileOpt != null && profileOpt.isProfileEnabled()) {
				if (profileOpt.getFollowedBlogs().contains(this.name.getName())) {
					results.add(profileOpt.getName());
				}

			}
		}
		followerCache = results;
		return results;
	}

	public static List<Profile> getEnabledProfiles() {
		List<Name> namesAsList = Controller.getInstance().getNamesAsList();
		List<Profile> results = new ArrayList<Profile>();
		for (Name name : namesAsList) {
			Profile profile = Profile.getProfileOpt(name);
			if (profile != null && profile.isProfileEnabled()) {
				results.add(profile);
			}
		}

		return results;
	}

	public String getBlogDescriptionOpt() {
		return (String) jsonRepresenation.get(Qorakeys.BLOGDESCRIPTION
				.toString());
	}

	public void saveBlogDescription(String blogDescription) {
		storeKeyValueIfNotBlank(Qorakeys.BLOGDESCRIPTION, blogDescription);
	}

	public void storeKeyValueIfNotBlank(Qorakeys key, String value) {
		if (!StringUtils.isBlank(value)) {
			jsonRepresenation.put(key.toString(), value);
		} else {
			jsonRepresenation.remove(key.toString());
		}
	}

	public void saveBlogTitle(String blogTitle) {
		storeKeyValueIfNotBlank(Qorakeys.BLOGTITLE, blogTitle);
	}

	public void saveAvatarTitle(String profileavatar) {
		storeKeyValueIfNotBlank(Qorakeys.PROFILEAVATAR, profileavatar);
	}

	public void saveProfileMainGraphicOpt(String maingraphicurl) {
		storeKeyValueIfNotBlank(Qorakeys.PROFILEMAINGRAPHIC, maingraphicurl);
	}

	public String getBlogTitleOpt() {
		return (String) jsonRepresenation.get(Qorakeys.BLOGTITLE.toString());
	}

	public String getAvatarOpt() {
		return (String) jsonRepresenation
				.get(Qorakeys.PROFILEAVATAR.toString());
	}

	public String getProfileGraphicOpt() {
		String graphiccontent = (String) jsonRepresenation
				.get(Qorakeys.PROFILEMAINGRAPHIC.toString());

		return graphiccontent;
	}

	public List<String> getFollowedBlogs() {
		return Collections.unmodifiableList(getFollowedBlogsInternal());
	}

	public List<String> getLikedPosts() {
		return Collections.unmodifiableList(getLikedPostsInternal());
	}

	private List<String> getFollowedBlogsInternal() {
		String profileFollowString = (String) jsonRepresenation
				.get(Qorakeys.PROFILEFOLLOW.toString());
		if (profileFollowString != null) {
			String[] profileFollowArray = StringUtils.split(
					profileFollowString, ";");
			return new ArrayList<String>(Arrays.asList(profileFollowArray));
		}

		return new ArrayList<String>();
	}

	private List<String> getLikedPostsInternal() {
		String profileLikeString = (String) jsonRepresenation
				.get(Qorakeys.PROFILELIKEPOSTS.toString());
		if (profileLikeString != null) {
			String[] profileLikeArray = StringUtils.split(profileLikeString,
					";");
			return new ArrayList<String>(Arrays.asList(profileLikeArray));
		}

		return new ArrayList<String>();
	}

	public void addFollowedBlog(String blogname) {
		addRemoveFollowedInternal(blogname, false);
	}

	public void removeFollowedBlog(String blogname) {
		addRemoveFollowedInternal(blogname, true);
	}

	public void addRemoveFollowedInternal(String blogname, boolean isRemove) {
		Name blogName = DBSet.getInstance().getNameMap().get(blogname);
		if (blogName != null) {
			Profile profile = Profile.getProfileOpt(blogName);
			// ADDING ONLY IF ENABLED REMOVE ALWAYS
			if (isRemove
					|| (profile != null && profile.isProfileEnabled() && profile
							.isBlogEnabled())) {
				List<String> followedBlogsInternal = getFollowedBlogsInternal();
				if (isRemove) {
					followedBlogsInternal.remove(blogname);
				} else {
					if (!followedBlogsInternal.contains(blogname)) {
						followedBlogsInternal.add(blogname);
					}
				}
				String joinResult = StringUtils
						.join(followedBlogsInternal, ";");
				jsonRepresenation.put(Qorakeys.PROFILEFOLLOW.toString(),
						joinResult);
			}
		}
	}

	public void addLikePost(String signature) {
		addRemoveLikeInternal(signature, false);
	}

	public void removeLikeProfile(String signature) {
		addRemoveLikeInternal(signature, true);
	}

	public void addRemoveLikeInternal(String signature, boolean isRemove) {
		// ADDING ONLY IF ENABLED REMOVE ALWAYS

		List<String> likedPostsInternal = getLikedPostsInternal();
		if (isRemove) {
			likedPostsInternal.remove(signature);
		} else {
			if (!likedPostsInternal.contains(signature)) {
				likedPostsInternal.add(signature);
			}
		}
		String joinResult = StringUtils.join(likedPostsInternal, ";");
		jsonRepresenation.put(Qorakeys.PROFILELIKEPOSTS.toString(), joinResult);
	}

	public boolean isProfileEnabled() {
		return jsonRepresenation.containsKey(Qorakeys.PROFILEENABLE.toString());
	}

	public void setProfileEnabled(boolean enabled) {
		if (enabled) {
			jsonRepresenation.put(Qorakeys.PROFILEENABLE.toString(), "");
		} else {
			jsonRepresenation.remove(Qorakeys.PROFILEENABLE.toString());
		}
	}

	public void setBlogEnabled(boolean enabled) {
		if (enabled) {
			jsonRepresenation.put(Qorakeys.BLOGENABLE.toString(), "");
		} else {
			jsonRepresenation.remove(Qorakeys.BLOGENABLE.toString());
		}
	}
	
	public void setBlockComments(boolean blockComments)
	{
		if (blockComments) {
			jsonRepresenation.put(Qorakeys.BLOGBLOCKCOMMENTS.toString(), "");
		} else {
			jsonRepresenation.remove(Qorakeys.BLOGBLOCKCOMMENTS.toString());
		}
	}

	public boolean isBlogEnabled() {
		return jsonRepresenation.containsKey(Qorakeys.BLOGENABLE.toString());
	}
	
	public boolean isCommentingAllowed(){
		return !isCommentingDisabled();
	}
	
	public boolean isCommentingDisabled(){
		return jsonRepresenation.containsKey(Qorakeys.BLOGBLOCKCOMMENTS.toString());
	}

	public BlogBlackWhiteList getBlogBlackWhiteList() {
		return blogBlackWhiteList;
	}

	public String saveProfile() throws WebApplicationException {

		JSONObject oldProfileJson = ProfileUtils.getProfile(name.getName());
		JSONObject oldBWListJson = ProfileUtils.getBlogBlackWhiteList(name
				.getName());

		Set<String> keySet = oldBWListJson.keySet();
		// COMBINING BOTH FOR COMPARISON
		for (String key : keySet) {
			oldProfileJson.put(key, oldBWListJson.get(key));
		}

		List<Pair<String, String>> addCompleteKeys = new ArrayList<>();
		List<String> removeCompleteKeys = new ArrayList<>();
		List<Pair<String, String>> addListKeys = new ArrayList<>();
		List<Pair<String, String>> removeListKeys = new ArrayList<>();

		// Combining actual values
		Pair<String, String> jsonKeyPairRepresentation = blogBlackWhiteList
				.getJsonKeyPairRepresentation();
		jsonRepresenation.put(jsonKeyPairRepresentation.getA(),
				jsonKeyPairRepresentation.getB());
		
		

		if (blogBlackWhiteList.getBlackwhiteList().isEmpty()) {
			StringUtils.isBlank((CharSequence) oldBWListJson
					.get(Qorakeys.BLOGWHITELIST.toString()));
		}

		List<Qorakeys> profileKeys = Arrays.asList(Qorakeys.BLOGBLACKLIST,
				Qorakeys.BLOGWHITELIST, Qorakeys.BLOGDESCRIPTION,
				Qorakeys.BLOGENABLE, Qorakeys.PROFILEAVATAR,
				Qorakeys.BLOGTITLE, Qorakeys.PROFILEENABLE,
				Qorakeys.PROFILEFOLLOW, Qorakeys.PROFILELIKEPOSTS,
				Qorakeys.PROFILEMAINGRAPHIC, Qorakeys.BLOGBLOCKCOMMENTS);

		for (Qorakeys qorakey : profileKeys) {

			String key = qorakey.toString();
			String newValueOpt = (String) jsonRepresenation.get(key);
			String oldValueOpt = (String) oldProfileJson.get(key);

			if (qorakey.getVariation() == KeyVariation.EXISTSKEY) {

				if (oldValueOpt == null && newValueOpt == null) {
					continue;
				}

				// NEW KEY ADDED
				if (oldValueOpt == null && newValueOpt != null) {
					addCompleteKeys.add(new Pair<String, String>(key, "yes"));
				} else if (oldValueOpt != null && newValueOpt == null) {
					removeCompleteKeys.add(key);
				}

				continue;
			}

			if (qorakey.getVariation() == KeyVariation.DEFAULTKEY) {

				if (StringUtils.isBlank(oldValueOpt)
						&& StringUtils.isBlank(newValueOpt)) {
					continue;
				}

				// NEW KEY ADDED
				if (oldValueOpt == null && newValueOpt != null) {
					addCompleteKeys.add(new Pair<String, String>(key,
							newValueOpt));
				} else if (oldValueOpt != null && newValueOpt == null) {
					removeCompleteKeys.add(key);
				} else {
					// value was there but is it equal?
					if (!oldValueOpt.equals(newValueOpt)) {
						addCompleteKeys.add(new Pair<String, String>(key,
								newValueOpt));
					}
				}

				continue;
			}

			if (qorakey.getVariation() == KeyVariation.LISTKEY) {
				if (StringUtils.isBlank(oldValueOpt)
						&& StringUtils.isBlank(newValueOpt)) {
					continue;
				}
				// NEW KEY ADDED
				if (StringUtils.isBlank(oldValueOpt)
						&& StringUtils.isNotBlank(newValueOpt)) {
					addCompleteKeys.add(new Pair<String, String>(key,
							newValueOpt));
				} else if (StringUtils.isNotBlank(oldValueOpt)
						&& StringUtils.isBlank(newValueOpt)) {
						removeCompleteKeys.add(key);
				} else {

					// value was there but is it equal?
					if (!oldValueOpt.equals(newValueOpt)) {
						List<String> oldValues = new ArrayList<String>(
								Arrays.asList(oldValueOpt.split(";")));
						List<String> newValues = new ArrayList<String>(
								Arrays.asList(newValueOpt.split(";")));

						List<String> copyNewValues = new ArrayList<String>(
								newValues);
						copyNewValues.removeAll(oldValues);

						oldValues.removeAll(newValues);

						if (copyNewValues.size() > 0) {
							addListKeys.add(new Pair<String, String>(key,
									StringUtils.join(copyNewValues, ";")));
						}
						if (oldValues.size() > 0) {
							removeListKeys.add(new Pair<String, String>(key,
									StringUtils.join(oldValues, ";")));
						}

					}
				}

				continue;
			}

		}
		
		
		
		BlogBlackWhiteList oldBlackWhiteList = Profile.getProfileOpt(name
				.getName()).getBlogBlackWhiteList();

		
		//BECAUSE THE BLACK AND WHITELIST EXCLUDE THEMSELVES WE HAVE TO PROCESS THESE EXTRA RULES TO MAKE SURE THE CONCEPT FITS.
		if (blogBlackWhiteList.isWhitelist()) {
			jsonRepresenation.remove(Qorakeys.BLOGBLACKLIST.toString());
			
			//switching kind of list from empty to empty!
			if(blogBlackWhiteList.getBlackwhiteList().isEmpty() && oldBlackWhiteList.isBlacklist() && oldBlackWhiteList.getBlackwhiteList().isEmpty())
			{
				removeCompleteKeys.add(
						Qorakeys.BLOGBLACKLIST.toString());
				addCompleteKeys.add(new Pair<String, String>(
						Qorakeys.BLOGWHITELIST.toString(), ""));
			}
		} else {
			jsonRepresenation.remove(Qorakeys.BLOGWHITELIST.toString());
			//switching kind of list from empty to empty!
			if (blogBlackWhiteList.getBlackwhiteList().isEmpty()
					&& oldBlackWhiteList.isWhitelist() && oldBlackWhiteList.getBlackwhiteList().isEmpty()) {
				addCompleteKeys.add(new Pair<String, String>(
						Qorakeys.BLOGBLACKLIST.toString(), ""));
				removeCompleteKeys.add(
						Qorakeys.BLOGWHITELIST.toString());
			}else if(blogBlackWhiteList.getBlackwhiteList().isEmpty() && oldBlackWhiteList.isBlacklist() && !oldBlackWhiteList.getBlackwhiteList().isEmpty())
			{
				addCompleteKeys.add(new Pair<String, String>(
						Qorakeys.BLOGBLACKLIST.toString(), ""));
				removeCompleteKeys.add(
						Qorakeys.BLOGWHITELIST.toString());
				
				removeCompleteKeys.remove(Qorakeys.BLOGBLACKLIST.toString());
			}
		}

		JSONObject jsonResult = StorageUtils.getStorageJsonObject(
				addCompleteKeys, removeCompleteKeys, addListKeys,
				removeListKeys, null, null);

		return new NameStorageResource().updateEntry(jsonResult.toJSONString(),
				name.getName());

		// String jsonString = jsonRepresenation.toJSONString();
		// String compressValue = GZIP.compress(jsonString);
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("fee", Controller.getInstance()
		// .calcRecommendedFeeForNameUpdate(name.getName(), compressValue)
		// .getA().toPlainString());
		// jsonObject.put("newowner", name.getOwner().getAddress());
		// jsonObject.put("newvalue", compressValue);
		//
		// return new NamesResource().updateName(jsonObject.toJSONString(),
		// name.getName());
	}

	public Name getName() {
		return name;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
