package main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;

import com.frostwire.jlibtorrent.*;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;

import main.DownloadHandler.Reason;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import uriSchemeHandler.URISchemeHandler;

public class MainWindow extends JPanel implements ActionListener
{
    private static final long serialVersionUID = -371374960795143493L;
    private static int windowWidth = 800;
    private static int windowHeight = 800;

    public enum Region
    {
    	ALL ("All"),
    	E ("Eur"),
    	J ("JPN"),
    	U ("USA"),
    	K ("Kor"),
    	T ("TWN");

    	final String desc;
    	Region(String desc)
    	{
    		this.desc = desc;
    	}
    	@Override
    	public String toString()
    	{
    		return desc;
    	}
    }

    public enum Firmware
    {
    	ALL ("All"),
    	L_f110 ("< 11.0"),
    	f110 ("11.0"),
    	f111 ("11.1"),
    	f112 ("11.2");

    	final String desc;
    	Firmware(String desc)
    	{
    		this.desc = desc;
    	}
    	@Override
    	public String toString()
    	{
    		return desc;
    	}
    }

    public enum Type
    {
    	ALL ("All"),
    	O ("Old"),
    	N ("New");

    	final String desc;
    	Type(String desc)
    	{
    		this.desc = desc;
    	}
    	@Override
    	public String toString()
    	{
    		return desc;
    	}
    }


    private JComboBox<Region> regionCombo;
    private JComboBox<Firmware> firmwareCombo;
    private JComboBox<Type> typeCombo;
    /*private JLabel searchLabel;
    private JTextArea search;*/
    private JTable pageTable;
    private Page page;
    private ArrayList<Page> pages;
    private JButton btnGo;
    private JButton btnReport;
    StatusWindow status;
    private ReportWindow report;
    private ConsoleVO console;


    public static void initialiseFontSize(float multiplier)
    {
    	UIDefaults defaults = UIManager.getDefaults();
    	int i = 0;
    	for(Enumeration<Object> e = defaults.keys(); e.hasMoreElements(); i++)
    	{
    		Object key = e.nextElement();
    		Object value = defaults.get(key);
    		if(value instanceof Font)
    		{
    			Font font = (Font)value;
    			int newSize = Math.round(font.getSize()*multiplier);
    			if(value instanceof FontUIResource)
    			{
    				defaults.put(key,  new FontUIResource(font.getName(), font.getStyle(), newSize));
    			}
    			else
    			{
    				defaults.put(key,  new Font(font.getName(), font.getStyle(), newSize));
    			}
    		}
    	}
    }

