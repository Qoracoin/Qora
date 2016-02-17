package gui.models;

import java.math.BigDecimal;

import javax.swing.table.AbstractTableModel;

import qora.assets.Asset;
import qora.voting.Poll;
import qora.voting.PollOption;
import utils.NumberAsString;

@SuppressWarnings("serial")
public class PollOptionsTableModel extends AbstractTableModel
{
	private static final int COLUMN_NAME = 0;
	public static final int COLUMN_VOTES = 1;
	public static final int COLUMN_PERCENTAGE = 2;
	
	private String[] columnNames = {"Name", "Votes", "% of Total"};
	private Poll poll;
	private Asset asset;
	
	public PollOptionsTableModel(Poll poll, Asset asset)
	{
		this.poll = poll;
		this.asset = asset;
	}
	
	public PollOption getPollOption(int row)
	{
		return this.poll.getOptions().get(row);
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
		 return this.poll.getOptions().size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.poll.getOptions() == null || row > this.poll.getOptions().size() - 1 )
		{
			return null;
		}
		
		PollOption option = this.poll.getOptions().get(row);
		
		switch(column)
		{
		case COLUMN_NAME:
			
			String key = option.getName();
			
			//CHECK IF ENDING ON A SPACE
			if(key.endsWith(" "))
			{
				key = key.substring(0, key.length()-1);
				key += ".";
			}
			
			return key;
		
		case COLUMN_VOTES:
			
			return NumberAsString.getInstance().numberAsString(option.getVotes(this.asset.getKey()));
			
		case COLUMN_PERCENTAGE:
			
			BigDecimal total = this.poll.getTotalVotes(this.asset.getKey());
			BigDecimal votes = option.getVotes(this.asset.getKey());
			
			if(votes.compareTo(BigDecimal.ZERO) == 0)
			{
				return "0 %";
			}
			
			return votes.divide(total, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(100)).toPlainString() + " %";
			
		}
		
		return null;
	}
	
	public void setAsset(Asset asset)
	{
		this.asset = asset;
		this.fireTableDataChanged();
	}
}
