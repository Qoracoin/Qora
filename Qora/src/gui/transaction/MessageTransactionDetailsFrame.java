package gui.transaction;

import gui.PasswordPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.crypto.InvalidCipherTextException;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.AEScrypto;
import qora.crypto.Base58;
import qora.transaction.MessageTransaction;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import controller.Controller;

@SuppressWarnings("serial")
public class MessageTransactionDetailsFrame extends JFrame
{
	private JTextField messageText;
	
	public MessageTransactionDetailsFrame(final MessageTransaction messageTransaction)
	{
		super("Qora - Transaction Details");
		
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
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 3;
		detailGBC.gridx = 1;		
		
		
		int componentLevel = 0;
		//LABEL TYPE
		labelGBC.gridy = componentLevel;
		
		JLabel typeLabel = new JLabel("Type:");
		this.add(typeLabel, labelGBC);
		
		//TYPE
		detailGBC.gridy = componentLevel;
		JLabel type = new JLabel("Message Transaction");
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel signatureLabel = new JLabel("Signature:");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = componentLevel;
		JTextField signature = new JTextField(Base58.encode(messageTransaction.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel referenceLabel = new JLabel("Reference:");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = componentLevel;
		JTextField reference = new JTextField(Base58.encode(messageTransaction.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel timestampLabel = new JLabel("Timestamp:");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = componentLevel;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(messageTransaction.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL SENDER
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel senderLabel = new JLabel("Creator:");
		this.add(senderLabel, labelGBC);
		
		//SENDER
		detailGBC.gridy = componentLevel;
		JTextField sender = new JTextField(messageTransaction.getCreator().getAddress());
		sender.setEditable(false);
		MenuPopupUtil.installContextMenu(sender);
		this.add(sender, detailGBC);
		
		//LABEL RECIPIENT
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel recipientLabel = new JLabel("Recipient:");
		this.add(recipientLabel, labelGBC);
		
		//RECIPIENT
		detailGBC.gridy = componentLevel;
		JTextField recipient = new JTextField(messageTransaction.getRecipient().getAddress());
		recipient.setEditable(false);
		MenuPopupUtil.installContextMenu(recipient);
		this.add(recipient, detailGBC);		
		
		//LABEL SERVICE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel serviceLabel = new JLabel("Message:");
		this.add(serviceLabel, labelGBC);
		
		//ISTEXT
		detailGBC.gridy = componentLevel;
		detailGBC.gridwidth = 2;
		messageText = new JTextField( ( messageTransaction.isText() ) ? new String(messageTransaction.getData(), Charset.forName("UTF-8")) : Converter.toHex(messageTransaction.getData()));
		messageText.setEditable(false);
		MenuPopupUtil.installContextMenu(messageText);
		this.add(messageText, detailGBC);			
		detailGBC.gridwidth = 3;
		
		//ENCRYPTED CHECKBOX
		
		//ENCRYPTED
		GridBagConstraints chcGBC = new GridBagConstraints();
		chcGBC.fill = GridBagConstraints.HORIZONTAL;  
		chcGBC.anchor = GridBagConstraints.NORTHWEST;
		chcGBC.gridy = componentLevel;
		chcGBC.gridx = 3;
		chcGBC.gridwidth = 1;
        final JCheckBox encrypted = new JCheckBox("Encrypted");
        
        encrypted.setSelected(messageTransaction.isEncrypted());
        encrypted.setEnabled(messageTransaction.isEncrypted());
        
        this.add(encrypted, chcGBC);
        
        encrypted.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		if(!encrypted.isSelected())
        		{
	        		if(!Controller.getInstance().isWalletUnlocked())
	        		{
	        			//ASK FOR PASSWORD
	        			String password = PasswordPane.showUnlockWalletDialog(); 
	        			if(!Controller.getInstance().unlockWallet(password))
	        			{
	        				//WRONG PASSWORD
	        				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
	        				
	        				encrypted.setSelected(!encrypted.isSelected());
	        				
	        				return;
	        			}
	        		}
	
	        		Account account = Controller.getInstance().getAccountByAddress(messageTransaction.getSender().getAddress());	
	        		
	        		byte[] privateKey = null; 
	        		byte[] publicKey = null;
	        		//IF SENDER ANOTHER
	        		if(account == null)
	        		{
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(messageTransaction.getRecipient().getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = messageTransaction.getCreator().getPublicKey();    				
	        		}
	        		//IF SENDER ME
	        		else
	        		{
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = Controller.getInstance().getPublicKeyByAddress(messageTransaction.getRecipient().getAddress());    				
	        		}
	        		
	        		try {
	        			messageText.setText(new String(AEScrypto.dataDecrypt(messageTransaction.getData(), privateKey, publicKey), "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvalidCipherTextException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		else
        		{
        			try {
        				messageText.setText(new String(messageTransaction.getData(), "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		//encrypted.isSelected();
        		
        	}
        });	  
        
		//LABEL AMOUNT
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel amountLabel = new JLabel("Amount:");
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		detailGBC.gridy = componentLevel;
		detailGBC.gridwidth = 2;
		JTextField amount = new JTextField(messageTransaction.getAmount().toPlainString());
		amount.setEditable(false);
		MenuPopupUtil.installContextMenu(amount);
		this.add(amount, detailGBC);	
		
		//ASSET
		detailGBC.gridy = componentLevel;
		detailGBC.gridx = 3;
		detailGBC.gridwidth = 1;
		JTextField asset = new JTextField(Controller.getInstance().getAsset( messageTransaction.getKey()).toString());
		asset.setEditable(false);
		MenuPopupUtil.installContextMenu(asset);
		this.add(asset, detailGBC);	
		detailGBC.gridx = 1;
		detailGBC.gridwidth = 3;
		
		//LABEL FEE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = componentLevel;
		JTextField fee = new JTextField(messageTransaction.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel confirmationsLabel = new JLabel("Confirmations:");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = componentLevel;
		JLabel confirmations = new JLabel(String.valueOf(messageTransaction.getConfirmations()));
		this.add(confirmations, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
