package gui.voting;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import gui.models.AssetsAllComboBoxModel;
import qora.assets.Asset;
import qora.voting.Poll;

@SuppressWarnings("serial")
public class PollFrame extends JFrame{

	private PollTabPane PollTabPane;
	private JComboBox<Asset> cbxAssets;
	
	public PollFrame(Poll poll, Asset asset) 
	{
		//CREATE FRAME
		super("Qora - Poll Details");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		
		//ASSET LABEL GBC
		GridBagConstraints assetLabelGBC = new GridBagConstraints();
		assetLabelGBC.insets = new Insets(5, 5, 5, 5);
		assetLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetLabelGBC.anchor = GridBagConstraints.CENTER;
		assetLabelGBC.weightx = 0;	
		assetLabelGBC.gridwidth = 1;
		assetLabelGBC.gridx = 0;
		assetLabelGBC.gridy = 1;
		
		//ASSETS GBC
		GridBagConstraints assetsGBC = new GridBagConstraints();
		assetsGBC.insets = new Insets(5, 5, 5, 5);
		assetsGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetsGBC.anchor = GridBagConstraints.NORTHWEST;
		assetsGBC.weightx = 0;	
		assetsGBC.gridwidth = 1;
		assetsGBC.gridx = 1;
		assetsGBC.gridy = 1;
		
		//POLLTABPANE GBC
		GridBagConstraints pollTabPaneGBC = new GridBagConstraints();
		pollTabPaneGBC.insets = new Insets(0, 5, 5, 0);
		pollTabPaneGBC.fill = GridBagConstraints.HORIZONTAL;   
		pollTabPaneGBC.anchor = GridBagConstraints.NORTHWEST;
		pollTabPaneGBC.weightx = 0;	
		pollTabPaneGBC.gridwidth = 2;
		pollTabPaneGBC.gridx = 0;
		pollTabPaneGBC.gridy = 2;
		
		this.add(new JLabel("Asset:"), assetLabelGBC);
		
		cbxAssets = new JComboBox<Asset>(new AssetsAllComboBoxModel());
		cbxAssets.setSelectedItem(asset);
		this.add(cbxAssets, assetsGBC);
		
		cbxAssets.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {

		    	Asset asset = ((Asset) cbxAssets.getSelectedItem());

		    	if(asset != null)
		    	{
		    		PollTabPane.setAsset(asset);
		    	}
		    }
		});
		
		//POLL TABPANE
        this.PollTabPane = new PollTabPane(poll, asset);
		
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//CLOSE POLL FRME
                PollTabPane.close();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
		       
		 //ADD POLL TABPANE TO FRAME
        this.add(this.PollTabPane, pollTabPaneGBC);
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
