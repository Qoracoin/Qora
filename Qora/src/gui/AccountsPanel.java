package gui;

import gui.models.AccountsTableModel;
import gui.models.AssetsComboBoxModel;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import qora.account.Account;
import qora.assets.Asset;
import utils.BigDecimalStringComparator;
import utils.NumberAsString;
import utils.TableMenuPopupUtil;
import controller.Controller;
import lang.lang;
@SuppressWarnings("serial")
public class AccountsPanel extends JPanel implements ItemListener
{
	private static JComboBox<Asset> cbxFavorites;
	private AccountsTableModel tableModel;

	@SuppressWarnings("unchecked")
	public AccountsPanel()
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
		tableGBC.gridx = 1;	
		tableGBC.gridy= 1;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 0);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 1;	
		buttonGBC.gridy = 2;	
		
		//FAVORITES GBC
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(10, 0, 10, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridx = 1;	
		favoritesGBC.gridy = 0;	
		
		//ASSET FAVORITES
		cbxFavorites = new JComboBox<Asset>(new AssetsComboBoxModel());
		this.add(cbxFavorites, favoritesGBC);
		
		//TABLE
		tableModel = new AccountsTableModel();
		final JTable table = Gui.createSortableTable(tableModel, 1);
		
		TableRowSorter<AccountsTableModel> sorter =  (TableRowSorter<AccountsTableModel>) table.getRowSorter();
		sorter.setComparator(AccountsTableModel.COLUMN_BALANCE, new BigDecimalStringComparator());
		sorter.setComparator(AccountsTableModel.COLUMN_CONFIRMED_BALANCE, new BigDecimalStringComparator());
		sorter.setComparator(AccountsTableModel.COLUMN_CONFIRMED_BALANCE, new BigDecimalStringComparator());
		
		//ON FAVORITES CHANGE
		cbxFavorites.addItemListener(this);
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	
		
		JMenuItem copyAddress = new JMenuItem(lang.Translate("Copy Address"));
		copyAddress.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyAddress);
				
		JMenuItem copyBalance = new JMenuItem(lang.Translate("Copy Balance"));
		copyBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getUnconfirmedBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		
		menu.add(copyBalance);
		
		JMenuItem copyConfirmedBalance = new JMenuItem(lang.Translate("Copy Confirmed Balance"));
		copyConfirmedBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getConfirmedBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyConfirmedBalance);
		
		JMenuItem copyGeneratingBalance = new JMenuItem(lang.Translate("Copy Generating Balance"));
		copyGeneratingBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getGeneratingBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyGeneratingBalance);
		
		TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
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
		
		//ADD ACCOUNTS TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		//ADD TOTAL BALANCE
		final JLabel totalBalance = new JLabel(lang.Translate("Confirmed Balance") + ": " + tableModel.getTotalBalance().toPlainString());
		this.add(totalBalance, buttonGBC);
		
		//ON TABLE CHANGE
		table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent arg0) {
				totalBalance.setText(lang.Translate("Confirmed Balance") + ": " + NumberAsString.getInstance().numberAsString(tableModel.getTotalBalance()));				
			}		
		});
		
		//ADD NEW ACCOUNT BUTTON
		buttonGBC.gridy++;
		JButton newButton = new JButton(lang.Translate("New account"));
		newButton.setPreferredSize(new Dimension(150, 25));
		newButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onNewClick();
		    }
		});	
		this.add(newButton, buttonGBC);
	}
	
	public static Asset getAsset()
	{
		return (Asset) cbxFavorites.getSelectedItem();
	}

	public void onNewClick()
	{
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, lang.Translate("Invalid password"),
						lang.Translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//GENERATE NEW ACCOUNT
		Controller.getInstance().generateNewAccount();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) 
	{
		if(e.getStateChange() == ItemEvent.SELECTED) 
		{		
			Asset asset = (Asset) cbxFavorites.getSelectedItem();
        	tableModel.setAsset(asset);  
		} 
	}
}
