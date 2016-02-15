package gui.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import qora.assets.Asset;
import qora.assets.Trade;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import database.DBSet;
import database.SortableList;

@SuppressWarnings("serial")
public class TradesTableModel extends QoraTableModel<Tuple2<BigInteger, BigInteger>, Trade> implements Observer
{
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_ASSET_1 = 2;
	public static final int COLUMN_PRICE = 3;
	public static final int COLUMN_ASSET_2 = 4;

	private SortableList<Tuple2<BigInteger, BigInteger>, Trade> trades;
	private Asset have;
	
	BigDecimal sumAsset1;
	BigDecimal sumAsset2;
	
	private String[] columnNames = {"Timestamp", "Type", "Asset 1", "Price", "Asset 2"};
	
	private void totalCalc()
	{
		sumAsset1 = BigDecimal.ZERO.setScale(8);
		sumAsset2 = BigDecimal.ZERO.setScale(8);
		
		for (Pair<Tuple2<BigInteger, BigInteger>, Trade> tradePair : this.trades) 	
		{
			String type = tradePair.getB().getInitiatorOrder(DBSet.getInstance()).getHave() == this.have.getKey() ? "Sell" : "Buy";

			if(type.equals("Buy"))
			{
				sumAsset1 = sumAsset1.add(tradePair.getB().getAmount());
				sumAsset2 = sumAsset2.add(tradePair.getB().getPrice());
			}
			else
			{
				sumAsset1 = sumAsset1.add(tradePair.getB().getPrice());
				sumAsset2 = sumAsset2.add(tradePair.getB().getAmount());
			}
			
		}
	}
	
	public TradesTableModel(Asset have, Asset want)
	{
		Controller.getInstance().addObserver(this);
		
		this.have = have;
		this.trades = Controller.getInstance().getTrades(have, want);
		this.trades.registerObserver();
		
		this.columnNames[2] = have.getShort();
		
		this.columnNames[4] = want.getShort();
		
		this.columnNames[3] = "Price: " + this.columnNames[4];
		
		totalCalc();
	}
	
	@Override
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getSortableList() 
	{
		return this.trades;
	}
	
	public Trade getTrade(int row)
	{
		return this.trades.get(row).getB();
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
		return this.trades.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.trades == null || row > this.trades.size() )
		{
			return null;
		}
		
		Trade trade = null;
		String type = null;
		if(row < this.trades.size())
		{
			trade = this.trades.get(row).getB();
			type = trade.getInitiatorOrder(DBSet.getInstance()).getHave() == this.have.getKey() ? "Sell" : "Buy";
		}
		
		switch(column)
		{
			case COLUMN_TIMESTAMP:
				
				if(row == this.trades.size())
					return "<html>Total:</html>";
				
				return DateTimeFormat.timestamptoString(trade.getTimestamp());
				
			case COLUMN_TYPE:
				
				return type;
	
			case COLUMN_ASSET_1:
				
				if(row == this.trades.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAsset1) + "</i></html>";
				
				if(type.equals("Buy"))
					return NumberAsString.getInstance().numberAsString(trade.getAmount());
				else
					return NumberAsString.getInstance().numberAsString(trade.getPrice());
				
			case COLUMN_PRICE:
				
				if(row == this.trades.size())
					return null;
				
				if(type.equals("Buy"))
					return NumberAsString.getInstance().numberAsString(trade.getPrice().divide(trade.getAmount(), 8, RoundingMode.FLOOR));
				else
					return NumberAsString.getInstance().numberAsString(trade.getAmount().divide(trade.getPrice(), 8, RoundingMode.FLOOR));
			
			case COLUMN_ASSET_2:

				if(row == this.trades.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAsset2) + "</i></html>";

				if(type.equals("Buy"))
					return NumberAsString.getInstance().numberAsString(trade.getPrice());
				else
					return NumberAsString.getInstance().numberAsString(trade.getAmount());
				
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
		if(message.getType() == ObserverMessage.ADD_TRADE_TYPE || message.getType() == ObserverMessage.REMOVE_TRADE_TYPE)
		{
			totalCalc();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.trades.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
