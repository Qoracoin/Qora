package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class BuyNameTransaction extends Transaction
{
	private static final int BUYER_LENGTH = 32;
	private static final int SELLER_LENGTH = 25;
	private static final int REFERENCE_LENGTH = 64;
	private static final int FEE_LENGTH = 8;
	private static final int SIGNATURE_LENGTH = 64;
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + BUYER_LENGTH + SELLER_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;
	
	private PublicKeyAccount buyer;
	private NameSale nameSale;
	private Account seller;
	
	public BuyNameTransaction(PublicKeyAccount buyer, NameSale nameSale, Account seller, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(BUY_NAME_TRANSACTION, fee, timestamp, reference, signature);
		
		this.buyer = buyer;
		this.nameSale = nameSale;
		this.seller = seller;
	}
	
	//GETTERS/SETTERS
	
	public PublicKeyAccount getBuyer()
	{
		return this.buyer;
	}
	
	public NameSale getNameSale()
	{
		return this.nameSale;
	}
	
	public Account getSeller()
	{
		return this.seller;
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
		
		//READ BUYER
		byte[] buyerBytes = Arrays.copyOfRange(data, position, position + BUYER_LENGTH);
		PublicKeyAccount buyer = new PublicKeyAccount(buyerBytes);
		position += BUYER_LENGTH;
		
		//READ NAMESALE
		NameSale nameSale = NameSale.Parse(Arrays.copyOfRange(data, position, data.length));
		position += nameSale.getDataLength();
		
		//READ SELLER
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + SELLER_LENGTH);
		Account seller = new Account(Base58.encode(recipientBytes));
		position += SELLER_LENGTH;
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new BuyNameTransaction(buyer, nameSale, seller, fee, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
								
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("buyer", this.buyer.getAddress());
		transaction.put("name", this.nameSale.getKey());
		transaction.put("amount", this.nameSale.getAmount().toPlainString());
		transaction.put("seller", this.seller.getAddress());
								
		return transaction;	
	}

	@Override
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(BUY_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE BUYER
		data = Bytes.concat(data, this.buyer.getPublicKey());
		
		//WRITE NAME SALE
		data = Bytes.concat(data, this.nameSale.toBytes());
		
		//WRITE SELLER
		data = Bytes.concat(data, Base58.decode(this.seller.getAddress()));
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		//SIGNATURE
		data = Bytes.concat(data, this.signature);
		
		return data;	
	}

	@Override
	public int getDataLength() 
	{
		return TYPE_LENGTH + BASE_LENGTH + this.nameSale.getDataLength();
	}
	
	//VALIDATE

	@Override
	public boolean isSignatureValid() 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(BUY_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE BUYER
		data = Bytes.concat(data, this.buyer.getPublicKey());
		
		//WRITE NAME SALE
		data = Bytes.concat(data, this.nameSale.toBytes());
		
		//WRITE SELLER
		data = Bytes.concat(data, Base58.decode(this.seller.getAddress()));
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().verify(this.buyer.getPublicKey(), this.signature, data);
	}

	@Override
	public int isValid(DBSet db) 
	{
		//CHECK NAME LENGTH
		int nameLength = this.nameSale.getKey().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK IF NAME EXISTS
		Name name = this.nameSale.getName(db);
		if(name == null)
		{
			return NAME_DOES_NOT_EXIST;
		}
				
		//CHECK IF BUYER IS OWNER
		if(name.getOwner().getAddress().equals(this.buyer.getAddress()))
		{
			return BUYER_ALREADY_OWNER;
		}
		
		//CHECK IF NAME FOR SALE ALREADY
		if(!db.getNameExchangeMap().contains(this.nameSale.getKey()))
		{
			return NAME_NOT_FOR_SALE;
		}
		
		//CHECK IF SELLER IS SELLER
		if(!name.getOwner().getAddress().equals(this.seller.getAddress()))
		{
			return INVALID_SELLER;
		}
		
		//CHECK IF BUYER HAS ENOUGH MONEY
		if(this.buyer.getBalance(1, db).compareTo(this.nameSale.getAmount().add(this.fee)) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF PRICE MATCHES
		NameSale nameSale = db.getNameExchangeMap().getNameSale(this.nameSale.getKey());
		if(!this.nameSale.getAmount().equals(nameSale.getAmount()))
		{
			return INVALID_AMOUNT;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.buyer.getLastReference(db), this.reference))
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
		//UPDATE BUYER
		this.buyer.setConfirmedBalance(this.buyer.getConfirmedBalance(db).subtract(this.fee).subtract(this.nameSale.getAmount()), db);
		
		//UPDATE SELLER
		Name name = this.nameSale.getName(db);
		this.seller.setConfirmedBalance(this.seller.getConfirmedBalance(db).add(this.nameSale.getAmount()), db);
		
		//UPDATE REFERENCE OF BUYER
		this.buyer.setLastReference(this.signature, db);
				
		//UPDATE NAME OWNER (NEW OBJECT FOR PREVENTING CACHE ERRORS)
		name = new Name(this.buyer, name.getName(), name.getValue());
		db.getNameMap().add(name);
		
		//DELETE NAME SALE FROM DATABASE
		db.getNameExchangeMap().delete(this.nameSale.getKey());
		
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE BUYER
		this.buyer.setConfirmedBalance(this.buyer.getConfirmedBalance(db).add(this.fee).add(this.nameSale.getAmount()), db);
		
		//UPDATE SELLER
		this.seller.setConfirmedBalance(this.seller.getConfirmedBalance(db).subtract(this.nameSale.getAmount()), db);
												
		//UPDATE REFERENCE OF OWNER
		this.buyer.setLastReference(this.reference, db);
				
		//UPDATE NAME OWNER (NEW OBJECT FOR PREVENTING CACHE ERRORS)
		Name name = this.nameSale.getName(db);
		name = new Name(this.seller, name.getName(), name.getValue());
		db.getNameMap().add(name);
		
		//RESTORE NAMESALE
		db.getNameExchangeMap().add(this.nameSale);
	}

	@Override
	public PublicKeyAccount getCreator() 
	{
		return this.buyer;
	}

	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
		
		accounts.add(this.buyer);
		accounts.add(this.getSeller());
		
		return accounts;
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.buyer.getAddress()))
		{
			return true;
		}
		
		if(address.equals(this.getSeller().getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.buyer.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee).subtract(this.nameSale.getAmount());
		}
		
		if(address.equals(this.getSeller().getAddress()))
		{
			return this.nameSale.getAmount();
		}
		
		return BigDecimal.ZERO.setScale(8);
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount buyer, NameSale nameSale, Account seller, BigDecimal fee, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(BUY_NAME_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, buyer.getLastReference(db));
		
		//WRITE BUYER
		data = Bytes.concat(data, buyer.getPublicKey());
		
		//WRITE NAME SALE
		data = Bytes.concat(data, nameSale.toBytes());
		
		//WRITE SELLER
		data = Bytes.concat(data, Base58.decode(seller.getAddress()));
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(buyer, data);
	}
}
