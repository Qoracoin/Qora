package gui.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import qora.account.Account;
import qora.assets.Asset;
import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import lang.lang;

@SuppressWarnings("serial")
public class AccountsTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_ADDRESS = 0;
	public static final int COLUMN_BALANCE = 1;
	public static final int COLUMN_CONFIRMED_BALANCE = 2;
	public static final int COLUMN_GENERATING_BALANCE = 3;
	
	private String[] columnNames = {
			lang.Translate("Address"), lang.Translate("Balance"),
			lang.Translate("Confirmed Balance"), lang.Translate("Generating Balance")
			};
	private List<Account> accounts;
	private Asset asset = null;
	
	public AccountsTableModel()
	{
		this.accounts = Controller.getInstance().getAccounts();
		Controller.getInstance().addWalletListener(this);
		Controller.getInstance().addObserver(this);
	}
	
	public Account getAccount(int row)
	{
		return accounts.get(row);
	}
	
	public void setAsset(Asset asset) 
	{
		this.asset = asset;
		this.fireTableDataChanged();
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
		 return this.accounts.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.accounts == null || row > this.accounts.size() - 1 )
		{
			return null;
		}
		
		Account account = this.accounts.get(row);
		
		switch(column)
		{
		case COLUMN_ADDRESS:
			
			return account.getAddress();
			
		case COLUMN_BALANCE:
			
			if(this.asset == null || this.asset.getKey() == 0l)
			{
				return NumberAsString.getInstance().numberAsString(account.getBalance(0));
			}
			else
			{
				return NumberAsString.getInstance().numberAsString(account.getConfirmedBalance(this.asset.getKey()));
			}
			
		case COLUMN_CONFIRMED_BALANCE:
			
			if(this.asset == null || this.asset.getKey() == 0l)
			{
				return NumberAsString.getInstance().numberAsString(account.getConfirmedBalance());	
			}
			else
			{
				return NumberAsString.getInstance().numberAsString(account.getConfirmedBalance(this.asset.getKey()));
			}
			
		case COLUMN_GENERATING_BALANCE:
			
			if(this.asset == null || this.asset.getKey() == 0l)
			{
				return  NumberAsString.getInstance().numberAsString(account.getGeneratingBalance());	
			}
			else
			{
				return NumberAsString.getInstance().numberAsString(BigDecimal.ZERO.setScale(8));
			}
			
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
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
			this.accounts = Controller.getInstance().getAccounts();	
			
			this.fireTableRowsUpdated(0, this.getRowCount()-1);  // WHEN UPDATE DATA - SELECTION DOES NOT DISAPPEAR
		}
		
		if(message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE)
		{
			this.fireTableDataChanged();
		}
	}

	public BigDecimal getTotalBalance() 
	{
		BigDecimal totalBalance = BigDecimal.ZERO.setScale(8);
		
		for(Account account: this.accounts)
		{
			if(this.asset == null || this.asset.getKey() == 0l)
			{
				totalBalance = totalBalance.add(account.getConfirmedBalance());
			}
			else
			{
				totalBalance = totalBalance.add(account.getConfirmedBalance(this.asset.getKey()));
			}
		}
		
		return totalBalance;
	}
}
