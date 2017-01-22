package main;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class ReportWindow extends JPanel
{
	private static final long serialVersionUID = 7108898474166468466L;

	private void initialise()
	{
		JLabel label = new JLabel("Please describe the problem:");
		//label.setPreferredSize(new Dimension(10, 50));
		//label.setMaximumSize(new Dimension(200, 50));
		JPanel panel = new JPanel();
		panel.add(label);
		add(panel);
		JTextArea text = new JTextArea();
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		add(text);
		JButton btn = new JButton("Submit");
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(btn);
	}

	public ReportWindow()
    {
        initialise();
    }

}
