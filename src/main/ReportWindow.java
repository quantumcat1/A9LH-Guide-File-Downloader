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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
	private JTextArea message;
	//private JTextArea contact;
	private JTextField contact;

	private void initialise()
	{
		/*JPanel panel1 = new JPanel();
		JLabel label1 = new JLabel("Your email or GBATemp ID if you'd like a response:");
		panel1.add(label1);
		contact = new JTextArea();
		panel1.add(contact);//scroll pane is just in case they try to put in lots of characters, so it won't warp the window
		add(panel1);*/
		JLabel label1 = new JLabel("Your email or GBATemp ID if you'd like a response:");
		JPanel panel1 = new JPanel();
		panel1.add(label1);
		add(panel1);
		//contact = new JTextArea(1, 1);
		contact = new JTextField();
		contact.setMaximumSize(new Dimension(600, 100));
		add(contact);

		JLabel label = new JLabel("Please describe the problem:");
		JPanel panel = new JPanel();
		panel.add(label);
		add(panel);
		message = new JTextArea();
		message.setWrapStyleWord(true);
		message.setLineWrap(true);
		add(message);
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
		HttpClient client = HttpClientBuilder.create().build();
		File file = new File("log.txt");
		HttpPost post = new HttpPost("http://quantumc.at/report.php");
		FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);

		String c = contact.getText().length() > 50 ? contact.getText().substring(0, 50) : contact.getText();
		StringBody contactBody = new StringBody(c, ContentType.MULTIPART_FORM_DATA);
		String m = message.getText().length() > 1000 ? message.getText().substring(0, 1000) : message.getText();
		StringBody messageBody = new StringBody(m, ContentType.MULTIPART_FORM_DATA);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("upfile", fileBody);
		builder.addPart("contact", contactBody);
		builder.addPart("message", messageBody);
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
