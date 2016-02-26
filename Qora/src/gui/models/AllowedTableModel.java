package gui.models;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import lang.lang;
import network.Peer;

@SuppressWarnings("serial")
public class AllowedTableModel extends AbstractTableModel implements Observer{

	private List<Peer> peers;
	
	private String[] columnNames = {"IP"};
	
	public ArrayList<String> getPeers()
	{
		ArrayList<String> peersstr= new ArrayList<String>(); 
		for(Peer peer: peers)
		{
			peersstr.add(peer.getAddress().getHostAddress().toString());
		}
		return peersstr;
	}
	
	public void addAddress(String address) 
	{
		InetAddress address1;
		try {
			address1 = InetAddress.getByName(address);
			Peer peer = new Peer(address1);
			peers.add(peer);
			this.fireTableDataChanged();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteAddress(int row) 
	{
		String address = this.getValueAt(row, 0).toString();
		int n = JOptionPane.showConfirmDialog(
				new JFrame(), lang.Translate("Do you want to remove address ")+ address +"?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			peers.remove(row);
			if(peers.isEmpty())
			{
				addAddress("127.0.0.1");
			}
			this.fireTableDataChanged();
		} 
	}
	
	public AllowedTableModel(String[] peerstrings)
	{
		InetAddress address;
		if(peers == null)
		{
			peers = new ArrayList<Peer>();
		}
		else
		{
			peers.clear();
		}
		try {
			for (String peerstr : peerstrings) {
					address = InetAddress.getByName(peerstr);
					Peer peer = new Peer(address);
					peers.add(peer);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		if(peers == null)
		{
			return 0;
		}
		
		return peers.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(peers == null)
		{
			return null;
		}
		
		return peers.get(row).getAddress().getHostAddress().toString();
		
	}


	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}