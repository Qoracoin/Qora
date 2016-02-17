package gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import controller.Controller;

@SuppressWarnings("serial")
public class ClosingDialog extends JFrame{

	private JDialog waitDialog;
	
	public ClosingDialog()
	{
		try {
			Gui.getInstance().hideMainFrame();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		//CREATE WAIT DIALOG
		JOptionPane optionPane = new JOptionPane("Saving database. Please wait...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
		this.waitDialog = new JDialog();
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.waitDialog.setIconImages(icons);
		this.waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);	
		this.waitDialog.setTitle("Closing...");
		this.waitDialog.setContentPane(optionPane);	
		this.waitDialog.setModal(false);
		this.waitDialog.pack();
		this.waitDialog.setLocationRelativeTo(null);
		this.waitDialog.setAlwaysOnTop(true);
		this.waitDialog.setVisible(true);
		
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	Controller.getInstance().stopAll();
		    	waitDialog.dispose();
		    	System.exit(0);
		    }
		});
    	
	}
}
