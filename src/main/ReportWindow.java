package main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class ReportWindow extends JPanel
{
	private static final long serialVersionUID = 7108898474166468466L;
	private JTextArea text;

	private void initialise()
	{
		JLabel label = new JLabel("Please describe the problem:");
		JPanel panel = new JPanel();
		panel.add(label);
		add(panel);
		text = new JTextArea();
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		add(text);
		JButton btn = new JButton("Submit");
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event)
        	{
        		try {
					report(event);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        });
		add(btn);
	}

	private void report(ActionEvent event) throws ClientProtocolException, IOException
	{
		System.out.println("submit button pressed");

		HttpClient client = HttpClientBuilder.create().build();
		File file = new File("log.txt");
		HttpPost post = new HttpPost("http://quantumc.at/report.php");
		FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
		StringBody stringBody1 = new StringBody("Message 1", ContentType.MULTIPART_FORM_DATA);
		StringBody stringBody2 = new StringBody("Message 2", ContentType.MULTIPART_FORM_DATA);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("upfile", fileBody);
		builder.addPart("text1", stringBody1);
		builder.addPart("text2", stringBody2);
		HttpEntity entity = builder.build();

		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		System.out.println(response.toString());
	}

	public ReportWindow()
    {
        initialise();
    }

}
