package gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Font;

import javax.swing.SwingConstants;

import utils.BuildTime;
import controller.Controller;

import java.awt.Insets;
import java.awt.Color;

@SuppressWarnings("serial")
public class AboutFrame extends JFrame{

	private AboutPanel aboutPanel;
	
	public AboutFrame() 
	{
		//CREATE FRAME
		super("Qora - Debug");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//DEBUG TABPANE
        this.aboutPanel = new AboutPanel();
        this.getContentPane().setPreferredSize(new Dimension(802,336));
        this.setUndecorated(true);
     
        this.aboutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	setVisible(false);
                dispose();
            }
        });
        
        this.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
		    	setVisible(false);
                dispose();
		    }
		});
        
        //ADD GENERAL TABPANE TO FRAME
        getContentPane().add(this.aboutPanel);
        GridBagLayout gbl_aboutPanel = new GridBagLayout();
        gbl_aboutPanel.columnWidths = new int[]{483, 181, 70, 0};
        gbl_aboutPanel.rowHeights = new int[]{252, 0, 0, 0, 0};
        gbl_aboutPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_aboutPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        aboutPanel.setLayout(gbl_aboutPanel);
        
        JLabel lblversionLabel = new JLabel("Version: " + Controller.getInstance().getVersion());
        lblversionLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblversionLabel.setForeground(Color.WHITE);
        lblversionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lbllversionLabel = new GridBagConstraints();
        gbc_lbllversionLabel.fill = GridBagConstraints.BOTH;
        gbc_lbllversionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lbllversionLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_lbllversionLabel.gridx = 1;
        gbc_lbllversionLabel.gridy = 1;
        aboutPanel.add(lblversionLabel, gbc_lbllversionLabel);
        
        JLabel label = new JLabel("Build date: " + BuildTime.getInstance().getBuildDateTimeString());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Tahoma", Font.PLAIN, 13));
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 1;
        gbc_label.gridy = 2;
        aboutPanel.add(label, gbc_label);

        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
