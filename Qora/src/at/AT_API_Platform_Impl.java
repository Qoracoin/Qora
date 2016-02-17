package at;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.mapdb.Fun.Tuple2;

import qora.account.Account;
import qora.block.Block;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.MessageTransaction;
import qora.transaction.Transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import database.ATTransactionMap;
import database.DBSet;
import database.TransactionFinalMap;


//QORA AT API IMPLEMENTATION
public class AT_API_Platform_Impl extends AT_API_Impl {

	private final static AT_API_Platform_Impl instance = new AT_API_Platform_Impl();

	private final static int FIX_HEIGHT_0 = 142000;

	private DBSet dbSet;

	AT_API_Platform_Impl()
	{

	}

	public static AT_API_Platform_Impl getInstance()
	{
		return instance;
	}

	public void setDBSet( DBSet newDBSet )
	{
		dbSet = newDBSet;
	}

	@Override
	public long get_Block_Timestamp( AT_Machine_State state ) 
	{		

		Block lastBlock = dbSet.getBlockMap().getLastBlock(); 
		return AT_API_Helper.getLongTimestamp( lastBlock.getHeight(dbSet) + 1 , 0 );
	}

	public long get_Creation_Timestamp( AT_Machine_State state ) 
	{
		return AT_API_Helper.getLongTimestamp( state.getCreationBlockHeight() , 0 );
	}

	@Override
	public long get_Last_Block_Timestamp( AT_Machine_State state ) 
	{
		Block block = dbSet.getBlockMap().getLastBlock(); 
		return AT_API_Helper.getLongTimestamp( block.getHeight(dbSet) , 0 );
	}

	@Override
	public void put_Last_Block_Hash_In_A( AT_Machine_State state ) 
	{
		byte[] signature = dbSet.getBlockMap().getLastBlockSignature(); //128 BYTES
		byte[] hash = Crypto.getInstance().digest(signature); //32 BYTES

		state.set_A1(Arrays.copyOfRange(hash, 0, 8));
		state.set_A2(Arrays.copyOfRange(hash, 8, 16));
		state.set_A3(Arrays.copyOfRange(hash, 16, 24));
		state.set_A4(Arrays.copyOfRange(hash, 24, 32));	
	}

