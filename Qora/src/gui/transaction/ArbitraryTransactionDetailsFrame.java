package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import gui.Gui;
import gui.models.PaymentsTableModel;
import qora.crypto.Base58;
import qora.transaction.ArbitraryTransaction;
import utils.BigDecimalStringComparator;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class ArbitraryTransactionDetailsFrame extends JFrame
{
	public ArbitraryTransactionDetailsFrame(ArbitraryTransaction arbitraryTransaction)
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
		
		int componentLevel = 0;

		//LABEL TYPE
		labelGBC.gridy = componentLevel;
		JLabel typeLabel = new JLabel("Type:");
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = componentLevel;
		JLabel type = new JLabel("Arbitrary Transaction");
		this.add(type, detailGBC);
		
		componentLevel ++;

		//LABEL SIGNATURE
		labelGBC.gridy = componentLevel;
		JLabel signatureLabel = new JLabel("Signature:");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = componentLevel;
		JTextField signature = new JTextField(Base58.encode(arbitraryTransaction.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		componentLevel ++;

		//LABEL REFERENCE
		labelGBC.gridy = componentLevel;
		JLabel referenceLabel = new JLabel("Reference:");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = componentLevel;
		JTextField reference = new JTextField(Base58.encode(arbitraryTransaction.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		componentLevel ++;

		//LABEL TIMESTAMP
		labelGBC.gridy = componentLevel;
		JLabel timestampLabel = new JLabel("Timestamp:");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = componentLevel;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(arbitraryTransaction.getTimestamp()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		componentLevel ++;
		
		//LABEL SENDER
		labelGBC.gridy = componentLevel;
		JLabel senderLabel = new JLabel("Creator:");
		this.add(senderLabel, labelGBC);
		
		//SENDER
		detailGBC.gridy = componentLevel;
		JTextField sender = new JTextField(arbitraryTransaction.getCreator().getAddress());
		sender.setEditable(false);
		MenuPopupUtil.installContextMenu(sender);
		this.add(sender, detailGBC);
		
		componentLevel ++;
		
		//LABEL SERVICE
		labelGBC.gridy = componentLevel;
		JLabel serviceLabel = new JLabel("Service ID:");
		this.add(serviceLabel, labelGBC);
		
		//SERVICE
		detailGBC.gridy = componentLevel;
		JTextField service = new JTextField(String.valueOf(arbitraryTransaction.getService()));
		service.setEditable(false);
		MenuPopupUtil.installContextMenu(service);
		this.add(service, detailGBC);			
		
		componentLevel ++;

		//LABEL DATA AS BASE58
		labelGBC.gridy = componentLevel;
		JLabel dataLabel = new JLabel("Data as Base58:");
		this.add(dataLabel, labelGBC);
				
		//DATA AS BASE58
		detailGBC.gridy = componentLevel;
		JTextArea txtAreaData = new JTextArea(Base58.encode(arbitraryTransaction.getData()));
		txtAreaData.setRows(6);
		txtAreaData.setColumns(63);
		txtAreaData.setBorder(sender.getBorder());
		txtAreaData.setEditable(false);
		txtAreaData.setLineWrap(true);
		MenuPopupUtil.installContextMenu(txtAreaData);

		JScrollPane AreaDataScroll = new JScrollPane(txtAreaData);
		AreaDataScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		AreaDataScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(AreaDataScroll, detailGBC);
		
		componentLevel ++;

		//LABEL DATA AS TEXT
		labelGBC.gridy = componentLevel;
		JLabel dataTextLabel = new JLabel("Data as Text:");
		this.add(dataTextLabel, labelGBC);
		
		//DATA AS TEXT
		detailGBC.gridy = componentLevel;
		JTextArea txtAreaDataText = new JTextArea(new String(arbitraryTransaction.getData(), Charset.forName("UTF-8")));
		txtAreaDataText.setRows(6);
		txtAreaData.setColumns(63);
		txtAreaDataText.setBorder(sender.getBorder());
		txtAreaDataText.setEditable(false);
		txtAreaDataText.setLineWrap(true);
		MenuPopupUtil.installContextMenu(txtAreaDataText);

		JScrollPane AreaDataTextScroll = new JScrollPane(txtAreaDataText);
		AreaDataTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		AreaDataTextScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(AreaDataTextScroll, detailGBC);
		
		if(arbitraryTransaction.getPayments().size() > 0)
		{
			componentLevel ++;
			
			//LABEL PAYMENTS
			labelGBC.gridy = componentLevel;
			JLabel paymentsLabel = new JLabel("Payments:");
			this.add(paymentsLabel, labelGBC);
			
			//PAYMENTS
			detailGBC.gridy = componentLevel;
			PaymentsTableModel paymentsTableModel = new PaymentsTableModel(arbitraryTransaction.getPayments());
			JTable table = Gui.createSortableTable(paymentsTableModel, 1);
			
			@SuppressWarnings("unchecked")
			TableRowSorter<PaymentsTableModel> sorter =  (TableRowSorter<PaymentsTableModel>) table.getRowSorter();
			sorter.setComparator(PaymentsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());
			
			this.add(new JScrollPane(table), detailGBC);
		}
		
		componentLevel ++;
		
		//LABEL FEE
		labelGBC.gridy = componentLevel;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = componentLevel;
		JTextField fee = new JTextField(arbitraryTransaction.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		componentLevel ++;

		//LABEL CONFIRMATIONS
		labelGBC.gridy = componentLevel;
		JLabel confirmationsLabel = new JLabel("Confirmations:");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = componentLevel;
		JLabel confirmations = new JLabel(String.valueOf(arbitraryTransaction.getConfirmations()));
		this.add(confirmations, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
