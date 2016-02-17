package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import qora.crypto.Base58;
import qora.transaction.UpdateNameTransaction;
import utils.DateTimeFormat;
import utils.GZIP;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class UpdateNameDetailsFrame extends JFrame
{
	public UpdateNameDetailsFrame(UpdateNameTransaction nameUpdate)
	{
		super("Qora - Transaction Details");
		
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
		
		//LABEL TYPE
		labelGBC.gridy = 0;
		JLabel typeLabel = new JLabel("Type:");
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = 0;
		JLabel type = new JLabel("Update Name Transaction");
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		labelGBC.gridy = 1;
		JLabel signatureLabel = new JLabel("Signature:");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = 1;
		JTextField signature = new JTextField(Base58.encode(nameUpdate.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		labelGBC.gridy = 2;
		JLabel referenceLabel = new JLabel("Reference:");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = 2;
		JTextField reference = new JTextField(Base58.encode(nameUpdate.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		labelGBC.gridy = 3;
		JLabel timestampLabel = new JLabel("Timestamp:");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = 3;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(nameUpdate.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL REGISTRANT
		labelGBC.gridy = 4;
		JLabel registrantLabel = new JLabel("Owner:");
		this.add(registrantLabel, labelGBC);
		
		//REGISTRANT
		detailGBC.gridy = 4;
		JTextField registrant = new JTextField(nameUpdate.getOwner().getAddress());
		registrant.setEditable(false);
		MenuPopupUtil.installContextMenu(registrant);
		this.add(registrant, detailGBC);
		
		//LABEL OWNER
		labelGBC.gridy = 5;
		JLabel ownerLabel = new JLabel("New Owner:");
		this.add(ownerLabel, labelGBC);
				
		//OWNER
		detailGBC.gridy = 5;
		JTextField owner = new JTextField(nameUpdate.getName().getOwner().getAddress());
		owner.setEditable(false);
		MenuPopupUtil.installContextMenu(owner);
		this.add(owner, detailGBC);
		
		//LABEL NAME
		labelGBC.gridy = 6;
		JLabel nameLabel = new JLabel("Name:");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 6;
		JTextField name = new JTextField(nameUpdate.getName().getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL VALUE
		labelGBC.gridy = 7;
		JLabel valueLabel = new JLabel("New Value:");
		this.add(valueLabel, labelGBC);
				
		//VALUE
		detailGBC.gridy = 7;

		JTextArea txtareaValue = new JTextArea(GZIP.webDecompress(nameUpdate.getName().getValue()));
		txtareaValue.setRows(10);
      	txtareaValue.setColumns(43);
      	txtareaValue.setEditable(false);
		MenuPopupUtil.installContextMenu(txtareaValue);
      	
      	JScrollPane valueScroll = new JScrollPane(txtareaValue);
      	valueScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	valueScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(valueScroll, detailGBC);
      	      	
      	//LABEL COMPRESSED
      	labelGBC.gridy = 8;
      	JLabel compressedLabel = new JLabel("Compressed:");
      	this.add(compressedLabel, labelGBC);
      		
  		//COMPRESSED
  		detailGBC.gridy = 8;
  		final JCheckBox compressed = new JCheckBox();
  		compressed.setSelected(nameUpdate.getName().getValue().startsWith("?gz!"));
  		compressed.setEnabled(false);
     	
  		this.add(compressed, detailGBC);		
      	
		//LABEL FEE
		labelGBC.gridy = 9;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = 9;
		JTextField fee = new JTextField(nameUpdate.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		labelGBC.gridy = 10;
		JLabel confirmationsLabel = new JLabel("Confirmations:");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = 10;
		JLabel confirmations = new JLabel(String.valueOf(nameUpdate.getConfirmations()));
		this.add(confirmations, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
