package gui;

import gui.models.AccountsComboBoxModel;
import gui.models.AssetsComboBoxModel;
import gui.models.MessagesTableModel;
import ntp.NTP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.crypto.AEScrypto;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import settings.Settings;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")

public class SendMessagePanel extends JPanel 
{
	//private final MessagesTableModel messagesTableModel;
    private final JTable table;
    
	private JComboBox<Account> cbxFrom;
	private JTextField txtTo;
	private JTextField txtAmount;
	private JTextField txtFee;
	public JTextArea txtMessage;
	private JCheckBox encrypted;
	private JCheckBox isText;
	private JButton sendButton;
	private AccountsComboBoxModel accountsModel;
	private JComboBox<Asset> cbxFavorites;
	private JTextField txtRecDetails;
	private JLabel messageLabel;
	
	public SendMessagePanel()
	{
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		this.setLayout(gridBagLayout);
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//ASSET FAVORITES
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = 0;	
		
		cbxFavorites = new JComboBox<Asset>(new AssetsComboBoxModel());
		this.add(cbxFavorites, favoritesGBC);
		this.accountsModel = new AccountsComboBoxModel();
        
		//LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 5, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
		labelFromGBC.weightx = 0;	
		labelFromGBC.gridx = 0;
		labelFromGBC.gridy = 1;
		JLabel fromLabel = new JLabel("From:");
		this.add(fromLabel, labelFromGBC);
		//fontHeight = fromLabel.getFontMetrics(fromLabel.getFont()).getHeight();
		
		//COMBOBOX FROM
		GridBagConstraints cbxFromGBC = new GridBagConstraints();
		cbxFromGBC.gridwidth = 4;
		cbxFromGBC.insets = new Insets(5, 5, 5, 0);
		cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxFromGBC.weightx = 0;	
		cbxFromGBC.gridx = 1;
		cbxFromGBC.gridy = 1;
		
		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);
		
		//ON FAVORITES CHANGE

