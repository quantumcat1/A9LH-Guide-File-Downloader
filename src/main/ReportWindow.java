package main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;

public class ReportWindow extends JPanel
{
	private static final long serialVersionUID = 7108898474166468466L;
	private JTextArea message;
	private JTextField contact;
	private JLabel status;

	private void initialise()
	{
		JLabel label1 = new JLabel("Enter your GBATemp ID if you'd like a response:");
		JPanel panel1 = new JPanel();
		panel1.add(label1);
		add(panel1);
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
    			new Thread()
    			{
    				public void run()
    				{
    					try
    					{
							report(event);
						}
    					catch (Exception e)
    					{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    			}.start();
        	}
        });
		add(btn);
		status = new JLabel();
		status.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(status);
	}

	private void report(ActionEvent event)
	{
		String code = "Please enter a message.";
		if(!message.getText().trim().equals(""))
		{
			code = "Sending...";
		}
		final String thing1 = code;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				status.setText(thing1);
			}
		});
		if(message.getText().trim().equals("")) return; //don't send a message if message field is blank

		HttpClient client = HttpClientBuilder.create().build();
		File file = new File("log.txt");
		HttpPost post = new HttpPost("http://quantumc.at/report.php");
		FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);

		String c = contact.getText().trim().length() > 50 ? contact.getText().substring(0, 50) : contact.getText();
		StringBody contactBody = new StringBody(c, ContentType.MULTIPART_FORM_DATA);
		String m = message.getText().trim().length() > 1000 ? message.getText().substring(0, 1000) : message.getText();
		StringBody messageBody = new StringBody(m, ContentType.MULTIPART_FORM_DATA);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("upfile", fileBody);
		builder.addPart("contact", contactBody);
		builder.addPart("message", messageBody);
		HttpEntity entity = builder.build();

		post.setEntity(entity);

		HttpResponse response = null;
		code = "Mail was not sent";

		try
		{
			response = client.execute(post);
			if(response.getStatusLine().getStatusCode() == 200)
			{
				code = "Mail sent successfully";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


		final String thing = code;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				status.setText(thing);
			}
		});
	}

	public ReportWindow()
    {
        initialise();
    }

}
