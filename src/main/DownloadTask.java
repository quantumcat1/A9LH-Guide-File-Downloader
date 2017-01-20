package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class DownloadTask extends SwingWorker<Void, Void>
{
    private static final int BUFFER_SIZE = 4096;
    private String downloadURL;
    private String saveDirectory;
    private StatusWindow gui;
    private String fileName;

    public DownloadTask(StatusWindow gui, FileVO file)
    {
        this.gui = gui;
        this.downloadURL = file.file;
        this.saveDirectory = file.path.replace("./", "/");
        fileName = "";
        gui.addMessage(file.name + ": " + file.message);
    }

    /**
     * Executed in background thread
     */
    @Override
    protected Void doInBackground() throws Exception
    {
        try
        {
            DownloadUtil util = new DownloadUtil();
            util.downloadFile(downloadURL);

            // set file information on the GUI
            //gui.setFileInfo(util.getFileName(), util.getContentLength());
            fileName = util.getFileName();
            ProgressPanel pp = gui.addNew(util.getFileName());
            addPropertyChangeListener(pp);

            String saveFilePath = saveDirectory + File.separator + util.getFileName();
            File f = new File(saveDirectory);
            if(!f.exists())
            {
            	f.mkdirs();
            }

            InputStream inputStream = util.getInputStream();
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            long totalBytesRead = 0;
            int percentCompleted = 0;
            long fileSize = util.getContentLength();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                percentCompleted = (int) (totalBytesRead * 100 / fileSize);

                setProgress(percentCompleted);
            }

            outputStream.close();

            util.disconnect();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(gui, "Error downloading file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            setProgress(0);
            cancel(true);
        }
        return null;
    }

    /**
     * Executed in Swing's event dispatching thread
     */
    @Override
    protected void done()
    {
        if (!isCancelled())
        {
        	String[] bits = fileName.split("\\.");
        	String ext = bits[bits.length-1];
        	if(ext.equals("7z") || ext.equals("zip"))
        	{
        		try {
        			ZipFile zipFile = new ZipFile(saveDirectory + File.separator + fileName);
       	         	zipFile.extractAll(saveDirectory);
        	    } catch (ZipException e) {
        	        e.printStackTrace();
        	    }
        	}
            JOptionPane.showMessageDialog(gui,
                    "File has been downloaded successfully!", "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}