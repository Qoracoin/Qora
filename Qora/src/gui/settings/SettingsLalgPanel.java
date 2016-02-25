package gui.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.json.simple.JSONObject;

import lang.lang;
import settings.Settings;

public class SettingsLalgPanel extends JPanel
{


	public JTextField textlangFolder;
	public String LangFileName;
	public static JSONObject langObj;
	
	
	
	
	public SettingsLalgPanel() {
		// TODO Auto-generated constructor stub
	String LabelText;
	if (!lang.langObj.isEmpty()) {		LabelText="Lang:"+lang.Translate("lang_name");}
	else{
		LabelText =("Lang unknow!!!");
	}
	
	final JLabel lblAddNewAddress = new JLabel (LabelText);
	
	this.setBorder(new EmptyBorder(10, 5, 5, 10));
    
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.rowHeights = new int[] {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30};
    gridBagLayout.columnWidths = new int[] {40, 70, 92, 88, 92, 30, 0};
    gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    setLayout(gridBagLayout);
	
	
		
	     GridBagConstraints gbc_lblAddNewAddress = new GridBagConstraints();
	     gbc_lblAddNewAddress.anchor = GridBagConstraints.NORTHEAST;
	     gbc_lblAddNewAddress.insets = new Insets(4, 0, 5, 5);
	     gbc_lblAddNewAddress.gridx = 1;
	     gbc_lblAddNewAddress.gridy = 3;
	     add(lblAddNewAddress, gbc_lblAddNewAddress);
	
	
		   JLabel lblDataDir = new JLabel("Lang file:");
	        GridBagConstraints gbc_lblDataDir = new GridBagConstraints();
	        gbc_lblDataDir.anchor = GridBagConstraints.WEST;
	        gbc_lblDataDir.insets = new Insets(0, 0, 5, 5);
	        gbc_lblDataDir.gridx = 1;
	        gbc_lblDataDir.gridy = 2;
	        add(lblDataDir, gbc_lblDataDir);
	        
	        textlangFolder = new JTextField();
	        textlangFolder.setText(Settings.getInstance().getLangPath()+"\\"+ Settings.getInstance().getLangFileName());
	        textlangFolder.setHorizontalAlignment(SwingConstants.LEFT);
	        textlangFolder.setColumns(10);
	        GridBagConstraints gbc_textDataFolder = new GridBagConstraints();
	        gbc_textDataFolder.gridwidth = 2;
	        gbc_textDataFolder.insets = new Insets(0, 0, 5, 5);
	        gbc_textDataFolder.fill = GridBagConstraints.HORIZONTAL;
	        gbc_textDataFolder.gridx = 2;
	        gbc_textDataFolder.gridy = 2;
	        add(textlangFolder, gbc_textDataFolder);
	        
	        JButton btnBrowseDataFolder = new JButton("Browse...");
	        GridBagConstraints gbc_btnBrowseDataFolder = new GridBagConstraints();
	        gbc_btnBrowseDataFolder.anchor = GridBagConstraints.WEST;
	        gbc_btnBrowseDataFolder.insets = new Insets(0, 0, 5, 5);
	        gbc_btnBrowseDataFolder.gridx = 4;
	        gbc_btnBrowseDataFolder.gridy = 2;
	        btnBrowseDataFolder.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                JFileChooser fileopen = new JFileChooser();  
	                fileopen.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	                fileopen.setCurrentDirectory(new File(textlangFolder.getText()));
	           //     fileopen.setCurrentDirectory(new File(Settings.getInstance().LangFileName()));
	                
	                int ret = fileopen.showDialog(null, "Set data dir");                
	                if (ret == JFileChooser.APPROVE_OPTION) {
	                	textlangFolder.setText(Settings.getInstance().getLangPath()+"\\"+fileopen.getSelectedFile().getName().toString());
	                	LangFileName = fileopen.getSelectedFile().getName().toString();
	                	// открываем установки языка	
	            		
	            		try {
	            			lang.langObj = lang.OpenLangFile(Settings.getInstance().getLangPath()+"\\"+fileopen.getSelectedFile().getName().toString());
	            		} catch (IOException e1) {
	            			// TODO Auto-generated catch block
	            			e1.printStackTrace();
	            		} 
	            		
	            		lblAddNewAddress.setText("Lang:"+lang.Translate("lang_name"));
	            		
	                
	                }
	            }
	        });
	        add(btnBrowseDataFolder, gbc_btnBrowseDataFolder);
	
	
	}

}