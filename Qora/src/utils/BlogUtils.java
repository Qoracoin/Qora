package utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;
import com.twitter.Extractor;

import api.BlogPostResource;
import controller.Controller;
import database.CommentPostMap;
import database.DBSet;
import qora.crypto.Base58;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.web.BlogBlackWhiteList;
import qora.web.BlogProfile;
import qora.web.NameStorageMap;
import qora.web.Profile;
import qora.web.blog.BlogEntry;

public class BlogUtils {

	public static int COMMENT_SERVICE_ID = 778;

	/**
	 * 
	 * @return triplet of name, title, description of all enabled blogs.
	 */
	public static List<BlogProfile> getEnabledBlogs(String searchvalueOpt) {

		NameStorageMap nameMap = DBSet.getInstance().getNameStorageMap();
		Set<String> names = nameMap.getKeys();

		Map<String, List<String>> followMap = new HashMap<>();
		List<Profile> resultProfiles = new ArrayList<>();

		for (String name : names) {

			Profile profile = Profile.getProfileOpt(name);

			if (profile != null && profile.isProfileEnabled()) {
				List<String> followedBlogs = profile.getFollowedBlogs();
				if (followedBlogs != null) {
					List<String> alreadyProcessed = new ArrayList<String>();
					for (String followedBlog : followedBlogs) {
						if (!alreadyProcessed.contains(followedBlog)
								&& !name.equals(followedBlog)) {
							alreadyProcessed.add(followedBlog);
							if (followMap.containsKey(followedBlog)) {
								List<String> followerList = followMap
										.get(followedBlog);
								if (!followerList.contains(name)) {
									followerList.add(name);
								}
								followMap.put(followedBlog, followerList);
							} else {
								List<String> followerList = new ArrayList<>();
								followerList.add(name);
								followMap.put(followedBlog, followerList);
							}
						}
					}
				}

				if (profile.isBlogEnabled()) {

					String title = profile.getBlogTitleOpt();
					String description = profile.getBlogDescriptionOpt();
					if (searchvalueOpt != null) {
						searchvalueOpt = searchvalueOpt.toLowerCase();
						if (name.toLowerCase().contains(searchvalueOpt)
								|| (title != null && title.toLowerCase()
										.contains(searchvalueOpt))
								|| (description != null)
								&& description.toLowerCase().contains(
										searchvalueOpt)) {
							resultProfiles.add(profile);
						}
						continue;
					}
					resultProfiles.add(profile);
				}
			}

		}

		List<BlogProfile> blogprofiles = new ArrayList<>();
		for (Profile profileWithBlog : resultProfiles) {

			String name = profileWithBlog.getName().getName();
			if (followMap.containsKey(name)) {
				blogprofiles.add(new BlogProfile(profileWithBlog, followMap
						.get(name)));
			} else {
				blogprofiles.add(new BlogProfile(profileWithBlog,
						new ArrayList<String>()));
			}

		}

		Collections.sort(blogprofiles);

		return blogprofiles;
	}

	public static List<BlogEntry> getBlogPosts(List<String> blogList) {
		List<BlogEntry> blogPosts = new ArrayList<BlogEntry>();
		for (String blogname : blogList) {
			blogPosts.addAll(getBlogPosts(blogname));
		}
		Collections.sort(blogPosts, new BlogEntryTimestampComparator());

		Collections.reverse(blogPosts);

		return blogPosts;
	}

	public static List<BlogEntry> getHashTagPosts(String hashtag) {
		List<BlogEntry> results = new ArrayList<BlogEntry>();
		List<byte[]> list = DBSet.getInstance().getHashtagPostMap()
				.get(hashtag);

		if (list != null) {
			for (byte[] bs : list) {
				BlogEntry blogEntryOpt = getBlogEntryOpt(bs);
				if (blogEntryOpt != null) {
					results.add(blogEntryOpt);
				}
			}
		}

		Collections.sort(results, new BlogEntryTimestampComparator());

		Collections.reverse(results);

		return results;
	}

	public static List<String> getHashTags(String text) {
		List<String> extractHashtags = new Extractor().extractHashtags(text);
		List<String> result = new ArrayList<String>();
		for (String hashTag : extractHashtags) {
			result.add("#" + hashTag);
		}
		return result;
	}

