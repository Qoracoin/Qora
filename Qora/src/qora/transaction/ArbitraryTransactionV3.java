package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;
import ntp.NTP;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Crypto;
import qora.payment.Payment;

public class ArbitraryTransactionV3 extends ArbitraryTransaction {
	protected static final int CREATOR_LENGTH = 32;
	protected static final int SERVICE_LENGTH = 4;
	protected static final int DATA_SIZE_LENGTH = 4;
	protected static final int REFERENCE_LENGTH = 64;
	protected static final int FEE_LENGTH = 8;
	protected static final int SIGNATURE_LENGTH = 64;
	private static final int PAYMENTS_SIZE_LENGTH = 4;
	protected static final int BASE_LENGTH = TIMESTAMP_LENGTH
			+ REFERENCE_LENGTH + CREATOR_LENGTH + SERVICE_LENGTH
			+ DATA_SIZE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH
			+ PAYMENTS_SIZE_LENGTH;

	public ArbitraryTransactionV3(PublicKeyAccount creator,
			List<Payment> payments, int service, byte[] data, BigDecimal fee,
			long timestamp, byte[] reference, byte[] signature) {
		super(fee, timestamp, reference, signature);

		this.service = service;
		this.data = data;
		this.payments = payments;
		if(payments == null)
		{
			this.payments = new ArrayList<Payment>();
		}
		this.creator = creator;
	}

	// PARSE CONVERT

	public static Transaction Parse(byte[] data) throws Exception {
		// CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH) {
			throw new Exception("Data does not match block length");
		}

		int position = 0;

		// READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position
				+ TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;

		// READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position
				+ REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;

		// READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position
				+ CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		// READ PAYMENTS SIZE
		byte[] paymentsLengthBytes = Arrays.copyOfRange(data, position,
				position + PAYMENTS_SIZE_LENGTH);
		int paymentsLength = Ints.fromByteArray(paymentsLengthBytes);
		position += PAYMENTS_SIZE_LENGTH;

		if (paymentsLength < 0 || paymentsLength > 400) {
			throw new Exception("Invalid payments length");
		}

		// READ PAYMENTS
		List<Payment> payments = new ArrayList<Payment>();
		for (int i = 0; i < paymentsLength; i++) {
			Payment payment = Payment.parse(Arrays.copyOfRange(data, position,
					position + Payment.BASE_LENGTH));
			payments.add(payment);

			position += Payment.BASE_LENGTH;
		}

		// READ SERVICE
		byte[] serviceBytes = Arrays.copyOfRange(data, position, position
				+ SERVICE_LENGTH);
		int service = Ints.fromByteArray(serviceBytes);
		position += SERVICE_LENGTH;

		// READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position
				+ DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);
		position += DATA_SIZE_LENGTH;

		// READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position
				+ dataSize);
		position += dataSize;

		// READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position
				+ FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;

		// READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position
				+ SIGNATURE_LENGTH);

		return new ArbitraryTransactionV3(creator, payments, service,
				arbitraryData, fee, timestamp, reference, signatureBytes);
	}

	@Override
	public byte[] toBytes() {
		byte[] data = new byte[0];

		// WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(ARBITRARY_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		// WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH,
				0);
		data = Bytes.concat(data, timestampBytes);

		// WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		// WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		// WRITE PAYMENTS SIZE
		int paymentsLength = this.payments.size();
		byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
		data = Bytes.concat(data, paymentsLengthBytes);

		// WRITE PAYMENTS
		for (Payment payment : this.payments) {
			data = Bytes.concat(data, payment.toBytes());
		}

		// WRITE SERVICE
		byte[] serviceBytes = Ints.toByteArray(this.service);
		data = Bytes.concat(data, serviceBytes);

		// WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		// WRITE DATA
		data = Bytes.concat(data, this.data);

		// WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		// SIGNATURE
		data = Bytes.concat(data, this.signature);

		return data;
	}

	@Override
	public int getDataLength() 
	{
		int paymentsLength = 0;
		for(Payment payment: this.getPayments())
		{
			paymentsLength += payment.getDataLength();
		}
		
		return TYPE_LENGTH + BASE_LENGTH + this.data.length +  paymentsLength;
	}
	
	// VALIDATE

	@Override
	public boolean isSignatureValid() {
		byte[] data = new byte[0];

		// WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(ARBITRARY_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		// WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH,
				0);
		data = Bytes.concat(data, timestampBytes);

		// WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		// WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		// WRITE PAYMENTS SIZE
		int paymentsLength = this.payments.size();
		byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
		data = Bytes.concat(data, paymentsLengthBytes);

		// WRITE PAYMENTS
		for (Payment payment : this.payments) {
			data = Bytes.concat(payment.toBytes());
		}

		// WRITE SERVICE
		byte[] serviceBytes = Ints.toByteArray(this.service);
		data = Bytes.concat(data, serviceBytes);

		// WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		// WRITE DATA
		data = Bytes.concat(data, this.data);

		// WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		return Crypto.getInstance().verify(this.creator.getPublicKey(),
				this.signature, data);
	}

	@Override
	public int isValid(DBSet db) {
		// CHECK IF RELEASED
		if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE()) {
			return NOT_YET_RELEASED;
		}

		if (this.getTimestamp() < Transaction.getPOWFIX_RELEASE()) {
			return NOT_YET_RELEASED;
		}

		// CHECK PAYMENTS SIZE
		if (this.payments.size() < 0 || this.payments.size() > 400) {
			return INVALID_PAYMENTS_LENGTH;
		}

		// CHECK DATA SIZE
		if (data.length > 4000 || data.length < 1) {
			return INVALID_DATA_LENGTH;
		}

		// REMOVE FEE
		DBSet fork = db.fork();
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(fork)
				.subtract(this.fee), fork);

		//CHECK IF SENDER HAS ENOUGH QORA BALANCE
		if(this.creator.getConfirmedBalance(fork).compareTo(BigDecimal.ZERO) == -1)
		{
			return NO_BALANCE;
		}	
		
		// CHECK PAYMENTS
		for (Payment payment : this.payments) {
			// CHECK IF RECIPIENT IS VALID ADDRESS
			if (!Crypto.getInstance().isValidAddress(
					payment.getRecipient().getAddress())) {
				return INVALID_ADDRESS;
			}

			// CHECK IF AMOUNT IS POSITIVE
			if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
				return NEGATIVE_AMOUNT;
			}

			// CHECK IF SENDER HAS ENOUGH ASSET BALANCE
			if (this.creator.getConfirmedBalance(payment.getAsset(), fork)
					.compareTo(payment.getAmount()) == -1) {
				return NO_BALANCE;
			}

			// CHECK IF AMOUNT IS DIVISIBLE
			if (!db.getAssetMap().get(payment.getAsset()).isDivisible()) {
				// CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
				if (payment.getAmount().stripTrailingZeros().scale() > 0) {
					// AMOUNT HAS DECIMALS
					return INVALID_AMOUNT;
				}
			}

			// PROCESS PAYMENT IN FORK
			payment.process(this.creator, fork);
		}

		// CHECK IF REFERENCE IS OKE
		if (!Arrays.equals(this.creator.getLastReference(db), this.reference)) {
			return INVALID_REFERENCE;
		}

		// CHECK IF FEE IS POSITIVE
		if (this.fee.compareTo(BigDecimal.ZERO) <= 0) {
			return NEGATIVE_FEE;
		}

		return VALIDATE_OK;
	}

	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator,
			List<Payment> payments, int service, byte[] arbitraryData,
			BigDecimal fee, long timestamp) {
		
		List<Payment> paymentsBuf = payments;
		
		if(paymentsBuf == null)
		{
			paymentsBuf = new ArrayList<Payment>();
		}
		
		byte[] data = new byte[0];

		// WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(ARBITRARY_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		// WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH,
				0);
		data = Bytes.concat(data, timestampBytes);

		// WRITE REFERENCE
		data = Bytes.concat(data, creator.getLastReference(db));

		// WRITE CREATOR
		data = Bytes.concat(data, creator.getPublicKey());

		// WRITE PAYMENTS SIZE
		int paymentsLength = paymentsBuf.size();
		byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
		data = Bytes.concat(data, paymentsLengthBytes);

		// WRITE PAYMENTS
		for (Payment payment : paymentsBuf) {
			data = Bytes.concat(payment.toBytes());
		}

		// WRITE SERVICE
		byte[] serviceBytes = Ints.toByteArray(service);
		data = Bytes.concat(data, serviceBytes);

		// WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(arbitraryData.length);
		data = Bytes.concat(data, dataSizeBytes);

		// WRITE DATA
		data = Bytes.concat(data, arbitraryData);

		// WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		return Crypto.getInstance().sign(creator, data);
	}
}