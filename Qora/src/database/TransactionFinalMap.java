package database;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import com.google.common.collect.Iterables;

import database.serializer.TransactionSerializer;
import qora.account.Account;
import qora.transaction.GenesisTransaction;
import qora.transaction.Transaction;

public class TransactionFinalMap extends DBMap<Tuple2<Integer, Integer>, Transaction>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private NavigableSet senderKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet recipientKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet typeKey;
	
	public TransactionFinalMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		/*this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);*/
	}

	public TransactionFinalMap(TransactionFinalMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database)
	{
	}
	
	@SuppressWarnings("unchecked")
	private Map<Tuple2<Integer, Integer>, Transaction>  openMap(DB database)
	{
		
		BTreeMap<Tuple2<Integer, Integer>, Transaction> map = database.createTreeMap("height_seq_transactions")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new TransactionSerializer())
				.makeOrGet();
		
		this.senderKey = database.createTreeSet("sender_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String run(Tuple2<Integer, Integer> key, Transaction val) {
				// TODO Auto-generated method stub
				if ( val instanceof GenesisTransaction )
					return "genesis";
				return val.getCreator().getAddress();
			}
		});
		
		this.recipientKey = database.createTreeSet("recipient_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKeys(map, this.recipientKey, new Fun.Function2<String[], Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String[] run(Tuple2<Integer, Integer> key, Transaction val) {
				List<String> recps = new ArrayList<String>();
				for ( Account acc : val.getInvolvedAccounts())
				{
					if ( val.getAmount(acc).compareTo( BigDecimal.ZERO) < 0 )
					{
						continue;
					}
					recps.add(acc.getAddress());
				}
				String[] ret = new String[ recps.size() ];
				ret = recps.toArray( ret );
				return ret;
			}
		});
		

		
		this.typeKey = database.createTreeSet("address_type_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKeys(map, this.typeKey, new Fun.Function2<Tuple2<String, Integer>[], Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public Tuple2<String, Integer>[] run(Tuple2<Integer, Integer> key, Transaction val) {
				List<Tuple2<String, Integer>> recps = new ArrayList<Tuple2<String, Integer>>();
				Integer type = val.getType();
				for ( Account acc : val.getInvolvedAccounts())
				{
						recps.add(new Tuple2<String, Integer>(acc.getAddress(),type));
					
				}
				//Tuple2<Integer, String>[] ret = (Tuple2<Integer, String>[]) new Object[ recps.size() ];
				Tuple2<String, Integer>[] ret = (Tuple2<String, Integer>[]) Array.newInstance(Fun.Tuple2.class,recps.size());
				ret = recps.toArray( ret );
				return ret;
			}
		});
		
		return map;
		
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Transaction> getMap(DB database) 
	{
		//OPEN MAP
		return openMap(database);
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Transaction> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.getMap(database);	}

	@Override
	protected Transaction getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Integer height)
	{	
		BTreeMap map = (BTreeMap) this.map;
		//GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, Transaction>) map).subMap(
				Fun.t2(height, null),
				Fun.t2(height, Fun.HI())).keySet();
		
		//DELETE TRANSACTIONS
		for(Tuple2<Integer, Integer> key: keys)
		{
			this.delete(key);
		}
	}
	
	public void delete(Integer height, Integer seq)
	{
		this.delete(new Tuple2<Integer, Integer>(height, seq));
	}
	
	
	public boolean add(Integer height, Integer seq, Transaction transaction)
	{
		return this.set(new Tuple2<Integer, Integer>(height, seq), transaction);
	}
	
	public Transaction getTransaction(Integer height, Integer seq)
	{
		Transaction tx = this.get(new Tuple2<Integer,Integer>(height, seq));
		if ( this.parent != null )
		{
			if ( tx == null )
			{
				return this.parent.get(new Tuple2<Integer,Integer>(height, seq));
			}
		}
		return tx;
	}
	
	public List<Transaction> getTransactionsByRecipient(String address)
	{
		return getTransactionsByRecipient(address, false, 0, 0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsByRecipient(String address, boolean desc, int offset, int limit)
	{
		Iterable keys = Fun.filter(this.recipientKey, address);
		
		if (desc) {
			int skip;
			if(limit == 0) {
				skip = offset;
			} else {
				skip = Iterables.size(keys) - offset - limit;
				
				if (skip < 0) {
					skip = Iterables.size(keys);
				}
			}
			keys = Iterables.skip(keys, skip);
		} else {
			keys = Iterables.skip(keys, offset);
		}
		
		Iterator iter = keys.iterator();
		
		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		if (desc) {
			Collections.reverse(txs);
		}
		
		return txs;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int getTransactionsByRecipientCount(String address)
	{
		Iterable keys = Fun.filter(this.recipientKey, address);
		
		return Iterables.size(keys);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int getTransactionsBySenderCount(String address)
	{
		Iterable keys = Fun.filter(this.senderKey, address);
		
		return Iterables.size(keys);
	}
	
	public List<Transaction> getTransactionsBySender(String address)
	{
		return getTransactionsBySender(address, false, 0, 0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsBySender(String address, boolean desc, int offset, int limit)
	{
		Iterable keys = Fun.filter(this.senderKey, address);

		if (desc) {
			int skip;
			if(limit == 0) {
				skip = offset;
			} else {
				skip = Iterables.size(keys) - offset - limit;
				
				if (skip < 0) {
					skip = Iterables.size(keys);
				}
			}
			keys = Iterables.skip(keys, skip);
		} else {
			keys = Iterables.skip(keys, offset);
		}

		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		if (desc) {
			Collections.reverse(txs);
		}

		return txs;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsByTypeAndAddress(String address, Integer type, int limit)
	{
		Iterable keys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type));
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		return txs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Tuple2<Integer, Integer> getTransactionsAfterTimestamp(int startHeight, int numOfTx,
			String address) {
		Iterable keys = Fun.filter(this.recipientKey, address);
		Iterator iter = keys.iterator();
		int prevKey = startHeight;
		while ( iter.hasNext() )
		{
			Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iter.next();
			if ( key.a >= startHeight )
			{
					if (key.a != prevKey)
					{
						numOfTx = 0;
					}
					prevKey = key.a;
					if ( key.b > numOfTx)
						return key;
			}
		}
		
		return null;
		
		
	}

	public DBMap<Tuple2<Integer, Integer>, Transaction> getParent() {
		return this.parent;
	}
}
