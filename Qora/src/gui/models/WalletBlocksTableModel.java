package gui.models;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import qora.block.Block;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import controller.Controller;
import database.SortableList;
import database.wallet.BlockMap;

@SuppressWarnings("serial")
public class WalletBlocksTableModel extends QoraTableModel<Tuple2<String, String>, Block> implements Observer{

	public static final int COLUMN_HEIGHT = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_GENERATOR = 2;
	public static final int COLUMN_BASETARGET = 3;
	public static final int COLUMN_TRANSACTIONS = 4;
	public static final int COLUMN_FEE = 5;
	
	private SortableList<Tuple2<String, String>, Block> blocks;
	
	private String[] columnNames = {"Height", "Timestamp", "Generator", "Generating Balance", "Transactions", "Fee"};
	
	public WalletBlocksTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, Block> getSortableList() {
		return this.blocks;
	}
	
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}

	@Override
	public String getColumnName(int index) 
	{
		return columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		if(blocks == null)
		{
			return 0;
		}
		
		return blocks.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		try 
		{
			if(blocks == null || blocks.size() - 1 < row)
			{
				return null;
			}
			
			Block block = this.blocks.get(row).getB();
			
			switch(column)
			{
			case COLUMN_HEIGHT:
				
				return block.getHeight();
				
			case COLUMN_TIMESTAMP:
				
				return DateTimeFormat.timestamptoString(block.getTimestamp());
				
			case COLUMN_GENERATOR:
				
				return block.getGenerator().getAddress();
				
			case COLUMN_BASETARGET:
				
				return block.getGeneratingBalance();
				
			case COLUMN_TRANSACTIONS:
				
				return block.getTransactionCount();
				
			case COLUMN_FEE:	
				
				return block.getTotalFee().toPlainString();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{
			if(this.blocks == null)
			{
				this.blocks = (SortableList<Tuple2<String, String>, Block>) message.getValue();
				this.blocks.registerObserver();
				this.blocks.sort(BlockMap.TIMESTAMP_INDEX, true);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
