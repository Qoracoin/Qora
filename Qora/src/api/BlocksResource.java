package api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;

import qora.BlockGenerator;
import qora.account.Account;
import qora.block.Block;
import qora.block.GenesisBlock;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import utils.APIUtils;
import utils.Pair;
import controller.Controller;
import database.DBSet;

@Path("blocks")
@Produces(MediaType.APPLICATION_JSON)
public class BlocksResource 
{
	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@GET
	public String getBlocks()
	{
		APIUtils.askAPICallAllowed("GET blocks", request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		List<Pair<Account, Block>> blocks = Controller.getInstance().getLastBlocks();
		JSONArray array = new JSONArray();
		
		for(Pair<Account, Block> block: blocks)
		{
			array.add(block.getB().toJson());
		}
		
		return array.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/address/{address}")	
	public String getBlocks(@PathParam("address") String address)
	{

		//CHECK ADDRESS
		if(!Crypto.getInstance().isValidAddress(address))
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
		}

		APIUtils.askAPICallAllowed("GET blocks/address/" + address, request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		//CHECK ACCOUNT IN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);	
		if(account == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}
		
		JSONArray array = new JSONArray();
		for(Block block: Controller.getInstance().getLastBlocks(account))
		{
			array.add(block.toJson());
		}
		
		return array.toJSONString();
	}
	
	@GET
	@Path("/{signature}")	
	public static String getBlock(@PathParam("signature") String signature)
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SIGNATURE);
		}
				
		Block block = Controller.getInstance().getBlock(signatureBytes);
				
		//CHECK IF BLOCK EXISTS
		if(block == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
		}
		
		return block.toJson().toJSONString();
	}
	
	@GET
	@Path("/first")	
	public String getFirstBlock()
	{
		return new GenesisBlock().toJson().toJSONString();
	}
	
	@GET
	@Path("/last")	
	public static String getLastBlock()
	{
		return Controller.getInstance().getLastBlock().toJson().toJSONString();
	}
	
	@GET
	@Path("/child/{signature}")	
	public String getChild(@PathParam("signature") String signature)
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SIGNATURE);
		}
				
		Block block = Controller.getInstance().getBlock(signatureBytes);
				
		//CHECK IF BLOCK EXISTS
		if(block == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
		}
		
		Block child = block.getChild();
		
		//CHECK IF CHILD EXISTS
		if(child == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
		}
		
		return child.toJson().toJSONString();
	}
	
	@GET
	@Path("/generatingbalance")
	public String getGeneratingBalance()
	{
		long generatingBalance = Controller.getInstance().getNextBlockGeneratingBalance();
		return String.valueOf(generatingBalance);
	}
	
	@GET
	@Path("/generatingbalance/{signature}")
	public String getGeneratingBalance(@PathParam("signature") String signature)
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SIGNATURE);
		}
		
		Block block = Controller.getInstance().getBlock(signatureBytes);
		
		//CHECK IF BLOCK EXISTS
		if(block == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
		}
		
		long generatingBalance = Controller.getInstance().getNextBlockGeneratingBalance(block);
		return String.valueOf(generatingBalance);
	}
	
	@GET
	@Path("/time")
	public String getTimePerBlock()
	{
		Block block = Controller.getInstance().getLastBlock();
		long timePerBlock = BlockGenerator.getBlockTime(block.getGeneratingBalance());
		return String.valueOf(timePerBlock);
	}
	
	@GET
	@Path("/time/{generatingbalance}")
	public String getTimePerBlock(@PathParam("generating") long generatingbalance)
	{
		long timePerBlock = BlockGenerator.getBlockTime(generatingbalance);
		return String.valueOf(timePerBlock);
	}
	
	@GET
	@Path("/height")
	public static String getHeight() 
	{
		return String.valueOf(Controller.getInstance().getHeight());
	}
	
	@GET
	@Path("/height/{signature}")
	public static String getHeight(@PathParam("signature") String signature) 
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SIGNATURE);
		}

		Block block = DBSet.getInstance().getBlockMap().get(signatureBytes);
				
		//CHECK IF BLOCK EXISTS
		if(block == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
		}
		
		return String.valueOf(block.getHeight());
	}
	
	@GET
	@Path("/byheight/{height}")
	public static String getbyHeight(@PathParam("height") int height) 
	{
		Block block;
		try
		{
			block = Controller.getInstance().getBlockByHeight(height);
			if(block == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
			}
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_BLOCK_NO_EXISTS);
		}
		return block.toJson().toJSONString();
	}
}
