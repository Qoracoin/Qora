package gui.models;

import java.awt.TrayIcon.MessageType;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import qora.account.Account;
import qora.transaction.MessageTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.Transaction;
import settings.Settings;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import utils.PlaySound;
import utils.SysTray;
import controller.Controller;
import database.DBSet;
import database.SortableList;
import database.wallet.TransactionMap;

@SuppressWarnings("serial")
public class WalletTransactionsTableModel extends QoraTableModel<Tuple2<String, String>, Transaction> implements Observer {
	
	public static final int COLUMN_CONFIRMATIONS = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_TYPE = 2;
	public static final int COLUMN_ADDRESS = 3;
	public static final int COLUMN_AMOUNT = 4;
	
	private SortableList<Tuple2<String, String>, Transaction> transactions;
	
	private String[] columnNames = {"Confirmations", "Timestamp", "Type", "Address", "Amount"};
	private String[] transactionTypes = {"", "Genesis", "Payment", "Name Registration", "Name Update", "Name Sale", "Cancel Name Sale", "Name purchase", "Poll Creation", "Poll Vote", "Arbitrary Transaction", "Asset Issue", "Asset Transfer", "Order Creation", "Cancel Order", "Multi Payment", "Deploy AT", "Message Transaction"};

	public WalletTransactionsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, Transaction> getSortableList() {
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
			
			Pair<Tuple2<String, String>, Transaction> data = this.transactions.get(row);
			Account account = new Account(data.getA().a);
			Transaction transaction = data.getB();
			
			switch(column)
			{
			case COLUMN_CONFIRMATIONS:
				
				return transaction.getConfirmations();
				
			case COLUMN_TIMESTAMP:
				
				return DateTimeFormat.timestamptoString(transaction.getTimestamp());
				
			case COLUMN_TYPE:
				
				return this.transactionTypes[transaction.getType()];
				
			case COLUMN_ADDRESS:
				
				return account.getAddress();
				
			case COLUMN_AMOUNT:
				
				return NumberAsString.getInstance().numberAsString(transaction.getAmount(account));			
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
				this.transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
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
		
		if(Controller.getInstance().getStatus() == Controller.STATUS_OK && message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{		
			if(DBSet.getInstance().getTransactionMap().contains(((Transaction) message.getValue()).getSignature()))
			{
				if(((Transaction) message.getValue()).getType() == Transaction.PAYMENT_TRANSACTION)
				{
					PaymentTransaction paymentTransaction = (PaymentTransaction) message.getValue();
					Account account = Controller.getInstance().getAccountByAddress(paymentTransaction.getRecipient().getAddress());	
					if(account != null)
					{
						if(Settings.getInstance().isSoundReceivePaymentEnabled())
						{
							PlaySound.getInstance().playSound("receivepayment.wav", ((Transaction) message.getValue()).getSignature());
						}
						
						SysTray.getInstance().sendMessage("Payment received", "From: " + paymentTransaction.getSender().getAddress() + "\nTo: " + account.getAddress() + "\nAmount: " + paymentTransaction.getAmount().toPlainString(), MessageType.INFO);
						
					}
					else if(Settings.getInstance().isSoundNewTransactionEnabled())
					{
						PlaySound.getInstance().playSound("newtransaction.wav", ((Transaction) message.getValue()).getSignature());
					}
				}
				else if(((Transaction) message.getValue()).getType() == Transaction.MESSAGE_TRANSACTION)
				{
					Account account = Controller.getInstance().getAccountByAddress(((MessageTransaction) message.getValue()).getRecipient().getAddress());	
					if(account != null)
					{
						if(Settings.getInstance().isSoundReceiveMessageEnabled())
						{
							PlaySound.getInstance().playSound("receivemessage.wav", ((Transaction) message.getValue()).getSignature()) ;
						}
					}
					else if(Settings.getInstance().isSoundNewTransactionEnabled())
					{
						PlaySound.getInstance().playSound("newtransaction.wav", ((Transaction) message.getValue()).getSignature());
					}
				}
				else if(Settings.getInstance().isSoundNewTransactionEnabled())
				{
					PlaySound.getInstance().playSound("newtransaction.wav", ((Transaction) message.getValue()).getSignature());
				}
			}
		}	

		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE
				|| message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
}
