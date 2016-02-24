package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import gui.assets.AssetsPanel;
import gui.at.ATPanel;
import gui.at.ATTransactionsPanel;
import gui.at.AcctPanel;
import gui.models.WalletBlocksTableModel;
import gui.models.WalletTransactionsTableModel;
import gui.naming.NamingServicePanel;
import gui.transaction.TransactionDetailsFactory;
import gui.voting.VotingPanel;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import database.wallet.BlockMap;
import database.wallet.TransactionMap;
import qora.transaction.Transaction;
import lang.lang;

public class GeneralTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private WalletTransactionsTableModel transactionsModel;
	private JTable transactionsTable;
	
	public GeneralTabPane()
	{
		super();
		
		//ACCOUNTS
		this.addTab(lang.Translate("Accounts"), new AccountsPanel());
        
		//SEND
		this.addTab(lang.Translate("Send money"), new SendMoneyPanel());

		//MESSAGE
		this.addTab(lang.Translate("Messages"), new SendMessagePanel());
		
		//TRANSACTIONS
		this.transactionsModel = new WalletTransactionsTableModel();
		this.transactionsTable = new JTable(this.transactionsModel);
		
		//TRANSACTIONS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS, TransactionMap.TIMESTAMP_INDEX);
		indexes.put(WalletTransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
		indexes.put(WalletTransactionsTableModel.COLUMN_ADDRESS, TransactionMap.ADDRESS_INDEX);
		indexes.put(WalletTransactionsTableModel.COLUMN_AMOUNT, TransactionMap.AMOUNT_INDEX);
		QoraRowSorter sorter = new QoraRowSorter(transactionsModel, indexes);
		transactionsTable.setRowSorter(sorter);
		
		//TRANSACTION DETAILS
		this.transactionsTable.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				if(e.getClickCount() == 2) 
				{
					//GET ROW
			        int row = transactionsTable.getSelectedRow();
			        row = transactionsTable.convertRowIndexToModel(row);
			        
			        //GET TRANSACTION
			        Transaction transaction = transactionsModel.getTransaction(row);
			         
			        //SHOW DETAIL SCREEN OF TRANSACTION
			        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
			    }
			}
		});			
		this.addTab(lang.Translate("Transactions"), new JScrollPane(this.transactionsTable));       
		
		//TRANSACTIONS
		WalletBlocksTableModel blocksModel = new WalletBlocksTableModel();
		JTable blocksTable = new JTable(blocksModel);
				
		//TRANSACTIONS SORTER
		indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletBlocksTableModel.COLUMN_HEIGHT, BlockMap.TIMESTAMP_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_TIMESTAMP, BlockMap.TIMESTAMP_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_GENERATOR, BlockMap.GENERATOR_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_BASETARGET, BlockMap.BALANCE_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_TRANSACTIONS, BlockMap.TRANSACTIONS_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_FEE, BlockMap.FEE_INDEX);
		sorter = new QoraRowSorter(blocksModel, indexes);
		blocksTable.setRowSorter(sorter);
		
        this.addTab(lang.Translate("Generated Blocks"), new JScrollPane(blocksTable));
        
        //NAMING
        this.addTab(lang.Translate("Naming service"), new NamingServicePanel());      
        
        //VOTING
        this.addTab(lang.Translate("Voting"), new VotingPanel());       
        
        //ASSETS
        this.addTab(lang.Translate("Assets"), new AssetsPanel());

		//ATs
		this.addTab("AT", new ATPanel());

		//AT TXs
		this.addTab("AT Transactions", new ATTransactionsPanel());

		//AT Acct
		this.addTab("ACCT", new AcctPanel());
	}
	
}
