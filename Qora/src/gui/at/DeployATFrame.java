package gui.at;

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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import at.AT_Constants;
import at.AT_Error;
import controller.Controller;
import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import settings.Settings;
import utils.Converter;
import utils.Pair;

@SuppressWarnings("serial")
public class DeployATFrame extends JFrame {
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JTextField txtType;
	private JTextField txtTags;
	private JTextField txtCode;
	private JTextField txtData;
	private JTextField txtDPages;
	private JTextField txtCPages;
	private JTextField txtUSPages;
	private JTextField txtMinActivationAmount;

	private JTextField txtQuantity;

	private JButton deployButton;


	public DeployATFrame()
	{
		super("Qora - Deploy AT Program");

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
		this.txtareaDescription.setRows(4);
		this.txtareaDescription.setBorder(this.txtName.getBorder());
		this.add(this.txtareaDescription, txtGBC);

		//LABEL TYPE
		labelGBC.gridy = 3;
		JLabel typeBytesLabel = new JLabel("Type/Domain:");
		this.add(typeBytesLabel, labelGBC);

		//TXTAREA TYPE
		txtGBC.gridy = 3;
		this.txtType = new JTextField();
		this.txtType.setBorder(this.txtType.getBorder());
		this.add(this.txtType, txtGBC);

		//LABEL TAGS
		labelGBC.gridy = 4;
		JLabel tagsBytesLabel = new JLabel("Tags:");
		this.add(tagsBytesLabel, labelGBC);

		//TXTAREA TAGS
		txtGBC.gridy = 4;
		this.txtTags = new JTextField();
		this.txtTags.setBorder(this.txtTags.getBorder());
		this.add(this.txtTags, txtGBC);


		//LABEL CODE
		labelGBC.gridy = 5;
		JLabel codeBytesLabel = new JLabel("Code:");
		this.add(codeBytesLabel, labelGBC);

		//TXTAREA CODE
		txtGBC.gridy = 5;
		this.txtCode = new JTextField();
		this.txtCode.setBorder(this.txtCode.getBorder());
		this.add(this.txtCode, txtGBC);

		//LABEL DATA
		labelGBC.gridy = 6;
		JLabel dataBytesLabel = new JLabel("Data:");
		this.add(dataBytesLabel, labelGBC);

		//TXTAREA DESCRIPTION
		txtGBC.gridy = 6;
		this.txtData = new JTextField();
		this.txtData.setBorder(this.txtData.getBorder());
		this.add(this.txtData, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 7;
		JLabel dPagesLabel = new JLabel("Data Pages:");
		this.add(dPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 7;
		this.txtDPages = new JTextField();
		this.add(this.txtDPages, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 8;
		JLabel csPagesLabel = new JLabel("CS Pages:");
		this.add(csPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 8;
		this.txtCPages = new JTextField();
		this.add(this.txtCPages, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 9;
		JLabel usPagesLabel = new JLabel("US Pages:");
		this.add(usPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 9;
		this.txtUSPages = new JTextField();
		this.add(this.txtUSPages, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 10;
		JLabel minActivationAmountLabel = new JLabel("Minimum Activation Amount:");
		this.add(minActivationAmountLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 10;
		this.txtMinActivationAmount = new JTextField();
		this.add(this.txtMinActivationAmount, txtGBC);

		//LABEL QUANTITY
		labelGBC.gridy = 11;
		JLabel quantityLabel = new JLabel("Quantity:");
		this.add(quantityLabel, labelGBC);

		//TXT QUANTITY
		txtGBC.gridy = 11;
		this.txtQuantity = new JTextField();
		this.txtQuantity.setText("1");
		this.add(this.txtQuantity, txtGBC);

		//LABEL FEE
		labelGBC.gridy = 12;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);

		//TXT FEE
		txtGBC.gridy = 12;
		this.txtFee = new JTextField();
		this.txtFee.setText("1");
		this.add(this.txtFee, txtGBC);

		//BUTTON Register
		buttonGBC.gridy = 13;
		this.deployButton = new JButton("Deploy");
		this.deployButton.setPreferredSize(new Dimension(80, 25));
		this.deployButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onDeployClick();
			}
		});
		this.add(this.deployButton, buttonGBC);

		//PACK
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void onDeployClick()
	{
		//DISABLE
		this.deployButton.setEnabled(false);

		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);

			//ENABLE
			this.deployButton.setEnabled(true);

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
				this.deployButton.setEnabled(true);

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
			BigDecimal quantity = new BigDecimal(this.txtQuantity.getText()).setScale(8);

			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);

				//ENABLE
				this.deployButton.setEnabled(true);

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
					this.deployButton.setEnabled(true);
					
					return;
				}
			}

			//
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());


			String code = this.txtCode.getText();
			if (code.length()==0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Code is empty!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			String data = this.txtData.getText();

			if(data == null)
				data = "";
			if((data.length() & 1) != 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Data error!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			int cpages = (code.length() / 2 / 256) + (((code.length() / 2) % 256 ) != 0 ? 1 : 0);

			String dPages = this.txtDPages.getText();
			String csPages = this.txtCPages.getText();
			String usPages = this.txtUSPages.getText();

			int dpages = Integer.parseInt( dPages );
			int cspages = Integer.parseInt( csPages );
			int uspages = Integer.parseInt( usPages );

			if ( dpages < 0 || cspages < 0 || uspages < 0 )
			{
				JOptionPane.showMessageDialog(new JFrame(), "Error in data or cs or us pages!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			byte[] balanceBytes = fee.unscaledValue().toByteArray();
			byte[] fill = new byte[8 - balanceBytes.length];
			balanceBytes = Bytes.concat(fill, balanceBytes);

			long lFee = Longs.fromByteArray(balanceBytes);
			
			if ( (cpages + dpages + cspages + uspages) * AT_Constants.getInstance().COST_PER_PAGE( DBSet.getInstance().getBlockMap().getLastBlock().getHeight()) > lFee )
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fees should be at least " + (cpages + dpages + cspages + uspages) * AT_Constants.getInstance().COST_PER_PAGE( DBSet.getInstance().getBlockMap().getLastBlock().getHeight()) + " !", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			BigDecimal minActivationAmountB = new BigDecimal(this.txtMinActivationAmount.getText()).setScale(8);

			byte[] minActivationAmountBytes = minActivationAmountB.unscaledValue().toByteArray();
			byte[] fillActivation = new byte[8 - minActivationAmountBytes.length];
			minActivationAmountBytes = Bytes.concat(fillActivation, minActivationAmountBytes);

			long minActivationAmount = Longs.fromByteArray(minActivationAmountBytes);
			
			int creationLength = 4;
			creationLength += 8; //pages
			creationLength += 8; //minActivationAmount
			creationLength += cpages * 256 <= 256 ? 1 : (cpages * 256 <= 32767 ? 2 : 4);
			creationLength += code.length() / 2;

			creationLength += dpages * 256 <= 256 ? 1 : (dpages * 256 <= 32767 ? 2 : 4); // data size
			creationLength += data.length() / 2;
			
			ByteBuffer creation = ByteBuffer.allocate(creationLength);
			creation.order(ByteOrder.LITTLE_ENDIAN);
			
			creation.putShort(AT_Constants.getInstance().AT_VERSION( DBSet.getInstance().getBlockMap().getLastBlock().getHeight() ));
			creation.putShort((short)0);
			creation.putShort((short)cpages);
			creation.putShort((short)dpages);
			creation.putShort((short)cspages);
			creation.putShort((short)uspages);
			creation.putLong(minActivationAmount);
			if(cpages * 256 <= 256)
				creation.put((byte)(code.length()/2));
			else if(cpages * 256 <= 32767)
				creation.putShort((short)(code.length()/2));
			else
				creation.putInt(code.length()/2);
			byte[] codeBytes = Converter.parseHexString(code);
			if(codeBytes != null)
				creation.put(codeBytes);
			if(dpages * 256 <= 256)
				creation.put((byte)(data.length()/2));
			else if(dpages * 256 <= 32767)
				creation.putShort((short)(data.length()/2));
			else
				creation.putInt(data.length()/2);
		
			byte[] dataBytes = Converter.parseHexString(data);
			if(dataBytes != null)
				creation.put(dataBytes);
			byte[] creationBytes = null;
			creationBytes = creation.array();

			
			
			BigDecimal recommendedFee = Controller.getInstance().calcRecommendedFeeForDeployATTransaction(this.txtName.getText(), this.txtareaDescription.getText(), this.txtType.getText(), this.txtTags.getText(), creationBytes).getA();
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
						this.txtFee.setText("1.00000000"); // Return to the default fee for the next message.
					}
					
					fee = recommendedFee; // Set recommended fee for this message.
					
				}
				else if (n == JOptionPane.NO_OPTION) {
					
				}	
				else {
					
					//ENABLE
					this.deployButton.setEnabled(true);
					
					return;
				}
			}			
			
			Pair<Transaction, Integer> result = Controller.getInstance().deployAT(creator, this.txtName.getText(), this.txtareaDescription.getText() , this.txtType.getText(), this.txtTags.getText() , creationBytes, quantity, fee);

			//CHECK VALIDATE MESSAGE
			if (result.getB() > 1000)
			{
				JOptionPane.showMessageDialog(new JFrame(), AT_Error.getATError( result.getB() - Transaction.AT_ERROR) , "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				JOptionPane.showMessageDialog(new JFrame(), "AT has been deployed!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
			case Transaction.NOT_YET_RELEASED:
				JOptionPane.showMessageDialog(new JFrame(), "AT will be enabled after " + Transaction.getAT_BLOCK_HEIGHT_RELEASE() + " block!",  "Error", JOptionPane.ERROR_MESSAGE);
				break;
			case Transaction.NEGATIVE_FEE:
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;	
			case Transaction.FEE_LESS_REQUIRED:
				JOptionPane.showMessageDialog(new JFrame(), "Fee below the minimum for this size of a transaction!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.NEGATIVE_AMOUNT:
				JOptionPane.showMessageDialog(new JFrame(), "Quantity must be at least 0!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.NO_BALANCE:
				JOptionPane.showMessageDialog(new JFrame(), "Not enough balance!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_NAME_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), "Name length exceeded!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_TAGS_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), "Tags length exceeded!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_TYPE_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), "Type length exceeded!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), "Description length exceeded!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			default:
				JOptionPane.showMessageDialog(new JFrame(), "Unknown error!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;	
			}
		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), "Unknown exception!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
			}
			e.printStackTrace();
		}

		//ENABLE
		this.deployButton.setEnabled(true);
	}



}
