package database.wallet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.mapdb.DB;

import utils.ObserverMessage;

public class AssetFavoritesSet extends Observable {

	private WalletDatabase walletDatabase;
	private Set<Long> assetsSet;
	
	public AssetFavoritesSet(WalletDatabase walletDatabase, DB database) 
	{
		this.walletDatabase = walletDatabase;
		
		//OPEN MAP
		this.assetsSet = database.getTreeSet("assetFavorites");
		
		//CHECK IF CONTAINS QORA
		if(!this.assetsSet.contains(0l))
		{
			this.add(0l);
		}
	}
	
	public void replace(List<Long> keys)
	{
		this.assetsSet.clear();
		this.assetsSet.addAll(keys);
		this.walletDatabase.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public void add(Long key)
	{
		this.assetsSet.add(key);
		this.walletDatabase.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public void delete(Long key)
	{
		this.assetsSet.remove(key);
		this.walletDatabase.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public boolean contains(Long key)
	{
		return this.assetsSet.contains(key);
	}
	
	@Override
	public void addObserver(Observer o) 
	{
		//ADD OBSERVER
		super.addObserver(o);	
		
		//NOTIFY LIST
		this.notifyFavorites();
	}
	
	private void notifyFavorites()
	{
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_ASSET_FAVORITES_TYPE, this.assetsSet));
	}
}
