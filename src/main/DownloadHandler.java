package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingWorker;

import main.MainWindow.Firmware;
import main.MainWindow.Region;
import main.MainWindow.Type;

public class DownloadHandler
{
	public enum Reason
    {
    	UNKNOWN ("This link does not have a direct download link in the database yet."
    			+ " Please look at the guide to see how to download the required"
    			+ "file."),
    	PAGE ("This link cannot have its file downloaded automatically. Please "
    			+ "look at the guide to see how to download this file."),
    	FIRMWARE (""),
    	REGION (""),
    	TYPE (""),
    	OK ("");

    	final String desc;
    	Reason(String desc)
    	{
    		this.desc = desc;
    	}
    	@Override
    	public String toString()
    	{
    		return desc;
    	}
    }
	private Reason willDownload(FileVO f, ConsoleVO c)
	{
		if(f.file.equals("unknown"))
		{
			return Reason.UNKNOWN;
		}
		else if(f.file.equals("page"))
		{
			return Reason.PAGE;
		}


		boolean foundone = false;

//..*****first check firmware of file to see if it matches the user's
		String fw = f.firmware;
		if(!fw.trim().equals("") && c.firmware != Firmware.ALL) //if blank, it's for all fws, so just keep going
		{
			String[] fws = fw.split("\\|");
			foundone = false; //assume we don't find any until we actually find one
			for(String afw : fws) //could be multiple firmwares separated by pipe character
			{
				if(afw.equals(c.firmware.desc))
				{
					foundone = true;
				}
			}
			if(!foundone)
			{
				return Reason.FIRMWARE;
			}
		}

		String reg = f.region;
		if(!reg.trim().equals("") && c.region != Region.ALL) //if blank, it's for all regions, so just keep going
		{
			if(!c.region.name().equals(reg))
			{
				return Reason.REGION;
			}
		}
 //..*****now check type to see if it matches user's
		String t = f.type.trim();
		if(!t.equals("") && c.type != Type.ALL) //if blank, for everyone, so just continue
		{
			if(!c.type.name().equals(t))
			{
				return Reason.TYPE;
			}
		}

		return Reason.OK;
	}

	public SwingWorker download(FileVO f, ConsoleVO c, StatusWindow status)
	{
		try
		{
			if(f.link.contains("magnet"))
			{
				TorrentWorker tw = new TorrentWorker(f, status);
				return tw;
				//return null;
			}
			else
			{
				//DownloadTask task = new DownloadTask(f, status);
        		//return task;
				return null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void downloadAll(Page page, StatusWindow status, ConsoleVO c)
	{
		ExecutorService es = Executors.newFixedThreadPool(4);
		for(FileVO f : page.getFiles())
    	{
			Reason reason = willDownload(f, c);
			if(reason == Reason.OK)
			{
				SwingWorker sw = download(f, c, status);
				if(sw != null) es.execute(sw);
			}
			else
			{
				status.addMessage(f.file + ": " + reason.toString());
			}
    	}
		es.shutdown();
	}
}
