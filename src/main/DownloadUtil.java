package main;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A utility that downloads a file from a URL.
 *
 * @author www.codejava.net
 *
 */
public class DownloadUtil {

    private HttpURLConnection httpConn;

    /**
     * hold input stream of HttpURLConnection
     */
    private InputStream inputStream;

    private String fileName;
    private int contentLength;

    /**
     * Downloads a file from a URL
     *
     * @param fileURL
     *            HTTP URL of the file to be downloaded
     * @throws IOException
     */
    public void downloadFile(String fileURL) throws IOException 
    {
        URL url = new URL(fileURL);
        httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        boolean redirect = false;
        if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
        		|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
        		|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
        {
        	redirect = true;
        }

        if(redirect)
        {
        	String newUrl = httpConn.getHeaderField("Location");
        	httpConn = (HttpURLConnection) new URL(newUrl).openConnection();
        	responseCode = httpConn.getResponseCode();
        }




        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 9,
                            disposition.length());
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            // output for debugging purpose only
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            inputStream = httpConn.getInputStream();

        } else {
            throw new IOException(
                    "No file to download. Server replied HTTP code: "
                            + responseCode);
        }
    }

    public void disconnect() throws IOException {
        inputStream.close();
        httpConn.disconnect();
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }
}