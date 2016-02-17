package gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import api.ApiClient;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class ConsolePanel extends JPanel 
{
	private ApiClient client;
	private JTextArea areaConsole;
	private JTextField txtCommand;
	private ArrayList<String> cmdHistory = new ArrayList<String>();
	private int INTcmdHistory = -1;	
	
	public ConsolePanel()
	{
		this.setLayout(new GridBagLayout());
		
		//CREATE SERVICE
		this.client = new ApiClient();//new RpcClient(new RpcServiceImpl());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		int componentLevel = 0;
		
		//TEXTAREA GBC
		GridBagConstraints areaGBC = new GridBagConstraints();
		areaGBC.insets = new Insets(5,5,5,5);
		areaGBC.fill = GridBagConstraints.BOTH;  
		areaGBC.anchor = GridBagConstraints.NORTHWEST;
		
		areaGBC.gridy = componentLevel;
		areaGBC.gridx = 0;	
		areaGBC.weighty = 1;	
		areaGBC.weightx = 1;
		areaGBC.gridwidth = 2;

		//TEXTAREA
		this.areaConsole = new JTextArea();
		this.areaConsole.setLineWrap(true);
		this.areaConsole.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.areaConsole);
		final JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
		this.add(scrollPane, areaGBC);
		
		//TEXTFIELD
		this.txtCommand = new JTextField();
		this.txtCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
            {
				execute();
            }
		});
		
		this.txtCommand.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
		    	if(INTcmdHistory != -1)
		    	{
		    		if(e.getKeyCode()==KeyEvent.VK_UP) {
		    			if(INTcmdHistory > 0)
		    			{
		    				INTcmdHistory--; 
		    				txtCommand.setText(cmdHistory.get(INTcmdHistory));
		    			}
		    		}
		    		
		    		if(e.getKeyCode()==KeyEvent.VK_DOWN) {
		    			if(INTcmdHistory < cmdHistory.size() - 1)
		    			{
		    				INTcmdHistory++;
			    			txtCommand.setText(cmdHistory.get(INTcmdHistory));
			    		}
		    		}
		    		
		    	}
		    } 
		});
		
		componentLevel ++;
		
		//TEXTBOX GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.CENTER;
		
		txtGBC.gridy = componentLevel;
		txtGBC.gridwidth = 1;
		txtGBC.gridx = 0;	
		txtGBC.weightx = 1;
		
		this.add(this.txtCommand, txtGBC);
		
        //BUTTON RUN
		GridBagConstraints buttonGBC = new GridBagConstraints();
		
		buttonGBC.gridy = componentLevel;
		buttonGBC.gridx = 1;
		buttonGBC.gridwidth = 1;
		
		buttonGBC.fill = GridBagConstraints.HORIZONTAL;  
		buttonGBC.anchor = GridBagConstraints.CENTER;
		JButton sendButton = new JButton("Run");
        sendButton.setPreferredSize(new Dimension(80, 25));
    	sendButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	execute();
		    }
		});	
		this.add(sendButton, buttonGBC);
		
		//CONTEXT MENU
		MenuPopupUtil.installContextMenu(this.areaConsole);
		MenuPopupUtil.installContextMenu(this.txtCommand);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
				public void run() {
					txtCommand.grabFocus();
					txtCommand.requestFocus();
				}
			});
	}
	
	public void execute()
	{
		//GET COMMAND
		String command = txtCommand.getText();
		
		cmdHistory.add(command);
		INTcmdHistory = cmdHistory.size();
		
		if(command.toLowerCase().equals("clear"))
		{
			areaConsole.setText("");
			txtCommand.setText("");
			return;
		}
			
		areaConsole.append("[COMMAND] " + command + "\n");
		
		//EMPTY COMMAND FIELD
		txtCommand.setText("");
		
		//GET RESULT
		String result = client.executeCommand(command);
		
		//APPEND RESULT
		areaConsole.append("[RESULT] " + result + "\n");
		areaConsole.setCaretPosition(areaConsole.getDocument().getLength());
	}
}
