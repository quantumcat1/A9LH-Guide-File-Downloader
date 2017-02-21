package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;


public class DownloadTask extends SwingWorker<Void, Void>
{
    private static final int BUFFER_SIZE = 4096;
    private String downloadURL;
    private String saveDirectory;
    private StatusWindow gui;
    private String fileName;

    public DownloadTask(FileVO file, StatusWindow gui)
    {
        this.gui = gui;
        downloadURL = file.file;
        saveDirectory = file.path.replace("./", "/");
        fileName = "";
        if(!file.message.equals("")) gui.addMessage(file.name + ": " + file.message);
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

            SingletonFile.getInstance().write("Downloading from " + downloadURL);

            fileName = util.getFileName();
            ProgressPanel pp = gui.addNew(util.getFileName());
            addPropertyChangeListener(pp);

            if (saveDirectory.endsWith("/"))
        	{
            	saveDirectory = saveDirectory.substring(0, saveDirectory.length() - 1);
        	}

            if (saveDirectory.startsWith("/"))
        	{
            	String basePath = new File("").getAbsolutePath();
            	saveDirectory = basePath + saveDirectory;
        	}
            String saveFilePath = saveDirectory.equals("/") ||  saveDirectory.equals("") ? util.getFileName() : saveDirectory + "/" + util.getFileName();



            File f = new File(saveDirectory);
            if(!f.exists())
            {
            	f.mkdirs();
            }

            SingletonFile.getInstance().write("Saving to " + saveFilePath);

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
            SingletonFile.getInstance().write(fileName + " successfully downloaded.");
        }
        catch (IOException ex)
        {
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
        	String saveFilePath = saveDirectory;
        	if (saveFilePath.endsWith("/"))
        	{
        		saveFilePath = saveFilePath.substring(0, saveFilePath.length() - 1);
        	}
        	saveFilePath = saveFilePath.equals("") ? fileName : saveFilePath + "/" + fileName;

        	File f = new File(saveDirectory);
			//String directory = f.getAbsolutePath();
			if(!f.exists()) f.mkdirs();
        	try
        	{
	        	if(ext.equals("zip"))
	        	{
        			/*ZipFile zipFile = new ZipFile(saveFilePath);
       	         	//zipFile.extractAll(saveDirectory);
        			zipFile.extractAll(FilenameUtils.getPath(saveFilePath));*/

	        		ZipFile zipFile = new ZipFile(new File(saveFilePath));
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
	        				if(!saveDirectory.endsWith("/"))
	        				{
	        					path = "/" + path;
	        				}
	        				path = saveDirectory + path;
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
	        		SevenZFile sevenZFile = new SevenZFile(new File(saveFilePath));
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
	        				if(!saveDirectory.endsWith("/"))
	        				{
	        					path = "/" + path;
	        				}
	        				path = saveDirectory + path;
	        				File g = new File(path);
	        				if(!g.exists()) g.mkdirs();
	        				path = path + FilenameUtils.getName(entry.getName());
	        			}
	        			else
	        			{
	        				if(!saveDirectory.endsWith("/"))
	        				{
	        					saveDirectory = saveDirectory + "/";
	        				}
	        				path = saveDirectory + entry.getName();
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
    	    	JOptionPane.showMessageDialog(gui, "Error extracting file: " + fileName + " " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
    	    	SingletonFile.getInstance().write("Error extracting file: " + fileName + " " + e.getMessage());
    	        e.printStackTrace();
    	    }
            JOptionPane.showMessageDialog(gui,
                    "File " + fileName + " has been downloaded successfully!", "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private boolean IsPathDirectory(String path)
    {
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