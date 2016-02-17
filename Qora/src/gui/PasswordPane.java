package gui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import controller.Controller;

public class PasswordPane 
{
	public static String showUnlockWalletDialog()
	{
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(2,2));

		//Labels for the textfield components        
		JLabel passwordLbl = new JLabel("Enter wallet password:");
		JPasswordField passwordFld = new JPasswordField();

		//Add the components to the JPanel        
		userPanel.add(passwordLbl);
		userPanel.add(passwordFld);

		Object[] options = {"Unlock",
                "Unlock for 2 minutes",
                "Cancel"};		
		
		//As the JOptionPane accepts an object as the message
		//it allows us to use any component we like - in this case 
		//a JPanel containing the dialog components we want
		
		int n = JOptionPane.showOptionDialog(
					null, 
					userPanel, 
					"Unlock Wallet",
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, 
					null,
					options, 
					passwordFld
				);
		
		if(n == JOptionPane.YES_OPTION) {
			Controller.getInstance().setSecondsToUnlock(-1);
		} else if (n == JOptionPane.NO_OPTION) {
			Controller.getInstance().setSecondsToUnlock(120);
		} else {
			return "";
		}
		
		return new String(passwordFld.getPassword());
	}
	
	public static void switchLockDialog()
	{
		if(Controller.getInstance().isWalletUnlocked())
		{
			Controller.getInstance().lockWallet();
		}
		else
		{
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!password.equals("") && !Controller.getInstance().unlockWallet(password))
			{
				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
