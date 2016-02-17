package settings;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import controller.Controller;
import network.Peer;
import ntp.NTP;

public class Settings {

	//NETWORK
	private static final int DEFAULT_MIN_CONNECTIONS = 10;
	private static final int DEFAULT_MAX_CONNECTIONS = 50;
	private static final int DEFAULT_MAX_RECEIVE_PEERS = 20;
	private static final int DEFAULT_MAX_SENT_PEERS = 20;
	private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
	private static final int DEFAULT_PING_INTERVAL = 30000;
	private static final boolean DEFAULT_TRYING_CONNECT_TO_BAD_PEERS = true;
	private static final String[] DEFAULT_PEERS = { };

	//TESTNET 
	public static final long DEFAULT_MAINNET_STAMP = 1400247274336L; // QORA RELEASE
	private long genesisStamp = -1;
	
	//RPC
	private static final int DEFAULT_RPC_PORT = 9085;
	private static final String DEFAULT_RPC_ALLOWED = "127.0.0.1";
	private static final boolean DEFAULT_RPC_ENABLED = true;
	
	//GUI CONSOLE
	private static final boolean DEFAULT_GUI_CONSOLE_ENABLED = true;
	
	//WEB
	private static final int DEFAULT_WEB_PORT = 9090;
	private static final String DEFAULT_WEB_ALLOWED = "127.0.0.1";
	private static final boolean DEFAULT_WEB_ENABLED = true;
	
	//GUI
	private static final boolean DEFAULT_GUI_ENABLED = true;
	
	//SETTINGS.JSON FILE
	private static final String DEFAULT_SETTINGS_PATH = "settings.json";
	
	//DATA
	private static final String DEFAULT_DATA_DIR = "data";
	private static final String DEFAULT_WALLET_DIR = "wallet";
	
	private static final boolean DEFAULT_GENERATOR_KEY_CACHING = false;
	private static final boolean DEFAULT_CHECKPOINTING = true;

	private static final boolean DEFAULT_SOUND_RECEIVE_COIN = true;
	private static final boolean DEFAULT_SOUND_MESSAGE = true;
	private static final boolean DEFAULT_SOUND_NEW_TRANSACTION = true;
	
	private static final int DEFAULT_MAX_BYTE_PER_FEE = 512;
	private static final boolean ALLOW_FEE_LESS_REQUIRED = false;
	
	private static final BigDecimal DEFAULT_BIG_FEE = new BigDecimal(1000);
	private static final String DEFAULT_BIG_FEE_MESSAGE = "Do you really want to set such a large fee?\nThese coins will go to the forgers.";
	
	//DATE FORMAT
	private static final String DEFAULT_TIME_ZONE = "";
	private static final String DEFAULT_TIME_FORMAT = "";
	
	private static final boolean DEFAULT_NS_UPDATE = false;
	
	private static Settings instance;
	
	private JSONObject settingsJSON;
	private JSONObject peersJSON;

	private String currentSettingsPath;
	private String currentPeersPath;
	
	private InetAddress localAddress;
	
	List<Peer> cacheInternetPeers;
	long timeLoadInternetPeers;
	
	public static Settings getInstance()
	{
		if(instance == null)
		{
			instance = new Settings();
		}
		
		return instance;
	}
	
	public static void FreeInstance()
	{
		if(instance != null)
		{
			instance = null;
		}
	}
	
	private Settings()
	{
		this.localAddress = this.getCurrentIp();
		int alreadyPassed = 0;
		String settingsFilePath = "settings.json";
		
		try
		{
			while(alreadyPassed<2)
			{
				//OPEN FILE
				File file = new File(settingsFilePath);
				currentSettingsPath = settingsFilePath;
				
				//CREATE FILE IF IT DOESNT EXIST
				if(!file.exists())
				{
					file.createNewFile();
				}
				
				//READ SETTINS JSON FILE
				List<String> lines = Files.readLines(file, Charsets.UTF_8);
				
				String jsonString = "";
				for(String line : lines){
					jsonString += line;
				}
				
				//CREATE JSON OBJECT
				this.settingsJSON = (JSONObject) JSONValue.parse(jsonString);
				
				alreadyPassed++;
				
				if(this.settingsJSON.containsKey("settingspath"))
				{
					settingsFilePath = (String) this.settingsJSON.get("settingspath");
				}
				else
				{
					alreadyPassed ++;
				}	
			}
		}
		catch(Exception e)
		{
			//STOP
			System.out.println("ERROR reading settings.json. closing");
			System.exit(0);
		}
		
		//TRY READ PEERS.JSON
		try
		{
			//OPEN FILE
			File file = new File(this.getCurrentPeersPath());
			
			//CREATE FILE IF IT DOESNT EXIST
			if(file.exists())
			{
				//READ PEERS FILE
				List<String> lines = Files.readLines(file, Charsets.UTF_8);
				
				String jsonString = "";
				for(String line : lines){
					jsonString += line;
				}
				
				//CREATE JSON OBJECT
				this.peersJSON = (JSONObject) JSONValue.parse(jsonString);
			} else {
				this.peersJSON = new JSONObject();
			}
			
		}
		catch(Exception e)
		{
			//STOP
			System.out.println("ERROR reading peers.json.");
			System.exit(0);
		}
	}
	
