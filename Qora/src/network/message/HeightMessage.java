package network.message;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class HeightMessage extends Message{

	private static final int HEIGHT_LENGTH = 4;
	
	private int height;
	
	public HeightMessage(int height)
	{
		super(HEIGHT_TYPE);	
		
		this.height = height;
	}
	
	public int getHeight()
	{
		return this.height;
	}

	public static Message parse(byte[] data) throws Exception {
		
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != HEIGHT_LENGTH)
		{
			throw new Exception("Data does not match length");
		}
		
		//READ HEIGHT
		int height = Ints.fromByteArray(data);
		
		return new HeightMessage(height);
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE HEIGHT
		byte[] heightBytes = Ints.toByteArray(this.height);
		heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
		data = Bytes.concat(data, heightBytes);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return HEIGHT_LENGTH;
	}

}
