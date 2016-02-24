package gui.assets;

import gui.QoraRowSorter;
import gui.models.WalletAssetsTableModel;
import lang.lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import qora.assets.Asset;

@SuppressWarnings("serial")
public class AssetsPanel extends JPanel
{
	public AssetsPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridwidth = 10;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 0;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 10);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;	
		buttonGBC.gridy = 1;	
		
		//TABLE
		final WalletAssetsTableModel assetsModel = new WalletAssetsTableModel();
		final JTable table = new JTable(assetsModel);
		
		//POLLS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		QoraRowSorter sorter = new QoraRowSorter(assetsModel, indexes);
		table.setRowSorter(sorter);
				
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletAssetsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletAssetsTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu assetsMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(lang.Translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				Asset asset = assetsModel.getAsset(row);
				new AssetFrame(asset);
			}
		});
		assetsMenu.add(details);
		JMenuItem dividend = new JMenuItem(lang.Translate("Pay dividend"));
		dividend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				Asset asset = assetsModel.getAsset(row);	
				new PayDividendFrame(asset);
			}
		});
		assetsMenu.add(dividend);
		table.setComponentPopupMenu(assetsMenu);
		
		//MOUSE ADAPTER
		table.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				table.setRowSelectionInterval(row, row);
		     }
		});
		
		table.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				table.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = table.convertRowIndexToModel(row);
					Asset asset = assetsModel.getAsset(row);
					new AssetFrame(asset);
				}
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		//ADD REGISTER BUTTON
		JButton issueButton = new JButton(lang.Translate("Issue Asset"));
		issueButton.setPreferredSize(new Dimension(100, 25));
		issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});	
		this.add(issueButton, buttonGBC);
		
		//ADD ALL BUTTON
		buttonGBC.gridx = 1;
		JButton allButton = new JButton(lang.Translate("All Assets"));
		allButton.setPreferredSize(new Dimension(100, 25));
		allButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onAllClick();
			}
		});	
		this.add(allButton, buttonGBC);
		
		//ADD MY ORDERS BUTTON
		buttonGBC.gridx = 2;
		JButton myOrdersButton = new JButton(lang.Translate("My Orders"));
		myOrdersButton.setPreferredSize(new Dimension(100, 25));
		myOrdersButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onMyOrdersClick();
			}
		});	
		this.add(myOrdersButton, buttonGBC);
	}
	
	public void onIssueClick()
	{
		new IssueAssetFrame();
	}
	
	public void onAllClick()
	{
		new AllAssetsFrame();
	}
	
	public void onMyOrdersClick()
	{
		new MyOrdersFrame();
	}
}
