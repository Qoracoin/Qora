package network.message;


import java.util.Arrays;

import qora.block.Block;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class BlockMessage extends Message{

	private static final int HEIGHT_LENGTH = 4;
	
	private Block block;
	private int height;
	
	public BlockMessage(Block block)
	{
		super(BLOCK_TYPE);	
		
		this.block = block;
	}
	
	public Block getBlock()
	{
		return this.block;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public static BlockMessage parse(byte[] data) throws Exception
	{
		//PARSE HEIGHT
		byte[] heightBytes =  Arrays.copyOfRange(data, 0, HEIGHT_LENGTH);
		int height = Ints.fromByteArray(heightBytes);
		
		//PARSE BLOCK
		Block block = Block.parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length + 1));

		//CREATE MESSAGE
		BlockMessage message = new BlockMessage(block);
		message.height = height;
		return message;
	}
	
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE BLOCK HEIGHT
		byte[] heightBytes = Ints.toByteArray(this.block.getHeight());
		data = Bytes.concat(data, heightBytes);
		
		//WRITE BLOCK
		byte[] blockBytes = this.block.toBytes();
		data = Bytes.concat(data, blockBytes);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}	
	
	protected int getDataLength()
	{
		return HEIGHT_LENGTH + this.block.getDataLength();
	}
	
}
