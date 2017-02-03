package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Entry;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;

public class TorrentWorker extends SwingWorker<Void, Void>
{
	private File torrentFile;
	private FileVO fileVO;
	private ProgressPanel pp;
	final private SessionManager s;

	public TorrentWorker(FileVO fileVO, StatusWindow status)
	{
		this.fileVO = fileVO;
		this.pp = status.addNew(fileVO.file);

		// making torrent file:
		torrentFile = new File("/torrents/" + fileVO.file + ".torrent");
		s = new SessionManager();
		s.start();
		System.out.println("Fetching the magnet uri, please wait...");
		if(torrentFile.exists())
		{
			System.out.println("Torrent already exists");
		}
		else
		{
			byte[] data = s.fetchMagnet(fileVO.link, 30);
			if (data != null)
			{
				System.out.println(Entry.bdecode(data));
				try
				{
					FileUtils.writeByteArrayToFile(torrentFile, data);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Torrent data saved to: " + torrentFile.getAbsolutePath());
			}
			else
			{
				System.out.println("Failed to retrieve the magnet");
			}
		}
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		try
		{
			addPropertyChangeListener(pp);
			CountDownLatch signal = new CountDownLatch(1);
			s.addListener(new AlertListener()
	        {
	            @Override
	            public int[] types()
	            {
	                return null;
	            }
	            @Override
	            public void alert(Alert<?> alert)
	            {
		            AlertType type = alert.type();

		            switch (type)
		            {
		                case TORRENT_ADDED:
		                    System.out.println("Torrent added");
		                    ((TorrentAddedAlert) alert).handle().resume();
		                    break;
		                case BLOCK_FINISHED:
		                    BlockFinishedAlert a = (BlockFinishedAlert) alert;
		                    int p = (int) (a.handle().status().progress() * 100);
		                    setProgress(p);
		                    System.out.println("Progress: " + p + " for torrent name: " + a.torrentName());
		                    System.out.println(s.stats().totalDownload());
		                    break;
		                case TORRENT_FINISHED:
		                	setProgress(100);
		                    System.out.println("Torrent finished");
		                    signal.countDown();
		                    break;
					default:
						break;
		            }
	            }
	        });

			TorrentInfo ti = new TorrentInfo(torrentFile);
			File saveDir = new File(fileVO.path.replace("./",  "/"));
			if(fileVO.file.endsWith("zip") || fileVO.file.endsWith("7z"))
			{
				saveDir = torrentFile.getParentFile();
			}
			s.download(ti, saveDir);

			signal.await();

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
    @Override
    protected void done()
    {
    	s.stop();
        if (!isCancelled())
        {
        	String[] bits = fileVO.file.split("\\.");
        	String ext = bits[bits.length-1];
        	String saveFilePath = fileVO.path.replace("./",  "/");
        	if (saveFilePath.endsWith("/")) {
        		saveFilePath = saveFilePath.substring(0, saveFilePath.length() - 1);
        	}
        	saveFilePath = saveFilePath.equals("") ? fileVO.file : saveFilePath + "/" + fileVO.file;

        	File f = new File(fileVO.path.replace("./",  "/"));
			//String directory = f.getAbsolutePath();
			if(!f.exists()) f.mkdirs();
        	try
        	{
	        	if(ext.equals("zip"))
	        	{
        			/*ZipFile zipFile = new ZipFile(saveFilePath);
       	         	//zipFile.extractAll(saveDirectory);
        			zipFile.extractAll(FilenameUtils.getPath(saveFilePath));*/

	        		ZipFile zipFile = new ZipFile(new File(torrentFile.getParentFile().getPath() + "/" + fileVO.file));
	        		final Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
	        	    while (entries.hasMoreElements())
	        		{
	        	    	ZipArchiveEntry entry = entries.nextElement();
	        	    	String name = entry.getName();
	        	    	if(name.endsWith("/") && !name.startsWith("/")) name = "/" + name;
	        			System.out.println(name);
	        			if(IsPathDirectory(name))
	        			{
	        				entry = entries.nextElement();
	        				continue;
	        			}

	        			String path = "";
	        			if(entry.getName().contains("/"))
	        			{
	        				path = FilenameUtils.getPath(entry.getName());
	        				if(!fileVO.path.replace("./",  "/").endsWith("/"))
	        				{
	        					path = "/" + path;
	        				}
	        				path = fileVO.path.replace("./",  "/") + path;
	        				File g = new File(path);
	        				if(!g.exists()) g.mkdirs();
	        				path = path + FilenameUtils.getName(entry.getName());
	        			}
	        			else
	        			{
	        				path = entry.getName();
	        			}

        		        FileOutputStream out = new FileOutputStream(path);
        		        InputStream in = zipFile.getInputStream(entry);
        		        IOUtils.copy(in,out);
        		        out.close();
        		        in.close();
	        		 }
	        		 zipFile.close();
	        	}
	        	else if(ext.equals("7z"))
	        	{
	        		//SevenZFile sevenZFile = new SevenZFile(new File(saveFilePath));
	        		SevenZFile sevenZFile = new SevenZFile(new File(torrentFile.getParentFile().getPath() + "/" + fileVO.file));
	        		SevenZArchiveEntry entry = sevenZFile.getNextEntry();
	        		while(entry != null)
	        		{
	        			System.out.println(entry.getName());
	        			//if(new File(entry.getName()).isDirectory())
	        			if(IsPathDirectory(entry.getName()))
	        			{
	        				entry = sevenZFile.getNextEntry();
	        				continue;
	        			}

	        			String path = "";
	        			if(entry.getName().contains("/"))
	        			{
	        				path = FilenameUtils.getPath(entry.getName());
	        				if(!fileVO.path.replace("./",  "/").endsWith("/"))
	        				{
	        					path = "/" + path;
	        				}
	        				path = fileVO.path.replace("./",  "/") + path;
	        				File g = new File(path);
	        				if(!g.exists()) g.mkdirs();
	        				path = path + FilenameUtils.getName(entry.getName());
	        			}
	        			else
	        			{
	        				path = entry.getName();
	        			}


        		        FileOutputStream out = new FileOutputStream(path);
        		        byte[] content = new byte[(int) entry.getSize()];
        		        sevenZFile.read(content, 0, content.length);
        		        out.write(content);
        		        out.close();
        		        entry = sevenZFile.getNextEntry();
	        		 }
	        		 sevenZFile.close();
	        	}
        	}
        	catch (Exception e)
        	{
    	    	e.printStackTrace();
        	}
        }
    }
    private boolean IsPathDirectory(String path) {
        File test = new File(path);

        // check if the file/directory is already there
        if (!test.exists()) {
            // see if the file portion it doesn't have an extension
            return test.getName().lastIndexOf('.') == -1;
        } else {
            // see if the path that's already in place is a file or directory
            return test.isDirectory();
        }
    }

}
