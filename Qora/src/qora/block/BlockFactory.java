package qora.block;

import qora.account.PublicKeyAccount;

public class BlockFactory {

	private static BlockFactory instance;
	
	public static BlockFactory getInstance()
	{
		if(instance == null)
		{
			instance = new BlockFactory();
		}
		
		return instance;
	}
	
	private BlockFactory()
	{
		
	}
	
	public Block parse(byte[] data) throws Exception
	{
		//PARSE BLOCK
		return Block.parse(data);
	}

	public Block create(int version, byte[] reference, long timestamp, long baseTarget, PublicKeyAccount generator, byte[] signature, byte[] atBytes, long atFees) 
	{		
		return new Block(version, reference, timestamp, baseTarget, generator, signature, atBytes, atFees);		
	}
	
	public Block create(int version, byte[] reference, long timestamp, long baseTarget, PublicKeyAccount generator, byte[] signature) 
	{		
		return new Block(version, reference, timestamp, baseTarget, generator, signature);		
	}
	
}
