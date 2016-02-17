package gui.naming;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import qora.naming.Name;
import utils.GZIP;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class NameDetailsFrame extends JFrame
{
	public NameDetailsFrame(Name name)
	{
		super("Qora - Name Details");
		
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
		
		//LABEL REGISTRANT
		labelGBC.gridy = 1;
		JLabel registrantLabel = new JLabel("Owner:");
		this.add(registrantLabel, labelGBC);
		
		//REGISTRANT
		detailGBC.gridy = 1;
		JTextField registrant = new JTextField(name.getOwner().getAddress());
		registrant.setEditable(false);
		MenuPopupUtil.installContextMenu(registrant);
		this.add(registrant, detailGBC);
		
		//LABEL NAME
		labelGBC.gridy = 2;
		JLabel nameLabel = new JLabel("Name:");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 2;
		JTextField txtName = new JTextField(name.getName());
		txtName.setEditable(false);
		MenuPopupUtil.installContextMenu(txtName);
		this.add(txtName, detailGBC);		
		
		//LABEL VALUE
		labelGBC.gridy = 3;
		JLabel valueLabel = new JLabel("Value:");
		this.add(valueLabel, labelGBC);
		           
		//VALUE
		detailGBC.gridy = 3;
		JTextArea txtAreaValue = new JTextArea(GZIP.webDecompress(name.getValue()));
		txtAreaValue.setRows(4);
		txtAreaValue.setBorder(txtName.getBorder());
		txtAreaValue.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaValue);
		this.add(txtAreaValue, detailGBC);	
		
        //PACK
		this.pack();
		this.setSize(500, this.getHeight());
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
