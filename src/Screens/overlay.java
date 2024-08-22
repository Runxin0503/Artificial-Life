package Screens;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.function.Supplier;

import World.*;

public class overlay extends JPanel {
    private JPanel timeDisplay;
    private final World world;
    private final HashMap<JLabel,Supplier<String>> dataVals;

    public overlay(World world){
        super();
        this.world = world;
        this.dataVals=new HashMap<JLabel,Supplier<String>>();
        createTimeDisplay();
        setLayout(null);
        setOpaque(false);
        setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g){
        updateVariables();
        super.paintComponent(g);
        timeDisplay.repaint();
    }

    private void createTimeDisplay(){
        timeDisplay = new JPanel(){{setVisible(true);setLayout(new GridBagLayout());setOpaque(true);setBorder(BorderFactory.createEtchedBorder());}};
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1,1,1,1);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy=0;
        addLabel(timeDisplay,()->"Days: "+world.day,gbc);
        gbc.gridy++;
        addLabel(timeDisplay,()->"Hours: "+world.hour,gbc);
        gbc.gridy++;
        addLabel(timeDisplay,()->"Minutes: "+world.minute,gbc);
        gbc.gridy++;
        addLabel(timeDisplay,()->"Seconds: "+world.second,gbc);
        timeDisplay.setBounds(0,0,100,100);
        add(timeDisplay);
    }

    public void updateVariables(){
        for(JLabel jl : dataVals.keySet()){
            jl.setText(dataVals.get(jl).get());
        }
    }
    private void addLabel(JPanel target,Supplier<String> iNeedToLearnLambdaStuff, GridBagConstraints gbc){
        JLabel label = new JLabel(iNeedToLearnLambdaStuff.get());
        target.add(label,gbc);
        dataVals.put(label,iNeedToLearnLambdaStuff);
    }
}