		cbxFavorites.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {

		    	Asset asset = ((Asset) cbxFavorites.getSelectedItem());

		    	if(asset != null)
		    	{
		    		((AccountRenderer)cbxFrom.getRenderer()).setAsset(asset.getKey());
		    		cbxFrom.repaint();
		    		refreshReceiverDetails();
		    	}

		    }
		});
		
		//LABEL TO
		GridBagConstraints labelToGBC = new GridBagConstraints();
		labelToGBC.gridy = 2;
		labelToGBC.insets = new Insets(5,5,5,5);
		labelToGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelToGBC.anchor = GridBagConstraints.NORTHWEST;
		labelToGBC.weightx = 0;	
		labelToGBC.gridx = 0;
		JLabel toLabel = new JLabel("To: (address or name)");
		this.add(toLabel, labelToGBC);
      	
      	//TXT TO
		GridBagConstraints txtToGBC = new GridBagConstraints();
		txtToGBC.gridwidth = 4;
		txtToGBC.insets = new Insets(5, 5, 5, 0);
		txtToGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtToGBC.anchor = GridBagConstraints.NORTHWEST;
		txtToGBC.weightx = 0;	
		txtToGBC.gridx = 1;
		txtToGBC.gridy = 2;

		txtTo = new JTextField();
		this.add(txtTo, txtToGBC);
		
        txtTo.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				refreshReceiverDetails();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				refreshReceiverDetails();
			}
        });
		        
      	
		//LABEL RECEIVER
		GridBagConstraints labelDetailsGBC = new GridBagConstraints();
		labelDetailsGBC.gridy = 3;
		labelDetailsGBC.insets = new Insets(5,5,5,5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
		labelDetailsGBC.weightx = 0;	
		labelDetailsGBC.gridx = 0;
      	JLabel recDetailsLabel = new JLabel("Receiver details:");
      	this.add(recDetailsLabel, labelDetailsGBC);
        
      	//RECEIVER DETAILS 
		GridBagConstraints txtReceiverGBC = new GridBagConstraints();
		txtReceiverGBC.gridwidth = 4;
		txtReceiverGBC.insets = new Insets(5, 5, 5, 0);
		txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtReceiverGBC.anchor = GridBagConstraints.NORTHWEST;
		txtReceiverGBC.weightx = 0;	
		txtReceiverGBC.gridx = 1;
      	txtReceiverGBC.gridy = 3;

      	txtRecDetails = new JTextField();
      	txtRecDetails.setEditable(false);
      	this.add(txtRecDetails, txtReceiverGBC);
      	
      	//LABEL MESSAGE
      	GridBagConstraints labelMessageGBC = new GridBagConstraints();
      	labelMessageGBC.insets = new Insets(5,5,5,5);
      	labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
      	labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
      	labelMessageGBC.weightx = 0;	
      	labelMessageGBC.gridx = 0;
      	labelMessageGBC.gridy = 4;
      	
      	messageLabel = new JLabel("Message:");
      	
		//TXT MESSAGE
		GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 0);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;	
		txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = 4;
        
        this.txtMessage = new JTextArea();
        this.txtMessage.setRows(4);
        this.txtMessage.setColumns(25);

        this.txtMessage.setBorder(this.txtTo.getBorder());

      	JScrollPane messageScroll = new JScrollPane(this.txtMessage);
      	messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(messageScroll, txtMessageGBC);
      	
      	this.add(messageLabel, labelMessageGBC);
      	
		//LABEL ISTEXT
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.gridy = 5;
		labelIsTextGBC.insets = new Insets(5,5,5,5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;	
		labelIsTextGBC.gridx = 0;     

		final JLabel isTextLabel = new JLabel("Text Message:");
      	isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      	this.add(isTextLabel, labelIsTextGBC);
     	
        //TEXT ISTEXT
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5,5,5,5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
		isChkTextGBC.weightx = 0;	
		isChkTextGBC.gridx = 1;
		isChkTextGBC.gridy = 5;
        
		isText = new JCheckBox();
        isText.setSelected(true);
        this.add(isText, isChkTextGBC);

        //LABEL ENCRYPTED
		GridBagConstraints labelEncGBC = new GridBagConstraints();
		labelEncGBC.insets = new Insets(5,5,5,5);
		labelEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelEncGBC.anchor = GridBagConstraints.NORTHWEST;
		labelEncGBC.weightx = 0;	
		labelEncGBC.gridx = 4;
		labelEncGBC.gridx = 2;
		labelEncGBC.gridy = 5;
		
		JLabel encLabel = new JLabel("Encrypt Message:");
		encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(encLabel, labelEncGBC);
		
        //ENCRYPTED CHECKBOX
		GridBagConstraints ChkEncGBC = new GridBagConstraints();
		ChkEncGBC.insets = new Insets(5,5,5,5);
		ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		ChkEncGBC.anchor = GridBagConstraints.NORTHWEST;
		ChkEncGBC.weightx = 0;	
		ChkEncGBC.gridx = 3;
		ChkEncGBC.gridy = 5;
		
		encrypted = new JCheckBox();
		encrypted.setSelected(true);
		this.add(encrypted, ChkEncGBC);
		
    	//LABEL AMOUNT
		GridBagConstraints amountlabelGBC = new GridBagConstraints();
		amountlabelGBC.insets = new Insets(5,5,5,5);
		amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		amountlabelGBC.anchor = GridBagConstraints.NORTHWEST;
		amountlabelGBC.weightx = 0;	
		amountlabelGBC.gridx = 0;
		amountlabelGBC.gridy = 6;
		
		final JLabel amountLabel = new JLabel("Amount:");
		amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(amountLabel, amountlabelGBC);
        
      	//TXT AMOUNT
		GridBagConstraints txtAmountGBC = new GridBagConstraints();
		txtAmountGBC.insets = new Insets(5,5,5,5);
		txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtAmountGBC.anchor = GridBagConstraints.NORTHWEST;
		txtAmountGBC.weightx = 0;	
		txtAmountGBC.gridx = 1;
		txtAmountGBC.gridy = 6;
		
		txtAmount = new JTextField("1.00000000");
		txtAmount.setPreferredSize(new Dimension(130,22));
		this.add(txtAmount, txtAmountGBC);
		
        //BUTTON SEND
        GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.BOTH;  
		buttonGBC.anchor = GridBagConstraints.PAGE_START;
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 11;
        
		sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 25));
    	sendButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onSendClick();
		    }
		});	
		this.add(sendButton, buttonGBC);
		
    	//LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.EAST;
		feelabelGBC.gridy = 6;
		feelabelGBC.insets = new Insets(5,5,5,5);
		feelabelGBC.fill = GridBagConstraints.BOTH;
		feelabelGBC.weightx = 0;	
		feelabelGBC.gridx = 2;
		final JLabel feeLabel = new JLabel("Fee:");
		feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		feeLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(feeLabel, feelabelGBC);
		
		//FEE TXT
		GridBagConstraints feetxtGBC = new GridBagConstraints();
		feetxtGBC.fill = GridBagConstraints.BOTH;
		feetxtGBC.insets = new Insets(5, 5, 5, 5);
		feetxtGBC.anchor = GridBagConstraints.NORTH;
		feetxtGBC.gridx = 3;	
		feetxtGBC.gridy = 6;

		txtFee = new JTextField();
		txtFee.setText("1.00000000");
		txtFee.setPreferredSize(new Dimension(130,22));
		this.add(txtFee, feetxtGBC);
		
		//BUTTON DECRYPTALL
		GridBagConstraints decryptAllGBC = new GridBagConstraints();
		decryptAllGBC.insets = new Insets(5,5,5,5);
		decryptAllGBC.fill = GridBagConstraints.HORIZONTAL;  
		decryptAllGBC.anchor = GridBagConstraints.NORTHWEST;
		decryptAllGBC.gridwidth = 1;
		decryptAllGBC.gridx = 3;
		decryptAllGBC.gridy = 11;
		JButton decryptButton = new JButton("Decrypt All");
    	this.add(decryptButton, decryptAllGBC);
		
		//MESSAGES HISTORY TABLE

    	table = new MessagesTableModel();
    	
    	table.setTableHeader(null);
    	table.setSelectionBackground(new Color(209, 232, 255, 255));
    	table.setEditingColumn(0);
    	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(100, 100));
        scrollPane.setWheelScrollingEnabled(true);

        //BOTTOM GBC
		GridBagConstraints messagesGBC = new GridBagConstraints();
		messagesGBC.insets = new Insets(5,5,5,5);
		messagesGBC.fill = GridBagConstraints.BOTH;   
		messagesGBC.anchor = GridBagConstraints.NORTHWEST;
		messagesGBC.weightx = 0;	
		messagesGBC.gridx = 0;
		
        //ADD BOTTOM SO IT PUSHES TO TOP
		messagesGBC.gridy = 13;
		messagesGBC.weighty = 4;
		messagesGBC.gridwidth = 5;
		
        add(scrollPane, messagesGBC);
 
		//BUTTON DECRYPTALL
    	decryptButton.addActionListener(new ActionListener()
    	{
		    public void actionPerformed(ActionEvent e)
		    {
		    	((MessagesTableModel) table).CryptoOpenBoxAll();
		    }
		});	

        //CONTEXT MENU
		MenuPopupUtil.installContextMenu(txtTo);
		MenuPopupUtil.installContextMenu(txtFee);
		MenuPopupUtil.installContextMenu(txtAmount);
		MenuPopupUtil.installContextMenu(txtMessage);
		MenuPopupUtil.installContextMenu(txtRecDetails);
		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(	new Runnable() { 
			public void run() {
				
				messageLabel.setText("<html>Message:<br>("+ txtMessage.getText().length()+"/4000)</html>");
				
			}}, 0, 500, TimeUnit.MILLISECONDS);
	}

	private void refreshReceiverDetails()
	{
		String toValue = txtTo.getText();
		Asset asset = ((Asset) cbxFavorites.getSelectedItem());
		
		if(toValue.isEmpty())
		{
			txtRecDetails.setText("");
			return;
		}
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			txtRecDetails.setText("Status must be OK to show receiver details.");
			return;
		}
		
		Account account = null;
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(toValue))
		{
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(toValue);
					
			if(nameToAdress.getB() == NameResult.OK)
			{
				account = nameToAdress.getA();
				txtRecDetails.setText(account.toString(asset.getKey()));
			}
			else
			{
				txtRecDetails.setText(nameToAdress.getB().getShortStatusMessage());
			}
		}else
		{
			account = new Account(toValue);
			
			txtRecDetails.setText(account.toString(asset.getKey()));
			
			if(account.toString(asset.getKey()).equals("0.00000000"))
			{
				txtRecDetails.setText(txtRecDetails.getText()+ " - Warning!");
			}
		}
		
		if(account!=null && account.getAddress().startsWith("A"))
		{
			encrypted.setEnabled(false);
			encrypted.setSelected(false);
			isText.setSelected(false);
		}
		else
		{
			encrypted.setEnabled(true);
		}
	}
	
	public void onSendClick()
	{
		//DISABLE
		this.sendButton.setEnabled(false);
		
		//TODO TEST
		//CHECK IF NETWORK OKE
		/*if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.sendButton.setEnabled(true);
			
			return;
		}*/
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(password.equals(""))
			{
				this.sendButton.setEnabled(true);
				return;
			}
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sendButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ SENDER
		Account sender = (Account) cbxFrom.getSelectedItem();
		
		
		//READ RECIPIENT
		String recipientAddress = txtTo.getText();
		
		Account recipient;
		
		//ORDINARY RECIPIENT
		if(Crypto.getInstance().isValidAddress(recipientAddress))
		{
			recipient = new Account(recipientAddress);
		//NAME RECIPIENT
		}else
		{
			Pair<Account, NameResult> result = NameUtils.nameToAdress(recipientAddress);
			
			if(result.getB() == NameResult.OK)
			{
				recipient = result.getA();
			}
			else		
			{
				JOptionPane.showMessageDialog(null, result.getB().getShortStatusMessage() , "Error", JOptionPane.ERROR_MESSAGE);
		
				//ENABLE
				this.sendButton.setEnabled(true);
			
				return;
			}
		}
		
		int parsing = 0;
		try
		{
			//READ AMOUNT
			parsing = 1;
			BigDecimal amount = new BigDecimal(txtAmount.getText()).setScale(8);
			
			//READ FEE
			parsing = 2;
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sendButton.setEnabled(true);
				
				return;
			}
			
			//CHECK BIG FEE
			if(fee.compareTo(Settings.getInstance().getBigFee()) >= 0)
			{
				int n = JOptionPane.showConfirmDialog(
						new JFrame(), Settings.getInstance().getBigFeeMessage(),
		                "Confirmation",
		                JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					
				} else {
					
					txtFee.setText("1.00000000");
					
					//ENABLE
					this.sendButton.setEnabled(true);
					
					return;
				}
			}
			
			String message = txtMessage.getText();
			
			boolean isTextB = isText.isSelected();
			
			byte[] messageBytes;
			
			if ( isTextB )
			{
				messageBytes = message.getBytes( Charset.forName("UTF-8") );
			}
			else
			{
				try
				{
					messageBytes = Converter.parseHexString( message );
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(new JFrame(), "Message format is not hex!", "Error", JOptionPane.ERROR_MESSAGE);
					
					//ENABLE
					this.sendButton.setEnabled(true);
					
					return;
				}
			}
			if ( messageBytes.length < 1 || messageBytes.length > 4000 )
			{
				JOptionPane.showMessageDialog(new JFrame(), "Message size exceeded!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sendButton.setEnabled(true);
				
				return;
			}

			boolean encryptMessage = encrypted.isSelected();
		
			byte[] encrypted = (encryptMessage)?new byte[]{1}:new byte[]{0};
			byte[] isTextByte = (isTextB)? new byte[] {1}:new byte[]{0};
			
			//CHECK IF PAYMENT OR ASSET TRANSFER
			Asset asset = (Asset) this.cbxFavorites.getSelectedItem();
			long key = asset.getKey(); 
			
			Pair<Transaction, Integer> result;
			
			if(encryptMessage)
			{
				//sender
				PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress().toString());
				byte[] privateKey = account.getPrivateKey();		

				//recipient
				byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
				if(publicKey == null)
				{
					JOptionPane.showMessageDialog(new JFrame(), "The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him.", "Error", JOptionPane.ERROR_MESSAGE);

					//ENABLE
					this.sendButton.setEnabled(true);
					
					return;
				}
				
				messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
			}

			BigDecimal recommendedFee = Controller.getInstance().calcRecommendedFeeForMessage(messageBytes).getA();
			if(fee.compareTo(recommendedFee) < 0)
			{
				int n = -1;
				if(Settings.getInstance().isAllowFeeLessRequired())
				{
					n = JOptionPane.showConfirmDialog(
						new JFrame(), "Fee less than the recommended values!\nChange to recommended?\n"
									+ "Press Yes to turn on recommended "+recommendedFee.toPlainString()
									+ ",\nor No to leave, but then the transaction may be difficult to confirm.",
		                "Confirmation",
		                JOptionPane.YES_NO_CANCEL_OPTION);
				}
				else
				{
					n = JOptionPane.showConfirmDialog(
							new JFrame(), "Fee less required!\n"
										+ "Press OK to turn on required "+recommendedFee.toPlainString() + ".",
			                "Confirmation",
			                JOptionPane.OK_CANCEL_OPTION);
				}
				if (n == JOptionPane.YES_OPTION || n == JOptionPane.OK_OPTION) {
					
					if(fee.compareTo(new BigDecimal(1.0)) == 1) //IF MORE THAN ONE
					{
						this.txtFee.setText("1.00000000"); // Return to the default fee for the next message.
					}
					
					fee = recommendedFee; // Set recommended fee for this message.
					
				}
				else if (n == JOptionPane.NO_OPTION) {
					
				}	
				else {
					
					//ENABLE
					this.sendButton.setEnabled(true);
					
					return;
				}
			}
			
			if(key != 0l && NTP.getTime() < Transaction.POWFIX_RELEASE)
			{	
				JOptionPane.showMessageDialog(new JFrame(), "Assets transactions will be enabled at " + DateTimeFormat.timestamptoString(Transaction.POWFIX_RELEASE) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//CREATE PAYMENT
			result = Controller.getInstance().sendMessage(Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), recipient, key, amount, fee, messageBytes, isTextByte, encrypted);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				//RESET FIELDS
				
				if(amount.compareTo(new BigDecimal(1.0)) == 1) //IF MORE THAN ONE
				{
					this.txtAmount.setText("1.00000000");
				}
				
				if(this.txtTo.getText().startsWith("A"))
				{
					this.txtTo.setText("");
				}
				
				this.txtMessage.setText("");
				
				if(this.txtTo.getText().startsWith("A"))
				{
					JOptionPane.showMessageDialog(new JFrame(), "Message with payment has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				}
				break;	
			
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid address!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), "Amount must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.FEE_LESS_REQUIRED:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee below the minimum for this size of a transaction!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NO_BALANCE:
			
				JOptionPane.showMessageDialog(new JFrame(), "Not enough balance!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			default:
				
				JOptionPane.showMessageDialog(new JFrame(), "Unknown error!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		}
		catch(Exception e)
		{
			//CHECK WHERE PARSING ERROR HAPPENED
			switch(parsing)
			{
			case 1:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case 2:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		//ENABLE
		this.sendButton.setEnabled(true);
	}
	
}