	@Override
	public void A_to_Tx_after_Timestamp( long val , AT_Machine_State state ) 
	{
		int height = AT_API_Helper.longToHeight( val );
		int numOfTx = AT_API_Helper.longToNumOfTx( val );

		byte[] address = state.getId();
		Account account = new Account(Base58.encode(address));

		clear_A( state );
		try
		{
			long transactionIDNew = findTransactionAfterHeight(height , account , numOfTx, state, dbSet);
			state.set_A1( AT_API_Helper.getByteArray( transactionIDNew ) );	
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	@Override
	public void A_to_Tx_at_Timestamp( long val , AT_Machine_State state ) 
	{
		clear_A( state );
		state.set_A1( AT_API_Helper.getByteArray( val ) );	
	}

	@Override
	public long get_Type_for_Tx_in_A( AT_Machine_State state ) 
	{
		byte[] id = state.get_A1();
		Object transaction = findTransaction(id, dbSet);

		if ( transaction != null )
		{
			if ( !transaction.getClass().equals( AT_Transaction.class ))
			{
				Transaction tx = (Transaction) transaction;
				return ( tx.getType() == Transaction.MESSAGE_TRANSACTION ) ? 1 : 0;
			}
			else
			{
				AT_Transaction tx = (AT_Transaction) transaction;
				return ( tx.getMessage().length > 0 ) ? 1 : 0;

			}
		}
		return -1;
	}

	@Override
	public long get_Amount_for_Tx_in_A( AT_Machine_State state ) 
	{
		byte[] id = state.get_A1();
		Object transaction = findTransaction(id, dbSet);
		long amount = -1;


		if ( transaction != null )
		{
			long txAmount = 0;
			if ( !transaction.getClass().equals( AT_Transaction.class ))
			{
				txAmount = getAmount((Transaction)transaction, new Account(Base58.encode(state.getId())), state.getHeight());
			}
			else
			{
				txAmount = ((AT_Transaction) transaction).getAmount();
			}
			if ( state.minActivationAmount() <= txAmount )
				amount = txAmount - state.minActivationAmount() ;
		}

		return amount;
	}

	@Override
	public long get_Timestamp_for_Tx_in_A( AT_Machine_State state ) {

		byte[] id = state.get_A1();
		Object transaction = findTransaction(id, dbSet);
		if ( transaction != null )
		{
			return AT_API_Helper.getLong( id );
		}
		return -1;
	}

	@Override
	public long get_Random_Id_for_Tx_in_A( AT_Machine_State state ) 
	{
		Transaction transaction = (Transaction)findTransaction(state.get_A1(), dbSet);

		if ( transaction != null )
		{

			int txBlockHeight; 
			int blockHeight = dbSet.getBlockMap().getLastBlock().getHeight(dbSet) + 1;
			byte[] senderPublicKey = new byte[32];

			if ( !transaction.getClass().equals( AT_Transaction.class ))
			{
				txBlockHeight = transaction.getParent().getHeight(dbSet);
				senderPublicKey = transaction.getCreator().getPublicKey();
			}
			else
			{
				txBlockHeight = AT_API_Helper.longToHeight(AT_API_Helper.getLong( state.get_A1() ));
			}

			if ( blockHeight - txBlockHeight < AT_Constants.getInstance().BLOCKS_FOR_TICKET( blockHeight ) ){ //for tests - for real case 1440
				state.setWaitForNumberOfBlocks( (int)AT_Constants.getInstance().BLOCKS_FOR_TICKET( blockHeight ) - ( blockHeight - txBlockHeight ) );
				state.getMachineState().pc -= 7;
				state.getMachineState().stopped = true;
				return 0;
			}

			byte[] sig = dbSet.getBlockMap().getLastBlockSignature();
			ByteBuffer bf = ByteBuffer.allocate( sig.length + Long.SIZE + senderPublicKey.length );
			bf.order( ByteOrder.LITTLE_ENDIAN );

			bf.put( dbSet.getBlockMap().getLastBlockSignature() );
			bf.put( state.get_A1() );
			bf.put( senderPublicKey );

			byte[] byteTicket = Crypto.getInstance().digest( bf.array() );

			long ticket = Math.abs( AT_API_Helper.getLong( Arrays.copyOfRange(byteTicket, 0, 8) ) );

			return ticket;	
		}
		return -1;
	}

	@Override
	public void message_from_Tx_in_A_to_B( AT_Machine_State state ) 
	{	
		Object tx = findTransaction(state.get_A1(), dbSet); //25 BYTES


		ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
		b.order( ByteOrder.LITTLE_ENDIAN );

		if ( tx != null)
		{
			if ( tx instanceof MessageTransaction )
			{
				MessageTransaction txMessage = (MessageTransaction) tx;
				if ( txMessage != null)
				{
					byte[] message = txMessage.getData();
					if ( message.length <= state.get_B1().length * 4 )
					{
						b.put( message );
					}

				}

			}
			else if ( tx.getClass().equals( AT_Transaction.class ) )
			{
				AT_Transaction txAT = (AT_Transaction) tx;
				byte[] message = txAT.getMessage();
				if ( message != null && message.length > 0 )
				{
					b.put( txAT.getMessage() );
				}
			}
		}

		b.clear();

		byte[] temp = new byte[ 8 ];

		b.get( temp, 0 , 8 );
		state.set_B1( temp );

		b.get( temp , 0 , 8 );
		state.set_B2( temp );

		b.get( temp , 0 , 8 );
		state.set_B3( temp );

		b.get( temp , 0 , 8 );
		state.set_B4( temp );


	}

	@Override
	public void B_to_Address_of_Tx_in_A( AT_Machine_State state ) 
	{
		Object tx = findTransaction( state.get_A1(), dbSet );

		clear_B( state );

		if ( tx != null )
		{
			byte[] address;
			if ( !tx.getClass().equals(AT_Transaction.class))
			{
				address = Base58.decode(( ( Transaction ) tx).getCreator().getAddress()); //25 BYTES
			}
			else
			{
				address = ((AT_Transaction) tx).getSenderId();
			}
			address = Bytes.ensureCapacity(address, 32, 0); // 32 BYTES
			clear_B( state );

			state.set_B1(Arrays.copyOfRange(address, 0, 8));
			state.set_B2(Arrays.copyOfRange(address, 8, 16));
			state.set_B3(Arrays.copyOfRange(address, 16, 24));
			state.set_B4(Arrays.copyOfRange(address, 24, 32));
		}

	}

	@Override
	public void B_to_Address_of_Creator( AT_Machine_State state ) 
	{
		byte[] address = state.getCreator(); //25 BYTES
		address = Bytes.ensureCapacity(address, 32, 0); // 32 BYTES

		clear_B( state );

		state.set_B1(Arrays.copyOfRange(address, 0, 8));
		state.set_B2(Arrays.copyOfRange(address, 8, 16));
		state.set_B3(Arrays.copyOfRange(address, 16, 24));
		state.set_B4(Arrays.copyOfRange(address, 24, 32));		
	}

	@Override
	public long get_Current_Balance( AT_Machine_State state ) 
	{
		return state.getG_balance();
	}

	@Override
	public long get_Previous_Balance( AT_Machine_State state ) {
		return state.getP_balance();
	}

	@Override
	public void send_to_Address_in_B( long val , AT_Machine_State state ) {

		if ( val < 1 ) return;

		if ( val < state.getG_balance() )
		{

			ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
			b.order( ByteOrder.LITTLE_ENDIAN );

			b.put( state.get_B1() );
			b.put( state.get_B2() );
			b.put( state.get_B3() );
			b.put( state.get_B4() );

			b.clear();

			byte[] finalAddress = new byte[AT_Constants.AT_ID_SIZE];

			b.get(finalAddress, 0, finalAddress.length);

			AT_Transaction tx = new AT_Transaction( state.getId(),  finalAddress , val , null);
			state.addTransaction( tx );

			state.setG_balance( state.getG_balance() - val );

		}
		else
		{
			ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
			b.order( ByteOrder.LITTLE_ENDIAN );

			b.put( state.get_B1() );
			b.put( state.get_B2() );
			b.put( state.get_B3() );
			b.put( state.get_B4() );

			b.clear();

			byte[] finalAddress = new byte[AT_Constants.AT_ID_SIZE];

			b.get(finalAddress, 0, finalAddress.length);

			AT_Transaction tx = new AT_Transaction( state.getId(),  finalAddress , state.getG_balance() , null);
			state.addTransaction( tx );

			state.setG_balance( 0L );
		}
	}

	@Override
	public void send_All_to_Address_in_B( AT_Machine_State state ) 
	{
		ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
		b.order( ByteOrder.LITTLE_ENDIAN );

		b.put( state.get_B1() );
		b.put( state.get_B2() );
		b.put( state.get_B3() );
		b.put( state.get_B4() );

		byte[] recipientBytes = new byte[ AT_Constants.AT_ID_SIZE ];
		b.clear();
		b.get(recipientBytes, 0, AT_Constants.AT_ID_SIZE);

		AT_Transaction tx = new AT_Transaction( state.getId(),  recipientBytes , state.getG_balance() , null);
		state.addTransaction( tx );

		state.setG_balance( 0L );
	}

	@Override
	public void send_Old_to_Address_in_B( AT_Machine_State state ) {

		ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
		b.order( ByteOrder.LITTLE_ENDIAN );

		b.put( state.get_B1() );
		b.put( state.get_B2() );
		b.put( state.get_B3() );
		b.put( state.get_B4() );

		b.clear();

		byte[] finalAddress = new byte[ AT_Constants.AT_ID_SIZE ];

		b.get(finalAddress, 0, finalAddress.length);

		if ( state.getP_balance() > state.getG_balance()  )
		{
			AT_Transaction tx = new AT_Transaction( state.getId(),  finalAddress , state.getG_balance() , null);
			state.addTransaction( tx );

			state.setG_balance( 0L );
			state.setP_balance( 0L );

		}
		else
		{
			AT_Transaction tx = new AT_Transaction( state.getId(),  finalAddress , state.getP_balance() , null);
			state.addTransaction( tx );

			state.setG_balance( state.getG_balance() - state.getP_balance() );
			state.setP_balance( 0l );

		}
	}

	//Send to B address the message stored in A
	@Override
	public void send_A_to_Address_in_B( AT_Machine_State state ) {
		ByteBuffer a = ByteBuffer.allocate( state.get_A1().length * 4);
		a.order( ByteOrder.LITTLE_ENDIAN );
		a.put( state.get_A1() );
		a.put( state.get_A2() );
		a.put( state.get_A3() );
		a.put( state.get_A4() );
		a.clear();

		ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
		b.order( ByteOrder.LITTLE_ENDIAN );

		b.put( state.get_B1() );
		b.put( state.get_B2() );
		b.put( state.get_B3() );
		b.put( state.get_B4() );

		b.clear();

		byte[] finalAddress = new byte[ AT_Constants.AT_ID_SIZE ];

		b.get(finalAddress, 0 , finalAddress.length );

		AT_Transaction tx = new AT_Transaction( state.getId(), finalAddress , 0L, a.array() );
		state.addTransaction(tx);

	}

	public long add_Minutes_to_Timestamp( long val1 , long val2 , AT_Machine_State state) {
		int height = AT_API_Helper.longToHeight( val1 );
		int numOfTx = AT_API_Helper.longToNumOfTx( val1 );
		int addHeight = height + (int) (val2 / AT_Constants.getInstance().AVERAGE_BLOCK_MINUTES(dbSet.getBlockMap().getLastBlock().getHeight(dbSet)));
		return AT_API_Helper.getLongTimestamp( addHeight , numOfTx );
	}

	protected static long findTransactionAfterHeight(int startHeight, Account account, int numOfTx, AT_Machine_State state, DBSet dbSet)
	{ 	
		//IF STARTHEIGHT IS VALID
		if ( startHeight < 0 )
		{
			return 0;
		}

		//STARTHEIGHT SHOULD BE GREATER OR EQUAL THAN CREATION BLOCK HEIGHT
		if ( startHeight < state.getCreationBlockHeight() )
		{
			startHeight = state.getCreationBlockHeight();
		}

		int forkHeight = getForkHeight(dbSet);

		Tuple2<Integer, Integer > atTxT = dbSet.getATTransactionMap().getNextATTransaction( startHeight , numOfTx, account.getAddress());

		int atTxs = dbSet.getATTransactionMap().getATTransactions( startHeight ).size();

		Tuple2<Integer, Integer> tx = dbSet.getTransactionFinalMap().getTransactionsAfterTimestamp(startHeight, (numOfTx > atTxs)?numOfTx-atTxs:0, account.getAddress());

		if ( forkHeight > 0 )
		{
			Tuple2<Integer, Integer > atTxTp = ((ATTransactionMap)dbSet.getATTransactionMap().getParent()).getNextATTransaction( startHeight , numOfTx, account.getAddress());
			int atTxsp = ((ATTransactionMap)dbSet.getATTransactionMap().getParent()).getATTransactions( startHeight ).size();

			Tuple2<Integer, Integer> txp = ((TransactionFinalMap)dbSet.getTransactionFinalMap().getParent()).getTransactionsAfterTimestamp(startHeight, (numOfTx > atTxs)?numOfTx-atTxsp:0, account.getAddress());
			if ( atTxTp != null && ( txp == null || atTxTp.a <= txp.a ) && atTxTp.a < forkHeight )
			{
				AT_Transaction atTx = ((ATTransactionMap)dbSet.getATTransactionMap().getParent()).get(atTxTp);
				if ( !atTx.getSender().equalsIgnoreCase( account.getAddress() ) && atTx.getRecipient().equalsIgnoreCase( account.getAddress() ) && atTx.getAmount() > state.minActivationAmount() )
				{
					return AT_API_Helper.getLongTimestamp( atTxTp.a , atTxTp.b + 1 );
				}

			}
			else if ( txp != null && txp.a < forkHeight )
			{
				atTxs = ((ATTransactionMap)dbSet.getATTransactionMap().getParent()).getATTransactions( txp.a ).size();
				Transaction transaction = ((TransactionFinalMap)dbSet.getTransactionFinalMap().getParent()).get(txp);

				long txAmount = getAmount((Transaction)transaction, new Account(Base58.encode(state.getId())), state.getHeight());
				
				if(transaction.isInvolved(account) && !transaction.getCreator().getAddress().equals(account.getAddress()) && txAmount >= state.minActivationAmount() )
				{
					return AT_API_Helper.getLongTimestamp( tx.a , tx.b + atTxs );
				}
			}
		}
		if ( atTxT != null &&  ( tx == null || atTxT.a <= tx.a ) )
		{
			AT_Transaction atTx = dbSet.getATTransactionMap().get(atTxT);
			if ( !atTx.getSender().equalsIgnoreCase( account.getAddress() ) && atTx.getRecipient().equalsIgnoreCase( account.getAddress() ) && atTx.getAmount() > state.minActivationAmount() )
			{
				return AT_API_Helper.getLongTimestamp( atTxT.a , atTxT.b + 1 );
			}
		}
		else if ( tx != null )
		{
			atTxs = dbSet.getATTransactionMap().getATTransactions( tx.a ).size();
			Transaction transaction = dbSet.getTransactionFinalMap().get(tx);

			long txAmount = getAmount( transaction, new Account(Base58.encode(state.getId())), state.getHeight() );
			
			if(transaction.isInvolved(account) && !transaction.getCreator().getAddress().equals(account.getAddress()) && txAmount >= state.minActivationAmount() )
			{
				return AT_API_Helper.getLongTimestamp( tx.a , tx.b + atTxs );
			}
		}

		return 0;

	}


	protected static long getAmount(Transaction tx, Account recipient, int height)
	{
		byte[] amountB = ( height >= FIX_HEIGHT_0 ) ? 	tx.getAmount( recipient ).unscaledValue().toByteArray():
			tx.getAmount( tx.getCreator() ).unscaledValue().toByteArray();
		if ( amountB.length < 8 )
		{
			byte[] fill = new byte[ 8 - amountB.length ];
			amountB = Bytes.concat(fill, amountB);
		}

		long txAmount = Longs.fromByteArray( amountB );
		return txAmount;
	}


	public static int getForkHeight(DBSet db)
	{
		//CHECK IF FORK
		if ( db.getBlockMap().getParentList() != null )
		{
			//FIND FORKHEIGHT
			if ( db.getBlockMap().getList().isEmpty()  )
			{
				return db.getBlockMap().getLastBlock().getHeight(db) + 1;
			}
			else
			{
				return Collections.min(db.getHeightMap().getValues()) + 1;
			}
		}
		return 0;
	}


	protected static Object findTransaction(byte[] id, DBSet db){
		int height = AT_API_Helper.longToHeight(AT_API_Helper.getLong(id));
		int position = AT_API_Helper.longToNumOfTx(AT_API_Helper.getLong(id));

		if ( position <= 0 )
		{
			return null;
		}

		int forkHeight = getForkHeight(db);

		//IF NOT FORK
		if ( forkHeight == 0 || forkHeight <= height )
		{
			LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = db.getATTransactionMap().getATTransactions( height );

			if (atTxs.size() >= position)
			{
				AT_Transaction key = atTxs.get(new Tuple2<Integer, Integer>( height, position - 1 ));
				return key;
			}
			else
			{
				return db.getTransactionFinalMap().getTransaction(height, position - atTxs.size());
			}
		}
		else if ( forkHeight > height )
		{
			LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction>  atTxs = ((ATTransactionMap)db.getATTransactionMap().getParent()).getATTransactions( height );

			if (atTxs.size() >= position)
			{
				AT_Transaction key = atTxs.get(new Tuple2<Integer, Integer>( height, position - 1 ));
				return key;
			}
			else
			{
				return db.getTransactionFinalMap().getTransaction(height, position - atTxs.size());
			}

		}

		return null;
	}

}
