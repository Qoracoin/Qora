package api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import database.DBSet;
import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import qora.web.blog.BlogEntry;
import utils.APIUtils;
import utils.BlogUtils;
import utils.Pair;
import utils.Qorakeys;

@Path("blogpost")
@Produces(MediaType.APPLICATION_JSON)
public class BlogPostResource {

	public static final String AUTHOR = "author";
	public static final String BLOGNAME_KEY = "blogname";
	public static final String TITLE_KEY = "title";
	public static final String SHARE_KEY = "share";
	public static final String DELETE_KEY = "delete";
	public static final String POST_KEY = "post";
	//THIS IS ONLY NEEDED FOR COMMENTS -> id of the post to comment!
	public static final String COMMENT_POSTID_KEY = "postid";
	
	


	@Context
	HttpServletRequest request;
	
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/comment")
	public String commentBlogEntry(String x) {
		try {
			
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String creator = (String) jsonObject.get("creator");
			String authorOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
			String title = (String) jsonObject.get("title");
			String body = (String) jsonObject.get("body");
			//this is the post we are commenting
			String postid = (String) jsonObject.get("postid");

			if (StringUtil.isBlank(body)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BODY_EMPTY);
			}
			
			if (StringUtil.isBlank(postid)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_POSTID_EMPTY);
			}
			
			BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(postid);
			
			if(blogEntryOpt == null)
			{
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_POST_NOT_EXISTING);
			}
			
			String blognameOpt = blogEntryOpt.getBlognameOpt();
			

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
			if (!Crypto.getInstance().isValidAddress(creator)) {
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			// TODO CHECK IF ALLOWED TO COMMENT - THIS NEEDS AN EXTRA LIST, MAYBE ONLY OWNER CAN POST BUT EVERYONE IS ALLOWED TO COMMENT!!!
//			isPostAllowed(blognameOpt);

			APIUtils.askAPICallAllowed("POST blogpost/comment" + "\n" +  x,
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

			if(authorOpt != null)
			{
			  Name	name = DBSet.getInstance().getNameMap().get(authorOpt);
				
			  	//Name is not owned by creator!
				if(name == null || !name.getOwner().getAddress().equals(creator))
				{
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_NAME_NOT_OWNER);
				}
				
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

			JSONObject dataStructure = new JSONObject();

			dataStructure.put(TITLE_KEY, title);
			dataStructure.put(POST_KEY, body);
			dataStructure.put(COMMENT_POSTID_KEY, postid);
			

			if (blognameOpt != null) {
				dataStructure.put(BLOGNAME_KEY, blognameOpt);
			}

			if (authorOpt != null) {
				dataStructure.put(AUTHOR, authorOpt);
			}

			// SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance()
					.createArbitraryTransaction(account, null, BlogUtils.COMMENT_SERVICE_ID,
							dataStructure.toJSONString().getBytes(StandardCharsets.UTF_8), bdFee);

			return ArbitraryTransactionsResource
					.checkArbitraryTransaction(result);

		} catch (NullPointerException | ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/{blogname}")
	public String addBlogEntry(String x, @PathParam("blogname") String blogname) {
		try {

			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String creator = (String) jsonObject.get("creator");
			String authorOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
			String title = (String) jsonObject.get("title");
			String body = (String) jsonObject.get("body");
			String share = (String) jsonObject.get(BlogPostResource.SHARE_KEY);
			String delete = (String) jsonObject.get(BlogPostResource.DELETE_KEY);

			if (StringUtil.isBlank(body)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BODY_EMPTY);
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

			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(creator)) {
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			isPostAllowed(blogname);

			APIUtils.askAPICallAllowed("POST blogpost/" + blogname + "\n" + x,
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

			if(authorOpt != null)
			{
			  Name	name = DBSet.getInstance().getNameMap().get(authorOpt);
				
			  	//Name is not owned by creator!
				if(name == null || !name.getOwner().getAddress().equals(creator))
				{
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_NAME_NOT_OWNER);
				}
				
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

			JSONObject dataStructure = new JSONObject();

			dataStructure.put(TITLE_KEY, title);
			dataStructure.put(POST_KEY, body);
			if(StringUtils.isNotBlank(share))
			{
				dataStructure.put(BlogPostResource.SHARE_KEY, share);
			}
			
			//TODO add delete logic including errors here!
			if(StringUtils.isNotBlank(delete))
			{
				dataStructure.put(BlogPostResource.DELETE_KEY, delete);
			}

			if (blogname != null) {
				dataStructure.put(BLOGNAME_KEY, blogname);
			}

			if (authorOpt != null) {
				dataStructure.put(AUTHOR, authorOpt);
			}

			// SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance()
					.createArbitraryTransaction(account, null, 777,
							dataStructure.toJSONString().getBytes(StandardCharsets.UTF_8), bdFee);

			return ArbitraryTransactionsResource
					.checkArbitraryTransaction(result);

		} catch (NullPointerException | ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}
	}

	static void isPostAllowed(String blogname) {
		
		//MAINBLOG allows posting always
		if(blogname == null)
		{
			return;
		}
		
		String blogenable = DBSet.getInstance().getNameStorageMap().getOpt(blogname, Qorakeys.BLOGENABLE.toString());
		
		if(blogenable == null)
		{

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_BLOG_DISABLED);
		}
		

	}

}
