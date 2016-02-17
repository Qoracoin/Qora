package gui.naming;

import gui.PasswordPane;
import gui.models.KeyValueTableModel;
import gui.models.NameComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.naming.Name;
import qora.transaction.Transaction;
import settings.Settings;
import utils.GZIP;
import utils.MenuPopupUtil;
import utils.Pair;
import utils.Qorakeys;
import controller.Controller;
import database.DBSet;

@SuppressWarnings("serial")
public class UpdateNameFrame extends JFrame
{
	private JComboBox<Name> cbxName;
	private JTextField txtOwner;
	private JTextField txtKey;
	private JTextArea txtareaValue;	
	private JTextField txtFee;
	private JButton updateButton;
	private JButton removeButton;
	private JButton addButton;
	private JLabel countLabel;
	private KeyValueTableModel namesModel;
	private boolean changed;
	private int selectedRow;

	public UpdateNameFrame(Name name)
	{
		super("Qora - Update Name");
		
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
		
		//LABEL NAME
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel("Name:");
      	this.add(nameLabel, labelGBC);
      	
      		
      	//TXT NAME
      	txtGBC.gridy = 1;
      	this.cbxName = new JComboBox<Name>(new NameComboBoxModel());
      	
        this.add(this.cbxName, txtGBC);
        
        //LABEL OWNER
      	labelGBC.gridy = 2;
      	JLabel ownerLabel = new JLabel("Owner:");
      	this.add(ownerLabel, labelGBC);
      	
      		
      	//TXT OWNER
      	txtGBC.gridy = 2;
      	this.txtOwner = new JTextField();
      	this.add(this.txtOwner, txtGBC);
      	
      	 //LABEL KEY
      	labelGBC.gridy = 3;
      	JLabel keyLabel = new JLabel("Key:");
      	this.add(keyLabel, labelGBC);
      	
      	//TXT KEY
      	txtGBC.gridy = 3;
      	this.txtKey = new JTextField();
      	this.add(this.txtKey, txtGBC);
        
      	
        //LABEL VALUE
      	labelGBC.gridy = 5;
      	JLabel valueLabel = new JLabel("Value:");
      	this.add(valueLabel, labelGBC);
      		
      	//TXTAREA VALUE
      	txtGBC.gridy = 5;
      	this.txtareaValue = new JTextArea();
      	
      	this.txtareaValue.setRows(20);
      	this.txtareaValue.setColumns(63);
      	this.txtareaValue.setBorder(cbxName.getBorder());

      	JScrollPane Valuescroll = new JScrollPane(this.txtareaValue);
      	Valuescroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	Valuescroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(Valuescroll, txtGBC);
        
      	//LABEL COUNT
		labelGBC.gridy = 6;
		//labelGBC.gridwidth = ;
		labelGBC.gridx = 1;
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
		
		
		txtareaValue.setEnabled(namesTable.getSelectedRow() != -1);
		txtKey.setEnabled(namesTable.getSelectedRow() != -1);
		
		this.cbxName.addItemListener(new ItemListener()
      	{
      		@Override
      	    public void itemStateChanged(ItemEvent event) 
      		{
      			if (event.getStateChange() == ItemEvent.SELECTED) 
      			{
      				Name name = (Name) event.getItem();

      				loadName(name, namesTable);
      			}
      	    }    
      	});
		
		
		//ADD NAMING SERVICE TABLE
		this.add(scrollPane, tableGBC);
		
      	//LABEL FEE
		labelGBC.gridx = 0;
		labelGBC.gridy = 9;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 9;
      	txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 10;
        updateButton = new JButton("Update");
        updateButton.setPreferredSize(new Dimension(80, 25));
        updateButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onUpdateClick();
		    }
		});
    	this.add(updateButton, buttonGBC);
             
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

    	//SET DEFAULT SELECTED ITEM
    	if(this.cbxName.getItemCount() > 0)
    	{
    		this.cbxName.setSelectedItem(name);
    		//this.txtareaValue.setText(name.getValue());
			this.txtOwner.setText(name.getOwner().getAddress());
    	}
    	
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
      	//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtOwner);
		MenuPopupUtil.installContextMenu(txtKey);
      	MenuPopupUtil.installContextMenu(txtareaValue);
      	MenuPopupUtil.installContextMenu(txtFee);
      	
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
		
		//cbxName.setSelectedIndex(0);
		//cbxName.revalidate();
		//this.cbxName.setSelectedItem(this.cbxName.getSelectedItem());
		
		loadName((Name)this.cbxName.getSelectedItem(), namesTable);
	}
	
	public void loadName(Name name, JTable namesTable)
	{
		String value = GZIP.webDecompress(name.getValue());
			JSONObject jsonObject;
			try {
			jsonObject = (JSONObject) JSONValue.parse(value);
			
		} catch (Exception e) {
			jsonObject = null;
		}
			
		List<Pair<String, String>> keyvaluepairs = new ArrayList<>();
		if(jsonObject != null)
		{
			@SuppressWarnings("unchecked")
		Set<String> keySet = jsonObject.keySet();
			for (String key : keySet) {
				Object object = jsonObject.get(key);
				if(object instanceof Long)
				{
					object = "" + object;
				}
			keyvaluepairs.add(new Pair<String, String>(key, (String) object));
			}
			
		}else
		{
			keyvaluepairs.add(new Pair<String, String>(Qorakeys.DEFAULT.toString(), value));
		}
		
		namesModel.setData(keyvaluepairs);
		namesTable.requestFocus();
		namesTable.changeSelection(0, 0, false, false);
	}
	
	public void onUpdateClick()
	{
		//DISABLE
		this.updateButton.setEnabled(false);
		
		
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.updateButton.setEnabled(true);
			
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
				this.updateButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ NAME
		Name name = (Name) this.cbxName.getSelectedItem();
		name.setOwner(DBSet.getInstance().getNameMap().get(name.getName()).getOwner());
		
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
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
					this.updateButton.setEnabled(true);
					
					return;
				}
			}
			
			
			Pair<Boolean, String> isUpdatable = namesModel.checkUpdateable();
			if(!isUpdatable.getA())
			{
				
				JOptionPane.showMessageDialog(new JFrame(), isUpdatable.getB(), "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
				return;
			}
			
			//CREATE NAME UPDATE
			PrivateKeyAccount owner = Controller.getInstance().getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			String currentValueAsJsonStringOpt = namesModel.getCurrentValueAsJsonStringOpt();
			if(currentValueAsJsonStringOpt == null)
			{
					JOptionPane.showMessageDialog(new JFrame(), "Bad Json value", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
				return;
			}
			
			
			currentValueAsJsonStringOpt = GZIP.compress(currentValueAsJsonStringOpt);
			
			
			BigDecimal recommendedFee = Controller.getInstance().calcRecommendedFeeForNameUpdate(name.getName(), currentValueAsJsonStringOpt).getA();
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
					this.updateButton.setEnabled(true);
					
					return;
				}
			}
			
			Pair<Transaction, Integer> result = Controller.getInstance().updateName(owner, new Account(this.txtOwner.getText()), name.getName(), currentValueAsJsonStringOpt, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name update has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
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
				
			case Transaction.NAME_DOES_NOT_EXIST:
				
				JOptionPane.showMessageDialog(new JFrame(), "That name does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NAME_ALREADY_FOR_SALE:
				
				JOptionPane.showMessageDialog(new JFrame(), "That name is already for sale!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid owner!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_NAME_OWNER:
				
				JOptionPane.showMessageDialog(new JFrame(), "You are no longer the owner this name!", "Error", JOptionPane.ERROR_MESSAGE);
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
		this.updateButton.setEnabled(true);
	}


	
}
