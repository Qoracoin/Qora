package gui.assets;

import gui.models.BalancesTableModel;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import qora.assets.Asset;

@SuppressWarnings("serial")
public class AssetFrame extends JFrame
{
	private Asset asset;
	
	public AssetFrame(Asset asset)
	{
		super("ERM - Asset Details");
		
		this.asset = asset;
		
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
		//this.setLayout(new GridBagLayout());
		
		//TAB PANE
		JTabbedPane tabPane = new JTabbedPane();
		
		//DETAILS
		tabPane.add("Details", new AssetDetailsPanel(this.asset));
		
		//BALANCES
		BalancesTableModel balancesTableModel = new BalancesTableModel(asset.getKey());
		final JTable balancesTable = new JTable(balancesTableModel);
		tabPane.add("Holders", new JScrollPane(balancesTable));
		
		//ADD TAB PANE
		this.add(tabPane);
		
        //PACK
		this.pack();
		//this.setSize(500, this.getHeight());
        //this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
