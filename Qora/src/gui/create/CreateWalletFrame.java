package gui.create;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import qora.crypto.Base58;

@SuppressWarnings("serial")
public class CreateWalletFrame extends JFrame {
	
	private byte[] seed;
	private NoWalletFrame parent;
	private Random random;
	
	public CreateWalletFrame(NoWalletFrame parent)
	{
		super("Qora - Create Wallet");
		
		//RANDOM
		this.random = new SecureRandom();
		
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
		buttonGBC.insets = new Insets(5,5,0,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 1;
		buttonGBC.gridx = 0;		
		
		//LABEL
		labelGBC.gridy = 0;
		JLabel label1 = new JLabel("Your wallet generation seed:");	
		this.add(label1, labelGBC);
		
		//ADD TEXTBOX
		labelGBC.gridy = 1;
		this.seed = this.generateSeed();
		final JTextField seedTxt = new JTextField();
		seedTxt.setText(Base58.encode(seed));
		seedTxt.setEditable(false);	
		seedTxt.setBackground(new JTextField().getBackground());
		this.add(seedTxt, labelGBC);
		
		// MENU
		JPopupMenu menu = new JPopupMenu();
		JMenuItem copySeed = new JMenuItem("Copy Seed");
		copySeed.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(seedTxt.getText());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copySeed);
		seedTxt.setComponentPopupMenu(menu);
		
		
		//LABEL
      	labelGBC.gridy = 2;
      	JLabel label2 = new JLabel("This seed is the result of a Base58 encoded 256bit random key.");
      	this.add(label2, labelGBC);
      	
      	//LABEL
      	labelGBC.gridy = 3;
      	JLabel label3 = new JLabel("This seed will allow you to recover your wallet if you would accidently delete the wallet file.");
      	this.add(label3, labelGBC);
      	
      	//LABEL
      	labelGBC.gridy = 4;
      	JLabel label4 = new JLabel("<html><b>KEEP THIS SEED PRIVATE AND SECURE!</b></html>");
      	this.add(label4, labelGBC);
      	
     	//LABEL
      	labelGBC.gridy = 5;
      	JLabel label5 = new JLabel("Anyone who has access to your seed will have access to your wallet.");
      	this.add(label5, labelGBC);
      		
      	 //BUTTON NEXT
        buttonGBC.gridy = 6;
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onNextClick();
		    }
		});	
        
        
        nextButton.setPreferredSize(new Dimension(80, 25));
    	this.add(nextButton, buttonGBC);
    	
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
	
	private void onNextClick() 
	{		
		//OPEN CONFIRM SEED FRAME
		this.setVisible(false);
		new ConfirmSeedFrame(this);	
	}
	
	private void onBackClick() 
	{
		
		this.parent.setVisible(true);
		
		this.dispose();		
		
	}

	public byte[] getSeed() 
	{
		return this.seed;
	}

	public void onConfirm(String password) 
	{
		//CREATE WALLET
		Controller.getInstance().recoverWallet(this.seed, password, 10);
		
		//LET GUI KNOW
		parent.onWalletCreated();
		
		//CLOSE THIS WINDOW
		this.dispose();
	}
	
	//GENERATE
	
	private byte[] generateSeed()
	{
		byte[] seed = new byte[32];
		this.random.nextBytes(seed);	
		return seed;
	}
}
