package main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class StatusWindow extends JPanel

{
    private static final long serialVersionUID = 3136350037469268319L;
    List<ProgressPanel> progressPanels;
    private JPanel panelOfPanels;
    //JLabel label;
    public void initialise()
    {
        //label = new JLabel("0%");
        //add(label);
        progressPanels = new ArrayList<ProgressPanel>();
        panelOfPanels = new JPanel();
        add(panelOfPanels);
    }

    public StatusWindow()
    {
        initialise();
    }

    public void setFileInfo(String fileName, int contentLength)
    {

    }

    public ProgressPanel addNew(String name)
    {
        if(name.trim().equals("")) return null;
        ProgressPanel pp = new ProgressPanel(name);
        progressPanels.add(pp);
        panelOfPanels.add(pp.getPanel());
        revalidate();
        repaint();
        return pp;
    }

    public ProgressPanel getPanel(String name)
    {
        for(ProgressPanel pp : progressPanels)
        {
            if(pp.getName().equals(name))
            {
                return pp;
            }
        }
        return null;
    }

    public void addProgress(String name, int progress)
    {
        ProgressPanel pp = getPanel(name);
        pp.addProgress(progress);
    }


}