    public void initialise() throws ParserConfigurationException, SAXException, IOException, ParseException
    {
    	console = new ConsoleVO();
    	pages = Page.getPages();


        /*JLabel title = new JLabel("A9LH Guide File Downloader");
        title.setMaximumSize(new Dimension(windowWidth, windowHeight/10));
        title.setMinimumSize(new Dimension(windowWidth, windowHeight/10));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);*/

        JTextArea desc = new JTextArea("This program will download "
        		+ "files associated with particular pages "
        		+ "of the A9LH guide. Select a page using "
        		+ "the table, and click Download. For"
        		+ " quickest use, run from the root of your "
        		+ "SD card, or otherwise move the files onto "
        		+ "your SD card afterward. Please note any messages "
        		+ "that come up in the download status window.");
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setMaximumSize(new Dimension(windowWidth, windowHeight/5));
        desc.setMinimumSize(new Dimension(windowWidth, windowHeight/5));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(desc);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel typeLabel = new JLabel("Console type: ");
        panel.add(typeLabel);

        typeCombo = new JComboBox<Type>(Type.values());
        typeCombo.addActionListener(this);
        panel.add(typeCombo);

        JLabel firmwareLabel = new JLabel(" Firmware: ");
        panel.add(firmwareLabel);

        firmwareCombo = new JComboBox<Firmware>(Firmware.values());
        firmwareCombo.addActionListener(this);
        panel.add(firmwareCombo);

        JLabel regionLabel = new JLabel(" Region: ");
        panel.add(regionLabel);

        regionCombo = new JComboBox<Region>(Region.values());
        regionCombo.addActionListener(this);
        panel.add(regionCombo);

        add(panel);

        /*JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        searchLabel = new JLabel("Search: ");
        searchLabel.setMaximumSize(new Dimension(windowWidth/3, windowHeight/10));
        searchLabel.setMinimumSize(new Dimension(windowWidth/3, windowHeight/10));
        panel.add(searchLabel);

        search = new JTextArea(1, 30);
        search.setMaximumSize(new Dimension(windowWidth*(2/3), windowHeight/10));
        search.setMinimumSize(new Dimension(windowWidth*(2/3), windowHeight/10));
        panel.add(search);

        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panel);*/

        pageTable = new JTable();
        pageTable.setMaximumSize(new Dimension(windowWidth, windowHeight/2));
        pageTable.setMinimumSize(new Dimension(windowWidth, windowHeight/2));
        setTableRows();

        pageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
            	page = pages.get(pageTable.getSelectedRow());
            	SingletonFile.getInstance().write("Page " + page.title + " selected.");
            }
        });
        JScrollPane scrollPane = new JScrollPane(pageTable);
        add(scrollPane);

        btnGo = new JButton("Download");
        //btnGo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGo.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event)
        	{
        		status = null;
				try
				{
					download(event);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        });
        //add(btnGo);
        btnReport = new JButton("Report a problem");
        btnReport.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event)
        	{
        		report(event);
        	}
        });
        JPanel p = new JPanel();
        p.add(btnReport);
        p.add(btnGo);
        add(p);
    }
    private void report(ActionEvent event)
    {
    	JFrame fr = new JFrame("Report a problem");
        fr.setPreferredSize(new Dimension(800, 600));

        report = new ReportWindow();
        report.setLayout(new BoxLayout(report, BoxLayout.PAGE_AXIS));
        report.setOpaque(true);
        fr.setContentPane(report);

        fr.pack();
        fr.setLocationRelativeTo(null);
        fr.setVisible(true);
    }

    private void download(ActionEvent event) throws IOException
    {
    	if(page == null) return;

    	if(status == null)
    	{
    		JFrame frame = new JFrame(page.getTitle());
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(500, 600));

            status = new StatusWindow();
            status.setLayout(new BoxLayout(status, BoxLayout.PAGE_AXIS));
            status.setOpaque(true);
            frame.setContentPane(status);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
    	}

    	DownloadHandler dh = new DownloadHandler();
    	dh.downloadAll(page, status, console);
    }

    private void setTableRows()
    {
        DefaultTableModel dtm = new DefaultTableModel(0, 0);
        //String[] header = new String[]{"Title", "URL"};
        String[] header = new String[]{"Title"/*, "Number of files"*/};
        dtm.setColumnIdentifiers(header);
        for(Page page : pages)
        {
            //dtm.addRow(new Object[]{page.title, page.url});
        	dtm.addRow(new Object[]{page.title/*, Integer.toString(page.files.size())*/});
        }
        pageTable.setModel(dtm);
        for(int row = 0; row < pageTable.getRowCount(); row++)
        {
        	int rowHeight = pageTable.getRowHeight();
        	Component comp = pageTable.prepareRenderer(pageTable.getCellRenderer(row, 0), row, 0);
        	rowHeight = Math.max(rowHeight,  comp.getPreferredSize().height);
        	pageTable.setRowHeight(row, rowHeight);
        }
    }

    public MainWindow() throws ParserConfigurationException, SAXException, IOException, ParseException
    {
    	SingletonFile.getInstance().write("Launching version compiled 22nd February");
        initialise();
    }

    private static void createWindow() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, ParserConfigurationException, SAXException, IOException, ParseException
    {
    	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	initialiseFontSize(2.0f);
    	JFrame frame = new JFrame("A9LH Guide File Downloader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(windowWidth, windowHeight));

        MainWindow newContentPane = new MainWindow();
        newContentPane.setLayout(new BoxLayout(newContentPane, BoxLayout.PAGE_AXIS));
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception//ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, ParserConfigurationException, SAXException, IOException, ParseException
    {
        createWindow();
    }



    @Override
    public void actionPerformed(ActionEvent e)
    {
    	JComboBox cb = (JComboBox)e.getSource();
    	if(cb == regionCombo)
    	{
    		console.region = (Region)cb.getSelectedItem();
    		SingletonFile.getInstance().write("Region " + console.region.desc + " selected.");
    	}
    	else if (cb == typeCombo)
    	{
    		console.type = (Type)cb.getSelectedItem();
    		SingletonFile.getInstance().write("Type " + console.type.desc + " selected.");
    	}
    	else if (cb == firmwareCombo)
    	{
    		console.firmware = (Firmware)cb.getSelectedItem();
    		SingletonFile.getInstance().write("Firmware " + console.firmware.desc + " selected.");
    	}
    }
}
