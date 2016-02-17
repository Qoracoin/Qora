package gui.models;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import at.AT;
import at.AT_API_Helper;
import controller.Controller;
import qora.account.Account;
import qora.crypto.Base58;
import utils.Converter;
import utils.NumberAsString;
import utils.ObserverMessage;
import database.SortableList;

@SuppressWarnings("serial")
public class AcctTableModel extends QoraTableModel<String, AT> implements Observer
{
	public static final int COLUMN_AT_NAME = 0;
	public static final int COLUMN_AT_DESCRIPTION = 1;
	public static final int COLUMN_AT_ADDRESS = 2;
	public static final int COLUMN_AT_CREATOR = 3;
	public static final int COLUMN_AT_AMOUNT = 4;
	public static final int COLUMN_AT_SECRET = 5;
	public static final int COLUMN_AT_RECIPIENT = 6;

	public static final int COLUMN_AT_EXPIRATION = 7;

	private SortableList<String, AT> ats;
	private String type;
	private boolean initiators;
	
	private String[] columnNames = {"Name", "Description", "Address", "Creator", "Amount", "Lock", "Recipient", "Expiration Block"};
	
	public AcctTableModel(String type, boolean initiators)
	{
		Controller.getInstance().addObserver(this);
		this.ats = Controller.getInstance().getAcctATs(type, true);
		this.ats.registerObserver();
		this.type = type;
		this.initiators = initiators;
	}
	
	@Override
	public SortableList<String, AT> getSortableList() 
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
		case COLUMN_AT_NAME:
			
			return at.getName();
			
		case COLUMN_AT_DESCRIPTION:
			
			return at.getDescription();
		
		case COLUMN_AT_ADDRESS:
			
			return new Account(Base58.encode(at.getId())).getAddress();
			
		case COLUMN_AT_CREATOR:
			
			return new Account(Base58.encode(at.getCreator())).getAddress();
		
		case COLUMN_AT_AMOUNT:
			
			return NumberAsString.getInstance().numberAsString(new Account(Base58.encode(at.getId())).getConfirmedBalance());
		
		case COLUMN_AT_SECRET:
			
			return Converter.toHex(Arrays.copyOfRange(at.getAp_data().array(), 0, 32));
		
		case COLUMN_AT_RECIPIENT:
			
			return Arrays.equals(Arrays.copyOfRange(at.getAp_data().array(), 32 + 8, 32+8+25), new byte[25]) ? "No recipient defined" : Base58.encode(Arrays.copyOfRange(at.getAp_data().array(), 32 + 8, 32+8+25));
			
		case COLUMN_AT_EXPIRATION:
			
			return AT_API_Helper.longToHeight(AT_API_Helper.getLong((Arrays.copyOfRange(at.getAp_data().array(), 32, 32+8))));

			
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
	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_AT_TYPE || message.getType() == ObserverMessage.REMOVE_AT_TYPE ||
				message.getType() == ObserverMessage.REMOVE_AT_TX || message.getType() == ObserverMessage.ADD_AT_TX_TYPE ||
				message.getType() == ObserverMessage.ADD_BLOCK_TYPE)
		{
			
			//CHECK IF LIST UPDATED
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK)
			{
				this.fireTableDataChanged();
			}
			this.ats = Controller.getInstance().getAcctATs(type, initiators);
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
