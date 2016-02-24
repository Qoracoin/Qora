package gui.at;


import gui.Gui;
import gui.models.ATTableModel;
import lang.lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import at.AT;
import qora.account.Account;
import qora.crypto.Base58;
import utils.BigDecimalStringComparator;
import utils.MenuPopupUtil;
import utils.StringComparator;
import utils.TableMenuPopupUtil;


@SuppressWarnings("serial")
public class ATPanel extends JPanel
{
	private ATTableModel atsTableModel;

	@SuppressWarnings("unchecked")
	public ATPanel()
	{
		this.setLayout(new GridBagLayout());

		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		this.atsTableModel = new ATTableModel();
		final JTable atsTable = Gui.createSortableTable(atsTableModel, 1);

		final TableRowSorter<ATTableModel> sorter =  (TableRowSorter<ATTableModel>) atsTable.getRowSorter();
		sorter.setComparator(ATTableModel.COLUMN_ADDRESS, new StringComparator());
		sorter.setComparator(ATTableModel.COLUMN_TYPE, new StringComparator());
		sorter.setComparator(ATTableModel.COLUMN_NAME, new StringComparator());
		sorter.setComparator(ATTableModel.COLUMN_CREATOR, new StringComparator());
		sorter.setComparator(ATTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());


		//SEARCH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 0;

		//SEARCH GBC
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

		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 10);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.SOUTHWEST;
		buttonGBC.gridx = 0;	
		buttonGBC.gridy = 2;	


		//ADD REGISTER BUTTON
		JButton registerButton = new JButton("Deploy AT");
		registerButton.setPreferredSize(new Dimension(100, 25));
		registerButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onRegisterClick();
			}
		});	
		this.add(registerButton, buttonGBC);

		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void onChange() {

				// GET VALUE
				String search = txtSearch.getText();

				// SET FILTER
				sorter.setRowFilter(RowFilter.regexFilter(search));
				atsTableModel.fireTableDataChanged();
			}
		});
		
		//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtSearch);

		//MENU
		JPopupMenu menu = new JPopupMenu();	

		JMenuItem copyAddress = new JMenuItem(lang.Translate("Copy Address",lang.NameLang));
		copyAddress.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = atsTable.getSelectedRow();
				row = atsTable.convertRowIndexToModel(row);

				Account account = new Account(Base58.encode(atsTableModel.getAT(row).getId()));

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyAddress);

		TableMenuPopupUtil.installContextMenu(atsTable, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
		atsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = atsTable.rowAtPoint(p);
				atsTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = atsTable.convertRowIndexToModel(row);
					AT at = atsTableModel.getAT(row);
					new ATDetailsFrame(at);
				}
			}
		});


		this.add(new JLabel("search:"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		this.add(new JScrollPane(atsTable), tableGBC);

	}

	public void onRegisterClick()
	{
		new DeployATFrame();
	}

	public void onExchangeClick()
	{
		//new NameExchangeFrame();
	}
}
