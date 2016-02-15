package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;
import ntp.NTP;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Crypto;

public class ArbitraryTransactionV1 extends ArbitraryTransaction {
	protected static final int CREATOR_LENGTH = 32;
	protected static final int SERVICE_LENGTH = 4;
	protected static final int DATA_SIZE_LENGTH = 4;
	protected static final int REFERENCE_LENGTH = 64;
	protected static final int FEE_LENGTH = 8;
	protected static final int SIGNATURE_LENGTH = 64;
	protected static final int BASE_LENGTH = TIMESTAMP_LENGTH
			+ REFERENCE_LENGTH + CREATOR_LENGTH + SERVICE_LENGTH
			+ DATA_SIZE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	public ArbitraryTransactionV1(PublicKeyAccount creator, int service,
			byte[] data, BigDecimal fee, long timestamp, byte[] reference,
			byte[] signature) {
		super(fee, timestamp, reference, signature);

		this.service = service;
		this.data = data;
		this.creator = creator;
		this.payments = new ArrayList<>();
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

		return new ArbitraryTransactionV1(creator, service, arbitraryData, fee,
				timestamp, reference, signatureBytes);
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
	public int getDataLength() {
		return TYPE_LENGTH + BASE_LENGTH + this.data.length;
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

		// CHECK DATA SIZE
		if (data.length > 4000 || data.length < 1) {
			return INVALID_DATA_LENGTH;
		}

		// CHECK IF CREATOR HAS ENOUGH MONEY
		if (this.creator.getBalance(1, db).compareTo(this.fee) == -1) {
			return NO_BALANCE;
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
			int service, byte[] arbitraryData, BigDecimal fee, long timestamp) {
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