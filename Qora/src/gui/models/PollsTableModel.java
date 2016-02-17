package gui.models;

import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import qora.assets.Asset;
import qora.voting.Poll;
import utils.NumberAsString;
import utils.ObserverMessage;
import database.DBSet;
import database.SortableList;

@SuppressWarnings("serial")
public class PollsTableModel extends QoraTableModel<String, Poll> implements Observer
{
	public static final int COLUMN_NAME = 0;
	private static final int COLUMN_CREATOR = 1;
	public static final int COLUMN_VOTES = 2;
	private Asset asset;
	
	private String[] columnNames = {"Name", "Creator", "Total Votes"};
	private SortableList<String, Poll> polls;
	
	public PollsTableModel()
	{
		this.asset = Controller.getInstance().getAsset(0l);
		Controller.getInstance().addObserver(this);
	}
	
	public void setAsset(Asset asset) 
	{
		this.asset = asset;
		this.fireTableDataChanged();
	}
	
	@Override
	public SortableList<String, Poll> getSortableList() {
		return this.polls;
	}
	
	public Poll getPoll(int row)
	{
		return this.polls.get(row).getB();
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
		 return this.polls.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.polls == null || row > this.polls.size() - 1 )
		{
			return null;
		}
		
		Poll poll = this.polls.get(row).getB();
		
		switch(column)
		{
		case COLUMN_NAME:
			
			String key = poll.getName();
			
			//CHECK IF ENDING ON A SPACE
			if(key.endsWith(" "))
			{
				key = key.substring(0, key.length()-1);
				key += ".";
			}
			
			return key;
		
		case COLUMN_CREATOR:
			
			return poll.getCreator().getAddress();
			
		case COLUMN_VOTES:
			
			return NumberAsString.getInstance().numberAsString(poll.getTotalVotes(this.asset.getKey()));
			
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
		if(message.getType() == ObserverMessage.LIST_POLL_TYPE)
		{			
			if(this.polls == null)
			{
				this.polls = (SortableList<String, Poll>) message.getValue();
				this.polls.registerObserver();
			}	
				
			this.fireTableDataChanged();
		}
				
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_POLL_TYPE || message.getType() == ObserverMessage.REMOVE_POLL_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.polls.removeObserver();
		DBSet.getInstance().getPollMap().deleteObserver(this);
	}
}
