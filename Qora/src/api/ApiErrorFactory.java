package api;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import at.AT_Constants;
import at.AT_Error;
import qora.transaction.Transaction;
import utils.Qorakeys;
import utils.NameUtils.NameResult;

public class ApiErrorFactory 
{
	//COMMON
	public static final int ERROR_UNKNOWN = 0;
	public static final int ERROR_JSON = 1;
	public static final int ERROR_NO_BALANCE = 2;
	public static final int ERROR_NOT_YET_RELEASED = 3;
	
	//VALIDATION
	public static final int ERROR_INVALID_SIGNATURE = 101;
	public static final int ERROR_INVALID_ADDRESS = 102;
	public static final int ERROR_INVALID_SEED = 103;
	public static final int ERROR_INVALID_AMOUNT = 104;
	public static final int ERROR_INVALID_FEE = 105;
	public static final int ERROR_INVALID_SENDER = 106;
	public static final int ERROR_INVALID_RECIPIENT = 107;
	public static final int ERROR_INVALID_NAME_LENGTH = 108;
	public static final int ERROR_INVALID_VALUE_LENGTH = 109;
	public static final int ERROR_INVALID_NAME_OWNER = 110;
	public static final int ERROR_INVALID_BUYER = 111;
	public static final int ERROR_INVALID_PUBLIC_KEY = 112;
	public static final int ERROR_INVALID_OPTIONS_LENGTH = 113;
	public static final int ERROR_INVALID_OPTION_LENGTH = 114;
	public static final int ERROR_INVALID_DATA = 115;
	public static final int ERROR_INVALID_DATA_LENGTH = 116;
	public static final int ERROR_INVALID_UPDATE_VALUE = 117;
	public static final int ERROR_KEY_ALREADY_EXISTS = 118;
	public static final int ERROR_KEY_NOT_EXISTS = 119;
	public static final int ERROR_LAST_KEY_IS_DEFAULT_KEY_ERROR = 120;
	public static final int ERROR_FEE_LESS_REQUIRED = 121;
	public static final int ERROR_WALLET_NOT_IN_SYNC = 122;
	public static final int ERROR_INVALID_NETWORK_ADDRESS = 123;
	
	//WALLET
	public static final int ERROR_WALLET_NO_EXISTS = 201;
	public static final int ERROR_WALLET_ADDRESS_NO_EXISTS = 202;
	public static final int ERROR_WALLET_LOCKED = 203;
	public static final int ERROR_WALLET_ALREADY_EXISTS = 204;
	public static final int ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER = 205;
	
	//BLOCKS
	public static final int ERROR_BLOCK_NO_EXISTS = 301;
	
	//TRANSACTIONS
	public static final int ERROR_TRANSACTION_NO_EXISTS = 311;
	public static final int ERROR_PUBLIC_KEY_NOT_FOUND = 304;	
		
	//NAMING
	public static final int ERROR_NAME_NO_EXISTS = 401;
	public static final int ERROR_NAME_ALREADY_EXISTS = 402;
	public static final int ERROR_NAME_ALREADY_FOR_SALE = 403;
	public static final int ERROR_NAME_NOT_LOWER_CASE = 404;
	public static final int ERROR_NAME_SALE_NO_EXISTS = 410;
	public static final int ERROR_BUYER_ALREADY_OWNER = 411;
	
	//POLLS
	public static final int ERROR_POLL_NO_EXISTS = 501;
	public static final int ERROR_POLL_ALREADY_EXISTS = 502;
	public static final int ERROR_DUPLICATE_OPTION = 503;
	public static final int ERROR_POLL_OPTION_NO_EXISTS = 504;
	public static final int ERROR_ALREADY_VOTED_FOR_THAT_OPTION = 505;
	
	//ASSET
	public static final int ERROR_INVALID_ASSET_ID = 601;
	
	//NAME PAYMENTS
	public static final int ERROR_NAME_NOT_REGISTERED = 701;
	public static final int ERROR_NAME_FOR_SALE = 702;
	public static final int ERROR_NAME_WITH_SPACE = 703;
	
	//ATs
	public static final int ERROR_INVALID_DESC_LENGTH = 801;
	public static final int ERROR_EMPTY_CODE = 802;
	public static final int ERROR_DATA_SIZE = 803;
	public static final int ERROR_NULL_PAGES = 804;
	public static final int ERROR_INVALID_TYPE_LENGTH = 805;
	public static final int ERROR_INVALID_TAGS_LENGTH = 806;
	public static final int ERROR_INVALID_CREATION_BYTES = 809;
	
