package gui.models;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.bouncycastle.crypto.InvalidCipherTextException;

import controller.Controller;
import database.DBSet;
import gui.PasswordPane;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.block.Block;
import qora.crypto.AEScrypto;
import qora.transaction.MessageTransaction;
import qora.transaction.Transaction;
import qora.wallet.Wallet;
import utils.Converter;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.TableMenuPopupUtil;

@SuppressWarnings("serial")
public class MessagesTableModel extends JTable implements Observer{
	private ArrayList<MessageBuf> messageBufs;
	Comparator<MessageBuf> comparator = new Comparator<MessageBuf>() {
	    public int compare(MessageBuf c1, MessageBuf c2) {
	        return (int) (c2.getTimestamp() - c1.getTimestamp());
	    }
	};

	JMenuItem menuDecrypt;
	private DefaultTableModel messagesModel;
	int width;
	int fontHeight;
	
	public MessagesTableModel()
	{
		this.setShowGrid(false);

		fontHeight = this.getFontMetrics(this.getFont()).getHeight();

		messageBufs = new ArrayList<MessageBuf>();
		messagesModel = new DefaultTableModel();
		this.setModel(messagesModel);
		messagesModel.addColumn("");
		
		DefaultTableCellRenderer topRenderer = new DefaultTableCellRenderer();
		topRenderer.setVerticalAlignment(DefaultTableCellRenderer.TOP);
		this.getColumn("").setCellRenderer( topRenderer );
		
		List<Transaction> transactions = new ArrayList<Transaction>();;

		for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions()) {
			if(transaction.getType() == Transaction.MESSAGE_TRANSACTION)
			{
				transactions.add(transaction);
			}
		}
		
