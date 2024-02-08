package ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel;

import javax.swing.*;
import java.awt.*;

public class ParameterConfigurationWindow {
    private JLabel   mainLabel;
    private JLabel   alphaLabel;
    private JLabel   betaLabel;
    private JLabel   ghamaLabel;
    private JLabel   thetaLabel;
    private JLabel   thresholdLabel;
    private JSlider  alphaSlier;
    private JSlider  betaSlider;
    private JSlider  ghamaSlider;
    private JSlider  thetaSlider;
    private JSlider  thresholdSlider;
    private JButton  yesButton;
    private double   alphaValue;
    private double   bethaValue;
    private double   ghamaValue;
    private double   thetaValue;
    private double   thresholdValue;
    private JPanel   mainPanel;
    private JPanel   topPanel;
    private JPanel   centerPanel;
    private JPanel   buttomPanel;


    public ParameterConfigurationWindow(){
        mainLabel         = new JLabel("Please set the parameters:");
        alphaLabel        = new JLabel("Alpa :");
        betaLabel         = new JLabel("Beta :");
        ghamaLabel        = new JLabel("Ghama :");
        thetaLabel        = new JLabel("Theta :");
        thresholdLabel    = new JLabel("Threshold");
        alphaSlier        = new JSlider(JSlider.HORIZONTAL,0,100,50);
        betaSlider        = new JSlider(JSlider.HORIZONTAL,0,100,50);
        ghamaSlider       = new JSlider(JSlider.HORIZONTAL,0,100,50);
        thetaSlider       = new JSlider(JSlider.HORIZONTAL,0,100,50);
        thresholdSlider   = new JSlider(JSlider.HORIZONTAL,0,100,50);
        mainPanel         = new JPanel();
        topPanel          = new JPanel();
        centerPanel       = new JPanel();
        buttomPanel       = new JPanel();
        alphaValue = 0.5;
        bethaValue = 0.5;
        ghamaValue = 0.5;
        thetaValue = 0.5;
        thresholdValue = 0.2;
        initial();
    }

    private void initial() {

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setSize(new Dimension(500,300));
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.white,2));

        topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.X_AXIS));
        centerPanel.setLayout(new GridLayout(5,2));
        buttomPanel.setLayout(new BoxLayout(buttomPanel,BoxLayout.X_AXIS));

        mainLabel.setFont(new Font("Times New Roman",Font.BOLD,14));
        alphaLabel.setFont(new Font("Times New Roman",Font.PLAIN,12));
        betaLabel.setFont(new Font("Times New Roman",Font.PLAIN,12));
        ghamaLabel.setFont(new Font("Times New Roman",Font.PLAIN,12));
        thetaLabel.setFont(new Font("Times New Roman",Font.PLAIN,12));
        thresholdLabel.setFont(new Font("Times New Roman",Font.PLAIN,12));

    }

    public void createLayout(){
        topPanel.add(mainLabel);
        centerPanel.add(alphaLabel);
        centerPanel.add(alphaSlier);
        centerPanel.add(betaLabel);
        centerPanel.add(betaSlider);
        centerPanel.add(ghamaLabel);
        centerPanel.add(ghamaSlider);
        centerPanel.add(thetaLabel);
        centerPanel.add(thetaSlider);
        centerPanel.add(thresholdLabel);
        centerPanel.add(thresholdSlider);

        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(centerPanel,BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, mainPanel,
                "Create complex test scenarios", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
       alphaValue = (double) alphaSlier.getValue()/100;
       bethaValue = (double) betaSlider.getValue()/100;
       ghamaValue = (double) ghamaSlider.getValue()/100;
       thetaValue = (double) thetaSlider.getValue()/100;
       thresholdValue = (double) thresholdSlider.getValue()/100;
    }

    public double getAlphaValue(){
        return alphaValue;
    }

    public double getBethaValue(){
        return bethaValue;
    }

    public double getGhamaValue(){
        return ghamaValue;
    }

    public double getThetaValue(){
        return thetaValue;
    }

    public double getThresholdValue(){
        return thresholdValue;
    }

}
