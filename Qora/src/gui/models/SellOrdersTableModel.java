package gui.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import qora.assets.Asset;
import qora.assets.Order;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import database.DBSet;
import database.SortableList;

@SuppressWarnings("serial")
public class SellOrdersTableModel extends QoraTableModel<BigInteger, Order> implements Observer
{
	public static final int COLUMN_PRICE = 0;
	public static final int COLUMN_AMOUNT = 1;
	public static final int COLUMN_TOTAL = 2;

	public SortableList<BigInteger, Order> orders;
	
	private String[] columnNames = {"Price", "Amount", "Buying Amount"};
	
	BigDecimal sumAmount;
	BigDecimal sumTotal;
	 
	public SellOrdersTableModel(Asset have, Asset want)
	{
		Controller.getInstance().addObserver(this);
		this.orders = Controller.getInstance().getOrders(have, want, true);
		
		this.orders.registerObserver();
		
		columnNames[COLUMN_PRICE] += " " + want.getShort();
		columnNames[COLUMN_AMOUNT] += " " + have.getShort();
		columnNames[COLUMN_TOTAL] += " " + want.getShort();
		
		totalCalc();
	}
	
	private void totalCalc()
	{
		sumAmount = BigDecimal.ZERO.setScale(8);
		sumTotal = BigDecimal.ZERO.setScale(8);
		for (Pair<BigInteger, Order> orderPair : this.orders) 	
		{
			sumAmount = sumAmount.add(orderPair.getB().getAmountLeft());
			sumTotal = sumTotal.add(orderPair.getB().getPrice().multiply(orderPair.getB().getAmountLeft()).setScale(8, RoundingMode.DOWN));
		}
	}
	
	@Override
	public SortableList<BigInteger, Order> getSortableList() 
	{
		return this.orders;
	}
	
	public Order getOrder(int row)
	{
		return this.orders.get(row).getB();
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
		return this.orders.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.orders == null || row > this.orders.size() )
		{
			return null;
		}
		
		Order order = null;
		if(row < this.orders.size())
		{
			order = this.orders.get(row).getB();
		}
		
		switch(column)
		{
			case COLUMN_PRICE:
				
				if(row == this.orders.size())
					return "<html>Total:</html>";
				
				return NumberAsString.getInstance().numberAsString(order.getPrice());
			
			case COLUMN_AMOUNT:
				
				if(row == this.orders.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAmount) + "</i></html>";
				
				
				// It shows unacceptably small amount of red.
				BigDecimal increment = order.calculateBuyIncrement(order, DBSet.getInstance());
				BigDecimal amount = order.getAmountLeft();
				String amountStr = NumberAsString.getInstance().numberAsString(order.getAmountLeft());
				amount = amount.subtract(amount.remainder(increment));
				
				if (amount.compareTo(BigDecimal.ZERO) <= 0)
					return "<html><font color=#808080>" + amountStr + "</font></html>";
				else
					return "<html>" + amountStr + "</html>";

			
			case COLUMN_TOTAL:
	
				if(row == this.orders.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumTotal) + "</i></html>";
	
				return NumberAsString.getInstance().numberAsString(order.getPrice().multiply(order.getAmountLeft()).setScale(8, RoundingMode.DOWN));
					
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
		if(message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE)
		{
			totalCalc();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.orders.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