	public static List<String> getBlogTags(String text) {
		List<String> extractScreenNames = new Extractor()
				.extractMentionedScreennames(text);
		List<String> result = new ArrayList<String>();
		for (String screenNames : extractScreenNames) {
			result.add("@" + screenNames);
		}
		return result;
	}

	public static List<BlogEntry> getBlogPosts(String blogOpt) {
		return getBlogPosts(blogOpt, -1);
	}

	public static List<BlogEntry> getCommentBlogPosts(String signatureOfBlogPost) {
		return getCommentBlogPosts(signatureOfBlogPost, -1);
	}

	public static List<BlogEntry> getCommentBlogPosts(
			String signatureOfBlogPost, int limit) {
		List<BlogEntry> results = new ArrayList<>();

		CommentPostMap commentPostMap = DBSet.getInstance().getCommentPostMap();

		List<byte[]> list = commentPostMap.get(Base58
				.decode(signatureOfBlogPost));

		Collections.reverse(list);

		List<ArbitraryTransaction> blogPostTX = new ArrayList<>();
		if (list != null) {
			for (byte[] blogArbTx : list) {
				Transaction transaction = Controller.getInstance()
						.getTransaction(blogArbTx);
				if (transaction != null) {
					blogPostTX.add((ArbitraryTransaction) transaction);
				}
			}
		}

		int i = 0;

		for (ArbitraryTransaction transaction : blogPostTX) {

			// String creator = transaction.getCreator().getAddress();

			// TODO ARE COMMENTS ALLOWED CHECK!
			// BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
			// .getBlogBlackWhiteList(blogOpt);

			BlogEntry blogEntry = getCommentBlogEntryOpt(transaction);

			// String nameOpt = blogEntry.getNameOpt();
			if (blogEntry != null) {
				results.add(blogEntry);
				i++;
			}
			// if (blogBlackWhiteList.isAllowedPost(
			// nameOpt != null ? nameOpt : creator, creator)) {
			// results.add(blogEntry);
			// i ++;
			// }

			if (i == limit)
				break;
		}

		return results;

	}

	public static List<BlogEntry> getBlogPosts(String blogOpt, int limit) {
		List<BlogEntry> results = new ArrayList<>();

		List<byte[]> blogPostList = DBSet.getInstance().getBlogPostMap()
				.get(blogOpt == null ? "QORA" : blogOpt);

		List<byte[]> list = blogPostList != null ? Lists
				.newArrayList(blogPostList) : new ArrayList<byte[]>();

		Collections.reverse(list);

		List<ArbitraryTransaction> blogPostTX = new ArrayList<>();
		if (list != null) {
			for (byte[] blogArbTx : list) {
				Transaction transaction = Controller.getInstance()
						.getTransaction(blogArbTx);
				if (transaction != null) {
					blogPostTX.add((ArbitraryTransaction) transaction);
				}
			}
		}

		int i = 0;

		for (ArbitraryTransaction transaction : blogPostTX) {

			String creator = transaction.getCreator().getAddress();

			BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
					.getBlogBlackWhiteList(blogOpt);

			BlogEntry blogEntry = getBlogEntryOpt(transaction);

			String nameOpt;
			if (blogEntry.getShareAuthorOpt() != null)
				nameOpt = blogEntry.getShareAuthorOpt();
			else
				nameOpt = blogEntry.getNameOpt();

			if (blogBlackWhiteList.isAllowedPost(nameOpt != null ? nameOpt
					: creator, creator)) {
				results.add(blogEntry);
				i++;
			}

			if (i == limit)
				break;
		}

		return results;

	}

	public static void addCommentsToBlogEntry(ArbitraryTransaction transaction,
			BlogEntry blogEntry) {
		CommentPostMap commentPostMap = DBSet.getInstance().getCommentPostMap();
		List<byte[]> comments = commentPostMap.get(transaction.getSignature());
		if(comments != null)
		{
			for (byte[] commentByteArray : comments) {
				Transaction commentTa = Controller.getInstance()
						.getTransaction(commentByteArray);
				if (commentTa != null) {
					BlogEntry commentBlogEntryOpt = getCommentBlogEntryOpt((ArbitraryTransaction) commentTa);
					if(commentBlogEntryOpt != null)
					{
						blogEntry.addComment(commentBlogEntryOpt);
					}
				}
			}
		}
	}

