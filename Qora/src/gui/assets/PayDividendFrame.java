package gui.assets;

import gui.BalanceRenderer;
import gui.models.BalancesComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import database.SortableList;
import qora.account.Account;
import qora.assets.Asset;
import qora.payment.Payment;
import utils.Pair;

@SuppressWarnings("serial")
public class PayDividendFrame extends JFrame
{
	private Asset asset;
	private JTextField txtAsset;
	private JTextField txtAccount;
	private JComboBox<Pair<Tuple2<String, Long>, BigDecimal>> cbxAssetToPay;
	private JTextField txtAmount;
	private JTextField txtHolders;
	private JButton generateButton;

	public PayDividendFrame(Asset asset)
	{
		super("ERM - Pay Dividend");
		
		this.asset = asset;
		
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
		
		//LABEL ASSET
		labelGBC.gridy = 0;
		JLabel assetLabel = new JLabel("Asset:");
		this.add(assetLabel, labelGBC);
		
		//TXT ASSET
		txtGBC.gridy = 0;
		this.txtAsset = new JTextField(asset.toString());
		this.txtAsset.setEditable(false);
        this.add(this.txtAsset, txtGBC);
        
        //LABEL ACCOUNT
      	labelGBC.gridy = 1;
      	JLabel accountLabel = new JLabel("Account:");
      	this.add(accountLabel, labelGBC);
      		
      	//TXT ACCOUNT
      	txtGBC.gridy = 1;
      	this.txtAccount = new JTextField(asset.getOwner().getAddress());
      	this.txtAccount.setEditable(false);
        this.add(this.txtAccount, txtGBC);
        
        //LABEL ASSET TO PAY
      	labelGBC.gridy = 2;
      	JLabel AssetToPayLabel = new JLabel("Asset to pay:");
      	this.add(AssetToPayLabel, labelGBC);
      		
      	//CBX ASSET TO PAY
      	txtGBC.gridy = 2;
      	this.cbxAssetToPay = new JComboBox<Pair<Tuple2<String, Long>, BigDecimal>>(new BalancesComboBoxModel(asset.getOwner()));
      	this.cbxAssetToPay.setRenderer(new BalanceRenderer());
        this.add(this.cbxAssetToPay, txtGBC);
      	
      	//LABEL AMOUNT
      	labelGBC.gridy = 3;
      	JLabel amountLabel = new JLabel("Amount to pay:");
      	this.add(amountLabel, labelGBC);
      		
      	//TXT AMOUNT
      	txtGBC.gridy = 3;
      	this.txtAmount = new JTextField();
      	this.txtAmount.setText("1");
        this.add(this.txtAmount, txtGBC);
        
      	//LABEL HOLDERS TO PAY
      	labelGBC.gridy = 4;
      	JLabel holdersToPayLabel = new JLabel("Holders to pay (1-400):");
      	this.add(holdersToPayLabel, labelGBC);
      		
      	//TXT QUANTITY
      	txtGBC.gridy = 4;
      	this.txtHolders = new JTextField();
      	this.txtHolders.setText("1");
        this.add(this.txtHolders, txtGBC);
		           
        //BUTTON GENERATE
        buttonGBC.gridy = 5;
        this.generateButton = new JButton("Generate Payment");
        this.generateButton.setPreferredSize(new Dimension(160, 25));
        this.generateButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onGenerateClick();
		    }
		});
    	this.add(this.generateButton, buttonGBC);
        
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	public void onGenerateClick()
	{
		int parsing = 0;
		try
		{
			//HOLDERS TO PAY
			parsing = 1;
			int holders = Integer.parseInt(this.txtHolders.getText());
			
			//AMOUNT TO PAY
			parsing = 2;
			BigDecimal amount = new BigDecimal(txtAmount.getText()).setScale(8);
			
			//ASSET TO PAY
			long assetKey = ((Pair<Tuple2<String, Long>, BigDecimal>) this.cbxAssetToPay.getSelectedItem()).getA().b;
			Asset assetToPay = Controller.getInstance().getAsset(assetKey);
			
			//BALANCES
			SortableList<Tuple2<String, Long>, BigDecimal> balances = Controller.getInstance().getBalances(this.asset.getKey());
			
			//GET ACCOUNTS AND THEIR TOTAL BALANCE
			List<Account> accounts = new ArrayList<Account>();
			BigDecimal total = BigDecimal.ZERO.setScale(8);
			for(int i=0; i<holders && i<balances.size(); i++)
			{
				Account account = new Account(balances.get(i).getA().a);
				accounts.add(account);
				
				total = total.add(balances.get(i).getB());
			}
			
			//CREATE PAYMENTS
			List<Payment> payments = new ArrayList<Payment>();
			for(Account account: accounts)
			{
				//CALCULATE PERCENTAGE OF TOTAL
				BigDecimal percentage = account.getConfirmedBalance(this.asset.getKey()).divide(total, 8, RoundingMode.DOWN);
				
				//CALCULATE AMOUNT
				BigDecimal accountAmount = amount.multiply(percentage);
				
				//ROUND AMOUNT
				if(assetToPay.isDivisible())
				{
					accountAmount = accountAmount.setScale(8, RoundingMode.DOWN);
				}
				else
				{
					accountAmount = accountAmount.setScale(0, RoundingMode.DOWN).setScale(8);
				}
				
				//CHECK IF AMOUNT NOT ZERO
				if(accountAmount.compareTo(BigDecimal.ZERO) > 0)
				{
					Payment payment = new Payment(account, assetToPay.getKey(), accountAmount);
					payments.add(payment);
				}
			}
			
			new MultiPaymentFrame(this.asset, payments);
		}
		catch(Exception e)
		{
			//CHECK WHERE PARSING ERROR HAPPENED
			switch(parsing)
			{
			case 1:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid holders to pay!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case 2:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid amount to pay!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
	}
}