	//BLOG/Namestorage
	public static final int ERROR_BODY_EMPTY = 901;
	public static final int ERROR_BLOG_DISABLED = 902;
	public static final int ERROR_NAME_NOT_OWNER = 903;
	public static final int ERROR_TX_AMOUNT = 904;
	public static final int ERROR_BLOG_ENTRY_NO_EXISTS = 905;
	public static final int ERROR_BLOG_EMPTY = 906;
	public static final int ERROR_POSTID_EMPTY = 907;
	public static final int ERROR_POST_NOT_EXISTING = 908;
	public static final int ERROR_COMMENTING_DISABLED = 909;
	public static final int ERROR_COMMENT_NOT_EXISTING = 910;
	public static final int ERROR_INVALID_COMMENT_OWNER = 911;
	
	//Messages
	public static final int ERROR_MESSAGE_FORMAT_NOT_HEX = 1001;
	public static final int ERROR_MESSAGE_BLANK = 1002;
	public static final int ERROR_NO_PUBLIC_KEY = 1003;
	public static final int ERROR_MESSAGESIZE_EXCEEDED = 1004;

	
	public static int BATCH_TX_AMOUNT = 50;
	
	
	private static ApiErrorFactory  instance;
	
	public static ApiErrorFactory  getInstance()
	{
		if(instance == null)
		{
			instance = new ApiErrorFactory();
		}
		
		return instance;
	}
	
	private Map<Integer, String> errorMessages;
	
