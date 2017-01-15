package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Page
{
	String title;
	String url;
	ArrayList<FileVO> files;

	public Page()
	{
		title = "";
		url = "";
		files = new ArrayList<FileVO>();
	}

	public Page(String title, ArrayList<FileVO> files)
	{
		this.title = title;
		url = "";
		this.files = files;
	}

	public ArrayList<FileVO> getFiles()
	{
		return files;
	}

	public static ArrayList<Page> getPages()
	//public static void getPages()
	{
		ArrayList<Page> list = new ArrayList<Page>();
	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	    HttpPost post = new HttpPost("http://quantumc.at/getAllFiles.php");
	    post.addHeader("Content-type", "application/json");
	    HttpResponse response = null;
	    String responseString = "";
	    boolean connected = true;
	    try
	    {
	        response = httpClient.execute(post);
	        responseString = EntityUtils.toString(response.getEntity());
	    }
	    catch (Exception e)
	    {
	        connected = false;
	        e.printStackTrace();
	    }
	    if(connected)
	    {
	        JSONArray arr = new JSONArray(responseString);
	        try
	        {
	        	httpClient.close();
	        }
	        catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	        for(int i = 0; i < arr.length(); i ++)
            {
                JSONObject obj = arr.getJSONObject(i);
                JSONArray jsonfiles = obj.getJSONArray("files");
                ArrayList<FileVO> files = new ArrayList<FileVO>();
                for(int j = 0; j < jsonfiles.length(); j ++)
                {
                	files.add(new FileVO(jsonfiles.getJSONObject(j).getString("name"),
                			jsonfiles.getJSONObject(j).getString("file"),
                			jsonfiles.getJSONObject(j).getString("link"),
                			jsonfiles.getJSONObject(j).getString("page"),
                			jsonfiles.getJSONObject(j).getString("path"),
                			jsonfiles.getJSONObject(j).getString("region"),
                			jsonfiles.getJSONObject(j).getString("firmware"),
                			jsonfiles.getJSONObject(j).getString("type"),
                			jsonfiles.getJSONObject(j).getString("ts")));
                }
                list.add(new Page(obj.getString("title"), files));
            }
	    }
	    return list;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
