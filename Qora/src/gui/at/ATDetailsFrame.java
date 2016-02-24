package gui.at;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import at.AT;
import qora.account.Account;
import qora.crypto.Base58;
import utils.Converter;

public class ATDetailsFrame extends JFrame {

	private static final long serialVersionUID = 4763074704570450206L;

	public ATDetailsFrame(AT at)
	{	
		super("ERM - AT Details");
		
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
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

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

		//LABEL ADDRESS
		labelGBC.gridy = 0;
		JLabel addressLabel = new JLabel("Address:");
		this.add(addressLabel, labelGBC);

		//OWNER
		detailGBC.gridy = 0;
		JTextField address = new JTextField(new Account(Base58.encode(at.getId())).getAddress().toString());
		address.setEditable(false);
		this.add(address, detailGBC);

		
		//LABEL AT TYPE
		labelGBC.gridy = 1;
		JLabel keyLabel = new JLabel("Type:");
		this.add(keyLabel, labelGBC);

		//TYPE
		detailGBC.gridy = 1;
		JTextField txtKey = new JTextField(at.getType());
		txtKey.setEditable(false);
		this.add(txtKey, detailGBC);	

		//LABEL NAME
		labelGBC.gridy = 2;
		JLabel nameLabel = new JLabel("Name:");
		this.add(nameLabel, labelGBC);

		//NAME
		detailGBC.gridy = 2;
		JTextField txtName = new JTextField(at.getName());
		txtName.setEditable(false);
		this.add(txtName, detailGBC);		

		//LABEL DESCRIPTION
		labelGBC.gridy = 3;
		JLabel descriptionLabel = new JLabel("Description:");
		this.add(descriptionLabel, labelGBC);

		//DESCRIPTION
		detailGBC.gridy = 3;
		JTextArea txtAreaDescription = new JTextArea(at.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(txtName.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);	

		//LABEL OWNER
		labelGBC.gridy = 4;
		JLabel ownerLabel = new JLabel("Creator:");
		this.add(ownerLabel, labelGBC);

		//OWNER
		detailGBC.gridy = 4;
		JTextField owner = new JTextField(new Account(Base58.encode(at.getCreator())).getAddress().toString());
		owner.setEditable(false);
		this.add(owner, detailGBC);

		//LABEL QUANTITY
		labelGBC.gridy = 5;
		JLabel quantityLabel = new JLabel("Quantity:");
		this.add(quantityLabel, labelGBC);

		//QUANTITY
		detailGBC.gridy = 5;
		JTextField txtQuantity = new JTextField(new Account(Base58.encode(at.getId())).getConfirmedBalance().toPlainString());
		txtQuantity.setEditable(false);
		this.add(txtQuantity, detailGBC);		

		//LABEL DIVISIBLE
		labelGBC.gridy = 6;
		JLabel divisibleLabel = new JLabel("Dead:");
		this.add(divisibleLabel, labelGBC);

		//DIVISIBLE
		detailGBC.gridy = 6;
		JCheckBox chkDivisible = new JCheckBox();
		chkDivisible.setSelected(at.getMachineState().isDead());
		chkDivisible.setEnabled(false);
		this.add(chkDivisible, detailGBC);	

		//CODE LABEL - TEXT
		labelGBC.gridy = 7;
		JLabel atCodeLabel = new JLabel("Code:");
		this.add(atCodeLabel, labelGBC);
		detailGBC.gridy = 7;
		JTextArea txtAreaCode = new JTextArea(Converter.toHex(at.getAp_Code()));
		txtAreaCode.setLineWrap(true);
		txtAreaCode.setWrapStyleWord(true);
		txtAreaCode.setBorder(txtName.getBorder());
		txtAreaCode.setEditable(false);
		this.add(txtAreaCode, detailGBC);
		((JFrame)txtAreaCode.getTopLevelAncestor()).pack();

		//DATA LABEL - TEXT
		labelGBC.gridy = 8;
		JLabel atDataLabel = new JLabel("Data:");
		this.add(atDataLabel, labelGBC);
		detailGBC.gridy = 8;
		JTextArea txtAreaData = new JTextArea(Converter.toHex(at.getAp_data().array()));
		txtAreaData.setLineWrap(true);
		txtAreaData.setWrapStyleWord(true);
		txtAreaData.setBorder(txtName.getBorder());
		txtAreaData.setEditable(false);
		txtAreaData.setAutoscrolls(true);
		txtAreaData.setAutoscrolls(true);
		this.add(txtAreaData, detailGBC);
		((JFrame)txtAreaData.getTopLevelAncestor()).pack();
		
		//CODE LABEL - TEXT
		labelGBC.gridy = 9;
		JLabel minActivationLabel = new JLabel("Min Activation Amount:");
		this.add(minActivationLabel, labelGBC);
		detailGBC.gridy = 9;
		JTextField txtMinActiv = new JTextField( BigDecimal.valueOf(at.minActivationAmount() , 8).toPlainString());
		txtMinActiv.setEditable(false);
		this.add(txtMinActiv, detailGBC);

		//PACK
		this.pack();
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
