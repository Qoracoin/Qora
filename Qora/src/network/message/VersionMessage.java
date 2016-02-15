package network.message;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class VersionMessage extends Message {

	private static final int DATA_LENGTH = 4;
	private static final int TIMESTAMP_LENGTH = 8; 

	private String strVersion;
	private long buildDateTime;
	
	public VersionMessage(String strVersion, long buildDateTime) 
	{
		super(Message.VERSION_TYPE);
		
		this.strVersion = strVersion;
		this.buildDateTime = buildDateTime;
	}
	
	public String getStrVersion()
	{
		return this.strVersion;
	}

	public long getBuildDateTime()
	{
		return this.buildDateTime;
	}

	public static VersionMessage parse(byte[] data) throws Exception
	{
		int position = 0;
		//READ LENGTH
		byte[] buildDateTimeBytes =  Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long buildDateTime = Ints.fromByteArray(buildDateTimeBytes);
		
		position += TIMESTAMP_LENGTH;
		
		//READ LENGTH
		byte[] lengthBytes =  Arrays.copyOfRange(data, position, position + DATA_LENGTH);
		int length = Ints.fromByteArray(lengthBytes);
		
		position += DATA_LENGTH;
		
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != TIMESTAMP_LENGTH + DATA_LENGTH + length)
		{
			throw new Exception("Data does not match length");
		}
		
		//READ STRVERSION
		byte[] strVersionBytes = Arrays.copyOfRange(data, position, position + length);
		String strVersion =  new String( strVersionBytes, Charset.forName("UTF-8") );
		
		// message.getBytes( Charset.forName("UTF-8") );	
		
		return new VersionMessage(strVersion, buildDateTime);
	}	
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//BUILDTIME
		byte[] buildDateTimeBytes = Longs.toByteArray(this.buildDateTime);
		buildDateTimeBytes = Bytes.ensureCapacity(buildDateTimeBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, buildDateTimeBytes);
		
		// STR VERSION
		byte[] strVersionBytes = this.strVersion.getBytes( Charset.forName("UTF-8") );
		//WRITE LENGTH
		int length = strVersionBytes.length;
		byte[] lengthBytes = Ints.toByteArray(length);
		lengthBytes = Bytes.ensureCapacity(lengthBytes, DATA_LENGTH, 0);
		data = Bytes.concat(data, lengthBytes);
		data = Bytes.concat(data, strVersionBytes);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return TIMESTAMP_LENGTH + DATA_LENGTH + this.strVersion.getBytes( Charset.forName("UTF-8") ).length;
	}
}