	public static BlogEntry getBlogEntryOpt(String signature) {
		return getBlogEntryOpt(Base58.decode(signature));
	}

	public static BlogEntry getBlogEntryOpt(byte[] signature) {
		ArbitraryTransaction transaction = null;
		try {
			transaction = (ArbitraryTransaction) Controller.getInstance()
					.getTransaction(signature);
		} catch (Exception e) {
			System.err.println(ExceptionUtils.getStackTrace(e));
			return null;
		}
		return transaction == null ? null : BlogUtils
				.getBlogEntryOpt(transaction);
	}

	public static BlogEntry getCommentBlogEntryOpt(
			ArbitraryTransaction transaction) {
		if (transaction.getService() != COMMENT_SERVICE_ID) {
			return null;
		}

		byte[] data = ((ArbitraryTransaction) transaction).getData();
		String string = new String(data, StandardCharsets.UTF_8);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
		if (jsonObject != null) {
			// MAINBLOG OR CUSTOM BLOG?

			String title = (String) jsonObject.get(BlogPostResource.TITLE_KEY);
			String post = (String) jsonObject.get(BlogPostResource.POST_KEY);
			String nameOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
			String blognameOpt = (String) jsonObject
					.get(BlogPostResource.BLOGNAME_KEY);
			String postID = (String) jsonObject
					.get(BlogPostResource.COMMENT_POSTID_KEY);

			String creator = transaction.getCreator().getAddress();

			if (StringUtil.isNotBlank(post) && StringUtil.isNotBlank(postID)) {
				BlogEntry be = new BlogEntry(title, post, nameOpt,
						transaction.getTimestamp(), creator,
						Base58.encode(transaction.getSignature()), blognameOpt);
				be.setCommentPostidOpt(postID);
				return be;
			}
		}

		return null;

	}

	/**
	 * returns blogentry without any restrictions
	 * 
	 * @param transaction
	 * @return
	 */
	// TODO MAYBE JOIN WITH SHARE SO THAT THIS ALSO CONTAINS SHAREDPOSTS!
	public static BlogEntry getBlogEntryOpt(ArbitraryTransaction transaction) {
		if (transaction.getService() != 777) {
			return null;
		}
		byte[] data = ((ArbitraryTransaction) transaction).getData();
		String string = new String(data, StandardCharsets.UTF_8);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
		if (jsonObject != null) {
			// MAINBLOG OR CUSTOM BLOG?

			String title = (String) jsonObject.get(BlogPostResource.TITLE_KEY);
			String post = (String) jsonObject.get(BlogPostResource.POST_KEY);
			String nameOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
			String blognameOpt = (String) jsonObject
					.get(BlogPostResource.BLOGNAME_KEY);
			String share = (String) jsonObject.get(BlogPostResource.SHARE_KEY);

			String creator = transaction.getCreator().getAddress();

			if (StringUtils.isNotEmpty(share)) {
				BlogEntry blogEntryToShareOpt = BlogUtils
						.getBlogEntryOpt((ArbitraryTransaction) Controller
								.getInstance().getTransaction(
										Base58.decode(share)));
				if (blogEntryToShareOpt != null
						&& StringUtils.isNotBlank(blogEntryToShareOpt
								.getDescription())) {
					// share gets time of sharing!
					blogEntryToShareOpt.setTime(transaction.getTimestamp());
					blogEntryToShareOpt
							.setShareAuthor(nameOpt != null ? nameOpt : creator);
					blogEntryToShareOpt.setShareSignatureOpt(Base58
							.encode(transaction.getSignature()));
					addCommentsToBlogEntry(transaction, blogEntryToShareOpt);
					return blogEntryToShareOpt;
				}
			}
			
			// POST NEEDS TO BE FILLED
			if (StringUtil.isNotBlank(post)) {
				BlogEntry resultBlogEntry = new BlogEntry(title, post, nameOpt,
						transaction.getTimestamp(), creator,
						Base58.encode(transaction.getSignature()), blognameOpt);
				addCommentsToBlogEntry(transaction, resultBlogEntry);
				return resultBlogEntry;
			}
		}

		return null;
	}

}
