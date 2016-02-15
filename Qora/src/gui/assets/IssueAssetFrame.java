package gui.assets;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import settings.Settings;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class IssueAssetFrame extends JFrame
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JTextField txtQuantity;
	private JCheckBox chkDivisible;
	private JButton issueButton;

	public IssueAssetFrame()
	{
		super("Qora - Issue Asset");
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
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
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		//LABEL FROM
		labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel("Account:");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = 0;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);
        
        //LABEL NAME
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel("Name:");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT NAME
      	txtGBC.gridy = 1;
      	this.txtName = new JTextField();
        this.add(this.txtName, txtGBC);
        
        //LABEL DESCRIPTION
      	labelGBC.gridy = 2;
      	JLabel descriptionLabel = new JLabel("Description:");
      	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA DESCRIPTION
      	txtGBC.gridy = 2;
      	this.txtareaDescription = new JTextArea();
       	
      	this.txtareaDescription.setRows(6);
      	this.txtareaDescription.setColumns(20);
      	this.txtareaDescription.setBorder(this.txtName.getBorder());

      	JScrollPane scrollDescription = new JScrollPane(this.txtareaDescription);
      	scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(scrollDescription, txtGBC);
      	
      	
      	//LABEL QUANTITY
      	labelGBC.gridy = 3;
      	JLabel quantityLabel = new JLabel("Quantity:");
      	this.add(quantityLabel, labelGBC);
      		
      	//TXT QUANTITY
      	txtGBC.gridy = 3;
      	this.txtQuantity = new JTextField();
      	this.txtQuantity.setText("1");
        this.add(this.txtQuantity, txtGBC);
        
      	//LABEL DIVISIBLE
      	labelGBC.gridy = 4;
      	JLabel divisibleLabel = new JLabel("Divisible:");
      	this.add(divisibleLabel, labelGBC);
      		
      	//CHECKBOX DIVISIBLE
      	txtGBC.gridy = 4;
      	this.chkDivisible = new JCheckBox();
      	this.chkDivisible.setSelected(true);
      	this.add(this.chkDivisible, txtGBC);
      	
        //LABEL FEE
      	labelGBC.gridy = 5;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 5;
      	this.txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(this.txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 6;
        this.issueButton = new JButton("Issue");
        this.issueButton.setPreferredSize(new Dimension(80, 25));
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
    	this.add(this.issueButton, buttonGBC);
        
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onIssueClick()
	{
		//DISABLE
		this.issueButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.issueButton.setEnabled(true);
			
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
				this.issueButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();
		
		long parse = 0;
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(this.txtFee.getText()).setScale(8);
			
			//READ QUANTITY
			parse = 1;
			long quantity = Long.parseLong(this.txtQuantity.getText());
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.issueButton.setEnabled(true);
				
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
					this.issueButton.setEnabled(true);
					
					return;
				}
			}
		
			BigDecimal recommendedFee = Controller.getInstance().calcRecommendedFeeForIssueAssetTransaction(this.txtName.getText(), this.txtareaDescription.getText()).getA();
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
						this.txtFee.setText("1"); // Return to the default fee for the next name.
					}
					
					fee = recommendedFee; // Set recommended fee for this name.
					
				}
				else if (n == JOptionPane.NO_OPTION) {
					
				}	
				else {
					
					//ENABLE
					this.issueButton.setEnabled(true);
					
					return;
				}
			}
			
			//CREATE ASSET
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().issueAsset(creator, this.txtName.getText(), this.txtareaDescription.getText(), quantity, this.chkDivisible.isSelected(), fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), "Asset issue has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
				
			case Transaction.NOT_YET_RELEASED:
				
				JOptionPane.showMessageDialog(new JFrame(), "Assets will be enabled at " + DateTimeFormat.timestamptoString(Transaction.getASSETS_RELEASE()) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.INVALID_QUANTITY:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
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
				
			case Transaction.INVALID_NAME_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name must be between 1 and 100 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Description must be between 1 and 1000 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_PAYMENTS_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			default:
				
				JOptionPane.showMessageDialog(new JFrame(), "Unknown error!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//ENABLE
		this.issueButton.setEnabled(true);
	}
}
