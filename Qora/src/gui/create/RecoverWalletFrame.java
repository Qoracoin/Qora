package gui.create;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import qora.crypto.Base58;

@SuppressWarnings("serial")
public class RecoverWalletFrame extends JFrame
{
	private NoWalletFrame parent;
	private JTextField seedTxt;
	private JTextField passwordTxt;
	private JTextField amountTxt;
	private JTextField confirmPasswordTxt;
	
	public RecoverWalletFrame(NoWalletFrame parent)
	{
		super("Qora - Recover Wallet");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//PARENT
		this.parent = parent;
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 1;	
		labelGBC.gridwidth = 2;
		labelGBC.gridx = 0;
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 1;
		buttonGBC.gridx = 0;		
		
		//LABEL
		labelGBC.gridy = 0;
		JLabel label1 = new JLabel("Please enter your wallet seed:");	
		this.add(label1, labelGBC);
		
		//ADD TEXTBOX
		labelGBC.gridy = 1;
		this.seedTxt = new JTextField();
		this.add(this.seedTxt, labelGBC);
		
		// MENU
		JPopupMenu menu = new JPopupMenu();
		JMenuItem pasteSeed = new JMenuItem("Paste");
		pasteSeed.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				try {
					String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
					seedTxt.setText(clipboardContent);
				} catch (UnsupportedFlavorException | IOException e1) {
					e1.printStackTrace();
				} 
			}
		});
		menu.add(pasteSeed);
		seedTxt.setComponentPopupMenu(menu);	
				
		//LABEL
      	labelGBC.gridy = 2;
      	labelGBC.insets.top = 00;
      	JLabel label2 = new JLabel("Make sure your seed is in base58 format.");
      	this.add(label2, labelGBC);
      	
      	//LABEL
      	labelGBC.gridy = 3;
      	labelGBC.insets.top = 10;
		JLabel label3 = new JLabel("Please enter your wallet password:");	
		this.add(label3, labelGBC);
		
		//ADD TEXTBOX
		labelGBC.gridy = 4;
		labelGBC.insets.top = 5;
		this.passwordTxt = new JPasswordField();
		this.add(this.passwordTxt, labelGBC);
		
		//LABEL
      	labelGBC.gridy = 5;
      	labelGBC.insets.top = 10;
		JLabel label4 = new JLabel("Please confirm your password:");	
		this.add(label4, labelGBC);
		
		//ADD TEXTBOX
		labelGBC.gridy = 6;
		labelGBC.insets.top = 5;
		this.confirmPasswordTxt = new JPasswordField();
		this.add(this.confirmPasswordTxt, labelGBC);
		
		//LABEL
      	labelGBC.gridy = 7;
      	labelGBC.insets.top = 10;
		JLabel label5 = new JLabel("Amount of accounts to recover:");	
		this.add(label5, labelGBC);
		
		//ADD TEXTBOX
		labelGBC.gridy = 8;
		labelGBC.insets.top = 5;
		this.amountTxt = new JTextField();
		this.amountTxt.setText("10");
		this.add(this.amountTxt, labelGBC);
		
		
		//BUTTON confirm
        buttonGBC.gridy = 9;
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onConfirmClick();
		    }
		});	
        confirmButton.setPreferredSize(new Dimension(80, 25));
    	this.add(confirmButton, buttonGBC);
    	
    	//BUTTON BACK
    	buttonGBC.gridx = 1;
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onBackClick();
		    }
		});
        backButton.setPreferredSize(new Dimension(80, 25));
    	this.add(backButton, buttonGBC);
    	
    	//CLOSE NICELY
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	Controller.getInstance().stopAll();
            	System.exit(0);
            }
        });
        
      	//CALCULATE HEIGHT WIDTH
      	this.pack();
      	this.setSize(500, this.getHeight());
      	
      	this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void onConfirmClick() {
		
		//CHECK IF SEEDS MATCH
		byte[] seed = null;
		try
		{
			seed = Base58.decode(this.seedTxt.getText());
		} catch(Exception e) {
			seed = null;		
		}
		
		if(seed == null || seed.length != 32)
		{
			//INVALID SEED
			String message = "Invalid or incorrect seed!";
			JOptionPane.showMessageDialog(new JFrame(), message, "Invalid seed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String password = this.passwordTxt.getText();
		if(password.length() == 0)
		{
			//PASSWORD CANNOT BE EMPTY
			String message = "Password cannot be empty!";
			JOptionPane.showMessageDialog(new JFrame(), message, "Invalid password", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(!password.equals(this.confirmPasswordTxt.getText()))
		{
			//PASSWORDS DO NOT MATCH
			String message = "Password do not match!";
			JOptionPane.showMessageDialog(new JFrame(), message, "Invalid password", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String amountString = this.amountTxt.getText();
		int amount = 0;
		
		try
		{
			amount = Integer.parseInt(amountString);
		}
		catch(Exception e)
		{
			//INVALID AMOUNT
			String message = "Invalid amount!";
			JOptionPane.showMessageDialog(new JFrame(), message, "Invalid amount", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(amount < 1 /*|| amount > 100*/)
		{
			//INVALID AMOUNT
			String message = "Amount must be between 1-100!";
			JOptionPane.showMessageDialog(new JFrame(), message, "Invalid amount", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//RECOVER WALLET
		Controller.getInstance().recoverWallet(seed, password, amount);
		
		//CALLBACK
		this.parent.onWalletCreated();
		
		//CLOSE THIS WINDOW
		this.dispose();
	}
	
	private void onBackClick() 
	{
		this.parent.setVisible(true);
		
		this.dispose();		
	}
}
