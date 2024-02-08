package ir.ac.um.guitestgenerating.ModelConstructor.FeatureRelationshipModel;


import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
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
    JBList activityList;
    JBList eventsList;
    JLabel activityLabel;
    JLabel eventLabel;
    JTextArea jTextAreaTestScenarioList;
    JTextArea jTextAreaCurrentTestScenario;
    JButton newScenarioButton;
    JButton appendScenarioButton;
    JButton addItemButton;
    JButton deleteLastScenarioButton;
    JButton deleteLastItemButton;
    JPanel  mainPanel;
    JPanel  topPanel;
    JPanel  topWestPanel;
    JPanel  topWestSubTopPanel;
    JBScrollPane  topWestSubCenterPanel;
    JPanel  topCenterPanel;
    JPanel  topCenterSubTopPanel;
    JBScrollPane topCenterSubCenterPanel;
    JPanel  topEastPanel;
    JBScrollPane  topSouthPanel;
    JBScrollPane  middlePanel;
    List<ActivityInformation> activitiesList;
    final DefaultListModel<String> activityNames;
    final DefaultListModel<String> eventNames;
    String selectedActivityName;
    String selectedEventName;
    String currentSenario;

    public  Window(){

           activityLabel                = new JLabel("Please select an appropriate activity");
           eventLabel                   = new JLabel("Please select an appropriate event handler:");
           activityNames                = new DefaultListModel<>();
           eventNames                   = new DefaultListModel<>();
           activityList                 = new JBList(activityNames);
           eventsList                   = new JBList(eventNames);
           jTextAreaTestScenarioList    = new JTextArea(10,21);
           jTextAreaCurrentTestScenario = new JTextArea(5,21);
           newScenarioButton            = new JButton("Generate New Test Scenario");
           appendScenarioButton         = new JButton("Append to Scenario List");
           addItemButton                = new JButton("Add Item into Current Scenario");
           deleteLastScenarioButton     = new JButton("Delete Last Scenario");
           deleteLastItemButton         = new JButton("Delete Last Item");
           mainPanel                    = new JPanel();
           topPanel                     = new JPanel();
           middlePanel                  = new JBScrollPane(jTextAreaTestScenarioList);
           topWestPanel                 = new JPanel();
           topWestSubTopPanel           = new JPanel();
           topWestSubCenterPanel        = new JBScrollPane();
           topCenterPanel               = new JPanel();
           topCenterSubTopPanel         = new JPanel();
           topCenterSubCenterPanel      = new JBScrollPane();
           topEastPanel                 = new JPanel();
           topSouthPanel                = new JBScrollPane(jTextAreaCurrentTestScenario);
//           activityListPanel            = new JBScrollPane(activityList);
//           eventsListPanel              = new JBScrollPane(eventsList);
           currentSenario               = "";
           initial();
    }

    private void initial() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(800,600));

        topPanel.setLayout(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(790,350));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

        middlePanel.setPreferredSize(new Dimension(790,200));
        middlePanel.setBorder(BorderFactory.createLineBorder(Color.GREEN,2));

        //topWestPanel.setLayout(new BoxLayout(topWestPanel,BoxLayout.Y_AXIS));
        topWestPanel.setLayout(new BorderLayout());
        topWestPanel.setPreferredSize(new Dimension(300,250));
        topWestPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

       // topCenterPanel.setLayout(new BoxLayout(topCenterPanel,BoxLayout.Y_AXIS));
        topCenterPanel.setLayout(new BorderLayout());
        topCenterPanel.setPreferredSize(new Dimension(300,250));
        topCenterPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

        topEastPanel.setLayout(new BoxLayout(topEastPanel,BoxLayout.Y_AXIS));
        topEastPanel.setPreferredSize(new Dimension(200,250));
        topEastPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

        //topSouthPanel.setLayout(new BoxLayout(topSouthPanel,BoxLayout.X_AXIS));
        topSouthPanel.setPreferredSize(new Dimension(200,150));
        topSouthPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

        middlePanel.setPreferredSize(new Dimension(800,250));
        middlePanel.setBorder(BorderFactory.createLineBorder(Color.GREEN,2));

