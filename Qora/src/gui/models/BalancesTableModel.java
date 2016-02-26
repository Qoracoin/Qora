package gui.models;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import org.mapdb.Fun.Tuple2;

import qora.account.Account;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import database.SortableList;
import lang.lang;

@SuppressWarnings("serial")
public class BalancesTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_ADDRESS = 0;
	public static final int COLUMN_BALANCE = 1;
	
	private long key;
	private String[] columnNames = {lang.Translate("Address"), lang.Translate("Balance")};
	private SortableList<Tuple2<String, Long>, BigDecimal> balances;
	
	public BalancesTableModel(long key)
	{
		this.key = key;
		Controller.getInstance().addObserver(this);
		this.balances = Controller.getInstance().getBalances(key);
		this.balances.registerObserver();
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
		 return this.balances.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.balances == null || row > this.balances.size() - 1 )
		{
			return null;
		}
		
		Pair<Tuple2<String, Long>, BigDecimal> aRow = this.balances.get(row);
		Account account = new Account(aRow.getA().a);
		
		switch(column)
		{
		case COLUMN_ADDRESS:
			
			return account.getAddress();
			
		case COLUMN_BALANCE:
			
			return NumberAsString.getInstance().numberAsString(account.getConfirmedBalance(this.key));
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
		if(message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.balances.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
