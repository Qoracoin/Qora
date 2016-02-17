package gui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import gui.status.StatusPanel;
import settings.Settings;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{

	public MainFrame()
	{
		//CREATE FRAME
		super("Qora");
		
		if(Settings.getInstance().isTestnet()) {
			setTitle("Qora TestNet " + Settings.getInstance().getGenesisStamp());
		}
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
        //MENU
        Menu menu = new Menu();

        //ADD MENU TO FRAME
        this.setJMenuBar(menu);
        
        //GENERAL TABPANE
        GeneralTabPane generalTabPane = new GeneralTabPane();
        
        //ADD GENERAL TABPANE TO FRAME
        this.add(generalTabPane);
        
        //STATS
        this.add(new StatusPanel(), BorderLayout.SOUTH);
        
        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	new ClosingDialog();
            }
        });
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
	}
}
