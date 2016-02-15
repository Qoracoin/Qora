package gui;

import gui.models.AccountsComboBoxModel;
import gui.models.AssetsComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import qora.account.Account;
import qora.assets.Asset;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import settings.Settings;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import utils.MenuPopupUtil;
import controller.Controller;

@SuppressWarnings("serial")
public class SendMoneyPanel extends JPanel 
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtTo;
	private JTextField txtAmount;
	private JTextField txtFee;
	private JTextField txtRecDetails;
	private JButton sendButton;
	private AccountsComboBoxModel accountsModel;
	private JComboBox<Asset> cbxFavorites;
	
	public SendMoneyPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;	
		
		//FAVORITES GBC
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 5);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 2;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = 0;	
		
		//ASSET FAVORITES
		cbxFavorites = new JComboBox<Asset>(new AssetsComboBoxModel());
		this.add(cbxFavorites, favoritesGBC);
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		//LABEL FROM
		labelGBC.gridy = 2;
		JLabel fromLabel = new JLabel("From:");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = 2;
		this.accountsModel = new AccountsComboBoxModel();
		this.cbxFrom = new JComboBox<Account>(accountsModel);
		cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, txtGBC);
        
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
      	labelGBC.gridy = 3;
      	JLabel toLabel = new JLabel("To: (address or name)");
      	this.add(toLabel, labelGBC);
      		
      	//TXT TO
      	txtGBC.gridy = 3;
      	txtTo = new JTextField();
        this.add(txtTo, txtGBC);
        
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
        
        //LABEL RECEIVER DETAILS 
      	labelGBC.gridy = 4;
      	JLabel recDetailsLabel = new JLabel("Receiver details:");
      	this.add(recDetailsLabel, labelGBC);
      		
      	//RECEIVER DETAILS 
      	txtGBC.gridy = 4;
      	txtRecDetails = new JTextField();
      	txtRecDetails.setEditable(false);
        this.add(txtRecDetails, txtGBC);
        
        //LABEL AMOUNT
      	labelGBC.gridy = 5;
      	JLabel amountLabel = new JLabel("Amount:");
      	this.add(amountLabel, labelGBC);
      		
      	//TXT AMOUNT
      	txtGBC.gridy = 5;
      	txtAmount = new JTextField();
        this.add(txtAmount, txtGBC);
        
        //LABEL FEE
      	labelGBC.gridy = 6;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT AMOUNT
      	txtGBC.gridy = 6;
      	txtFee = new JTextField();
      	txtFee.setText("1");
        this.add(txtFee, txtGBC);
        
        //BUTTON SEND
        buttonGBC.gridy = 7;
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
        
        //ADD BOTTOM SO IT PUSHES TO TOP
        labelGBC.gridy = 6;
        labelGBC.weighty = 1;
      	this.add(new JPanel(), labelGBC);
      	
      	//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtTo);
      	MenuPopupUtil.installContextMenu(txtAmount);
      	MenuPopupUtil.installContextMenu(txtFee);
      	MenuPopupUtil.installContextMenu(txtRecDetails);
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
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			txtRecDetails.setText("Status must be OK to show receiver details.");
			return;
		}
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(toValue))
		{
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(toValue);
					
			if(nameToAdress.getB() == NameResult.OK)
			{
				Account account = nameToAdress.getA();
				txtRecDetails.setText(account.toString(asset.getKey()));
			}
			else
			{
				txtRecDetails.setText(nameToAdress.getB().getShortStatusMessage());
			}
		}else
		{
			Account account = new Account(toValue);
			txtRecDetails.setText(account.toString(asset.getKey()));
		}	
	}
	
	public void onSendClick()
	{
		//DISABLE
		this.sendButton.setEnabled(false);
		
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.sendButton.setEnabled(true);
			
			return;
		}
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
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
		
		//READ RECIPIENT (NAME OR QORA ADRESS)
		String recipientAddress = txtTo.getText();
		
		
		Account recipient;
		
		//ORDINARY PAYMENT
		if(Crypto.getInstance().isValidAddress(recipientAddress))
		{
			recipient = new Account(recipientAddress);
		//NAME PAYMENT
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
					
				}
				if (n == JOptionPane.NO_OPTION) {
					
					txtFee.setText("1");
					
					//ENABLE
					this.sendButton.setEnabled(true);
					
					return;
				}
			}
			
			//CHECK IF PAYMENT OR ASSET TRANSFER
			Asset asset = (Asset) this.cbxFavorites.getSelectedItem();
			Pair<Transaction, Integer> result;
			if(asset.getKey() == 0l)
			{
				//CREATE PAYMENT
				result = Controller.getInstance().sendPayment(Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), recipient, amount, fee);
			}
			else
			{
				//CREATE ASSET TRANSFER
				result = Controller.getInstance().transferAsset(Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), recipient, asset, amount, fee);
			}
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				//RESET FIELDS
				this.txtAmount.setText("");
				this.txtTo.setText("");
				
				JOptionPane.showMessageDialog(new JFrame(), "Payment has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				break;	
			
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid address or name!", "Error", JOptionPane.ERROR_MESSAGE);
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
