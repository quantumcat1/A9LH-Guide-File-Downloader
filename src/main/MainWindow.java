package main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
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

import org.xml.sax.SAXException;

import uriSchemeHandler.CouldNotOpenUriSchemeHandler;
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

    	private final String desc;
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

    	private final String desc;
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

    	private final String desc;
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
    private StatusWindow status;
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
    	//Page.getPages();

        JLabel title = new JLabel("A9LH Guide File Downloader");
        title.setMaximumSize(new Dimension(windowWidth, windowHeight/10));
        title.setMinimumSize(new Dimension(windowWidth, windowHeight/10));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);

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
            }
        });
        JScrollPane scrollPane = new JScrollPane(pageTable);
        add(scrollPane);

        btnGo = new JButton("Download");
        btnGo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGo.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event)
        	{
        		status = null;
        		try {
					download(event);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CouldNotOpenUriSchemeHandler e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        });
        add(btnGo);
    }
    private void download(ActionEvent event) throws URISyntaxException, CouldNotOpenUriSchemeHandler
    {
    	if(page == null) return;
    	//String saveDir = "C:/Java Projects";

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

    	for(FileVO f : page.getFiles())
    	{
    		if(f.file.equals("unknown")) continue; //need to fill in direct file link in database

    		boolean foundone = false;

    //..*****first check firmware of file to see if it matches the user's
    		String fw = f.firmware;
    		if(!fw.trim().equals("") || console.firmware == Firmware.ALL) //if blank, it's for all fws, so just keep going
    		{
    			String[] fws = fw.split("\\|");
    			foundone = false; //assume we don't find any until we actually find one
    			for(String afw : fws) //could be multiple firmwares separated by pipe character
    			{
    				if(afw.equals(console.firmware.desc))
    				{
    					foundone = true;
    				}
    			}
    			if(!foundone) continue; //file is not for user's firmware so don't download this file
    		}

    //..*****now check region to see if it matches user's
    		String reg = f.region;
    		if(!reg.trim().equals("") || console.region == Region.ALL) //if blank, it's for all regions, so just keep going
    		{
    			if(!console.region.name().equals(reg))
    			{
    				continue; //file is not for user's region
    			}
    		}
     //..*****now check type to see if it matches user's
    		String t = f.type;
    		if(!t.trim().equals("") || console.type == Type.ALL) //if blank, for everyone, so just continue
    		{
    			if(!console.type.name().equals(t))
    			{
    				continue; //file is not for user's console type
    			}
    		}

    		if(f.link.contains("magnet"))
    		{
    			URI magnetLinkUri = new URI(f.link);
    			URISchemeHandler uriSchemeHandler = new URISchemeHandler();
    			uriSchemeHandler.open(magnetLinkUri);
    		}
    		else
    		{
	        	try
	        	{
	        		DownloadTask task = new DownloadTask(status, f);
	        		task.execute();
	        	}
	        	catch(Exception ex)
	        	{
	        		JOptionPane.showMessageDialog(this,  "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        	}
    		}
    	}
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

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, ParserConfigurationException, SAXException, IOException, ParseException
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
    	}
    	else if (cb == typeCombo)
    	{
    		console.type = (Type)cb.getSelectedItem();
    	}
    	else if (cb == firmwareCombo)
    	{
    		console.firmware = (Firmware)cb.getSelectedItem();
    	}

    }
}
