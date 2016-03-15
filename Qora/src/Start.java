import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import api.ApiClient;
import controller.Controller;
import gui.Gui;
import gui.create.SettingLangFrame;
import lang.Lang;
import settings.Settings;
import utils.SysTray;

public class Start {
	private static JSONObject settingsLangJSON1;

	public static void main(String args[])
	{	
		boolean cli = false;
		
		for(String arg: args)
		{
			if(arg.equals("-cli"))
			{
				cli = true;
			} if(arg.equals("-testnet")) {
				Settings.getInstance().setGenesisStamp(System.currentTimeMillis());
			} else if(arg.startsWith("-testnet=") && arg.length() > 9) {
				try
				{
					Settings.getInstance().setGenesisStamp(Long.parseLong(arg.substring(9)));
				} catch(Exception e) {
					Settings.getInstance().setGenesisStamp(Settings.DEFAULT_MAINNET_STAMP);
				}
			}
		}
		
		if(!cli)
		{			
			try
			{
				
				// setting lang
				
				File file = new File("settings.json");
				
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
					
			settingsLangJSON1 = (JSONObject) JSONValue.parse(jsonString);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if (!settingsLangJSON1.containsKey("lang")) new SettingLangFrame(null);
				else if (! new File("languages/" + settingsLangJSON1.get("lang")).exists()) new SettingLangFrame(null);
		        
		        
				
				//ONE MUST BE ENABLED
				if(!Settings.getInstance().isGuiEnabled() && !Settings.getInstance().isRpcEnabled())
				{
					throw new Exception(Lang.getInstance().translate("Both gui and rpc cannot be disabled!"));
				}
				
				System.out.println(Lang.getInstance().translate("Starting %qora% / version: %version% / build date: %builddate% / ...")
						.replace("%version%", Controller.getInstance().getVersion())
						.replace("%builddate%", Controller.getInstance().getBuildDateString())
						.replace("%qora%", Lang.getInstance().translate("Qora"))
						);
				
				//STARTING NETWORK/BLOCKCHAIN/RPC
				Controller.getInstance().start();
				
				try
				{
					
			        	
			       
					
					//START GUI
						if(Gui.getInstance() != null && Settings.getInstance().isSysTrayEnabled())
						{					
							SysTray.getInstance().createTrayIcon();
						}
				} catch(Exception e) {
					System.out.println(Lang.getInstance().translate("GUI ERROR") + ": " + e.getMessage());
				}
				
			} catch(Exception e) {
				
				e.printStackTrace();
				
				//USE SYSTEM STYLE
		        try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				
				//ERROR STARTING
				System.out.println(Lang.getInstance().translate("STARTUP ERROR") + ": " + e.getMessage());
				
				if(Gui.isGuiStarted())
				{
					JOptionPane.showMessageDialog(null, e.getMessage(), Lang.getInstance().translate("Startup Error"), JOptionPane.ERROR_MESSAGE);
				}
				
				//FORCE SHUTDOWN
				System.exit(0);
			}
		}
		else
		{
			Scanner scanner = new Scanner(System.in);
			ApiClient client = new ApiClient();
			
			while(true)
			{
				
				System.out.print("[COMMAND] ");
				String command = scanner.nextLine();
				
				if(command.equals("quit"))
				{
					scanner.close();
					System.exit(0);
				}
				
				String result = client.executeCommand(command);
				System.out.println("[RESULT] " + result);
			}
		}		
	}
}
