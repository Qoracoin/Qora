package utils;

public class ObserverMessage {

	public static final int ADD_BLOCK_TYPE = 1;
	public static final int REMOVE_BLOCK_TYPE = 2;
	public static final int LIST_BLOCK_TYPE = 3;
	
	public static final int ADD_TRANSACTION_TYPE = 4;
	public static final int REMOVE_TRANSACTION_TYPE = 5;
	public static final int LIST_TRANSACTION_TYPE = 6;
	
	public static final int ADD_PEER_TYPE = 7;
	public static final int REMOVE_PEER_TYPE = 8;
	public static final int LIST_PEER_TYPE = 9;
	
	public static final int ADD_ACCOUNT_TYPE = 10;
	public static final int REMOVE_ACCOUNT_TYPE = 11;
	
	public static final int WALLET_STATUS = 12;
	public static final int NETWORK_STATUS = 13;
	
	public static final int ADD_NAME_TYPE = 14;
	public static final int REMOVE_NAME_TYPE = 15;
	public static final int LIST_NAME_TYPE = 16;
	
	public static final int ADD_NAME_SALE_TYPE = 17;
	public static final int REMOVE_NAME_SALE_TYPE = 18;
	public static final int LIST_NAME_SALE_TYPE = 19;

	public static final int ADD_POLL_TYPE = 20;
	public static final int REMOVE_POLL_TYPE = 21;
	public static final int LIST_POLL_TYPE = 22;
	
	public static final int ADD_ASSET_TYPE = 23;
	public static final int REMOVE_ASSET_TYPE = 24;
	public static final int LIST_ASSET_TYPE = 25;
	
	public static final int ADD_ORDER_TYPE = 26;
	public static final int REMOVE_ORDER_TYPE = 27;
	public static final int LIST_ORDER_TYPE = 28;
	
	public static final int ADD_TRADE_TYPE = 29;
	public static final int REMOVE_TRADE_TYPE = 30;
	public static final int LIST_TRADE_TYPE = 31;
	
	public static final int ADD_BALANCE_TYPE = 32;
	public static final int REMOVE_BALANCE_TYPE = 33;
	public static final int LIST_BALANCE_TYPE = 34;
	
	public static final int LIST_ASSET_FAVORITES_TYPE = 35;
	
	public static final int FORGING_STATUS = 36;

	public static final int LIST_ATS = 37;
	public static final int ADD_AT_TYPE = 38;
	public static final int ADD_AT_TX_TYPE = 39;
	public static final int LIST_AT_TXS = 40;
	public static final int REMOVE_AT_TYPE = 41;
	public static final int REMOVE_AT_TX = 42;
	
	public static final int WALLET_SYNC_STATUS = 43;
	
	private int type;
	private Object value;
	
	public ObserverMessage(int type, Object value)
	{
		this.type = type;
		this.value = value;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public Object getValue()
	{
		return this.value;
	}
	
}
