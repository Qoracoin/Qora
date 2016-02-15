package gui.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import qora.account.Account;
import utils.AccountBalanceComparator;
import utils.ObserverMessage;
import controller.Controller;

@SuppressWarnings("serial")
public class AccountsComboBoxModel extends DefaultComboBoxModel<Account> implements Observer {

	public AccountsComboBoxModel()
	{
		//INSERT ALL ACCOUNTS
		List<Account> accounts = Controller.getInstance().getAccounts();
		synchronized(accounts)
		{
	 		sortAndAdd();
		}
		
		Controller.getInstance().addWalletListener(this);
		Controller.getInstance().addObserver(this);
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

		if((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
			||((Controller.getInstance().getStatus() == Controller.STATUS_OK) && (message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE || message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE)))
		{
			//GET SELECTED ITEM
			Account selected = (Account) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			sortAndAdd();
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				this.setSelectedItem(selected);
			}
			
		}
	}

	//SORTING BY BALANCE (BIGGEST BALANCE FIRST)
	private void sortAndAdd() {
		//TO AVOID PROBLEMS WE DON'T WANT TO SORT THE ORIGINAL LIST! 
		ArrayList<Account> accoountsToSort = new ArrayList<Account>( Controller.getInstance().getAccounts());
		Collections.sort(accoountsToSort, new AccountBalanceComparator() );
		Collections.reverse(accoountsToSort);
		for(Account account: accoountsToSort)
		{
			this.addElement(account);
		}
	}
	
	public void removeObservers()
	{
		Controller.getInstance().deleteWalletObserver(this);
		Controller.getInstance().deleteObserver(this);
	}
}
