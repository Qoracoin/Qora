package gui.assets;

import gui.models.BuyOrdersTableModel;
import gui.models.SellOrdersTableModel;
import gui.models.TradesTableModel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import qora.assets.Asset;


public class ExchangeFrame extends JFrame
{
	private static final long serialVersionUID = -7052380905136603354L;
	
	private Asset have;
	private Asset want;
	
	private SellOrdersTableModel sellOrdersTableModel;
	private BuyOrdersTableModel buyOrdersTableModel;
	private TradesTableModel tradesTableModel;
	private OrderPanel sellOrderPanel;
	private OrderPanel buyOrderPanel;
	
	public ExchangeFrame(Asset have, Asset want) 
	{
		super("Qora - Asset Exchange");
		
		this.have = have;
		this.want = want;
		
		this.setTitle("Qora - Asset Exchange - " + this.have.toString() + " / " + this.want.toString());
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		
		//ORDER GBC
		GridBagConstraints orderGBC = new GridBagConstraints();
		orderGBC.insets = new Insets(0, 5, 5, 0);
		orderGBC.fill = GridBagConstraints.BOTH;  
		orderGBC.anchor = GridBagConstraints.NORTHWEST;
		orderGBC.weightx = 1;
		orderGBC.gridy = 2;	
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridy = 4;	
		
		//CREATE TITLE LABEL
		JLabel lblTitle = new JLabel(this.have.toString() + " / " + this.want.toString());
				
		lblTitle.setFont(new Font("Serif", Font.PLAIN, 24));
		this.add(lblTitle, labelGBC);
		
		//CREATE BUY LABEL
		labelGBC.gridy = 1;
		JLabel lblBuy = new JLabel( "Buy " + this.have.toString() + " \u2014 Sell " + this.want.toString());
		lblBuy.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblBuy, labelGBC);
		
		//CREATE SELL LABEL
		labelGBC.gridx = 1;
		JLabel lblSell = new JLabel( "Sell " + this.have.toString()	+ " \u2014 Buy " + this.want.toString());
		lblSell.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblSell, labelGBC);
		
		//CREATE BUY PANEL
		buyOrderPanel = new OrderPanel(this.want, this.have, true);
		this.add(buyOrderPanel, orderGBC);
		//buyOrderPanel.setBackground(Color.BLUE);
		
		//CREATE SELL PANEL
		orderGBC.gridx = 1;
		sellOrderPanel = new OrderPanel(this.have, this.want, false);
		//sellOrderPanel.setBackground(Color.BLUE);
		
		orderGBC.fill = GridBagConstraints.NORTH;  
		
		this.add(sellOrderPanel, orderGBC);
		
		sellOrderPanel.setPreferredSize(new Dimension((int)sellOrderPanel.getPreferredSize().getWidth(), (int)buyOrderPanel.getPreferredSize().getHeight()));
		sellOrderPanel.setMinimumSize(new Dimension((int)sellOrderPanel.getPreferredSize().getWidth(), (int)buyOrderPanel.getPreferredSize().getHeight()));
		
		orderGBC.fill = GridBagConstraints.BOTH;  
		
		//CREATE SELL ORDERS LABEL
		labelGBC.gridx = 0;
		labelGBC.gridy = 3;
		JLabel lblSellOrders = new JLabel("Sell orders");
		lblSellOrders.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblSellOrders, labelGBC);
		
		//CREATE BUY ORDERS LABEL
		labelGBC.gridx = 1;
		JLabel lblBuyOrders = new JLabel("Buy orders");
		lblBuyOrders.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblBuyOrders, labelGBC);
						
		//CREATE SELL ORDERS TABLE
		this.sellOrdersTableModel = new SellOrdersTableModel(this.have, this.want);
		final JTable sellOrdersTable = new JTable(this.sellOrdersTableModel);
		
		sellOrdersTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable)e.getSource();
					int row = target.getSelectedRow();

					if(row < sellOrdersTableModel.orders.size())
					{
						buyOrderPanel.txtPrice.setText(BigDecimal.ONE.setScale(8).divide(sellOrdersTableModel.orders.get(row).getB().getPrice(), 8, RoundingMode.DOWN).toPlainString());
						buyOrderPanel.txtAmount.setText(sellOrdersTableModel.orders.get(row).getB().getPrice().multiply(sellOrdersTableModel.orders.get(row).getB().getAmountLeft()).setScale(8, RoundingMode.DOWN).toPlainString());
					}
				}
			}
		});
		
		this.add(new JScrollPane(sellOrdersTable), tableGBC);
		
		//CREATE BUY ORDERS TABLE
		tableGBC.gridx = 1;
		this.buyOrdersTableModel = new BuyOrdersTableModel(this.want, this.have);
		final JTable buyOrdersTable = new JTable(this.buyOrdersTableModel);
		
		buyOrdersTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable)e.getSource();
					int row = target.getSelectedRow();

					if(row < buyOrdersTableModel.orders.size())
					{
						sellOrderPanel.txtPrice.setText(BigDecimal.ONE.setScale(8).divide(buyOrdersTableModel.orders.get(row).getB().getPrice(), 8, RoundingMode.DOWN).toPlainString());
						sellOrderPanel.txtAmount.setText(buyOrdersTableModel.orders.get(row).getB().getPrice().multiply(buyOrdersTableModel.orders.get(row).getB().getAmountLeft()).setScale(8, RoundingMode.DOWN).toPlainString());
					}
				}
			}
		});
		
		this.add(new JScrollPane(buyOrdersTable), tableGBC);
		
		//CREATE TRADE HISTORY LABEL
		labelGBC.gridx = 0;
		labelGBC.gridy = 5;
		JLabel lblTradeHistory = new JLabel("Trade History");
		lblTradeHistory.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblTradeHistory, labelGBC);
		
		//CREATE TRADE HISTORY TABLE
		tableGBC.gridx = 0;
		tableGBC.gridy = 6;
		tableGBC.gridwidth = 2;
		this.tradesTableModel = new TradesTableModel(this.have, this.want);
		final JTable TradesTable = new JTable(this.tradesTableModel);
		
		this.add(new JScrollPane(TradesTable), tableGBC);
		
		//PACK
		this.pack();
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