	public JSONObject Dump()
	{
		return settingsJSON;
	}
	
	public String getCurrentSettingsPath()
	{
		return currentSettingsPath;
	}
	
	public String getCurrentPeersPath()
	{
		if(this.currentPeersPath == null) {
			if(this.currentSettingsPath == "settings.json" || this.currentSettingsPath == "") {
				this.currentPeersPath = "peers.json";
			} else {
				File file = new File(this.currentSettingsPath);
			    if(file.exists()){
			    	this.currentPeersPath = file.getAbsoluteFile().getParent() + "/peers.json";
			    } else {
			    	this.currentPeersPath = "peers.json";
			    }
			}
		}
		return this.currentPeersPath;
	}
	
	public JSONArray getPeersJson()
	{
		if(this.peersJSON != null && this.peersJSON.containsKey("knownpeers")) {
			return (JSONArray) this.peersJSON.get("knownpeers");
		} else {
			return new JSONArray();
		}
		
	}
		
	@SuppressWarnings("unchecked")
	public List<Peer> getKnownPeers()
	{
		try {
			boolean loadPeersFromInternet =	(
				Controller.getInstance().getToOfflineTime() != 0L 
				&& 
				NTP.getTime() - Controller.getInstance().getToOfflineTime() > 5*60*1000
				);
			
			List<Peer> knownPeers = new ArrayList<Peer>();
			JSONArray peersArray = new JSONArray();
	
			try {
				JSONArray peersArraySettings = (JSONArray) this.settingsJSON.get("knownpeers");
	
				if(peersArraySettings != null)
				{
					for (Object peer : peersArraySettings) {
						if(!peersArray.contains(peer)) {
							peersArray.add(peer);
						}
					}
				}
			} catch (Exception e) {
				Logger.getGlobal().info("Error with loading knownpeers from settings.json.");
			}
			
			try {
				JSONArray peersArrayPeers = (JSONArray) this.peersJSON.get("knownpeers");
				
				if(peersArrayPeers != null)
				{
					for (Object peer : peersArrayPeers) {
						if(!peersArray.contains(peer)) {
							peersArray.add(peer);
						}
					}
				}
				
			} catch (Exception e) {
				Logger.getGlobal().info("Error with loading knownpeers from peers.json.");
			}
			
			knownPeers = getKnownPeersFromJSONArray(peersArray);
			
			if(knownPeers.size() == 0 || loadPeersFromInternet)
			{
				knownPeers = getKnownPeersFromInternet();
			}
				
			return knownPeers;
		
		} catch (Exception e) {
			Logger.getGlobal().info("Error in getKnownPeers().");
			return new ArrayList<Peer>();
		}
	}
	
