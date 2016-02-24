package gui.at;


import gui.Gui;
import gui.models.ATTableModel;
import gui.models.AcctTableModel;
import lang.lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import qora.account.Account;
import qora.crypto.Base58;
import utils.BigDecimalStringComparator;
import utils.MenuPopupUtil;
import utils.StringComparator;
import utils.TableMenuPopupUtil;


@SuppressWarnings("serial")
public class AcctPanel extends JPanel
{
	private AcctTableModel atsTableModel;

	@SuppressWarnings("unchecked")
	public AcctPanel()
	{
		this.setLayout(new GridBagLayout());

		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		this.atsTableModel = new AcctTableModel("acct", true);
		final JTable atsTable = Gui.createSortableTable(atsTableModel, 1);
		
		final TableRowSorter<ATTableModel> sorter =  (TableRowSorter<ATTableModel>) atsTable.getRowSorter();
		sorter.setComparator(AcctTableModel.COLUMN_AT_ADDRESS, new StringComparator());
		sorter.setComparator(AcctTableModel.COLUMN_AT_NAME, new StringComparator());
		sorter.setComparator(AcctTableModel.COLUMN_AT_AMOUNT, new BigDecimalStringComparator());

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

		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.gridwidth = 2;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridx = 0;	
		tableGBC.gridy = 1;	

		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 10);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.SOUTHWEST;
		buttonGBC.gridx = 0;	
		buttonGBC.gridy = 2;	
		
		//BUTTON GBC
		GridBagConstraints buttonResponseGBC = new GridBagConstraints();
		buttonResponseGBC.insets = new Insets(10, 0, 0, 10);
		buttonResponseGBC.fill = GridBagConstraints.NONE;  
		buttonResponseGBC.anchor = GridBagConstraints.SOUTHWEST;
		buttonResponseGBC.gridx = 0;	
		buttonResponseGBC.gridy = 2;	


		//CREATE SEARCH LABEL
		this.add(new JLabel("search:"), searchLabelGBC);
		
		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		// UPDATE FILTER ON TEXT CHANGE
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
      	
		this.add(txtSearch,searchGBC);

		//ADD REGISTER BUTTON
		JButton initiateButton = new JButton("Initiate ACCT");
		initiateButton.setPreferredSize(new Dimension(100, 25));
		initiateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onInitiateClick();
			}
		});	
		this.add(initiateButton, buttonGBC);
		
		buttonGBC.gridx = 1;
		JButton responseButton = new JButton("Response ACCT");
		responseButton.setPreferredSize(new Dimension(100, 25));
		responseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onResponseClick();
			}
		});	
		this.add(responseButton, buttonGBC);



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
		
		JMenuItem copyHash = new JMenuItem(lang.Translate("Copy Secret Hash",lang.NameLang));
		copyHash.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection((String) atsTable.getValueAt(atsTable.getSelectedRow(), 5));
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyHash);
		
		TableMenuPopupUtil.installContextMenu(atsTable, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
		this.add(new JScrollPane(atsTable), tableGBC);

	}

	public void onInitiateClick()
	{
		new InitiateAcctFrame();
	}

	public void onResponseClick()
	{
		new ResponseAcctFrame();
	}
}
