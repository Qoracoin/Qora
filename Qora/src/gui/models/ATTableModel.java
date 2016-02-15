package gui.models;

import java.util.Observable;
import java.util.Observer;

import at.AT;
import controller.Controller;
import qora.account.Account;
import qora.crypto.Base58;
import utils.NumberAsString;
import utils.ObserverMessage;
import database.SortableList;

@SuppressWarnings("serial")
public class ATTableModel extends QoraTableModel<Long, AT> implements Observer
{
	public static final int COLUMN_TYPE = 0;
	public static final int COLUMN_ADDRESS = 1;
	public static final int COLUMN_NAME = 2;
	public static final int COLUMN_DESCRIPTION = 3;
	public static final int COLUMN_AMOUNT = 4;
	public static final int COLUMN_CREATOR = 5;
	
	private SortableList<Long, AT> ats;
	
	private String[] columnNames = {"Type/Domain", "Address", "Name", "Description", "Quantity", "Creator"};
	
	public ATTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, AT> getSortableList() 
	{
		return this.ats;
	}
	
	public AT getAT(int row)
	{
		return this.ats.get(row).getB();
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
		
		AT at = this.ats.get(row).getB();
		
		switch(column)
		{
		case COLUMN_TYPE:
			return at.getType();
		case COLUMN_ADDRESS:
			
			return Base58.encode(at.getId());
		
		case COLUMN_NAME:
			
			return at.getName();
		
		case COLUMN_DESCRIPTION:
			
			return at.getDescription();
			
		case COLUMN_AMOUNT:
		{
			Account account = new Account(Base58.encode(at.getId()));
			return NumberAsString.getInstance().numberAsString(account.getConfirmedBalance());
		}	
		case COLUMN_CREATOR:
			
			return Base58.encode(at.getCreator());
			
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
		if(message.getType() == ObserverMessage.LIST_ATS)
		{			
			if(this.ats == null)
			{
				this.ats = (SortableList<Long, AT>) message.getValue();
				this.ats.registerObserver();
			}	
			
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK)
			{
				this.fireTableDataChanged();
			}
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_AT_TYPE )
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