	public ApiErrorFactory()
	{
		this.errorMessages = new HashMap<Integer, String>();
		
		//COMMON
		this.errorMessages.put(ERROR_UNKNOWN, "unknown error");
		this.errorMessages.put(ERROR_JSON, "failed to parse json message");
		this.errorMessages.put(ERROR_NO_BALANCE, "not enough balance");
		this.errorMessages.put(ERROR_NOT_YET_RELEASED, "that feature is not yet released");
		
		//VALIDATION		
		this.errorMessages.put(ERROR_INVALID_SIGNATURE, "invalid signature");
		this.errorMessages.put(ERROR_INVALID_ADDRESS, "invalid address");
		this.errorMessages.put(ERROR_INVALID_SEED, "invalid seed");
		this.errorMessages.put(ERROR_INVALID_AMOUNT, "invalid amount");
		this.errorMessages.put(ERROR_INVALID_FEE, "invalid fee");
		this.errorMessages.put(ERROR_INVALID_SENDER, "invalid sender");
		this.errorMessages.put(ERROR_INVALID_RECIPIENT, "invalid recipient");
		this.errorMessages.put(ERROR_INVALID_NAME_LENGTH, "invalid name length");
		this.errorMessages.put(ERROR_INVALID_VALUE_LENGTH, "invalid value length");
		this.errorMessages.put(ERROR_INVALID_NAME_OWNER, "invalid name owner");
		this.errorMessages.put(ERROR_INVALID_BUYER, "invalid buyer");
		this.errorMessages.put(ERROR_INVALID_PUBLIC_KEY, "invalid public key");
		this.errorMessages.put(ERROR_INVALID_OPTIONS_LENGTH, "invalid options length");
		this.errorMessages.put(ERROR_INVALID_OPTION_LENGTH, "invalid option length");
		this.errorMessages.put(ERROR_INVALID_DATA, "invalid data");
		this.errorMessages.put(ERROR_INVALID_DATA_LENGTH, "invalid data length");
		this.errorMessages.put(ERROR_INVALID_UPDATE_VALUE, "invalid update value");
		this.errorMessages.put(ERROR_KEY_ALREADY_EXISTS, "key already exists, edit is false");
		this.errorMessages.put(ERROR_KEY_NOT_EXISTS, "the key does not exist");
		this.errorMessages.put(ERROR_LAST_KEY_IS_DEFAULT_KEY_ERROR, "you can't delete the key \""+Qorakeys.DEFAULT.toString() +"\" if it is the only key");
		this.errorMessages.put(ERROR_FEE_LESS_REQUIRED, "fee less required");
		this.errorMessages.put(ERROR_WALLET_NOT_IN_SYNC, "wallet needs to be synchronized");
		this.errorMessages.put(ERROR_INVALID_NETWORK_ADDRESS,"invalid network address");
				
		//WALLET
		this.errorMessages.put(ERROR_WALLET_NO_EXISTS, "wallet does not exist");
		this.errorMessages.put(ERROR_WALLET_ADDRESS_NO_EXISTS, "address does not exist in wallet");
		this.errorMessages.put(ERROR_WALLET_LOCKED, "wallet is locked");
		this.errorMessages.put(ERROR_WALLET_ALREADY_EXISTS, "wallet already exists");
		this.errorMessages.put(ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER, "user denied api call");
		
		//BLOCK
		this.errorMessages.put(ERROR_BLOCK_NO_EXISTS, "block does not exist");
		
		//TRANSACTIONS
		this.errorMessages.put(ERROR_TRANSACTION_NO_EXISTS, "transactions does not exist");
		this.errorMessages.put(ERROR_PUBLIC_KEY_NOT_FOUND, "public key not found");
		
		//NAMING
		this.errorMessages.put(ERROR_NAME_NO_EXISTS, "name does not exist");
		this.errorMessages.put(ERROR_NAME_ALREADY_EXISTS, "name already exists");
		this.errorMessages.put(ERROR_NAME_ALREADY_FOR_SALE, "name already for sale");
		this.errorMessages.put(ERROR_NAME_NOT_LOWER_CASE, "name must be lower case");
		this.errorMessages.put(ERROR_NAME_SALE_NO_EXISTS, "namesale does not exist");
		this.errorMessages.put(ERROR_BUYER_ALREADY_OWNER, "buyer is already owner");
		
		//POLLS
		this.errorMessages.put(ERROR_POLL_NO_EXISTS, "poll does not exist");
		this.errorMessages.put(ERROR_POLL_ALREADY_EXISTS, "poll already exists");
		this.errorMessages.put(ERROR_DUPLICATE_OPTION, "not all options are unique");
		this.errorMessages.put(ERROR_POLL_OPTION_NO_EXISTS, "option does not exist");
		this.errorMessages.put(ERROR_ALREADY_VOTED_FOR_THAT_OPTION, "already voted for that option");
		
		//ASSETS
		this.errorMessages.put(ERROR_INVALID_ASSET_ID, "invalid asset id");
		
		//NAME PAYMENTS
		this.errorMessages.put(ERROR_NAME_NOT_REGISTERED, NameResult.NAME_NOT_REGISTERED.getStatusMessage());
		this.errorMessages.put(ERROR_NAME_FOR_SALE, NameResult.NAME_FOR_SALE.getStatusMessage());
		this.errorMessages.put(ERROR_NAME_WITH_SPACE, NameResult.NAME_WITH_SPACE.getStatusMessage());
		
		//AT
		this.errorMessages.put(ERROR_INVALID_CREATION_BYTES,"error in creation bytes");
		this.errorMessages.put(ERROR_INVALID_DESC_LENGTH,"invalid description length. max length " + AT_Constants.DESC_MAX_LENGTH);
		this.errorMessages.put(ERROR_EMPTY_CODE,"code is empty");
		this.errorMessages.put(ERROR_DATA_SIZE,"invalid data length");
		this.errorMessages.put(ERROR_INVALID_TYPE_LENGTH,"invalid type length");
		this.errorMessages.put(ERROR_INVALID_TAGS_LENGTH,"invalid tags length");
		this.errorMessages.put(ERROR_NULL_PAGES,"invalid pages");
		
		//BLOG
		this.errorMessages.put(ERROR_BODY_EMPTY,"invalid body it must not be empty");
		this.errorMessages.put(ERROR_BLOG_DISABLED,"this blog is disabled");
		this.errorMessages.put(ERROR_NAME_NOT_OWNER,"the creator address does not own the author name");
		this.errorMessages.put(ERROR_TX_AMOUNT,"the data size is too large - currently only "+BATCH_TX_AMOUNT+" arbitrary transactions are allowed at once!");
		this.errorMessages.put(ERROR_BLOG_ENTRY_NO_EXISTS,"transaction with this signature contains no entries!");
		this.errorMessages.put(ERROR_BLOG_EMPTY,"this blog is empty");
		this.errorMessages.put(ERROR_POSTID_EMPTY,"the attribute postid is empty! this is the signature of the post you want to comment");
		this.errorMessages.put(ERROR_POST_NOT_EXISTING,"for the given postid no blogpost to comment was found");
		this.errorMessages.put(ERROR_COMMENTING_DISABLED,"commenting is for this blog disabled");
		this.errorMessages.put(ERROR_COMMENT_NOT_EXISTING,"for the given signature no comment was found");
		this.errorMessages.put(ERROR_INVALID_COMMENT_OWNER,"invalid comment owner");
		
		
		//MESSAGES
		this.errorMessages.put(ERROR_MESSAGE_FORMAT_NOT_HEX,"the Message format is not hex - correct the text or use isTextMessage = true");
		this.errorMessages.put(ERROR_MESSAGE_BLANK,"The message attribute is missing or content is blank");
		this.errorMessages.put(ERROR_NO_PUBLIC_KEY,"The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him.");
		this.errorMessages.put(ERROR_MESSAGESIZE_EXCEEDED,"Message size exceeded!");

	}
	
	
	@SuppressWarnings("unchecked")
	public WebApplicationException createError(int error)
	{

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error", error);
		if ( error > Transaction.AT_ERROR )
		{
			jsonObject.put("message", AT_Error.getATError(error - Transaction.AT_ERROR) );
		}
		else
		{
			jsonObject.put("message", this.errorMessages.get(error));
		}
		
		
		return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(jsonObject.toJSONString()).build());
	}
}
