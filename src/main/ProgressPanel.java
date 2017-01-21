package main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressPanel implements PropertyChangeListener
{
    private JPanel panel;
    private JLabel label;
    private JProgressBar progress;

    public ProgressPanel()
    {
        initialise();
    }

    public ProgressPanel(String label)
    {
        initialise();
        this.label.setText(label);
    }

    private void initialise()
    {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        label = new JLabel();
        progress = new JProgressBar(0, 100);
        panel.add(label);
        panel.add(progress);
    }

    public JPanel getPanel()
    {
        return panel;
    }

    public String getName()
    {
        return label.getText();
    }

    public void setName(String name)
    {
        label.setText(name);
    }

    public void addProgress(int progress)
    {
        this.progress.setValue(progress);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
    	if(evt.getPropertyName().equals("progress"))
    	{
    		int progress = (Integer)evt.getNewValue();
    		this.progress.setValue(progress);
    	}
    }
}
