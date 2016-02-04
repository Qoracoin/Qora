package qora.transaction;

import java.util.Arrays;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class TransactionFactory {

	private static TransactionFactory instance;
	
	public static TransactionFactory getInstance()
	{
		if(instance == null)
		{
			instance = new TransactionFactory();
		}
		
		return instance;
	}
	
	private TransactionFactory()
	{
		
	}
	
	public Transaction parse(byte[] data) throws Exception
	{
		//READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, Transaction.TYPE_LENGTH);
		int type = Ints.fromByteArray(typeBytes);
		
		byte[] timeStampBytes = Arrays.copyOfRange(data, 4, 4 + Transaction.TIMESTAMP_LENGTH);
		long timeStamp = Longs.fromByteArray(timeStampBytes);
		
		switch(type)
		{
		case Transaction.GENESIS_TRANSACTION:
					
			//PARSE GENESIS TRANSACTION
			return GenesisTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			
		case Transaction.PAYMENT_TRANSACTION:
			
			//PARSE PAYMENT TRANSACTION
			return PaymentTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
		
		case Transaction.REGISTER_NAME_TRANSACTION:
			
			//PARSE REGISTER NAME TRANSACTION
			return RegisterNameTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			
		case Transaction.UPDATE_NAME_TRANSACTION:
			
			//PARSE UPDATE NAME TRANSACTION
			return UpdateNameTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			
		case Transaction.SELL_NAME_TRANSACTION:
			
			//PARSE SELL NAME TRANSACTION
			return SellNameTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			
		case Transaction.CANCEL_SELL_NAME_TRANSACTION:
			
			//PARSE CANCEL SELL NAME TRANSACTION
			return CancelSellNameTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			
		case Transaction.BUY_NAME_TRANSACTION:
			
			//PARSE CANCEL SELL NAME TRANSACTION
			return BuyNameTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));	
			
		case Transaction.CREATE_POLL_TRANSACTION:
			
			//PARSE CREATE POLL TRANSACTION
			return CreatePollTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));	
			
		case Transaction.VOTE_ON_POLL_TRANSACTION:
			
			//PARSE CREATE POLL VOTE
			return VoteOnPollTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));		
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			if(timeStamp < Transaction.POWFIX_RELEASE)
			{
				//PARSE ARBITRARY TRANSACTION V1
				return ArbitraryTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));			

			}
			else
			{
				//PARSE ARBITRARY TRANSACTION V3
				return ArbitraryTransactionV3.Parse(Arrays.copyOfRange(data, 4, data.length));			
			}
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			//PARSE ISSUE ASSET TRANSACTION
			return IssueAssetTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			
		case Transaction.TRANSFER_ASSET_TRANSACTION:
			
			//PARSE TRANSFER ASSET TRANSACTION
			return TransferAssetTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));	
		
		case Transaction.CREATE_ORDER_TRANSACTION:
			
			//PARSE ORDER CREATION TRANSACTION
			return CreateOrderTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));	
			
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			//PARSE ORDER CANCEL
			return CancelOrderTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));	
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			//PARSE MULTI PAYMENT
			return MultiPaymentTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));		
		
		case Transaction.DEPLOY_AT_TRANSACTION:
			return DeployATTransaction.Parse( Arrays.copyOfRange(data, 4 , data.length));

		case Transaction.MESSAGE_TRANSACTION:

			if(timeStamp < Transaction.POWFIX_RELEASE)
			{
				// PARSE MESSAGE TRANSACTION V1
				return MessageTransaction.Parse(Arrays.copyOfRange(data, 4, data.length));
			}
			else
			{
				// PARSE MESSAGE TRANSACTION V3
				return MessageTransactionV3.Parse(Arrays.copyOfRange(data, 4, data.length));
			}
			
		}

		throw new Exception("Invalid transaction type");
	}
	
}
