package settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import network.Peer;

public class Settings {

	//NETWORK
	private static final int DEFAULT_MIN_CONNECTIONS = 5;
	private static final int DEFAULT_MAX_CONNECTIONS = 20;
	private static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
	private static final int DEFAULT_PING_INTERVAL = 30000;
	
	//RPC
	private static final int DEFAULT_RPC_PORT = 9085;
	private static final String DEFAULT_RPC_ALLOWED = "127.0.0.1";
	
	//WEB
	private static final int DEFAULT_WEB_PORT = 9083;
	private static final String DEFAULT_WEB_ALLOWED = "127.0.0.1";
	
	//DATA
	private static final String DEFAULT_DATA_DIR = "data";
	private static final String DEFAULT_WALLET_DIR = "wallet";
	
	private static final boolean DEFAULT_GENERATOR_KEY_CACHING = false;
	
	private static final int DEFAULT_MAX_BYTE_PER_FEE = 512;
	
	private static Settings instance;
	
	private JSONObject settingsJSON;
	
	public static Settings getInstance()
	{
		if(instance == null)
		{
			instance = new Settings();
		}
		
		return instance;
	}
	
	private Settings()
	{
		BufferedReader reader;
		
		try
		{
			//OPEN FILE
			File file = new File("settings.json");
			
			//CREATE FILE IF IT DOESNT EXIST
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			//READ SETTINGS FILE
			reader = new BufferedReader(new FileReader(file));
			
			String line;
			String jsonString = "";
			
			//READ LINE
			while ((line = reader.readLine()) != null)
			{
				jsonString += line;
		    }
			
			//CREATE JSON OBJECT
			this.settingsJSON = (JSONObject) JSONValue.parse(jsonString);
			
			//CLOSE
			reader.close();
		}
		catch(Exception e)
		{
			//STOP
			System.out.println("ERROR reading settings.ini. closing");
			System.exit(0);
		}
	}
	
	public List<Peer> getKnownPeers()
	{
		try
		{
			//GET PEERS FROM JSON
			JSONArray peersArray = (JSONArray) this.settingsJSON.get("knownpeers");
			
			//CREATE LIST WITH PEERS
			List<Peer> peers = new ArrayList<Peer>();
			
			for(int i=0; i<peersArray.size(); i++)
			{
				InetAddress address = InetAddress.getByName((String) peersArray.get(i));
				
				//CHECK IF SOCKET IS NOT LOCALHOST
				if(!address.equals(InetAddress.getLocalHost()))
				{
					//CREATE PEER
					Peer peer = new Peer(address);
								
					//ADD TO LIST
					peers.add(peer);
				}
			}
			
			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			//RETURN EMPTY LIST
			return new ArrayList<Peer>();
		}
	}
	
	public int getMaxConnections()
	{
		if(this.settingsJSON.containsKey("maxconnections"))
		{
			return ((Long) this.settingsJSON.get("maxconnections")).intValue();
		}
		
		return DEFAULT_MAX_CONNECTIONS;
	}
	
	public int getMinConnections()
	{
		if(this.settingsJSON.containsKey("minconnections"))
		{
			return ((Long) this.settingsJSON.get("minconnections")).intValue();
		}
		
		return DEFAULT_MIN_CONNECTIONS;
	}
	
	public int getConnectionTimeout()
	{
		if(this.settingsJSON.containsKey("connectiontimeout"))
		{
			return ((Long) this.settingsJSON.get("connectiontimeout")).intValue();
		}
		
		return DEFAULT_CONNECTION_TIMEOUT;
	}
	
	public int getRpcPort()
	{
		if(this.settingsJSON.containsKey("rpcport"))
		{
			return ((Long) this.settingsJSON.get("rpcport")).intValue();
		}
		
		return DEFAULT_RPC_PORT;
	}

	public String[] getRpcAllowed()
	{
		try
		{
			if(this.settingsJSON.containsKey("rpcallowed"))
			{
				//GET PEERS FROM JSON
				JSONArray allowedArray = (JSONArray) this.settingsJSON.get("rpcallowed");
				
				//CREATE LIST WITH PEERS
				String[] allowed = new String[allowedArray.size()];
				for(int i=0; i<allowedArray.size(); i++)
				{
					allowed[i] = (String) allowedArray.get(i);
				}
				
				//RETURN
				return allowed;	
			}
			
			//RETURN
			return DEFAULT_RPC_ALLOWED.split(";");
		}
		catch(Exception e)
		{
			//RETURN EMPTY LIST
			return new String[0];
		}
	}
	
	public int getWebPort()
	{
		if(this.settingsJSON.containsKey("webport"))
		{
			return ((Long) this.settingsJSON.get("webport")).intValue();
		}
		
		return DEFAULT_WEB_PORT;
	}
	
	public String[] getWebAllowed()
	{
		try
		{
			if(this.settingsJSON.containsKey("weballowed"))
			{
				//GET PEERS FROM JSON
				JSONArray allowedArray = (JSONArray) this.settingsJSON.get("weballowed");
				
				//CREATE LIST WITH PEERS
				String[] allowed = new String[allowedArray.size()];
				for(int i=0; i<allowedArray.size(); i++)
				{
					allowed[i] = (String) allowedArray.get(i);
				}
				
				//RETURN
				return allowed;	
			}
			
			//RETURN
			return DEFAULT_WEB_ALLOWED.split(";");
		}
		catch(Exception e)
		{
			//RETURN EMPTY LIST
			return new String[0];
		}
	}
	
	public String getWalletDir()
	{
		if(this.settingsJSON.containsKey("walletdir"))
		{
			return (String) this.settingsJSON.get("walletdir");
		}
		
		return DEFAULT_WALLET_DIR;
	}
	
	public String getDataDir()
	{
		if(this.settingsJSON.containsKey("datadir"))
		{
			return (String) this.settingsJSON.get("datadir");
		}
		
		return DEFAULT_DATA_DIR;
	}
	
	public int getPingInterval()
	{
		if(this.settingsJSON.containsKey("pinginterval"))
		{
			return ((Long) this.settingsJSON.get("pinginterval")).intValue();
		}
		
		return DEFAULT_PING_INTERVAL;
	}

	public boolean isGeneratorKeyCachingEnabled() 
	{
		if(this.settingsJSON.containsKey("generatorkeycaching"))
		{
			return ((Boolean) this.settingsJSON.get("generatorkeycaching")).booleanValue();
		}
		
		return DEFAULT_GENERATOR_KEY_CACHING;
	}
	
	public int getMaxBytePerFee() 
	{
		if(this.settingsJSON.containsKey("maxbyteperfee"))
		{
			return ((Long) this.settingsJSON.get("maxbyteperfee")).intValue();
		}
		
		return DEFAULT_MAX_BYTE_PER_FEE;
	}
}
