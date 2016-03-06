package gui.create;

import gui.Gui;

import settings.Settings;
import lang.Lang;



import utils.SaveStrToFile;


import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JList;

import javax.swing.JComponent;
import javax.swing.JFrame;


import javax.swing.JOptionPane;

import javax.swing.border.EmptyBorder;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import controller.Controller;

@SuppressWarnings("serial")
public class SettingLangFrame extends JDialog {
	
	public JButton btnLoadNewLang;
	
	public  JList<String> listLang;
	public JSONObject inernetLangsJSON;
	public String userPath ="";
	private JSONObject settingsLangJSON;
	
	public SettingLangFrame(Gui parent) throws Exception
	{
		
		this.setTitle("Qora" + " - "+ "Language select");
		this.setModal(true);
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
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 1;	
		labelGBC.gridwidth = 2;
		labelGBC.gridx = 0;
		
		//OPTIONS GBC
		GridBagConstraints optionsGBC = new GridBagConstraints();
		//optionsGBC.insets = new Insets(5,5,5,5);
		optionsGBC.fill = GridBagConstraints.NONE;  
		optionsGBC.anchor = GridBagConstraints.NORTHWEST;
		optionsGBC.weightx = 1;	
		optionsGBC.gridwidth = 2;
		optionsGBC.gridx = 0;	
		optionsGBC.gridy = 2;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,0,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 1;
		buttonGBC.gridx = 0;		
		
		
	
 
    	
        //CALCULATE HEIGHT WIDTH
      	this.pack();
      	this.setSize(500, 500);
      	//this.getHeight();
      	
      	
 
      	// read internet 
      	final DefaultListModel<String> listModel = new DefaultListModel<String>();
      	
        String stringFromInternet = "";
		try {
			String url = Lang.translationsUrl + Controller.getInstance().getVersion().replace(" ", "%20") + "/available.json";

			URL u = new URL(url);
			InputStream in = u.openStream();
			stringFromInternet = IOUtils.toString(in, Charsets.UTF_8);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		inernetLangsJSON = (JSONObject) JSONValue.parse(stringFromInternet);
		
		// set element
		for (Object internetKey : inernetLangsJSON.keySet()) {
			JSONObject internetValue = (JSONObject) inernetLangsJSON.get(internetKey);
			listModel.addElement((String)(internetValue).get("download lang_name translation"));
		}
        
		buttonGBC.gridy = 3;
	 
		listLang = new JList(listModel);
		listLang.setSelectedIndex(0);
		listLang.setFocusable(false);
		add(listLang, buttonGBC);	

        
		//BUTTON NEXT
	      buttonGBC.gridy = 4;
	      JButton nextButton = new JButton(">>");
	      nextButton.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			        onNextClick();
			    }
			});	
	      nextButton.setPreferredSize(new Dimension(80, 25));
	  	this.add(nextButton, buttonGBC);
		      
	  	//CLOSE NICELY
	      this.addWindowListener(new WindowAdapter()
	      {
	          public void windowClosing(WindowEvent e)
	          {
	          	Controller.getInstance().stopAll();
	          	System.exit(0);
	          }
	      });
	      	
	      
	      	this.setResizable(false);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			
			
		}
 
      	
      	
      
  
	
	public void onNextClick()
	{
			
		// open settings.json file
		
	
				File file = new File(this.userPath + "settings.json");
				
				//CREATE FILE IF IT DOESNT EXIST
				if(!file.exists())
				{
					try {
						file.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//READ SETTINS JSON FILE
				List<String> lines;
				try {
					lines = Files.readLines(file, Charsets.UTF_8);
					
					String jsonString = "";
					for(String line : lines){
						jsonString += line;
					}
					
			settingsLangJSON = (JSONObject) JSONValue.parse(jsonString);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				for (Object internetKey : inernetLangsJSON.keySet()) {
		       JSONObject internetValue = (JSONObject) inernetLangsJSON.get(internetKey);
		       if ((String)(internetValue).get("download lang_name translation")==(String) listLang.getSelectedValue())
		       {
		    	   String langFileName = (String)(internetValue).get("_file_");
		    	 
		    	   
		    	   settingsLangJSON.put("lang", langFileName); 
		    	   try {
		   			SaveStrToFile.saveJsonFine(userPath + "settings.json", settingsLangJSON);
		   		//	Lang.openLangFile(langFileName);
		   			
		   			
		   			File file1 = new File("/languages/" + langFileName);	
		   			if(!file1.exists())
					{
		   				String url = Lang.translationsUrl + Controller.getInstance().getVersion().replace(" ", "%20") + "/languages/" + langFileName;
		   				FileUtils.copyURLToFile(new URL(url), new File(Settings.getInstance().getUserPath() + "languages/" + langFileName));
					
		   				
					
					}
		   			
		   	
		   			
		   		} catch (IOException e) {
		   			e.printStackTrace();
		   			JOptionPane.showMessageDialog(
		   					new JFrame(), "Error writing to the file: "
		   							+ "\nProbably there is no access.",
		   	                "Error!",
		   	                JOptionPane.ERROR_MESSAGE);
		   			
		    	   
		       }
		    	   
		}
					
		}
				
		
		this.dispose();
		
	}
	
	
	
	
	
	
	
	
	
}
