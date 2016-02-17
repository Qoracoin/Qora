package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Order;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class CancelOrderTransaction extends Transaction
{
	private static final int CREATOR_LENGTH = 32;
	private static final int ORDER_LENGTH = 64;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + ORDER_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;
	
	private PublicKeyAccount creator;
	private BigInteger order;
	
	public CancelOrderTransaction(PublicKeyAccount creator, BigInteger order, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(CANCEL_ORDER_TRANSACTION, fee, timestamp, reference, signature);
		
		this.creator = creator;
		this.order = order;
	}
	
	//GETTERS/SETTERS
	
	public BigInteger getOrder()
	{
		return this.order;
	}
	
	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		int position = 0;
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ ORDER
		byte[] orderBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger order = new BigInteger(orderBytes);
		position += ORDER_LENGTH;
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CancelOrderTransaction(creator, order, fee, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
								
		//ADD CREATOR/ORDER
		transaction.put("creator", this.creator.getAddress());
		transaction.put("order", Base58.encode(this.order.toByteArray()));
								
		return transaction;	
	}

	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_ORDER_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE ORDER
		byte[] orderBytes = this.order.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - orderBytes.length];
		orderBytes = Bytes.concat(fill, orderBytes);
		data = Bytes.concat(data, orderBytes);
				
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		//SIGNATURE
		data = Bytes.concat(data, this.signature);
		
		return data;	
	}

	@Override
	public int getDataLength() 
	{
		return TYPE_LENGTH + BASE_LENGTH;
	}
	
	//VALIDATE

	@Override
	public boolean isSignatureValid() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_ORDER_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE ORDER
		byte[] orderBytes = this.order.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - orderBytes.length];
		orderBytes = Bytes.concat(fill, orderBytes);
		data = Bytes.concat(data, orderBytes);
				
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data);
	}

	@Override
	public int isValid(DBSet db) 
	{
		//CHECK IF ORDER EXISTS
		Order order = db.getOrderMap().get(this.order);
		if(order== null)
		{
			return ORDER_DOES_NOT_EXIST;
		}
		
		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.creator.getAddress()))
		{
			return INVALID_ADDRESS;
		}
				
		//CHECK IF CREATOR IS CREATOR
		if(!order.getCreator().getAddress().equals(this.creator.getAddress()))
		{
			return INVALID_ORDER_CREATOR;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getBalance(1, db).compareTo(this.fee) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF FEE IS POSITIVE
		if(this.fee.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_FEE;
		}
		
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.fee), db);
												
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
				
		//SET ORPHAN DATA
		Order order = db.getOrderMap().get(this.order);
		db.getCompletedOrderMap().add(order);
		
		//UPDATE BALANCE OF CREATOR
		this.creator.setConfirmedBalance(order.getHave(), this.creator.getConfirmedBalance(order.getHave(), db).add(order.getAmountLeft()), db);
		
		//DELETE FROM DATABASE
		db.getOrderMap().delete(this.order);	
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.fee), db);
												
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//ADD TO DATABASE
		Order order = db.getCompletedOrderMap().get(this.order);
		db.getOrderMap().add(order);	
		
		//REMOVE BALANCE OF CREATOR
		this.creator.setConfirmedBalance(order.getHave(), this.creator.getConfirmedBalance(order.getHave(), db).subtract(order.getAmountLeft()), db);
		
		//DELETE ORPHAN DATA
		db.getCompletedOrderMap().delete(this.order);
	}

	@Override
	public PublicKeyAccount getCreator() 
	{
		return this.creator;
	}

	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.creator);
		return accounts;
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, BigInteger order, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_ORDER_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, creator.getLastReference(db));
		
		//WRITE CREATOR
		data = Bytes.concat(data, creator.getPublicKey());
		
		//WRITE ORDER
		byte[] orderBytes = order.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - orderBytes.length];
		orderBytes = Bytes.concat(fill, orderBytes);
		data = Bytes.concat(data, orderBytes);
				
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
}
