package gui.models;

import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import utils.Converter;
import utils.ObserverMessage;
import at.AT_Transaction;
import controller.Controller;
import database.SortableList;

@SuppressWarnings("serial")
public class ATTxsTableModel extends QoraTableModel<Tuple2<Integer, Integer>, AT_Transaction> implements Observer
{
	public static final int COLUMN_SENDER = 1;
	public static final int COLUMN_RECIPIENT = 2;
	public static final int COLUMN_AMOUNT = 3;
	public static final int COLUMN_HEIGHT = 0;
	public static final int COLUMN_MESSAGE = 4;
	public static final int COLUMN_SEQUENCE = 5;

	private SortableList<Tuple2<Integer, Integer>, AT_Transaction > ats;
	
	private String[] columnNames = {"Height", "Sender", "Recipient", "Amount", "Message","Seq"};
	
	public ATTxsTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList< Tuple2<Integer, Integer>, AT_Transaction > getSortableList() 
	{
		return this.ats;
	}
	
	public Long getAT(int row)
	{
		return this.ats.get(row).getB().getAmount();
	}
	
	public int getBlockHeight(int row)
	{
		return this.ats.get(row).getB().getBlockHeight();
	}
	
	public int getSeq(int row)
	{
		return this.ats.get(row).getB().getSeq();
	}

	public String getSender(int row)
	{
		return this.ats.get(row).getB().getSender();
	}
	
	public String getRecipient(int row)
	{
		return this.ats.get(row).getB().getRecipient();
	}
	
	public Long getAmount(int row)
	{
		return this.ats.get(row).getB().getAmount();
	}
	
	public String getMessage(int row)
	{
		String message = "";
		if ( this.ats.get(row).getB().getMessage() != null)
		{
			message = Converter.toHex(this.ats.get(row).getB().getMessage());
		}
		return message;
	}
	
	@Override
	public int getColumnCount() 
	{
		return this.columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		return this.ats.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.ats == null || row > this.ats.size() - 1 )
		{
			return null;
		}
		
		switch(column)
		{
		case COLUMN_HEIGHT:
			return this.ats.get(row).getB().getBlockHeight();
		case COLUMN_SEQUENCE:
			return this.ats.get(row).getB().getSeq();
		case COLUMN_SENDER:
			return this.ats.get(row).getB().getSender();
		case COLUMN_RECIPIENT:
			return this.ats.get(row).getB().getRecipient();
		case COLUMN_AMOUNT:
			return this.ats.get(row).getB().getAmount();
		case COLUMN_MESSAGE:
			String message = "";
			if ( this.ats.get(row).getB().getMessage() != null)
			{
				message = Converter.toHex(this.ats.get(row).getB().getMessage());
			}
			return message;
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
		if(message.getType() == ObserverMessage.LIST_AT_TXS)
		{			
			if(this.ats == null)
			{
				this.ats = (SortableList<Tuple2<Integer, Integer>, AT_Transaction >) message.getValue();
				this.ats.registerObserver();
			}	
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK)
			{
				this.fireTableDataChanged();
			}
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_AT_TX_TYPE )
		{
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK)
			{
				this.fireTableDataChanged();
			}
		}
		
		//STATUS_OK
		if(message.getType() == ObserverMessage.NETWORK_STATUS )
		{
			if((int)message.getValue() == Controller.STATUS_OK)
			{
				this.fireTableDataChanged();
			}
		}
	}
	
	public void removeObservers() 
	{
		this.ats.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
