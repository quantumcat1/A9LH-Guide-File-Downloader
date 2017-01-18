package main;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

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

import static java.util.concurrent.TimeUnit.*;

public class Page {
	String title;
	String url;
	ArrayList<FileVO> files;

	public Page() {
		title = "";
		url = "";
		files = new ArrayList<FileVO>();
	}

	public Page(String title, ArrayList<FileVO> files) {
		this.title = title;
		url = "";
		this.files = files;
	}

	public ArrayList<FileVO> getFiles() {
		return files;
	}

	public static ArrayList<Page> getPages() throws ParseException
	// public static void getPages()
	{
		ArrayList<Page> list = new ArrayList<Page>();
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://quantumc.at/getAllFiles.php");
		post.addHeader("Content-type", "application/json");
		HttpResponse response = null;
		String responseString = "";
		boolean connected = true;
		try {
			response = httpClient.execute(post);
			responseString = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			connected = false;
			e.printStackTrace();
		}
		if (connected) {
			JSONArray arr = new JSONArray(responseString);
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date mostRecentTS = sdf.parse("2001-01-01 00:00:00");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				JSONArray jsonfiles = obj.getJSONArray("files");
				ArrayList<FileVO> files = new ArrayList<FileVO>();

				for (int j = 0; j < jsonfiles.length(); j++) {
					String ts = jsonfiles.getJSONObject(j).getString("ts");
					if (sdf.parse(ts).after(mostRecentTS)) {
						mostRecentTS = sdf.parse(ts);
					}

					files.add(new FileVO(jsonfiles.getJSONObject(j).getString("name"),
							jsonfiles.getJSONObject(j).getString("file"), jsonfiles.getJSONObject(j).getString("link"),
							jsonfiles.getJSONObject(j).getString("page"), jsonfiles.getJSONObject(j).getString("path"),
							jsonfiles.getJSONObject(j).getString("region"),
							jsonfiles.getJSONObject(j).getString("firmware"),
							jsonfiles.getJSONObject(j).getString("type"), jsonfiles.getJSONObject(j).getString("ts")));
				}
				list.add(new Page(obj.getString("title"), files));
			}
			// now cut out ones that are too old
			Iterator<Page> p_iter = list.iterator();

			long max_duration = MILLISECONDS.convert(30, MINUTES);
			// for(Page p : list)
			while (p_iter.hasNext())
			{
				Page p = p_iter.next();
				// for(FileVO f : p.files)
				Iterator<FileVO> f_iter = p.files.iterator();

				while (f_iter.hasNext())
				{
					FileVO f = f_iter.next();
					Date ts = sdf.parse(f.ts);
					long duration = mostRecentTS.getTime() - ts.getTime();

					if (duration >= max_duration)
					{
						f_iter.remove();
					}
				}
				if (p.files.isEmpty())
				{
					// list.remove(p);
					p_iter.remove();
				}
			}
		}
		return list;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

}
