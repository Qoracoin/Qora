package gui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import controller.Controller;
import gui.settings.SettingsFrame;
import settings.Settings;
import utils.URLViewer;

public class Menu extends JMenuBar 
{
	private static final long serialVersionUID = 5237335232850181080L;
	public static JMenuItem webServerItem;
	public static JMenuItem blockExplorerItem;
	public static JMenuItem lockItem;
	private ImageIcon lockedIcon;
	private ImageIcon unlockedIcon;

	public Menu()
	{
		super();
		
		//FILE MENU
        JMenu fileMenu = new JMenu("File");
        fileMenu.getAccessibleContext().setAccessibleDescription("File menu");
        this.add(fileMenu);

        //LOCK

        //LOAD IMAGES
		try {
			BufferedImage lockedImage = ImageIO.read(new File("images/wallet/locked.png"));
			this.lockedIcon = new ImageIcon(lockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
	
			BufferedImage unlockedImage = ImageIO.read(new File("images/wallet/unlocked.png"));
			this.unlockedIcon = new ImageIcon(unlockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
        lockItem = new JMenuItem("lock");
        lockItem.getAccessibleContext().setAccessibleDescription("Lock/Unlock Wallet");
        lockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        
        lockItem.addActionListener(new ActionListener()
        {
        	
        	public void actionPerformed(ActionEvent e)
        	{
				PasswordPane.switchLockDialog();
        	}
        });
        fileMenu.add(lockItem);
        
        //SEPARATOR
        fileMenu.addSeparator();
        
        //CONSOLE
        JMenuItem consoleItem = new JMenuItem("Debug");
        consoleItem.getAccessibleContext().setAccessibleDescription("Debug information");
        consoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        consoleItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new DebugFrame();
        	}
        });
        fileMenu.add(consoleItem);
        
        //SETTINGS
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.getAccessibleContext().setAccessibleDescription("Settings of program");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        settingsItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new SettingsFrame();
        	}
        });
        fileMenu.add(settingsItem);        

        //WEB SERVER
        webServerItem = new JMenuItem("Decentralized Web server");
        webServerItem.getAccessibleContext().setAccessibleDescription("http://127.0.0.1:"+Settings.getInstance().getWebPort());
        webServerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK));
        webServerItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		try {
        			URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort()));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
        	}
        });
        fileMenu.add(webServerItem);   
        
        webServerItem.setVisible(Settings.getInstance().isWebEnabled());
        
        //WEB SERVER
        blockExplorerItem = new JMenuItem("Built-in BlockExplorer");
        blockExplorerItem.getAccessibleContext().setAccessibleDescription("http://127.0.0.1:"+Settings.getInstance().getWebPort()+"/index/blockexplorer.html");
        blockExplorerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
        blockExplorerItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		try {
        			URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort()+"/index/blockexplorer.html"));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
        	}
        });
        fileMenu.add(blockExplorerItem);   
        
        blockExplorerItem.setVisible(Settings.getInstance().isWebEnabled());
        
        //ABOUT
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.getAccessibleContext().setAccessibleDescription("Information about the application");
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        aboutItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new AboutFrame();
        	}
        });
        fileMenu.add(aboutItem);
        
        //SEPARATOR
        fileMenu.addSeparator();
        
        //QUIT
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        quitItem.getAccessibleContext().setAccessibleDescription("Quit the application");
        quitItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		new ClosingDialog();
        	}
        });
       
        fileMenu.add(quitItem);    
        
        fileMenu.addMenuListener(new MenuListener()
        {
			@Override
			public void menuSelected(MenuEvent arg0) {
        		if(Controller.getInstance().isWalletUnlocked()) {
        			lockItem.setText("Lock Wallet");
        			lockItem.setIcon(lockedIcon);
        		} else {
        			lockItem.setText("Unlock Wallet");
        			lockItem.setIcon(unlockedIcon);
        		}
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				
			}
        });
        
        /*//HELP MENU
        JMenu helpMenu = new JMenu("Help");
        helpMenu.getAccessibleContext().setAccessibleDescription("Help menu");
        this.add(helpMenu);
        
        //ABOUT
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.getAccessibleContext().setAccessibleDescription("Information about the application");
        helpMenu.add(aboutItem);  */ 
	}
}
