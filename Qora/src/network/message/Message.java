package network.message;

import java.util.Arrays;

import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import controller.Controller;
import network.Peer;

public class Message {

	public static final byte[] MAINNET_MAGIC = {0x12, 0x34, 0x56, 0x78};
	
	public static final int MAGIC_LENGTH = 4;
	
	public static final int TYPE_LENGTH = 4;
	public static final int ID_LENGTH = 4;
	public static final int MESSAGE_LENGTH = 4;
	public static final int CHECKSUM_LENGTH = 4;
	
	public static final int GET_PEERS_TYPE = 1;
	public static final int PEERS_TYPE = 2;
	public static final int HEIGHT_TYPE = 3;
	public static final int GET_SIGNATURES_TYPE = 4;
	public static final int SIGNATURES_TYPE = 5;
	public static final int GET_BLOCK_TYPE = 6;
	public static final int BLOCK_TYPE = 7;
	public static final int TRANSACTION_TYPE = 8;
	public static final int PING_TYPE = 9;
	public static final int VERSION_TYPE = 10;
	public static final int FIND_MYSELF_TYPE = 11;

	private int type;
	private Peer sender;
	private int id;
	
	public Message(int type)
	{
		this.type = type;
		this.id = -1;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public boolean hasId()
	{
		return this.id > 0;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public void setSender(Peer sender)
	{
		this.sender = sender;
	}
	
	public Peer getSender()
	{
		return this.sender;
	}
	
	public byte[] getHash()
	{
		return Crypto.getInstance().digest(this.toBytes());
	}
	
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE MAGIC
		data = Bytes.concat(data, Controller.getInstance().getMessageMagic());
		
		//WRITE MESSAGE TYPE
		byte[] typeBytes = Ints.toByteArray(this.type);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE HASID
		if(this.hasId())
		{
			byte[] hasIdBytes = new byte[]{1}; 
			data = Bytes.concat(data, hasIdBytes);
			
			//WRITE ID
			byte[] idBytes = Ints.toByteArray(this.id);
			idBytes = Bytes.ensureCapacity(idBytes, ID_LENGTH, 0);
			data = Bytes.concat(data, idBytes);
		}
		else
		{
			byte[] hasIdBytes = new byte[]{0}; 
			data = Bytes.concat(data, hasIdBytes);
		}
		
		//WRITE LENGTH
		byte[] lengthBytes = Ints.toByteArray(this.getDataLength());
		data = Bytes.concat(data, lengthBytes);
		
		return data;
	}
	
	protected byte[] generateChecksum(byte[] data)
	{
		byte[] checksum = Crypto.getInstance().digest(data);
		checksum = Arrays.copyOfRange(checksum, 0, CHECKSUM_LENGTH);
		return checksum;
	}
	
	protected int getDataLength()
	{
		return 0;
	}
}
