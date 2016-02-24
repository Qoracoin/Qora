package gui.assets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gui.QoraRowSorter;
import gui.models.WalletOrdersTableModel;
import lang.lang;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import qora.assets.Order;

@SuppressWarnings("serial")
public class MyOrdersFrame extends JFrame{
	
	private WalletOrdersTableModel ordersTableModel;

	public MyOrdersFrame() {
		
		super("ERM" + " - " + lang.Translate("My Orders"));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//SEACH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 0;
		
		//SEACH GBC
		GridBagConstraints searchGBC = new GridBagConstraints();
		searchGBC.insets = new Insets(0, 5, 5, 0);
		searchGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchGBC.anchor = GridBagConstraints.NORTHWEST;
		searchGBC.weightx = 1;	
		searchGBC.gridwidth = 1;
		searchGBC.gridx = 1;
		searchGBC.gridy = 0;
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 1;	
		
		//CREATE TABLE
		this.ordersTableModel = new WalletOrdersTableModel();
		final JTable ordersTable = new JTable(this.ordersTableModel);
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = ordersTable.getColumnModel().getColumn(WalletOrdersTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(ordersTable.getDefaultRenderer(Boolean.class));
		
		//ASSETS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		QoraRowSorter sorter = new QoraRowSorter(this.ordersTableModel, indexes);
		ordersTable.setRowSorter(sorter);
		

		// MENU
		JPopupMenu ordersMenu = new JPopupMenu();
		JMenuItem trades = new JMenuItem(lang.Translate("Trades"));
		trades.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = ordersTable.getSelectedRow();
				row = ordersTable.convertRowIndexToModel(row);

				Order order = ordersTableModel.getOrder(row);
				new TradesFrame(order);
			}
		});
		ordersMenu.add(trades);
		JMenuItem cancel = new JMenuItem(lang.Translate("Cancel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = ordersTable.getSelectedRow();
				row = ordersTable.convertRowIndexToModel(row);

				Order order = ordersTableModel.getOrder(row);
				new CancelOrderFrame(order);
			}
		});
		ordersMenu.add(cancel);
		ordersTable.setComponentPopupMenu(ordersMenu);
		
		
		
		ordersTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = ordersTable.rowAtPoint(p);
				ordersTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = ordersTable.convertRowIndexToModel(row);
					Order order = ordersTableModel.getOrder(row);
					new TradesFrame(order);
				}
			}
		});

		this.add(new JScrollPane(ordersTable), tableGBC);
		
		//PACK
		this.pack();
		//this.setSize(500, this.getHeight());
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void removeObservers() {
		this.ordersTableModel.removeObservers();
	}
}
