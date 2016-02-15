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
import qora.assets.Asset;
import qora.assets.Order;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class CreateOrderTransaction extends Transaction 
{
	private static final int CREATOR_LENGTH = 32;
	private static final int HAVE_LENGTH = 8;
	private static final int WANT_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 12;
	private static final int PRICE_LENGTH = 12;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + HAVE_LENGTH + WANT_LENGTH + AMOUNT_LENGTH + PRICE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount creator;
	private Order order;
	
	public CreateOrderTransaction(PublicKeyAccount creator, long have, long want, BigDecimal amount, BigDecimal price, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		super(CREATE_ORDER_TRANSACTION, fee, timestamp, reference, signature);
		
		this.creator = creator;
		this.order = new Order(new BigInteger(this.signature), creator, have, want, amount, price, timestamp);
	}

	//GETTERS/SETTERS
	
	public Order getOrder()
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
		
		//READ HAVE
		byte[] haveBytes = Arrays.copyOfRange(data, position, position + HAVE_LENGTH);
		long have = Longs.fromByteArray(haveBytes);	
		position += HAVE_LENGTH;
		
		//READ WANT
		byte[] wantBytes = Arrays.copyOfRange(data, position, position + WANT_LENGTH);
		long want = Longs.fromByteArray(wantBytes);	
		position += WANT_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
		
		//READ PRICE
		byte[] priceBytes = Arrays.copyOfRange(data, position, position + PRICE_LENGTH);
		BigDecimal price = new BigDecimal(new BigInteger(priceBytes), 8);
		position += PRICE_LENGTH;
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CreateOrderTransaction(creator, have, want, amount, price, fee, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/ORDER
		transaction.put("creator", this.creator.getAddress());
		
		JSONObject order = new JSONObject();
		order.put("have", this.order.getHave());
		order.put("want", this.order.getWant());
		order.put("amount", this.order.getAmount().toPlainString());
		order.put("price", this.order.getPrice().toPlainString());
		
		transaction.put("order", order);
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CREATE_ORDER_TRANSACTION);
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
		
		//WRITE HAVE
		byte[] haveBytes = Longs.toByteArray(this.order.getHave());
		haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);
		
		//WRITE WANT
		byte[] wantBytes = Longs.toByteArray(this.order.getWant());
		wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.order.getAmount().unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE PRICE
		byte[] priceBytes = this.order.getPrice().unscaledValue().toByteArray();
		fill = new byte[PRICE_LENGTH - priceBytes.length];
		priceBytes = Bytes.concat(fill, priceBytes);
		data = Bytes.concat(data, priceBytes);
		
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
	
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CREATE_ORDER_TRANSACTION);
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
		
		//WRITE HAVE
		byte[] haveBytes = Longs.toByteArray(this.order.getHave());
		haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);
		
		//WRITE WANT
		byte[] wantBytes = Longs.toByteArray(this.order.getWant());
		wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.order.getAmount().unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE PRICE
		byte[] priceBytes = this.order.getPrice().unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - priceBytes.length];
		priceBytes = Bytes.concat(fill, priceBytes);
		data = Bytes.concat(data, priceBytes);
		
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
		//CHECK IF ASSETS NOT THE SAME
		if(this.order.getHave() == this.order.getWant())
		{
			return HAVE_EQUALS_WANT;
		}
		
		//CHECK IF AMOUNT POSITIVE
		if(this.order.getAmount().compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECCK IF PRICE POSITIVE
		if(this.order.getPrice().compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_PRICE;
		}
		
		//REMOVE FEE
		DBSet fork = db.fork();
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(fork).subtract(this.fee), fork);
		
		//CHECK IF SENDER HAS ENOUGH ASSET BALANCE
		if(this.creator.getConfirmedBalance(this.order.getHave(), fork).compareTo(this.order.getAmount()) == -1)
		{
			return NO_BALANCE;
		}
		
		//ONLY AFTER POWFIX_RELEASE TO SAVE THE OLD NETWORK
		if(this.timestamp >= Transaction.getPOWFIX_RELEASE()) {
			//CHECK IF SENDER HAS ENOUGH QORA BALANCE
			if(this.creator.getConfirmedBalance(fork).compareTo(BigDecimal.ZERO) == -1)
			{
				return NO_BALANCE;
			}	
		}
		
		//CHECK IF HAVE IS NOT DIVISBLE
		if(!this.order.getHaveAsset(db).isDivisible())
		{
			//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if(this.order.getAmount().stripTrailingZeros().scale() > 0)
			{
				//AMOUNT HAS DECIMALS
				return INVALID_AMOUNT;
			}
		}
		
		//CHECK IF WANT EXISTS
		Asset wantAsset = this.order.getWantAsset(db);
		if(wantAsset == null)
		{
			//WANT DOES NOT EXIST
			return ASSET_DOES_NOT_EXIST;
		}
		
		//CHECK IF WANT IS NOT DIVISIBLE
		if(!wantAsset.isDivisible())
		{
			//CHECK IF TOTAL RETURN DOES NOT HAVE ANY DECIMALS
			if(this.order.getAmount().multiply(this.order.getPrice()).stripTrailingZeros().scale() > 0)
			{
				return INVALID_RETURN;
			}
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
		
		//PROCESS ORDER
		this.order.copy().process(db, this);
	}


	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.fee), db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//ORPHAN ORDER
		this.order.copy().orphan(db);
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
		if(account.getAddress().equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, long have, long want, BigDecimal amount, BigDecimal price, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		Order order = new Order(null, creator, have, want, amount, price, timestamp);
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CREATE_ORDER_TRANSACTION);
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
		
		//WRITE HAVE
		byte[] haveBytes = Longs.toByteArray(order.getHave());
		haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);
		
		//WRITE WANT
		byte[] wantBytes = Longs.toByteArray(order.getWant());
		wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = order.getAmount().unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE PRICE
		byte[] priceBytes = order.getPrice().unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - priceBytes.length];
		priceBytes = Bytes.concat(fill, priceBytes);
		data = Bytes.concat(data, priceBytes);
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
}
