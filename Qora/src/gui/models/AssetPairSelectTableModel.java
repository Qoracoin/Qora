package gui.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import org.mapdb.Fun.Tuple6;

import qora.assets.Asset;
import qora.blockexplorer.BlockExplorer;
import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import database.DBSet;
import lang.lang;

@SuppressWarnings("serial")
public class AssetPairSelectTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_KEY = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_ORDERS_COUNT = 2;
	private static final int COLUMN_ORDERS_VOLUME = 3;
	private static final int COLUMN_TRADES_COUNT = 4;
	private static final int COLUMN_TRADES_VOLUME = 5;
	
	public long key;
	private String[] columnNames = {lang.Translate("Key"), lang.Translate("Name"), "<html>"+lang.Translate("Orders")+"<br>"+lang.Translate("Count")+"</html>", lang.Translate("Orders Volume"), "<html>"+lang.Translate("Trades")+"<br>"+lang.Translate("Count")+"</html>", lang.Translate("Trades Volume")};
	public List<Asset> assets;
	Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all; 
	
	public AssetPairSelectTableModel(long key)
	{
		this.key = key;
		//Controller.getInstance().addObserver(this);
		Collection<Asset> assetsBuf = Controller.getInstance().getAllAssets();
		this.assets = new ArrayList<Asset>();
		
		for (Asset asset : assetsBuf) {
			if(asset.getKey() != this.key)
			{
				assets.add(asset);
			}
		}
				
		this.all = 
				BlockExplorer.getInstance().calcForAsset(
						DBSet.getInstance().getOrderMap().getOrders(this.key, true), 
						DBSet.getInstance().getTradeMap().getTrades(this.key));
		
		
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
		 return this.assets.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.assets == null || row > this.assets.size() - 1 )
		{
			return null;
		}
		
		long key = this.assets.get(row).getKey();
		
		try	{
			
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return key;
			
		case COLUMN_NAME:
			
			return this.assets.get(row).getName();
			
		case COLUMN_ORDERS_COUNT:
			
			return this.all.get(key).a;
			
		case COLUMN_ORDERS_VOLUME:
			
			return "<html>" + NumberAsString.getInstance().numberAsString(this.all.get(key).c) 
					+ " " + this.assets.get(row).getShort() + "<br>" 
					+ NumberAsString.getInstance().numberAsString(this.all.get(key).d) 
					+ " " + Controller.getInstance().getAsset(this.key).getShort()
					+ "</html>";

		case COLUMN_TRADES_COUNT:
			
			if(this.all.get(key).b > 0)
				return this.all.get(key).b;
			else
				return null;
			
		case COLUMN_TRADES_VOLUME:
			
			if(this.all.get(key).b > 0)
				return "<html>" + NumberAsString.getInstance().numberAsString(this.all.get(key).e) 
					+ " " + this.assets.get(row).getShort() + "<br>" 
					+ NumberAsString.getInstance().numberAsString(this.all.get(key).f) 
					+ " " + Controller.getInstance().getAsset(this.key).getShort()
					+ "</html>";
			else
				return null;
			
			
		}
		
		} catch ( NullPointerException e)
		{
			
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
		//this.balances.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
