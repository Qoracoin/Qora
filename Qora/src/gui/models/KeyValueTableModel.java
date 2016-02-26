package gui.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import org.json.simple.JSONObject;

import lang.lang;
import utils.Pair;


@SuppressWarnings("serial")
public class KeyValueTableModel extends AbstractTableModel implements Observer{

	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_VALUE = 1;
	
	private String[] columnNames = {lang.Translate("Key"), lang.Translate("Value")};
	
	private List<Pair<String, String>> keyvaluepairs = new ArrayList<Pair<String,String>>();
	
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return keyvaluepairs == null ? 0 : this.keyvaluepairs.size();
	}
	

	@Override
	public String getValueAt(int row, int column) {
		
		if(this.keyvaluepairs == null || row > this.keyvaluepairs.size() - 1 || row == -1)
		{
			return null;
		}
		
		
		switch(column)
		{
		case COLUMN_KEY:
			return keyvaluepairs.get(row).getA();
		case COLUMN_VALUE:
			
			return keyvaluepairs.get(row).getB();
		
			
		}
		
		return null;
	}
	
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		
		if(getRowCount() > rowIndex && rowIndex != -1)
		{
			if(columnIndex == 0)
			{
				keyvaluepairs.get(rowIndex).setA((String) aValue);
			}else if(columnIndex == 1)
			{
				keyvaluepairs.get(rowIndex).setB((String) aValue);
			}
			
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public JSONObject getCurrentValueAsJsonOpt()
	{
		JSONObject json = null;
		if(keyvaluepairs != null)
		{
			json = new JSONObject();
			for (Pair<String,String> keyvaluepair : keyvaluepairs) {
				json.put(keyvaluepair.getA(), keyvaluepair.getB());
			}
			
		}
		if(json == null || json.isEmpty())
		{
			json = null;
		}
		
		return json;
	}
	
	//CONSISTENCY CHECK
	public Pair<Boolean, String> checkUpdateable()
	{
		if(keyvaluepairs.size() == 0)
		{
			return new Pair<Boolean, String>(false, lang.Translate("You need to add atleast one key/value pair to properly update the name."));
		}
		
		List<String> keys = new ArrayList<String>();
		for (Pair<String, String> pair : keyvaluepairs) {
			
			String key = pair.getA();
			if(key == null || "".equalsIgnoreCase(key))
			{
				return new Pair<Boolean, String>(false, lang.Translate("The entry at position ") + keyvaluepairs.indexOf(pair) + lang.Translate(" is missing a key!"));
			}
			
			if(keys.contains(key))
			{
				return new Pair<Boolean, String>(false, lang.Translate("There are atleast two entries with duplicate keys ") + (lang.Translate("Bad key: ") + key));
			}
				keys.add(key);
		}
		
		
		
		
		return new Pair<Boolean, String>(true, "");
		
		
	}
	
	public String getCurrentValueAsJsonStringOpt()
	{
		String result = null;
		JSONObject json = getCurrentValueAsJsonOpt();
		if(json != null)
		{
//			result = JSONObject.
			return json.toJSONString();
		}
		
		return result;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void setData(List<Pair<String, String>> keyvaluepairs)
	{
		this.keyvaluepairs = keyvaluepairs;
		fireTableDataChanged();
	}
	
	public void removeEntry(int index)
	{
		if(getRowCount() > index)
		{
			keyvaluepairs.remove(index);
			fireTableRowsDeleted(index, index);
		}
	}
	
	
	public void addAtEnd()
	{
		keyvaluepairs.add(new Pair<String, String>("",""));
		int index = getRowCount()-1;
		fireTableRowsInserted(index, index);
	}


//	@Override
//	public SortableList<String, String> getSortableList() {
//		return this.keyvaluepairs;
//	}
//	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

}
