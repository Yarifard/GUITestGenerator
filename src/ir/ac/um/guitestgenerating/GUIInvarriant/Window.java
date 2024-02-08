package ir.ac.um.guitestgenerating.GUIInvarriant;


import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import ir.ac.um.guitestgenerating.Database.Invariant;
import ir.ac.um.guitestgenerating.ModelConstructor.Analysis.Activity.ActivityInformation;
import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Window {
    private static final int PostCondition = 1;
    private static final int PreCondittion = 0;
    private JPanel  mainPanel;
    private JPanel  titlePanel;
    private JPanel  contentPanel;
    private JBScrollPane listPanel;
    private JBList itemList;
    private JLabel mainTitle;
    private JLabel activityTitle;
    private JLabel eventTitle;
    final DefaultListModel<String> items;

    public Window(){

        items                        = new DefaultListModel<>();
        mainPanel                    = new JPanel();
        titlePanel                   = new JPanel();
        contentPanel                 = new JPanel();
        mainTitle                    = new JLabel();
        activityTitle                = new JLabel();
        eventTitle                   = new JLabel();
        listPanel                    = new JBScrollPane();
        initial();
    }

    private void initial() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1000,500));

        titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.Y_AXIS));
        titlePanel.setPreferredSize(new Dimension(1000,100));
        titlePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

        contentPanel.setPreferredSize(new Dimension(1000,400));
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.white,2));

        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainTitle.setFont(new Font("Times New Romans",Font.BOLD,16));

        activityTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        activityTitle.setFont(new Font("Times New Romans",Font.BOLD,14));

        eventTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        eventTitle.setFont(new Font("Times New Romans",Font.BOLD,12));


    }

    public int createLayout(String activityName,String eventName,List<Invariant> invariantList, String patterns, int type){
        String msg = initialWindowTitle(type);
        fillInvariantItems(invariantList);
        itemList = new JBList(items);

        mainTitle.setText(patterns);
        activityTitle.setText("ActivityName is: " + activityName);
        eventTitle.setText("EventName is: " + eventName);
        titlePanel.add(mainTitle);
        titlePanel.add(activityTitle);
        titlePanel.add(eventTitle);

        contentPanel.add(listPanel);
        listPanel.getViewport().setView(itemList);


        mainPanel.add(titlePanel,BorderLayout.NORTH);
        mainPanel.add(contentPanel,BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, mainPanel,
                msg, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if(result == JOptionPane.YES_OPTION)
            return itemList.getSelectedIndex() + 1;
        else if(result == JOptionPane.CANCEL_OPTION)
            return 0;
        return -2;
    }

    private String initialWindowTitle(int type) {
        if(isPostCondition(type))
            return "Select invariant for Exit point:";
        return "Select invariant for entry point";
    }

    private boolean isPostCondition(int type) {
        if(PostCondition == type)
            return true;
        return false;
    }

    private void fillInvariantItems(List<Invariant> invariantList){
        for(Invariant invariant : invariantList)
            items.addElement(invariant.getInvariant());
    }


}
