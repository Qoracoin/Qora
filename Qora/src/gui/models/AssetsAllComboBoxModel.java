package gui.models;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.DefaultComboBoxModel;

import controller.Controller;
import qora.assets.Asset;

@SuppressWarnings("serial")
public class AssetsAllComboBoxModel extends DefaultComboBoxModel<Asset> {
	Lock lock = new ReentrantLock();
	
	public AssetsAllComboBoxModel()
	{
		Collection<Asset> allAssets = Controller.getInstance().getAllAssets();
		
		for (Asset asset : allAssets) {
			this.addElement(asset);
		}
		
		this.setSelectedItem(this.getElementAt(0));
	}
}
