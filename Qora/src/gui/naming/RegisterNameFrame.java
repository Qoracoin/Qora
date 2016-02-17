package gui.naming;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import gui.models.KeyValueTableModel;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import settings.Settings;
import utils.GZIP;
import utils.MenuPopupUtil;
import utils.NameUtils;
import utils.Qorakeys;
import utils.NameUtils.NameResult;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class RegisterNameFrame extends JFrame
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtName;
	private JTextArea txtareaValue;
	private JLabel countLabel;
	private JButton registerButton;
	private JTextField txtRecDetails;
	private JTextField txtKey;
	private KeyValueTableModel namesModel;
	private JButton removeButton;
	private JButton addButton;
	private boolean changed;
	private int selectedRow;
	
	public RegisterNameFrame()
	{
		super("Qora - Register Name");
		
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
        
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            
 			@Override
 			public void changedUpdate(DocumentEvent arg0) {
 				refreshReceiverDetails();
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
       	labelGBC.gridy = 2;
       	JLabel recDetailsLabel = new JLabel("Name details:");
       	this.add(recDetailsLabel, labelGBC);
       		
       	//NAME DETAILS 
       	txtGBC.gridy = 2;
       	txtRecDetails = new JTextField();
       	txtRecDetails.setEditable(false);
        this.add(txtRecDetails, txtGBC);
         
        //KEY 
        txtGBC.gridy = 2;
        txtRecDetails = new JTextField();
        txtRecDetails.setEditable(false);
        this.add(txtRecDetails, txtGBC);
        
        //LABEL KEY
      	labelGBC.gridy = 3;
      	JLabel keyLabel = new JLabel("Key:");
      	this.add(keyLabel, labelGBC);
      	
    	txtGBC.gridy = 3;
      	this.txtKey = new JTextField();
      	this.add(this.txtKey, txtGBC);
      	txtKey.setText(Qorakeys.DEFAULT.toString());
      	
        
        //LABEL NAME
      	labelGBC.gridy = 5;
      	JLabel valueLabel = new JLabel("Value:");
      	this.add(valueLabel, labelGBC);
      		
      	//TXTAREA NAME
      	txtGBC.gridy = 5;
      	this.txtareaValue = new JTextArea();
      	
      	
      	this.txtareaValue.setRows(20);
      	this.txtareaValue.setColumns(63);
      	this.txtareaValue.setBorder(this.txtName.getBorder());

      	JScrollPane Valuescroll = new JScrollPane(this.txtareaValue);
      	Valuescroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	Valuescroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(Valuescroll, txtGBC);
      			
      		
      	//LABEL COUNT
		labelGBC.gridy = 6;
		labelGBC.gridx = 1;
		labelGBC.fill = GridBagConstraints.BOTH;   
		labelGBC.anchor = GridBagConstraints.CENTER;
		
		countLabel = new JLabel("Character count: 0/4000");
		this.add(countLabel, labelGBC);
		
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridwidth = 10;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 7;	

		namesModel = new KeyValueTableModel();
		final JTable namesTable = new JTable(namesModel);
		
		JScrollPane scrollPane = new JScrollPane(namesTable);
        scrollPane.setPreferredSize(new Dimension(100, 150));
        scrollPane.setWheelScrollingEnabled(true);

		namesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		namesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						if(!e.getValueIsAdjusting())
						{
							ListSelectionModel lsm = (ListSelectionModel)e.getSource();
							
							int minSelectionIndex = lsm.getMinSelectionIndex();
							txtareaValue.setEnabled(minSelectionIndex != -1);
							txtKey.setEnabled(minSelectionIndex != -1);
							txtareaValue.setText((String) namesModel.getValueAt(minSelectionIndex, 1)); 
							txtKey.setText((String) namesModel.getValueAt(minSelectionIndex, 0)); 
						}
					}
				});
				
			}
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(scrollPane, tableGBC);

		//BUTTON REMOVE
        buttonGBC.gridy = 8;
        buttonGBC.gridx = 1;
        buttonGBC.fill = GridBagConstraints.EAST;
        buttonGBC.anchor = GridBagConstraints.EAST;
        
        removeButton = new JButton("Remove");
        removeButton.setPreferredSize(new Dimension(150, 25));
        this.add(removeButton, buttonGBC);
        
        buttonGBC.gridx = 0;
        buttonGBC.fill = GridBagConstraints.WEST;
        buttonGBC.anchor = GridBagConstraints.WEST;
        addButton = new JButton("Add");
        addButton.setPreferredSize(new Dimension(150, 25));
        this.add(addButton, buttonGBC);
    
        removeButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
				int index = namesTable.getSelectionModel().getMinSelectionIndex();
				if(index != -1)
				{
					namesModel.removeEntry(index);
					
					if(namesModel.getRowCount() > index)
					{
						namesTable.requestFocus();
						namesTable.changeSelection(index, index, false, false);
					}
				}
		    }
		});
        
        
        addButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
				namesModel.addAtEnd();
				namesTable.requestFocus();
				int index = namesModel.getRowCount();
				namesTable.changeSelection(index-1, index-1, false, false);
		    }
		});		
		

		//LABEL FEE
      	labelGBC.gridy = 9;
      	labelGBC.gridx = 0;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      	
      	//TXT FEE
      	txtGBC.gridy = 9;
      	this.txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(this.txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 10;
        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(80, 25));
        registerButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onRegisterClick();
		    }
		});
    	this.add(registerButton, buttonGBC);
        
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
      	//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(this.txtName);
      	MenuPopupUtil.installContextMenu(this.txtareaValue);
      	MenuPopupUtil.installContextMenu(this.txtareaValue);
		MenuPopupUtil.installContextMenu(this.txtKey);
      	MenuPopupUtil.installContextMenu(this.txtRecDetails);
      	MenuPopupUtil.installContextMenu(this.txtFee);
      	
      	
		txtKey.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				valueChanged(e);
			}
			
			public void removeUpdate(DocumentEvent e) {
				valueChanged(e);
			}
			
			public void insertUpdate(DocumentEvent e) {
				valueChanged(e);
			}
		
			public void valueChanged(final DocumentEvent e) {
				selectedRow = namesTable.getSelectedRow();

				changed = true;
			}
			});
		
    	ArrayList<Pair<String, String>> list = new ArrayList<>();
    	list.add(new Pair<String, String>(Qorakeys.DEFAULT.toString(), ""));
    	namesModel.setData(list);
    	namesTable.requestFocus();
		namesTable.changeSelection(0,0,false, false);
		
		txtareaValue.setEnabled(namesTable.getSelectedRow() != -1);
		txtKey.setEnabled(namesTable.getSelectedRow() != -1);
		
		this.txtareaValue.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				update(namesModel, namesTable);
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				update(namesModel, namesTable);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				update(namesModel, namesTable);
			}
			public void update(final KeyValueTableModel namesModel,
					final JTable namesTable) {
				selectedRow = namesTable.getSelectedRow();
				changed = true;
			}
      	 });
		
		changed = true;

		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(	new Runnable() { 
			public void run() {
				if(changed)
				{
					String newValue = txtareaValue.getText();
					String newKey = txtKey.getText().toLowerCase();
					namesModel.setValueAt(newValue, selectedRow, 1);
					namesModel.fireTableCellUpdated(selectedRow, 1);
					namesModel.setValueAt(newKey, selectedRow, 0);
					namesModel.fireTableCellUpdated(selectedRow, 0);
					countLabel.setText(GZIP.getZippedCharacterCount(namesModel));
					changed = false;
				}
			}}, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	private void refreshReceiverDetails()
	{
		String nameCheck = txtName.getText().toLowerCase();
		
		if(nameCheck.isEmpty())
		{
			txtRecDetails.setText("");
			return;
		}
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			txtRecDetails.setText("Status must be OK to show receiver details.");
			return;
		}
		Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(nameCheck);
		if(nameToAdress.getB() == NameResult.OK)
		{
			txtRecDetails.setText("Already registered by someone.");
		}
		else if(nameToAdress.getB() == NameResult.NAME_FOR_SALE)
		{
			txtRecDetails.setText("Already registered. Sale: " + Controller.getInstance().getNameSale(nameCheck).getAmount());
		}else if(nameToAdress.getB() == NameResult.NAME_NOT_REGISTERED)
		{
			txtRecDetails.setText("The name is free, you can register it!");
		}
	}

	public void onRegisterClick()
	{
		//DISABLE
		this.registerButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.registerButton.setEnabled(true);
			
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
				this.registerButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) cbxFrom.getSelectedItem();
		
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.registerButton.setEnabled(true);
				
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
					this.registerButton.setEnabled(true);
					
					return;
				}
			}
			
			Pair<Boolean, String> isUpdatable = namesModel.checkUpdateable();
			if(!isUpdatable.getA())
			{
				
				JOptionPane.showMessageDialog(new JFrame(), isUpdatable.getB(), "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.registerButton.setEnabled(true);
				
				return;
			}
			
			//CREATE NAME UPDATE
			String currentValueAsJsonStringOpt = namesModel.getCurrentValueAsJsonStringOpt();
			if(currentValueAsJsonStringOpt == null)
			{
					JOptionPane.showMessageDialog(new JFrame(), "Bad Json value", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.registerButton.setEnabled(true);
				
				return;
			}
			
			
			currentValueAsJsonStringOpt = GZIP.compress(currentValueAsJsonStringOpt);
		
			BigDecimal recommendedFee = Controller.getInstance().calcRecommendedFeeForNameRegistration(this.txtName.getText(), currentValueAsJsonStringOpt).getA();
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
					this.registerButton.setEnabled(true);
					
					return;
				}
			}
			
			//CREATE NAME REGISTRATION
			PrivateKeyAccount registrant = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().registerName(registrant, registrant, this.txtName.getText(), currentValueAsJsonStringOpt, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name registration has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
			
			case Transaction.NAME_NOT_LOWER_CASE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name must be lower case!", "Error", JOptionPane.ERROR_MESSAGE);
				this.txtName.setText(this.txtName.getText().toLowerCase());
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
				
				JOptionPane.showMessageDialog(new JFrame(), "Name must be between 1 and 400 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_VALUE_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Value must be between 1 and 4000 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NAME_ALREADY_REGISTRED:
				
				JOptionPane.showMessageDialog(new JFrame(), "That name is already registred!", "Error", JOptionPane.ERROR_MESSAGE);
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
		this.registerButton.setEnabled(true);
	}
}
