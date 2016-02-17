package gui.voting;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import database.PollMap;
import gui.QoraRowSorter;
import gui.models.AssetsAllComboBoxModel;
import gui.models.PollsTableModel;
import qora.assets.Asset;
import qora.voting.Poll;

@SuppressWarnings("serial")
public class AllPollsFrame extends JFrame{

	private PollsTableModel pollsTableModel;
	private JComboBox<Asset> cbxAssets;
	
	public AllPollsFrame() 
	{
		//CREATE FRAME
		super("Qora - All Polls");
		
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
		
		//SEACH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.CENTER;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 1;
		
		//SEACH GBC
		GridBagConstraints searchGBC = new GridBagConstraints();
		searchGBC.insets = new Insets(0, 5, 5, 0);
		searchGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchGBC.anchor = GridBagConstraints.NORTHWEST;
		searchGBC.weightx = 1;	
		searchGBC.gridwidth = 1;
		searchGBC.gridx = 1;
		searchGBC.gridy = 1;
				
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 2;	
		
		//ASSET LABEL GBC
		GridBagConstraints assetLabelGBC = new GridBagConstraints();
		assetLabelGBC.insets = new Insets(0, 5, 5, 0);
		assetLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetLabelGBC.anchor = GridBagConstraints.CENTER;
		assetLabelGBC.weightx = 0;	
		assetLabelGBC.gridwidth = 1;
		assetLabelGBC.gridx = 0;
		assetLabelGBC.gridy = 0;
		
		//ASSETS GBC
		GridBagConstraints assetsGBC = new GridBagConstraints();
		assetsGBC.insets = new Insets(0, 5, 5, 0);
		assetsGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetsGBC.anchor = GridBagConstraints.NORTHWEST;
		assetsGBC.weightx = 0;	
		assetsGBC.gridwidth = 1;
		assetsGBC.gridx = 1;
		assetsGBC.gridy = 0;

		//CREATE TABLE
		this.pollsTableModel = new PollsTableModel();
		final JTable pollsTable = new JTable(this.pollsTableModel);
				
		//NAMESALES SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(PollsTableModel.COLUMN_NAME, PollMap.DEFAULT_INDEX);
		QoraRowSorter sorter = new QoraRowSorter(this.pollsTableModel, indexes);
		pollsTable.setRowSorter(sorter);

		pollsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = pollsTable.rowAtPoint(p);
				pollsTable.setRowSelectionInterval(row, row);
		     }
		});
		
		pollsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = pollsTable.rowAtPoint(p);
				pollsTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = pollsTable.convertRowIndexToModel(row);
					Poll poll = pollsTableModel.getPoll(row);
					Asset asset = (Asset) cbxAssets.getSelectedItem();
					new PollFrame(poll, asset);
				}
		     }
		});
		
		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		// UPDATE FILTER ON TEXT CHANGE
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void onChange() {

				// GET VALUE
				String search = txtSearch.getText();

			 	// SET FILTER
				pollsTableModel.getSortableList().setFilter(search);
				pollsTableModel.fireTableDataChanged();
			}
		});

		this.add(new JLabel("Search:"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		this.add(new JScrollPane(pollsTable), tableGBC);

		this.add(new JLabel("Asset:"), assetLabelGBC);
		
		cbxAssets = new JComboBox<Asset>(new AssetsAllComboBoxModel());
		this.add(cbxAssets, assetsGBC);
		
		cbxAssets.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {

		    	Asset asset = ((Asset) cbxAssets.getSelectedItem());

		    	if(asset != null)
		    	{
		    		pollsTableModel.setAsset(asset);
		    	}
		    }
		});
		
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//REMOVE OBSERVERS/HANLDERS
            	pollsTableModel.removeObservers();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
		       
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