		for (Account account : Controller.getInstance().getAccounts()) {
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(account.getAddress(), Transaction.MESSAGE_TRANSACTION, 0));	
		}
		
		for (Transaction messagetx : transactions) {
			boolean is = false;
			for (MessageBuf message : messageBufs) {
				if(Arrays.equals(messagetx.getSignature(), message.getSignature()))
				{
					is = true;
					break;
				}
			}
			if(!is)
			{
				addMessage(messageBufs.size(),(MessageTransaction)messagetx);
			}
		}
				
		Collections.sort(messageBufs, comparator);
		
		messagesModel.setRowCount(messageBufs.size());
		for ( int j = messageBufs.size()-1; j >= 0; j-- )
		{
			setHeight(j);	
		}
		
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	

		JMenuItem copyMessage = new JMenuItem("Copy Message");
		copyMessage.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getDecrMessageTXT());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyMessage);
		
		JMenuItem copyAllMessages = new JMenuItem("Copy All Messages");
		copyAllMessages.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				String strvalue = "";
	
				for (int i = 0; i < messageBufs.size(); i++) {
					strvalue += messageBufs.get(i).getDecrMessageTXT() +"\n"; 
				}
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(strvalue);
			    clipboard.setContents(value, null);
		        
			}
		});
		menu.add(copyAllMessages);

		JMenuItem copySender = new JMenuItem("Copy Sender Address");
		copySender.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); 
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getSender());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copySender);
				
		JMenuItem copyRecipient = new JMenuItem("Copy Recipient Address");
		copyRecipient.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); 
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getRecipient());
			    clipboard.setContents(value, null);
			}
		});
		
		menu.add(copyRecipient);
		
		menuDecrypt = new JMenuItem("Decrypt");
		menuDecrypt.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); 
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);
				
				CryptoOpenBox(row, 0);
				
				invokerAsJComponent.repaint();
			}
	
		});
		
		menu.add(menuDecrypt);
		
		TableMenuPopupUtil.installContextMenu(this, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
        this.addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent e) 
				{
					if(e.getClickCount() == 2) 
					{
						MessagesTableModel tableModelparent = (MessagesTableModel) e.getSource();
						
				        Point p = e.getPoint();
					    int row = tableModelparent.rowAtPoint(p);
					    
					    CryptoOpenBox(row, 0);	
					    tableModelparent.repaint();
				    }
				}
			});
        
        
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				int row = lsm.getMinSelectionIndex();

				if(row > -1)
				{
					if(!messageBufs.get(row).getEncrypted())
					{
						menuDecrypt.setVisible(false);
					}
					else
					{
						menuDecrypt.setVisible(true);
					}	
					if(messageBufs.get(row).getOpend())
					{
						menuDecrypt.setText("Hide decrypted");
					}
					else
					{
						menuDecrypt.setText("Decrypt");
					}
				}
			}
		});

		Controller.getInstance().addWalletListener(this);
		DBSet.getInstance().getBlockMap().addObserver(this);
	}

	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Object value = getValueAt(row, column);
		
		boolean isSelected = false;
		boolean hasFocus = false;
		
		// Only indicate the selection and focused cell if not printing
		if (!isPaintingForPrint()) {
			isSelected = isCellSelected(row, column);
		
			boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
			boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
		
			hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
		}
		JComponent cellRenderer = (JComponent) renderer.getTableCellRendererComponent(this, value, isSelected,
		hasFocus, row, column);
		
		if (isSelected && hasFocus) {
			cellRenderer.setBorder(BorderFactory.createLineBorder(new Color(102, 167, 232, 255), 1));
		} else {
			cellRenderer.setBorder(BorderFactory.createLineBorder(new Color(205, 205, 205, 255), 1));
		}
		return cellRenderer;
	}
   
	@Override
	public boolean isCellEditable(int row, int column) {
		//all cells false
	    return false;
	}
	 

	@Override
	public Object getValueAt(int row, int column)
	{
		return messageBufs.get(row).getDecrMessageHtml(this.getWidth(), (this.getSelectedRow() == row), true);
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	public synchronized void syncUpdate(Observable o, Object arg)
	{

		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.WALLET_STATUS)
		{
			int status = (int) message.getValue();
			
			if(status == Wallet.STATUS_LOCKED)
			{
				cryptoCloseAll();
			}
		}
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE
				|| message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK)
			{
				this.repaint();
			}
			else if (message.getType() == ObserverMessage.ADD_BLOCK_TYPE &&
					((Block)message.getValue()).getHeight() == Controller.getInstance().getMaxPeerHeight())
			{
				this.repaint();
			}
			
		}

		if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{		
			boolean is;
			if(((Transaction) message.getValue()).getType() == Transaction.MESSAGE_TRANSACTION)
			{
				is = false;
				for ( int i = messageBufs.size()-1; i >= 0; i-- )
				for (MessageBuf messageBuf : messageBufs) {
					if(Arrays.equals(((MessageTransaction) message.getValue()).getSignature(), messageBuf.getSignature()))
					{
						is = true;
						break;
					}
				}
				if(!is)
				{
					addMessage(0, (MessageTransaction) message.getValue());
					
					messagesModel.setRowCount( messageBufs.size() );
					
					for ( int j = messageBufs.size()-1; j >= 0; j-- )
					{
						setHeight(j);	
					}
					
					if(messageBufs.get(1).getOpend() && Controller.getInstance().isWalletUnlocked())
					{
						CryptoOpenBox( 0, 1 );
					}
					
					this.repaint();
				}
				
			}
		}
	}

	private void addMessage(int pos, MessageTransaction transaction)
	{
		messageBufs.add(pos, new MessageBuf(
				transaction.getData(), 
				transaction.isEncrypted(),
				transaction.getSender().getAddress(),
				transaction.getRecipient().getAddress(),
				transaction.getTimestamp(),
				transaction.getAmount(),
				transaction.getKey(),
				transaction.getFee(),
				transaction.getSignature(),
				transaction.getCreator().getPublicKey(),
				transaction.isText()
		));
	}
	
	public void cryptoCloseAll()
	{
		for (int i = 0; i < messageBufs.size(); i++) {
			CryptoOpenBox(i, 2);
		}
		
		menuDecrypt.setText("Decrypt");
		this.repaint();
	}
	
	
	public void CryptoOpenBoxAll()
	{
		int toOpen = 0;
		if(messageBufs.size()>0 && messageBufs.get(0).getOpend())
		{
			toOpen = 2;
		}
		else
		{
			toOpen = 1;
		}
	
		for (int i = 0; i < messageBufs.size(); i++) {
			CryptoOpenBox(i, toOpen);
		}
	}
	
	private void CryptoOpenBox(int row, int toOpen)
	{
		// toOpen 0 - switch, 1 - open, 2 - close
	
		if(messageBufs.get(row).getEncrypted())
		{
			if(toOpen != 2 && !messageBufs.get(row).getOpend())
			{
				if( !Controller.getInstance().isWalletUnlocked() )
				{
					//ASK FOR PASSWORD
					String password = PasswordPane.showUnlockWalletDialog(); 
					if( password.equals("") )
					{
						return;
					}
					if( !Controller.getInstance().unlockWallet(password) )
					{
						//WRONG PASSWORD
						JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
						
						return;
					}
				}
		
				Account account = Controller.getInstance().getAccountByAddress(messageBufs.get(row).getSender());	
				
				byte[] privateKey = null; 
				byte[] publicKey = null;
				//IF SENDER ANOTHER
				if( account == null )
				{
		    		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(messageBufs.get(row).getRecipient());
					privateKey = accountRecipient.getPrivateKey();		
					
					publicKey = messageBufs.get(row).getSenderPublicKey();    				
				}
				//IF SENDER ME
				else
				{
		    		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
					privateKey = accountRecipient.getPrivateKey();		
					
					if( messageBufs.get(row).getToPublicKey() == null )
					{
						messageBufs.get(row).setRecipientPublicKey(Controller.getInstance().getPublicKeyByAddress( messageBufs.get(row).getRecipient()));
					}
					publicKey = messageBufs.get(row).getToPublicKey();    				
				}
				
				byte[] decrypt = null;
				
				try {
					decrypt = AEScrypto.dataDecrypt(messageBufs.get(row).getMessage(), privateKey, publicKey);
				} catch ( InvalidCipherTextException | NullPointerException e1 ) {
					messageBufs.get(row).setDecryptedMessage("Decrypt Error!");
				} 
		
				if(decrypt == null)
				{
					messageBufs.get(row).setDecryptedMessage("Decrypt Error!");			        			
				}
				else
				{
					messageBufs.get(row).setDecryptedMessage(( messageBufs.get(row).isText() ) ? new String(decrypt, Charset.forName("UTF-8")) : Converter.toHex(decrypt));
					messageBufs.get(row).setOpend(true);
					menuDecrypt.setText("Hide decrypted");
				}
			}
			else
			{
				if(toOpen != 1)
				{
					messageBufs.get(row).setDecryptedMessage("");
					messageBufs.get(row).setOpend(false);
					menuDecrypt.setText("Decrypt");
				}
			}
	
		
			setHeight(row);
		}	
	} 
	
	private void setHeight(int row)
	{
		int textHeight = (3+lineCount(messageBufs.get(row).getDecrMessage()))*fontHeight;
		if(textHeight< 24 + 3*fontHeight)
		{
			textHeight = 24 + 3*fontHeight;
		}
		this.setRowHeight(row, textHeight);
	}
	
	int lineCount( String text ) 
	{
		int lineCount = 1;
		
		for(int k = 0; k < text.length(); k++) 
	    {
	       if( text.charAt(k) == '\n' )
	       {
	    	   lineCount++;
	       }
	    }
		return lineCount;
	}

	public class MessageBuf
	{
		private byte[] rawMessage;
		private String decryptedMessage;
		private boolean encrypted;
		private boolean opened;
		private boolean isText;
		private String sender;
		private byte[] senderPublicKey;
		private String recipient;
		private byte[] recipientPublicKey;
		private long timestamp;
		private BigDecimal amount;
		private long assetKey;
		private BigDecimal fee;
		private byte[] signature;
		
		public MessageBuf( byte[] rawMessage, boolean encrypted, String sender, String recipient, long timestamp, BigDecimal amount, long assetKey, BigDecimal fee, byte[] signature, byte[] senderPublicKey, boolean isText )
		{
			this.rawMessage = rawMessage;
			this.encrypted = encrypted;	
			this.decryptedMessage = "";
			this.opened = false;
			this.sender = sender;
			this.recipient = recipient;
			this.timestamp = timestamp;
			this.amount = amount;
			this.assetKey = assetKey;
			this.fee = fee;
			this.senderPublicKey = senderPublicKey;
			this.recipientPublicKey = null;
			this.signature = signature;
			this.isText = isText;
		}

		public byte[] getMessage()
		{
			return this.rawMessage;
		}
		public boolean getEncrypted()
		{
			return this.encrypted;
		}
		public String getDecrMessage()
		{
			if( decryptedMessage.equals("") )
			{
				if( this.encrypted && !this.opened )
				{
					this.decryptedMessage = "Encrypted";
				}
				if( !this.encrypted )
				{
					this.decryptedMessage = ( isText ) ? new String( this.rawMessage, Charset.forName("UTF-8") ) : Converter.toHex( this.rawMessage );
				}
			}
			return this.decryptedMessage;
		}
		public String getSender()
		{
			return this.sender;
		}
		public String getRecipient()
		{
			return this.recipient;
		}
		public BigDecimal getFee()
		{
			return this.fee;
		}
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		public long getAssetKey()
		{
			return this.assetKey;
		}
		public byte[] getSignature()
		{
			return this.signature;
		}
		public byte[] getSenderPublicKey()
		{
			return this.senderPublicKey;
		}
		public byte[] getToPublicKey()
		{
			return this.recipientPublicKey;
		}
		public void setRecipientPublicKey(byte[] recipientPublicKey)
		{
			this.recipientPublicKey = recipientPublicKey;
		}
		public long getTimestamp()
		{
			return this.timestamp;
		}
		public boolean getOpend()
		{
			return this.opened;
		}
		public void setOpend(boolean opened)
		{
			this.opened = opened;
		}
		public void setDecryptedMessage(String decryptedMessage)
		{
			this.decryptedMessage = decryptedMessage;
		}
		public int getConfirmations()
		{
			
			if( DBSet.getInstance().getTransactionMap().contains(this.signature) )
			{
				return 0;
			}
			else
			{
				Transaction tx = Controller.getInstance().getTransaction(this.signature);
				if(tx != null)
				{
					return tx.getConfirmations();	
				}
				else
				{
					return 0;
				}
			}
			
		}

		public boolean isText()
		{
			return isText;
		}
		
		public String getDecrMessageHtml(int width, boolean selected, boolean images)
		{
			Account account = Controller.getInstance().getAccountByAddress( this.sender );
			String imginout = "";
			if(account != null)
		    {
				imginout = "<img src='file:images/messages/receive.png'>";
			}
			else
			{
				imginout = "<img src='file:images/messages/send.png'>";
		    }
			
			String imgLock = "";
			
			if(this.encrypted)
			{	
				if(this.opened)
				{
					imgLock = "<img src='file:images/messages/unlocked.png'>";
				}
				else
				{
					imgLock = "<img src='file:images/messages/locked.png'>";
				}
			}
			else
			{
				imgLock = "<img src='file:images/messages/unlockedred.png'>";
			}
		
			int confirmations = this.getConfirmations();
			
			String strconfirmations = Integer.toString( confirmations );
			
			if( confirmations < 1 )
			{
				strconfirmations = "<font color=red>" + strconfirmations +"</font>";
			}
			
			String colorHeader = "F0F0F0";
			String colorTextHeader = "000000";
			String colorTextMessage = "000000";
			String colorTextBackground = "FFFFFF";
			
			
			if( selected )
			{
				colorHeader = "C4DAEF";
				colorTextBackground = "D1E8FF";
			}
			
			if( this.encrypted )
			{
				if( this.opened )
				{
					colorTextMessage = "0000FF";
				}
				else
				{
					colorTextMessage = "FF0000";
				}	
			}
			
			String decrMessage = this.getDecrMessage(); 
			decrMessage = decrMessage.replace( "<" , "&lt;" );
			decrMessage = decrMessage.replace( ">" , "&gt;" );
			decrMessage = decrMessage.replace( "\n" , "<br>" );
			
			String fontSize = "";
			
			if(this.amount.compareTo(new BigDecimal(10)) >= 0)
			{
				fontSize = " size='2'";
			} else {
				fontSize = " size='2'";
			}
			

			String strAsset = Controller.getInstance().getAsset(this.getAssetKey()).getShort();
		
			return	  "<html>\n"
					+ "<body width='" + width + "'>\n"
					+ "<table border='0' cellpadding='3' cellspacing='0'><tr>\n<td bgcolor='" + colorHeader + "' width='" + (width/2-1) + "'>\n"
					+ "<font size='2' color='" + colorTextHeader + "'>\nFrom:" + this.sender
					+ "\n<br>\nTo:"
					+ this.recipient + "\n</font></td>\n"
					+ "<td bgcolor='" + colorHeader + "' align='right' width='" + (width/2-1) + "'>\n"
					+ "<font size='2' color='" + colorTextHeader + "'>\n" + strconfirmations + " . "
					+ DateTimeFormat.timestamptoString(this.timestamp) + "\n<br></font>\n"
					+ "<font" + fontSize + " color='" + colorTextHeader + "'>Amount: " +  NumberAsString.getInstance().numberAsString(this.amount) + " " + strAsset + " . Fee: "
					+ NumberAsString.getInstance().numberAsString(fee)+"</font>"
					+ "</td></tr></table>"
					+ "<table border='0' cellpadding='3' cellspacing='0'>\n<tr bgcolor='"+colorTextBackground+"'><td width='25'>"+imginout
					+ "<td width='" + width + "'>\n"
					+ "<font size='2.5' color='" + colorTextMessage + "'>\n"
					+ decrMessage
					+ "\n</font>"
					+ "<td width='30'>"+ imgLock
					+ "</td></tr>\n</table>\n"
					+ "</body></html>\n";
		}
		
		public String getDecrMessageTXT()
		{
			Account account = Controller.getInstance().getAccountByAddress( this.sender );
			
			String imginout = "";
			if( account != null )
		    {
				imginout = "Receive";
			}
			else
			{
				imginout = "Send'>";
		    }
			
			String imgLock = "";
			
			if( this.encrypted )
			{	
				if( this.opened )
				{
					imgLock = "Decrypted";
				}
				else
				{
					imgLock = "Encrypted";
				}
			}
			else
			{
				imgLock = "Unencrypted";
			}
			
			int confirmations = this.getConfirmations();
			
			String strConfirmations = Integer.toString( confirmations );
			
			if( confirmations < 1 )
			{
				strConfirmations = strConfirmations + " !";
			}
			
			String strAsset = Controller.getInstance().getAsset(this.getAssetKey()).getShort();
			
			return 	  "Date: " + DateTimeFormat.timestamptoString(this.timestamp) + "\n"
					+ "Sender: " + this.sender + "\n"
					+ "Recipient: " + this.recipient + "\n"
					+ "Amount: " +  NumberAsString.getInstance().numberAsString(this.amount) + " " + strAsset + " . Fee: " + NumberAsString.getInstance().numberAsString(this.fee) + "\n"
					+ "Type: " + imginout + ". " + imgLock + "\n"
					+ "Confirmations: " + strConfirmations + "\n"
					+ "[MESSAGE START]\n"
					+ getDecrMessage() + "\n"
					+ "[MESSAGE END]\n";
		}
	}

}

