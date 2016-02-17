package gui.status;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import qora.BlockGenerator;
import qora.account.Account;
import utils.GUIUtils;
import utils.ObserverMessage;
import controller.Controller;

@SuppressWarnings("serial")
public class ForgingStatus extends JLabel implements Observer {

	private ImageIcon forgingDisabledIcon;
	private ImageIcon forgingEnabledIcon;
	private ImageIcon forgingIcon;
	
	public ForgingStatus()
	{
		super();
		
		//CREATE ICONS
		this.forgingDisabledIcon = this.createIcon(Color.RED);
		this.forgingEnabledIcon = this.createIcon(Color.ORANGE);
		this.forgingIcon = this.createIcon(Color.GREEN);

		//TOOLTIP
		ToolTipManager.sharedInstance().setDismissDelay( (int) TimeUnit.SECONDS.toMillis(5));
		this.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent mEvt) {
				if(Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING)
				{
					BigDecimal totalBalance = BigDecimal.ZERO.setScale(8);
		           	
		            for(Account account: Controller.getInstance().getAccounts())
			        {
			        	totalBalance = totalBalance.add(account.getConfirmedBalance());
			        }
			        long totalBalanceInt = totalBalance.longValue();
		            
			        String timeForge = "";
			        if(totalBalanceInt>0)
			        {
			        	timeForge = getTimeToGoodView((60*5+19)*Controller.getInstance().getLastBlock().getGeneratingBalance()/totalBalanceInt);
			        }
			        else
			        {
			        	timeForge = "infinity";
			        }
			        
		            setToolTipText("Approx time to find the block: " + timeForge + ".");
				}
				else if (Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING_DISABLED && Controller.getInstance().getStatus() == Controller.STATUS_OK) 
				{
					setToolTipText("To start forging you need to unlock the wallet.");
				}
				else
				{
					setToolTipText("For forging wallet must be online and fully synchronized.");
				}
				
	    }});

		//LISTEN ON STATUS
		Controller.getInstance().addObserver(this);	
		setIconAndText(Controller.getInstance().getForgingStatus());
	}
	
	public static String getTimeToGoodView(long intdif) {
		String result = "";
		long diff = intdif * 1000;
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		final int ONE_SECOND = ONE_MINUTE / 60;
		
		long d = diff / ONE_DAY;
		diff %= ONE_DAY;
		
		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;
		
		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;
		
		long s = diff / ONE_SECOND;
		//long ms = diff % ONE_SECOND;
		
		if(d>0)
		{
			result += d > 1 ? d + " days " : d + " day ";	
		}
		
		if(h>0 && d<5)
		{
			result += h > 1 ? h + " hours " : h + " hour ";	
		}	
		
		if(m>0 && d == 0 && h<10 )
		{
			result += m > 1 ? m + " mins " : m + " min ";	
		}
		
		if(s>0 && d == 0 && h == 0 && m<15)
		{
			result += s > 1 ? s + " seconds " : s + " second ";
		}
		
		return result.substring(0, result.length() - 1);
	}

	private ImageIcon createIcon(Color color)
	{
		return GUIUtils.createIcon(color, this.getBackground());
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.FORGING_STATUS)
		{
			BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();
			
			setIconAndText(status);
		}		
	}

	private void setIconAndText(BlockGenerator.ForgingStatus status) {
		if(status == BlockGenerator.ForgingStatus.FORGING_DISABLED)
		{
			forgingDisabled();
		}
		if(status ==BlockGenerator.ForgingStatus.FORGING_ENABLED)
		{
			this.setIcon(forgingEnabledIcon);
			this.setText(status.getName());
		}
		if(status == BlockGenerator.ForgingStatus.FORGING)
		{
			this.setIcon(forgingIcon);
			this.setText(status.getName());
		}
	}

	public void forgingDisabled() {
		this.setIcon(forgingDisabledIcon);
		this.setText(BlockGenerator.ForgingStatus.FORGING_DISABLED.getName());
	}

 
}
