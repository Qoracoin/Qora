package gui.assets;

import gui.PasswordPane;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import qora.account.PrivateKeyAccount;
import qora.assets.Order;
import qora.transaction.Transaction;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class CancelOrderFrame extends JFrame
{
	private Order order;
	private JTextField txtFee;
	private JButton cancelOrderButton;
	
	public CancelOrderFrame(Order order)
	{
		super("Qora - Cancel Order");
		
		this.order = order;
		
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
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;	
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;		
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		//LABEL TIMESTAMP
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel("Timestamp:");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT TIMESTAMP
      	txtGBC.gridy = 1;
    
		JTextField txtTimestamp = new JTextField(DateTimeFormat.timestamptoString(order.getTimestamp()));
      	txtTimestamp.setEditable(false);
      	this.add(txtTimestamp, txtGBC);
        
        //LABEL HAVE
      	labelGBC.gridy = 2;
      	JLabel haveLabel = new JLabel("Have:");
      	this.add(haveLabel, labelGBC);
      		
      	//TXT HAVE
      	txtGBC.gridy = 2;
      	JTextField txtHave = new JTextField(String.valueOf(order.getHave()));
      	txtHave.setEditable(false);
      	this.add(txtHave, txtGBC);
      	
        //LABEL WANT
      	labelGBC.gridy = 3;
      	JLabel wantLabel = new JLabel("Want:");
      	this.add(wantLabel, labelGBC);
      		
      	//TXT WANT
      	txtGBC.gridy = 3;
      	JTextField txtWant = new JTextField(String.valueOf(order.getWant()));
      	txtWant.setEditable(false);
      	this.add(txtWant, txtGBC);
      	
        //LABEL AMOUNT
      	labelGBC.gridy = 4;
      	JLabel amountLabel = new JLabel("Amount:");
      	this.add(amountLabel, labelGBC);
      		
      	//TXT WANT
      	txtGBC.gridy = 4;
      	JTextField txtAmount = new JTextField(order.getAmount().toPlainString());
      	txtAmount.setEditable(false);
      	this.add(txtAmount, txtGBC);
        
        //LABEL PRICE
      	labelGBC.gridy = 5;
      	JLabel priceLabel = new JLabel("Price:");
      	this.add(priceLabel, labelGBC);
      		
      	//TXT PRICE
      	txtGBC.gridy = 5;
      	JTextField txtPrice = new JTextField(order.getPrice().toPlainString());
      	txtPrice.setEditable(false);
      	this.add(txtPrice, txtGBC);
      	
        //LABEL FULFILLED
      	labelGBC.gridy = 6;
      	JLabel fulfilledLabel = new JLabel("Fulfilled:");
      	this.add(fulfilledLabel, labelGBC);
      		
      	//TXT FULFILLED
      	txtGBC.gridy = 6;
      	JTextField txtFulfilled = new JTextField(order.getFulfilled().toPlainString());
      	txtFulfilled.setEditable(false);
      	this.add(txtFulfilled, txtGBC);
      	
      	//LABEL FEE
      	labelGBC.gridy = 7;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 7;
      	txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(txtFee, txtGBC);
		           
        //BUTTON CANCEL SALE
        buttonGBC.gridy = 8;
        cancelOrderButton = new JButton("Cancel Order");
        cancelOrderButton.setPreferredSize(new Dimension(120, 25));
        cancelOrderButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onCancelOrderClick();
			}
		});	
        this.add(cancelOrderButton, buttonGBC);
    	
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onCancelOrderClick()
	{
		//DISABLE
		this.cancelOrderButton.setEnabled(false);
		
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.cancelOrderButton.setEnabled(true);
			
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
				this.cancelOrderButton.setEnabled(true);
				
				return;
			}
		}
		
		try
		{
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.cancelOrderButton.setEnabled(true);
				
				return;
			}
		
			//CREATE NAME UPDATE
			PrivateKeyAccount owner = Controller.getInstance().getPrivateKeyAccountByAddress(order.getCreator().getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().cancelOrder(owner, order, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), "Cancel order has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;		
				
			case Transaction.ORDER_DOES_NOT_EXIST:
				
				JOptionPane.showMessageDialog(new JFrame(), "That order does not exist or has already been completed!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
			
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid creator!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_ORDER_CREATOR:
				
				JOptionPane.showMessageDialog(new JFrame(), "You are not the creator this order!", "Error", JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		this.cancelOrderButton.setEnabled(true);
	}
}
