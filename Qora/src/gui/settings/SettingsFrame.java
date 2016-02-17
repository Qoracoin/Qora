package gui.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import controller.Controller;
import gui.Menu;
import network.Network;
import settings.Settings;
import utils.JSonWriter;

@SuppressWarnings("serial")
public class SettingsFrame extends JFrame{
	public JSONObject settingsJSONbuf;
	private SettingsTabPane settingsTabPane;
	
	public SettingsFrame() 
	{
		
		//CREATE FRAME
		super("Qora - Settings");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		setResizable(false);

		settingsJSONbuf = new JSONObject();
		settingsJSONbuf = Settings.getInstance().Dump(); 
		
		GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{339, 0};
        gridBagLayout.rowHeights = new int[] {300, 30};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0};
        getContentPane().setLayout(gridBagLayout);
        
        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 1;
        getContentPane().add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {80, 80, 80, 80, 80, 80};
        gbl_panel.rowHeights = new int[]{23, 0};
        gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        JButton btnNewButton = new JButton("Apply");
        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.gridwidth = 2;
        gbc_btnNewButton.fill = GridBagConstraints.BOTH;
        gbc_btnNewButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_btnNewButton.insets = new Insets(3, 0, 0, 3);
        gbc_btnNewButton.gridx = 1;
        gbc_btnNewButton.gridy = 0;
        
        btnNewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				
				int n = JOptionPane.showConfirmDialog(
						new JFrame(), "To apply the new settings?",
		                "Confirmation",
		                JOptionPane.OK_CANCEL_OPTION);
				if (n == JOptionPane.OK_OPTION) {
					if(saveSettings())
					{
						settingsTabPane.close();
						//DISPOSE
						setVisible(false);
						dispose();
					}
				}
				if (n == JOptionPane.CANCEL_OPTION) {
					
				}
			}
		});	  
        
        panel.add(btnNewButton, gbc_btnNewButton);
        
        JButton btnCancel = new JButton("Cancel");
        GridBagConstraints gbc_btnCancel = new GridBagConstraints();
        gbc_btnCancel.gridwidth = 2;
        gbc_btnCancel.fill = GridBagConstraints.BOTH;
        gbc_btnCancel.insets = new Insets(3, 0, 0, 0);
        gbc_btnCancel.gridx = 3;
        gbc_btnCancel.gridy = 0;
        btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				settingsTabPane.close();
                
                //DISPOSE
                setVisible(false);
                dispose();
			}
		});	  
        panel.add(btnCancel, gbc_btnCancel);
        //////////
		//SETTINGS TABPANE
        this.settingsTabPane = new SettingsTabPane();
		
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//CLOSE DEBUG
            	settingsTabPane.close();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
		       
		 //ADD GENERAL TABPANE TO FRAME
        this.add(this.settingsTabPane); 
  
        /////////
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        
        
        this.setVisible(true);
	}	
	public void onClose()
	{
		
	}
	
	@SuppressWarnings("unchecked")
	public boolean saveSettings()
	{
		boolean changeKeyCaching = false;
		boolean changeWallet = false;
		boolean changeDataDir = false;
		boolean limitConnections = false;

		if(Settings.getInstance().isGeneratorKeyCachingEnabled() != settingsTabPane.settingsParametersPanel.chckbxKeyCaching.isSelected())
		{
			settingsJSONbuf.put("generatorkeycaching", settingsTabPane.settingsParametersPanel.chckbxKeyCaching.isSelected());
			changeKeyCaching = true;
		}
		
		if(Settings.getInstance().isSoundNewTransactionEnabled() != settingsTabPane.settingsParametersPanel.chckbxSoundNewTransaction.isSelected())
		{
			settingsJSONbuf.put("soundnewtransaction", settingsTabPane.settingsParametersPanel.chckbxSoundNewTransaction.isSelected());
		}
		
		if(Settings.getInstance().isSoundReceiveMessageEnabled() != settingsTabPane.settingsParametersPanel.chckbxSoundReceiveMessage.isSelected())
		{
			settingsJSONbuf.put("soundreceivemessage", settingsTabPane.settingsParametersPanel.chckbxSoundReceiveMessage.isSelected());
		}

		if(Settings.getInstance().isSoundReceivePaymentEnabled() != settingsTabPane.settingsParametersPanel.chckbxSoundReceivePayment.isSelected())
		{
			settingsJSONbuf.put("soundreceivepayment", settingsTabPane.settingsParametersPanel.chckbxSoundReceivePayment.isSelected());
		}

		if(Settings.getInstance().isGuiEnabled() != settingsTabPane.settingsParametersPanel.chckbxGuiEnabled.isSelected())
		{
			settingsJSONbuf.put("guienabled", settingsTabPane.settingsParametersPanel.chckbxGuiEnabled.isSelected());
		}
		
		if(Settings.getInstance().isRpcEnabled() != settingsTabPane.settingsParametersPanel.chckbxRpcEnabled.isSelected())
		{
			settingsJSONbuf.put("rpcenabled", settingsTabPane.settingsParametersPanel.chckbxRpcEnabled.isSelected());	
			settingsTabPane.settingsAllowedPanel.rpcServiceRestart = true;
		}
		
		if(!settingsTabPane.settingsParametersPanel.chckbxGuiEnabled.isSelected() && !settingsTabPane.settingsParametersPanel.chckbxRpcEnabled.isSelected())
		{
			JOptionPane.showMessageDialog(
					new JFrame(), "Both gui and rpc cannot be disabled!",
	                "Error!",
	                JOptionPane.ERROR_MESSAGE);
			return false;	
		}
			
		if(Settings.getInstance().isWebEnabled() != settingsTabPane.settingsParametersPanel.chckbxWebEnabled.isSelected())
		{
			settingsJSONbuf.put("webenabled", settingsTabPane.settingsParametersPanel.chckbxWebEnabled.isSelected());
			settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
		}
		
		int newRpcPort = Integer.parseInt(settingsTabPane.settingsParametersPanel.txtRpcPort.getText());
		if(Settings.getInstance().getRpcPort() != newRpcPort)
		{
			if(Network.isPortAvailable(newRpcPort))
			{
				settingsJSONbuf.put("rpcport", newRpcPort);
				settingsTabPane.settingsAllowedPanel.rpcServiceRestart = true;
			}
			else
			{
				JOptionPane.showMessageDialog(
						new JFrame(), "Rpc port " + newRpcPort + " already in use!",
		                "Error!",
		                JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		int newWebPort = Integer.parseInt(settingsTabPane.settingsParametersPanel.txtWebport.getText());
		
		if(Settings.getInstance().getWebPort() != newWebPort)
		{
			if(Network.isPortAvailable(newWebPort))
			{
				settingsJSONbuf.put("webport", newWebPort);
				settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
			}
			else
			{
				JOptionPane.showMessageDialog(
						new JFrame(), "Web port " + newWebPort + " already in use!",
		                "Error!",
		                JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		int MinConnections = Integer.parseInt(settingsTabPane.settingsParametersPanel.textMinConnections.getText());
		if(Settings.getInstance().getMinConnections() != MinConnections)
		{
			settingsJSONbuf.put("minconnections", MinConnections);
			limitConnections = true;
		}
		
		int MaxConnections = Integer.parseInt(settingsTabPane.settingsParametersPanel.textMaxConnections.getText());
		if(Settings.getInstance().getMaxConnections() != MaxConnections)
		{
			settingsJSONbuf.put("maxconnections", MaxConnections);
			limitConnections = true;
		}
		
		if(!Settings.getInstance().getWalletDir().equals(settingsTabPane.settingsParametersPanel.textWallet.getText()))
		{
			settingsJSONbuf.put("walletdir", settingsTabPane.settingsParametersPanel.textWallet.getText());
			changeWallet = true;
		}
		
		if(!Settings.getInstance().getDataDir().equals(settingsTabPane.settingsParametersPanel.textDataFolder.getText()))
		{
			settingsJSONbuf.put("datadir", settingsTabPane.settingsParametersPanel.textDataFolder.getText());
			changeDataDir = true;
		}
		
		List<String> peersToSave = settingsTabPane.settingsKnownPeersPanel.knownPeersTableModel.getPeers();
		
		JSONArray peersJson = Settings.getInstance().getPeersJson();
		JSONArray newPeersJson = new JSONArray();
		
		for (Object peer : peersJson) {
			if(peersToSave.contains((String) peer)){
				newPeersJson.add(peer);
			}
		}
		
		if(newPeersJson.size() != peersJson.size())
		{
			try {
		        Writer writer = new JSonWriter();
		        
		        JSONObject jsonObject = new JSONObject();
		        jsonObject.put("knownpeers", newPeersJson);
		        
		        jsonObject.writeJSONString(writer);
					
				FileWriter file = new FileWriter(Settings.getInstance().getCurrentPeersPath());
				file.write(writer.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(
						new JFrame(), "Error writing to the file: " + Settings.getInstance().getCurrentPeersPath()
								+ "\nProbably there is no access.",
		                "Error!",
		                JOptionPane.ERROR_MESSAGE);
			}	
		}
			
		JSONArray peersToSaveApproved = new JSONArray();
		
		if(peersJson != null)
		{
			for (String peer : peersToSave) {
				if(!peersJson.contains(peer)) {
					peersToSaveApproved.add(peer);
				}
			}
		}
		
		settingsJSONbuf.put("knownpeers", peersToSaveApproved);
		
		if(settingsTabPane.settingsAllowedPanel.chckbxWebAllowForAll.isSelected())
		{
			settingsJSONbuf.put("weballowed", new ArrayList<String>());
		}
		else
		{
			settingsJSONbuf.put("weballowed",settingsTabPane.settingsAllowedPanel.webAllowedTableModel.getPeers());			
		}
		
		if(settingsTabPane.settingsAllowedPanel.chckbxRpcAllowForAll.isSelected())
		{
			settingsJSONbuf.put("rpcallowed", new ArrayList<String>());
		}
		else
		{
			settingsJSONbuf.put("rpcallowed",settingsTabPane.settingsAllowedPanel.rpcAllowedTableModel.getPeers());
		}
		
		try {
	        Writer writer = new JSonWriter();
	        settingsJSONbuf.writeJSONString(writer);
				
			FileWriter file = new FileWriter(Settings.getInstance().getCurrentSettingsPath());
			file.write(writer.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					new JFrame(), "Error writing to the file: " + Settings.getInstance().getCurrentSettingsPath()
							+ "\nProbably there is no access.",
	                "Error!",
	                JOptionPane.ERROR_MESSAGE);
		}

		Settings.FreeInstance();
		
		if(settingsTabPane.settingsAllowedPanel.rpcServiceRestart)
		{
			Controller.getInstance().rpcServiceRestart();
		}
		
		if(settingsTabPane.settingsAllowedPanel.webServiceRestart)
		{
			Controller.getInstance().webServiceRestart();
			
			Menu.webServerItem.setVisible(Settings.getInstance().isWebEnabled());
			Menu.blockExplorerItem.setVisible(Settings.getInstance().isWebEnabled());
		}
		
		if(changeDataDir || changeWallet)
		{
			JOptionPane.showMessageDialog(
				new JFrame(), "You changed WalletDir or DataDir. You need to restart the wallet for the changes to take effect.",
                "Attention!",
                JOptionPane.WARNING_MESSAGE);
		}
		if(changeKeyCaching)
		{
			JOptionPane.showMessageDialog(
				new JFrame(), "You changed Generator Key Caching option.You need to restart the wallet for the changes to take effect.",
                "Attention!",
                JOptionPane.WARNING_MESSAGE);
		}
		if(limitConnections)
		{
			JOptionPane.showMessageDialog(
				new JFrame(), "You changed max connections or min connections. You need to restart the wallet for the changes to take effect.",
                "Attention!",
                JOptionPane.WARNING_MESSAGE);
		}
		return true;
	}
}