	public List<Peer> getKnownPeersFromInternet() 
	{
		try {
			
			if(this.cacheInternetPeers == null) {
				
				this.cacheInternetPeers = new ArrayList<Peer>();
			}
				
			if(this.cacheInternetPeers.size() == 0 || NTP.getTime() - this.timeLoadInternetPeers > 24*60*60*1000 )
			{
				this.timeLoadInternetPeers = NTP.getTime();
				URL u = new URL("https://raw.githubusercontent.com/Qoracoin/Qora/master/Qora/peers.json");
				InputStream in = u.openStream();
				String stringInternetSettings = IOUtils.toString( in );
				JSONObject internetSettingsJSON = (JSONObject) JSONValue.parse(stringInternetSettings);
				JSONArray peersArray = (JSONArray) internetSettingsJSON.get("knownpeers");
				if(peersArray != null) {
					this.cacheInternetPeers = getKnownPeersFromJSONArray(peersArray);
				}
			}
			
			Logger.getGlobal().info("Peers loaded from Internet : " + this.cacheInternetPeers.size());

			return this.cacheInternetPeers;
			
		} catch (Exception e) {
			//RETURN EMPTY LIST

			Logger.getGlobal().info("Peers loaded from Internet with errors : " + this.cacheInternetPeers.size());
						
			return this.cacheInternetPeers;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Peer> getKnownPeersFromJSONArray(JSONArray peersArray)
	{
		try
		{
			//GET PEERS FROM JSON
			
			if(peersArray.isEmpty())
				peersArray.addAll(Arrays.asList(DEFAULT_PEERS));
				
			//CREATE LIST WITH PEERS
			List<Peer> peers = new ArrayList<Peer>();
			
			for(int i=0; i<peersArray.size(); i++)
			{
				try
				{
					InetAddress address = InetAddress.getByName((String) peersArray.get(i));
					
					if(!this.isLocalAddress(address))
					{
						//CREATE PEER
						Peer peer = new Peer(address);
									
						//ADD TO LIST
						peers.add(peer);
					}
				}catch(Exception e)
				{
					Logger.getGlobal().info((String) peersArray.get(i) + " - invalid peer address!");
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
	
	public void setGenesisStamp(long testNetStamp) {
		this.genesisStamp = testNetStamp;
	}
	
	public boolean isTestnet () {
		return this.getGenesisStamp() != DEFAULT_MAINNET_STAMP;
	}
	
	public long getGenesisStamp() {
		if(this.genesisStamp == -1) {
			if(this.settingsJSON.containsKey("testnetstamp"))
			{
				if(this.settingsJSON.get("testnetstamp").toString().equals("now") ||
						((Long) this.settingsJSON.get("testnetstamp")).longValue() == 0) {
					this.genesisStamp = System.currentTimeMillis();				
				} else {
					this.genesisStamp = ((Long) this.settingsJSON.get("testnetstamp")).longValue();
				}
			} else {
				this.genesisStamp = DEFAULT_MAINNET_STAMP;
			}
		}
		
		return this.genesisStamp;
	}
	
	public int getMaxConnections()
	{
		if(this.settingsJSON.containsKey("maxconnections"))
		{
			return ((Long) this.settingsJSON.get("maxconnections")).intValue();
		}
		
		return DEFAULT_MAX_CONNECTIONS;
	}
	
	public int getMaxReceivePeers()
	{
		if(this.settingsJSON.containsKey("maxreceivepeers"))
		{
			return ((Long) this.settingsJSON.get("maxreceivepeers")).intValue();
		}
		
		return DEFAULT_MAX_RECEIVE_PEERS;
	}
	
	public int getMaxSentPeers()
	{
		if(this.settingsJSON.containsKey("maxsentpeers"))
		{
			return ((Long) this.settingsJSON.get("maxsentpeers")).intValue();
		}
		
		return DEFAULT_MAX_SENT_PEERS;
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
	
	public boolean isTryingConnectToBadPeers()
	{
		if(this.settingsJSON.containsKey("tryingconnecttobadpeers"))
		{
			return ((Boolean) this.settingsJSON.get("tryingconnecttobadpeers")).booleanValue();
		}
		
		return DEFAULT_TRYING_CONNECT_TO_BAD_PEERS;
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

	public boolean isRpcEnabled() 
	{
		if(this.settingsJSON.containsKey("rpcenabled"))
		{
			return ((Boolean) this.settingsJSON.get("rpcenabled")).booleanValue();
		}
		
		return DEFAULT_RPC_ENABLED;
	}
	
	public int getWebPort()
	{
		if(this.settingsJSON.containsKey("webport"))
		{
			return ((Long) this.settingsJSON.get("webport")).intValue();
		}
		
		return DEFAULT_WEB_PORT;
	}
	
	public boolean isGuiConsoleEnabled() 
	{
		if(this.settingsJSON.containsKey("guiconsoleenabled"))
		{
			return ((Boolean) this.settingsJSON.get("guiconsoleenabled")).booleanValue();
		}
		
		return DEFAULT_GUI_CONSOLE_ENABLED;
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

	public boolean isWebEnabled() 
	{
		if(this.settingsJSON.containsKey("webenabled"))
		{
			return ((Boolean) this.settingsJSON.get("webenabled")).booleanValue();
		}
		
		return DEFAULT_WEB_ENABLED;
	}
	
	public boolean updateNameStorage() 
	{
		if(this.settingsJSON.containsKey("nsupdate"))
		{
			return ((Boolean) this.settingsJSON.get("nsupdate")).booleanValue();
		}
		
		return DEFAULT_NS_UPDATE;
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
	
	public String getSettingsPath()
	{
		if(this.settingsJSON.containsKey("settingspath"))
		{
			return (String) this.settingsJSON.get("settingspath");
		}
		
		return DEFAULT_SETTINGS_PATH;
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
	
	public boolean isCheckpointingEnabled() 
	{
		if(this.settingsJSON.containsKey("checkpoint"))
		{
			return ((Boolean) this.settingsJSON.get("checkpoint")).booleanValue();
		}
		
		return DEFAULT_CHECKPOINTING;
	}
	
	public boolean isSoundReceivePaymentEnabled() 
	{
		if(this.settingsJSON.containsKey("soundreceivepayment"))
		{
			return ((Boolean) this.settingsJSON.get("soundreceivepayment")).booleanValue();
		}
		
		return DEFAULT_SOUND_RECEIVE_COIN;
	}
	
	public boolean isSoundReceiveMessageEnabled() 
	{
		if(this.settingsJSON.containsKey("soundreceivemessage"))
		{
			return ((Boolean) this.settingsJSON.get("soundreceivemessage")).booleanValue();
		}
		
		return DEFAULT_SOUND_MESSAGE;
	}
	
	public boolean isSoundNewTransactionEnabled() 
	{
		if(this.settingsJSON.containsKey("soundnewtransaction"))
		{
			return ((Boolean) this.settingsJSON.get("soundnewtransaction")).booleanValue();
		}
		
		return DEFAULT_SOUND_NEW_TRANSACTION;
	}
	
	public int getMaxBytePerFee() 
	{
		if(this.settingsJSON.containsKey("maxbyteperfee"))
		{
			return ((Long) this.settingsJSON.get("maxbyteperfee")).intValue();
		}
		
		return DEFAULT_MAX_BYTE_PER_FEE;
	}
	
	public boolean isAllowFeeLessRequired() 
	{
		if(this.settingsJSON.containsKey("allowfeelessrequired"))
		{
			return ((Boolean) this.settingsJSON.get("allowfeelessrequired")).booleanValue();
		}
		
		return ALLOW_FEE_LESS_REQUIRED;
	}
	
	public BigDecimal getBigFee() 
	{
		return DEFAULT_BIG_FEE;
	}
	
	public String getBigFeeMessage() 
	{
		return DEFAULT_BIG_FEE_MESSAGE;
	}

	public boolean isGuiEnabled() 
	{
		
		if(!Controller.getInstance().doesWalletDatabaseExists())
		{
			return true;
		}
		
		if(System.getProperty("nogui") != null)
		{
			return false;
		}
		if(this.settingsJSON.containsKey("guienabled"))
		{
			return ((Boolean) this.settingsJSON.get("guienabled")).booleanValue();
		}
		
		return DEFAULT_GUI_ENABLED;
	}
	
	public String getTimeZone()
	{
		if(this.settingsJSON.containsKey("timezone")) {
			return (String) this.settingsJSON.get("timezone");
		}
		
		return DEFAULT_TIME_ZONE;
	}
	
	public String getTimeFormat()
	{
		if(this.settingsJSON.containsKey("timeformat")) {
			return (String) this.settingsJSON.get("timeformat");
		}
		
		return DEFAULT_TIME_FORMAT;
	}

	public boolean isSysTrayEnabled() {
		if(this.settingsJSON.containsKey("systray"))
		{
			return ((Boolean) this.settingsJSON.get("systray")).booleanValue();
		}
		return true;
	}

	public boolean isLocalAddress(InetAddress address) {
		try {
			if(this.localAddress == null) {
				return false;
			} else {
				return address.equals(this.localAddress);
			}
		} catch (Exception e) {
            return false;
        }
	}
	
	public InetAddress getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while(nias.hasMoreElements()) {
                    InetAddress ia= (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() 
                     && !ia.isLoopbackAddress()
                     && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("unable to get current IP " + e.getMessage());
        }
		return null;
    }

}
