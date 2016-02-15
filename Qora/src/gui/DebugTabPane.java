package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import database.BlockMap;
import database.TransactionMap;
import gui.models.BlocksTableModel;
import gui.models.PeersTableModel;
import gui.models.TransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;
import qora.transaction.Transaction;
import settings.Settings;

public class DebugTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private PeersTableModel peersTableModel;
	private TransactionsTableModel transactionsTableModel;
	private BlocksTableModel blocksTableModel;
	private LoggerTextArea loggerTextArea;
	private JTable transactionsTable;
	
	public DebugTabPane()
	{
		super();
		
		//ADD TABS
        if(Settings.getInstance().isGuiConsoleEnabled())
        {
        	this.addTab("Console", new ConsolePanel());
        }
        
        this.peersTableModel = new PeersTableModel();
		this.addTab("Peers", new JScrollPane(new JTable(this.peersTableModel)));
        
		//TRANSACTIONS TABLE MODEL
		this.transactionsTableModel = new TransactionsTableModel();
		this.transactionsTable = new JTable(this.transactionsTableModel);
		
		//TRANSACTIONS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(TransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
		QoraRowSorter sorter = new QoraRowSorter(transactionsTableModel, indexes);
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
			        Transaction transaction = transactionsTableModel.getTransaction(row);
			         
			        //SHOW DETAIL SCREEN OF TRANSACTION
			        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
			    }
			}
		});
		
		//ADD TRANSACTIONS TABLE
		this.addTab("Transactions", new JScrollPane(this.transactionsTable)); 
	           
		//BLOCKS TABLE MODEL
		this.blocksTableModel = new BlocksTableModel();
		JTable blocksTable = new JTable(this.blocksTableModel);
		
		//BLOCKS SORTER
		indexes = new TreeMap<Integer, Integer>();
		indexes.put(BlocksTableModel.COLUMN_HEIGHT, BlockMap.HEIGHT_INDEX);
		sorter = new QoraRowSorter(blocksTableModel, indexes);
		blocksTable.setRowSorter(sorter);
		
		//ADD BLOCK TABLE
		this.addTab("Blocks", new JScrollPane(blocksTable));
		
        this.loggerTextArea = new LoggerTextArea(Logger.getGlobal());
        JScrollPane scrollPane = new JScrollPane(this.loggerTextArea);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
        this.addTab("Logger", scrollPane);
	}

	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.peersTableModel.removeObservers();
		
		this.transactionsTableModel.removeObservers();
		
		this.blocksTableModel.removeObservers();
		
		this.loggerTextArea.removeNotify();	
	}
	
}
