package main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

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
	private File f;
	private String fileName;
	private String magnetLink;
	private ProgressPanel pp;
	final private SessionManager s;

	public TorrentWorker(String magnetLink, String fileName, StatusWindow status)
	{
		this.fileName = fileName;
		this.magnetLink = magnetLink;
		this.pp = status.addNew(fileName);

		// making torrent files:
		f = new File("/torrents/" + fileName + ".torrent");
		s = new SessionManager();
		s.start();
		System.out.println("Fetching the magnet uri, please wait...");
		byte[] data = s.fetchMagnet(magnetLink, 30);
		if (data != null)
		{
			System.out.println(Entry.bdecode(data));
			try
			{
				FileUtils.writeByteArrayToFile(f, data);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Torrent data saved to: " + f.getAbsolutePath());
		}
		else
		{
			System.out.println("Failed to retrieve the magnet");
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

			TorrentInfo ti = new TorrentInfo(f);
			s.download(ti, f.getParentFile());

			signal.await();

		}
		catch(Exception e)
		{

		}
		return null;
	}

	@Override
	public void done()
	{
		s.stop();
	}

}
