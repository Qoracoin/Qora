package gui.models;

import java.util.Observable;
import java.util.Observer;

import qora.transaction.Transaction;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import database.SortableList;
import database.TransactionMap;

@SuppressWarnings("serial")
public class TransactionsTableModel extends QoraTableModel<byte[], Transaction> implements Observer {
	
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_FEE = 2;
	
	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = {"Timestamp", "Type", "Fee"};
	private String[] transactionTypes = {"", "Genesis", "Payment", "Name Registration", "Name Update", "Name Sale", "Cancel Name Sale", "Name Purchase", "Poll Creation", "Poll Vote", "Arbitrary Transaction", "Asset Issue", "Asset Transfer", "Order Creation", "Cancel Order", "Multi Payment", "Deploy AT", "Message Transaction"};

	public TransactionsTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<byte[], Transaction> getSortableList() 
	{
		return this.transactions;
	}
	
	public Transaction getTransaction(int row)
	{
		return transactions.get(row).getB();
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
		if(this.transactions == null)
		{
			return 0;
		}
		
		return this.transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		try
		{
			if(this.transactions == null || this.transactions.size() -1 < row)
			{
				return null;
			}
			
			Transaction transaction = this.transactions.get(row).getB();
			
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				return DateTimeFormat.timestamptoString(transaction.getTimestamp());
				
			case COLUMN_TYPE:
				
				return this.transactionTypes[transaction.getType()];
				
			case COLUMN_FEE:
				
				return NumberAsString.getInstance().numberAsString(transaction.getFee());		
			}
			
			return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
		{
			if(this.transactions == null)
			{
				this.transactions = (SortableList<byte[], Transaction>) message.getValue();
				this.transactions.registerObserver();
				this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}

	public void removeObservers() 
	{
		this.transactions.removeObserver();
		Controller.getInstance().deleteObserver(this);		
	}
}
