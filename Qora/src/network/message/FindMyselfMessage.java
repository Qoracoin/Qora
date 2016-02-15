package network.message;

import java.util.Arrays;

import com.google.common.primitives.Bytes;

public class FindMyselfMessage extends Message{

	private static final int FIND_MYSELF_ID_LENGTH = 128;
	
	private byte[] foundMyselfID;
	
	public FindMyselfMessage(byte[] foundMyselfID)
	{
		super(FIND_MYSELF_TYPE);	
		
		this.foundMyselfID = foundMyselfID;
	}
	
	public byte[] getFoundMyselfID()
	{
		return this.foundMyselfID;
	}
	
	public static FindMyselfMessage parse(byte[] data) throws Exception
	{
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != FIND_MYSELF_ID_LENGTH)
		{
			throw new Exception("Data does not match length");
		}
		
		//READ FIND_MYSELF_ID
		byte[] foundMyselfID = Arrays.copyOfRange(data, 0, FIND_MYSELF_ID_LENGTH);
		
		return new FindMyselfMessage(foundMyselfID);
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE  FIND_MYSELF_ID
		data = Bytes.concat(data, this.foundMyselfID);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return FIND_MYSELF_ID_LENGTH;
	}

}
