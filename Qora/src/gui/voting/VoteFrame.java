package gui.voting;

import gui.AccountRenderer;
import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import gui.models.AssetsAllComboBoxModel;
import gui.models.OptionsComboBoxModel;

import java.awt.Component;
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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.transaction.Transaction;
import qora.voting.Poll;
import qora.voting.PollOption;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class VoteFrame extends JFrame
{
	private Poll poll;
	private JComboBox<Account> cbxAccount;
	private JComboBox<PollOption> cbxOptions;
	private JButton voteButton;
	private JTextField txtFee;
	private JComboBox<Asset> cbxAssets;
	
	public VoteFrame(Poll poll, int option, Asset asset)
	{
		super("Qora - Vote");
		
		this.poll = poll;
		
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
		detailGBC.gridwidth = 2;
		detailGBC.gridx = 1;		
		
		//LABEL NAME
		labelGBC.gridy = 1;
		JLabel nameLabel = new JLabel("Poll:");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 1;
		JTextField name = new JTextField(poll.getName());
		name.setEditable(false);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = 2;
		JLabel descriptionLabel = new JLabel("Description:");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = 2;
		JTextArea txtAreaDescription = new JTextArea(poll.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);		

		//ASSET LABEL GBC
		GridBagConstraints assetLabelGBC = new GridBagConstraints();
		assetLabelGBC.insets = new Insets(0, 5, 5, 0);
		assetLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetLabelGBC.anchor = GridBagConstraints.CENTER;
		assetLabelGBC.weightx = 0;	
		assetLabelGBC.gridwidth = 1;
		assetLabelGBC.gridx = 0;
		assetLabelGBC.gridy = 3;
		
		//ASSETS GBC
		GridBagConstraints assetsGBC = new GridBagConstraints();
		assetsGBC.insets = new Insets(0, 5, 5, 0);
		assetsGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetsGBC.anchor = GridBagConstraints.NORTHWEST;
		assetsGBC.weightx = 0;	
		assetsGBC.gridwidth = 2;
		assetsGBC.gridx = 1;
		assetsGBC.gridy = 3;
		
		this.add(new JLabel("Asset:"), assetLabelGBC);
		
		cbxAssets = new JComboBox<Asset>(new AssetsAllComboBoxModel());
		cbxAssets.setSelectedItem(asset);
		this.add(cbxAssets, assetsGBC);
		
		cbxAssets.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {

		    	Asset asset = ((Asset) cbxAssets.getSelectedItem());

		    	if(asset != null)
		    	{
		    		((AccountRenderer)cbxAccount.getRenderer()).setAsset(asset.getKey());
		    		cbxAccount.repaint();
		    		cbxOptions.repaint();
		    		
		    	}
		    }
		});
		
        //LABEL ACCOUNT
      	labelGBC.gridy = 4;
      	JLabel ownerLabel = new JLabel("Account:");
      	this.add(ownerLabel, labelGBC);
      		
      	//CBX ACCOUNT
      	detailGBC.gridy = 4;
      	this.cbxAccount = new JComboBox<Account>(new AccountsComboBoxModel());
      	cbxAccount.setRenderer(new AccountRenderer(asset.getKey()));
      	
      	this.add(this.cbxAccount, detailGBC);
		
      	//LABEL OPTIONS
      	labelGBC.gridy = 5;
      	JLabel optionsLabel = new JLabel("Option:");
      	this.add(optionsLabel, labelGBC);
      		
      	//CBX ACCOUNT
      	detailGBC.gridy = 5;
      	this.cbxOptions = new JComboBox<PollOption>(new OptionsComboBoxModel(poll.getOptions()));
      	this.cbxOptions.setSelectedIndex(option);
      	this.cbxOptions.setRenderer(new DefaultListCellRenderer() {
      	    @SuppressWarnings("rawtypes")
			@Override
      	    public Component getListCellRendererComponent(JList list,
      	                                               Object value,
      	                                               int index,
      	                                               boolean isSelected,
      	                                               boolean cellHasFocus) {
      	    	PollOption employee = (PollOption)value;
      	        
      	    	Asset asset = ((Asset) cbxAssets.getSelectedItem());
      	    	
      	    	value = employee.toString(asset.getKey());
      	        return super.getListCellRendererComponent(list, value,
      	                index, isSelected, cellHasFocus);
      	    }
      	});
      	
      	this.add(this.cbxOptions, detailGBC);
      	
      	 //LABEL FEE
      	labelGBC.gridy = 6;
      	JLabel feeLabel = new JLabel("Fee(Qora):");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	detailGBC.gridy = 6;
      	this.txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(this.txtFee, detailGBC);
		
		//ADD EXCHANGE BUTTON
		detailGBC.gridy = 7;
		voteButton = new JButton("Vote");
		voteButton.setPreferredSize(new Dimension(100, 25));
		voteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onVoteClick();
			}
		});	
		this.add(voteButton, detailGBC);
		
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onVoteClick()
	{
		//DISABLE
		this.voteButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.voteButton.setEnabled(true);
			
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
				this.voteButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) cbxAccount.getSelectedItem();
		
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.voteButton.setEnabled(true);
				
				return;
			}
		
			//CREATE POLL
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			PollOption option = (PollOption) this.cbxOptions.getSelectedItem();
			
			Pair<Transaction, Integer> result = Controller.getInstance().createPollVote(creator, poll, option, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), "Poll vote has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
				
			case Transaction.NOT_YET_RELEASED:
				
				JOptionPane.showMessageDialog(new JFrame(), "Voting will be enabled at " + DateTimeFormat.timestamptoString(Transaction.getVOTING_RELEASE()) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
				break;
			
			case Transaction.ALREADY_VOTED_FOR_THAT_OPTION:
				
				JOptionPane.showMessageDialog(new JFrame(), "You have already voted for that option!", "Error", JOptionPane.ERROR_MESSAGE);
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
		this.voteButton.setEnabled(true);
	}
}
