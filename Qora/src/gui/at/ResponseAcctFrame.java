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
import java.math.RoundingMode;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Base58;
import qora.transaction.Transaction;
import utils.Converter;
import utils.Pair;
import at.AT_Constants;
import at.AT_Error;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import controller.Controller;
import database.DBSet;

@SuppressWarnings("serial")
public class ResponseAcctFrame extends JFrame {
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtHashSecret;
	private JTextField txtExpirationBlocks;
	private JTextField txtRecipient;
	private JTextField txtMinActivationAmount;
	private JTextField txtAmount;
	private JTextField txtAmountOther;
	private JTextField txtBurstAddress;

	private JButton deployButton;


	public ResponseAcctFrame()
	{
		super("Qora - Response ACCT");

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
		cbxGBC.gridwidth = 4;
		cbxGBC.gridx = 1;	

		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
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
		cbxGBC.gridy = 0;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
		this.add(this.cbxFrom, cbxGBC);

		//SWAP LABEP
		labelGBC.gridy = 1;
		JLabel swapLabel = new JLabel("Trade:");
		this.add(swapLabel, labelGBC);

		//TXT AMOUNT
		txtGBC.gridy = 1;
		this.txtAmount = new JTextField();
		this.add(this.txtAmount, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 1;
		labelGBC.gridx = 2;
		JLabel qoraLabel = new JLabel("QORA for");
		this.add(qoraLabel, labelGBC);

		//TXT AMOUNT
		txtGBC.gridy = 1;
		txtGBC.gridx = 3;
		this.txtAmountOther = new JTextField();
		this.add(this.txtAmountOther, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 1;
		labelGBC.gridx = 4;
		JLabel otherLabel = new JLabel("BURST");
		this.add(otherLabel, labelGBC);

		//LABEL NAME
		labelGBC.gridy = 2;
		labelGBC.gridx = 0;
		JLabel csPagesLabel = new JLabel("Recipient:");
		this.add(csPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 2;
		txtGBC.gridx = 1;
		txtGBC.gridwidth = 4;
		this.txtRecipient = new JTextField();
		this.add(this.txtRecipient, txtGBC);
		txtGBC.gridwidth = 1;
		
		//LABEL RECIPIENT
		labelGBC.gridx = 3;
		JLabel recipientAlert = new JLabel(" ( * optional field )");
		this.add(recipientAlert, labelGBC);

		//LABEL BURST ADDRESS
		labelGBC.gridy = 3;
		labelGBC.gridx = 0;
		JLabel burstAddressLabel = new JLabel("My BURST address:");
		this.add(burstAddressLabel, labelGBC);

		//TXT LABEL ADDRESS
		txtGBC.gridy = 3;
		txtGBC.gridx = 1;
		txtGBC.gridwidth = 4;
		this.txtBurstAddress = new JTextField();
		this.add(this.txtBurstAddress, txtGBC);
		txtGBC.gridwidth = 1;


		//LABEL DATA
		labelGBC.gridy = 4;
		labelGBC.gridx = 0;
		JLabel dataBytesLabel = new JLabel("Lock:");
		this.add(dataBytesLabel, labelGBC);

		//TXTAREA DESCRIPTION
		txtGBC.gridy = 4;
		txtGBC.gridx = 1;
		txtGBC.gridwidth = 4;
		this.txtHashSecret = new JTextField();
		this.txtHashSecret.setBorder(this.txtHashSecret.getBorder());
		this.add(this.txtHashSecret, txtGBC);
		txtGBC.gridwidth = 1;

		//PASSWORD ALERT
		labelGBC.gridy = 5;
		labelGBC.gridx = 0;
		labelGBC.gridwidth = 4;
		JLabel passAlert = new JLabel("( Use the lock of the matching counterparty )");
		this.add(passAlert, labelGBC);

		//LABEL FEE
		labelGBC.gridy = 6;
		labelGBC.gridx = 0;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);

		//TXT FEE
		txtGBC.gridy = 6;
		txtGBC.gridx = 1;
		this.txtFee = new JTextField();
		this.txtFee.setText("20.00000000");
		this.add(this.txtFee, txtGBC);

		//LABEL FEES ALERT
		labelGBC.gridx = 2;
		labelGBC.gridwidth = 3;
		JLabel feesAlertLabel = new JLabel("( fees should be at least 20 )");
		this.add(feesAlertLabel, labelGBC);

		//LABEL NAME
		labelGBC.gridy = 7;
		labelGBC.gridx = 0;
		final JLabel dPagesLabel = new JLabel("Expire after:");
		this.add(dPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 7;
		txtGBC.gridx = 1;
		this.txtExpirationBlocks = new JTextField("200");
		this.add(this.txtExpirationBlocks, txtGBC);

		//LABEL BLOCKS
		labelGBC.gridx = 2;
		labelGBC.gridwidth = 3;
		JLabel blocksLabel = new JLabel("blocks ( 1 block approx. " + + AT_Constants.getInstance().AVERAGE_BLOCK_MINUTES(DBSet.getInstance().getBlockMap().getLastBlock().getHeight()) +" min )");
		this.add(blocksLabel, labelGBC);
		labelGBC.gridwidth = 1;

		//LABEL NAME
		labelGBC.gridy = 8;
		labelGBC.gridx = 0;
		JLabel minActivationAmountLabel = new JLabel("Min activation amount:");
		this.add(minActivationAmountLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 8;
		this.txtMinActivationAmount = new JTextField("20.00000000");
		this.add(this.txtMinActivationAmount, txtGBC);


		//BUTTON Register
		buttonGBC.gridy = 9;
		this.deployButton = new JButton("Respond");
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
			BigDecimal quantity = new BigDecimal(this.txtAmount.getText()).setScale(8);
			BigDecimal burstQuantity = new BigDecimal(this.txtAmountOther.getText()).setScale(8);


			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);

				//ENABLE
				this.deployButton.setEnabled(true);

				return;
			}

			//CREATE POLL
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());


			String code = "3501030900000006040000000900000029302009000000040000000f1ab4000000330403090000003525010a000000260a000000320903350703090000003526010a0000001b0a000000cd322801331601000000003317010100000033180102000000331901030000003505020a0000001b0a000000a1320b033205041e050000001833000509000000320a033203041ab400000033160105000000331701060000003318010700000033190108000000320304320b033203041ab7000000";
			if (code.length()==0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Code is empty!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			int byteBufferSize = 40;
			byte[] address = new byte[25];
			byte[] empty = new byte[7];
			ByteBuffer b = ByteBuffer.allocate( byteBufferSize );
			if ( !this.txtRecipient.getText().isEmpty() )
			{
				byteBufferSize += 4*8;
				String addr = this.txtRecipient.getText();
				address= Base58.decode(addr);
			}

			b = ByteBuffer.allocate( byteBufferSize );
			b.order(ByteOrder.LITTLE_ENDIAN);
			
			int blocksToEnd = 0;
			try
			{
				blocksToEnd= Integer.parseInt( this.txtExpirationBlocks.getText() );
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Expiration error", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}
			
			if ( blocksToEnd < 20 )
			{
				JOptionPane.showMessageDialog(new JFrame(), "Expiration should be at least 20", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			b.putInt(0);
			b.putInt(blocksToEnd);
			if ( !this.txtRecipient.getText().isEmpty() )
			{
				b.put(address);
				b.put(empty);
			}
			b.clear();

			String pas = this.txtHashSecret.getText();
			String data = pas+Converter.toHex(b.array()).toLowerCase();


			if((data.length() & 1) != 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Data error!", "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			int cpages = (code.length() / 2 / 256) + (((code.length() / 2) % 256 ) != 0 ? 1 : 0);


			int dpages = 1;
			int cspages = 0;
			int uspages = 0;

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
				this.deployButton.setEnabled(true);
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

			String name = "BURSTQORA @ " + burstQuantity.divide(quantity, 2, RoundingMode.HALF_UP);
			String desc = "Responders BURST address: " + this.txtBurstAddress.getText();
			String type = "acct";
			String tags = "acct,atomic cross chain tx,respond,responder";


			Pair<Transaction, Integer> result = Controller.getInstance().deployAT(creator, name, desc , type , tags , creationBytes, quantity, fee);

			//CHECK VALIDATE MESSAGE
			if (result.getB() > 1000)
			{
				JOptionPane.showMessageDialog(new JFrame(), AT_Error.getATError( result.getB() - Transaction.AT_ERROR) , "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				JOptionPane.showMessageDialog(new JFrame(), "AT has been deployed!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
			case Transaction.NOT_YET_RELEASED:
				JOptionPane.showMessageDialog(new JFrame(), "AT will be enabled at !",  "Error", JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.NEGATIVE_FEE:
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(new JFrame(), "Name must be between 1 and 100 characters!", "Error", JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(new JFrame(), "Description must be between 1 and 1000 characters!", "Error", JOptionPane.ERROR_MESSAGE);
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
