package qora.assets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import database.DBSet;
import qora.account.Account;
import qora.crypto.Base58;
import qora.transaction.Transaction;

public class Asset {

	private static final int OWNER_LENGTH = 25;
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int DESCRIPTION_SIZE_LENGTH = 4;
	private static final int QUANTITY_LENGTH = 8;
	private static final int DIVISIBLE_LENGTH = 1;
	private static final int REFERENCE_LENGTH = 64;
	
	private Account owner;
	private String name;
	private String description;
	private long quantity;
	private boolean divisible;
	private byte[] reference;
	
	public Asset(Account owner, String name, String description, long quantity, boolean divisible, byte[] reference)
	{
		this.owner = owner;
		this.name = name;
		this.description = description;
		this.quantity = quantity;
		this.divisible = divisible;
		this.reference = reference;
	}
	
	//GETTERS/SETTERS
	
	public Account getOwner() {
		return this.owner;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public Long getQuantity() {
		return this.quantity;
	}
	
	public boolean isDivisible() {
		return this.divisible;
	}
	
	public byte[] getReference() {
		return this.reference;
	}
	
	public long getKey() {
		
		return DBSet.getInstance().getIssueAssetMap().get(this.reference);
	}
	
	public boolean isConfirmed() {
		return DBSet.getInstance().getIssueAssetMap().contains(this.reference);
	}
	
	//PARSE
	
	public static Asset parse(byte[] data) throws Exception
	{	
		int position = 0;
		
		//READ OWNER
		byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
		Account owner = new Account(Base58.encode(ownerBytes));
		position += OWNER_LENGTH;
		
		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
		
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
		
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
		
		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
		
		if(descriptionLength < 1 || descriptionLength > 4000)
		{
			throw new Exception("Invalid description length");
		}
		
		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;
		
		//READ QUANTITY
		byte[] quantityBytes = Arrays.copyOfRange(data, position, position + QUANTITY_LENGTH);
		long quantity = Longs.fromByteArray(quantityBytes);	
		position += QUANTITY_LENGTH;
		
		//READ DIVISABLE
		byte[] divisibleBytes = Arrays.copyOfRange(data, position, position + DIVISIBLE_LENGTH);
		boolean divisable = divisibleBytes[0] == 1;
		position += DIVISIBLE_LENGTH;
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		
		//RETURN
		return new Asset(owner, name, description, quantity, divisable, reference);
	}
	
	public byte[] toBytes(boolean includeReference)
	{
		byte[] data = new byte[0];
		
		//WRITE OWNER
		try
		{
			data = Bytes.concat(data , Base58.decode(this.owner.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE DESCRIPTION SIZE
		byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
		int descriptionLength = descriptionBytes.length;
		byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
		data = Bytes.concat(data, descriptionLengthBytes);
				
		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);
		
		//WRITE QUANTITY
		byte[] quantityBytes = Longs.toByteArray(this.quantity);
		data = Bytes.concat(data, quantityBytes);
		
		//WRITE DIVISIBLE
		byte[] divisibleBytes = new byte[1];
		divisibleBytes[0] = (byte) (this.divisible == true ? 1 : 0);
		data = Bytes.concat(data, divisibleBytes);
		
		if(includeReference)
		{
			//WRITE REFERENCE
			data = Bytes.concat(data, this.reference);
		}
		else
		{
			//WRITE EMPTY REFERENCE
			data = Bytes.concat(data, new byte[64]);
		}
		
		return data;
	}

	public int getDataLength() 
	{
		return OWNER_LENGTH + NAME_SIZE_LENGTH + this.name.getBytes(StandardCharsets.UTF_8).length + DESCRIPTION_SIZE_LENGTH + this.description.getBytes(StandardCharsets.UTF_8).length + QUANTITY_LENGTH + DIVISIBLE_LENGTH + REFERENCE_LENGTH;
	}	
	
	//OTHER
	
	public String toString()
	{
		/*
		if(this.getKey() == 0)
		{
			return "Qora";
		}
		else
		{
		*/	
		
		return "(" + this.getKey() + ") " + this.getName();
	}
	
	public String getShort()
	{
		return "(" + this.getKey() + ")" + this.getName().substring(0, Math.min(this.getName().length(), 4));
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject assetJSON = new JSONObject();

		// ADD DATA
		assetJSON.put("key", this.getKey());
		assetJSON.put("name", this.getName());
		assetJSON.put("description", this.getDescription());
		assetJSON.put("owner", this.getOwner().getAddress());
		assetJSON.put("quantity", this.getQuantity());
		assetJSON.put("isDivisible", this.isDivisible());
		assetJSON.put("isConfirmed", this.isConfirmed());
		assetJSON.put("reference", Base58.encode(this.getReference()));
		
		Transaction txReference = Controller.getInstance().getTransaction(this.getReference());
		if(txReference != null)
		{
			assetJSON.put("timestamp", txReference.getTimestamp());
		}
		
		return assetJSON;
	}
}