//        activityListPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
//        eventsListPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        jTextAreaTestScenarioList.setFont(new Font("Times New Romans",Font.PLAIN,12));
        jTextAreaTestScenarioList.setEditable(false);
        jTextAreaTestScenarioList.setLineWrap(true);
        jTextAreaTestScenarioList.setWrapStyleWord(true);

        jTextAreaCurrentTestScenario.setFont(new Font("Times New Romans",Font.PLAIN,12));
        jTextAreaCurrentTestScenario.setEditable(false);
        jTextAreaCurrentTestScenario.setLineWrap(true);
        jTextAreaCurrentTestScenario.setWrapStyleWord(true);


        activityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        activityLabel.setFont(new Font("Times New Romans",Font.BOLD,12));

        eventLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        eventLabel.setFont(new Font("Times New Romans",Font.BOLD,12));

        newScenarioButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newScenarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 currentSenario = "";
                 jTextAreaCurrentTestScenario.setText("");
            }
        });

        appendScenarioButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        appendScenarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = jTextAreaTestScenarioList.getText();
                content += "\n< " + currentSenario + " >";
                jTextAreaTestScenarioList.setText(content);
            }
        });

        deleteLastScenarioButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteLastScenarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = jTextAreaTestScenarioList.getText();
                if(content.contains("\n"))
                   content = content.substring(0,content.lastIndexOf("\n"));
                else
                    content = "";
                jTextAreaTestScenarioList.setText(content);
            }
        });

        deleteLastItemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteLastItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentSenario.contains(",")){
                   currentSenario = currentSenario.substring(0,currentSenario.lastIndexOf(","));
                    jTextAreaCurrentTestScenario.setText("< " + currentSenario + " >");
                }
                else{
                    currentSenario = "";
                    jTextAreaCurrentTestScenario.setText("");

                }
            }
        });

        addItemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(currentSenario.isEmpty())
                    currentSenario += selectedActivityName + "." + selectedEventName;
                else
                    currentSenario += ", " + selectedActivityName + "." + selectedEventName;
                jTextAreaCurrentTestScenario.setText("< " + currentSenario + " >");
            }
        });

    }

    private String createLayout(List<ActivityInformation> activitiesList){
        this.activitiesList = activitiesList;
        updateActivityNames(activitiesList);
        activityList = new JBList(activityNames);
        topWestSubTopPanel.add(activityLabel);
        topWestSubTopPanel.setBackground(Color.GRAY);
        topWestPanel.add(topWestSubTopPanel,BorderLayout.NORTH);
        topWestPanel.add(topWestSubCenterPanel,BorderLayout.CENTER);
        topWestSubCenterPanel.getViewport().setView(activityList);
        activityList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()){
                    JBList source = (JBList)e.getSource();
                    if(!source.isEmpty()){
                        selectedActivityName = (String) source.getSelectedValue();
                       updateEventsList(selectedActivityName);
                    }
                }
            }
        });

        eventsList   = new JBList(eventNames);
        topCenterSubTopPanel.add(eventLabel);
        topCenterSubTopPanel.setBackground(Color.gray);
        topCenterPanel.add(topCenterSubTopPanel,BorderLayout.NORTH);
        topCenterPanel.add(topCenterSubCenterPanel,BorderLayout.CENTER);
        topCenterSubCenterPanel.getViewport().setView(eventsList);
        eventsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()){
                    JBList source = (JBList)e.getSource();
                    if(!source.isEmpty())
                       selectedEventName = (String) source.getSelectedValue();
                }
            }
        });

        topEastPanel.add(newScenarioButton);
        topEastPanel.add(deleteLastScenarioButton);
        topEastPanel.add(appendScenarioButton);
        topEastPanel.add(addItemButton);
        topEastPanel.add(deleteLastItemButton);

        //topSouthPanel.add(jTextAreaCurrentTestScenario);

        topPanel.add(topWestPanel,BorderLayout.WEST);
        topPanel.add(topCenterPanel,BorderLayout.CENTER);
        topPanel.add(topEastPanel,BorderLayout.EAST);
        topPanel.add(topSouthPanel,BorderLayout.SOUTH);

        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(middlePanel,BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, mainPanel,
                "Create complex test scenarios", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if(result == JOptionPane.YES_OPTION)
           return jTextAreaTestScenarioList.getText();
        return "";
    }

    private void updateEventsList(String selectedActivityName) {
        ActivityInformation activityName = findActivityByName(activitiesList,selectedActivityName);
        List<EventHandlerInformation> events = activityName.getMainFeaturesList();
        eventNames.clear();
        for(EventHandlerInformation event:events)
            eventNames.addElement(event.getTitle());
    }

    private static ActivityInformation findActivityByName(List<ActivityInformation> activitiesList,String selectedItem) {
        for(ActivityInformation activity : activitiesList)
            if(activity.getActivityName().equals(selectedItem))
                return activity;
        return null;
    }

    private  void updateActivityNames(List<ActivityInformation> activitiesList) {
        for(ActivityInformation activityInformation:activitiesList)
            activityNames.addElement(activityInformation.getActivityName());
    }



    public String getTestSenarios(List<ActivityInformation> activityInformations){
        return createLayout(activityInformations);
    }

}
