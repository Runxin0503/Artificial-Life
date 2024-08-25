package Screens;

import Constants.Constants.*;
import Constants.Constants.WindowConstants;
import Entity.Immovable.Bush;
import World.World;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.function.IntConsumer;
import java.util.function.Supplier;


public class Settings extends JDialog {
    private HashMap<JLabel,Supplier<String>> dataVals;
    public Settings(Frame owner, String title, boolean modal, World world){
        super(owner,title,modal);
        setSize(WindowConstants.settingsWidth,WindowConstants.settingsHeight);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(this);

        this.dataVals = new HashMap<JLabel,Supplier<String>>();
        setup(world);
    }

    private void setup(World world){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx=0;
        gbc.gridy=0;

        addButton("Skip 5 Minutes",()->{world.fastForward(WorldConstants.Settings.ticksToSecond*60*5);},gbc);
        gbc.gridx++;
        addButton("Skip 30 Minutes",()->{world.fastForward(WorldConstants.Settings.ticksToSecond*60*30);},gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addButton("Skip 1 Hour",()->{world.fastForward(WorldConstants.Settings.ticksToSecond*3600);},gbc);
        gbc.gridx++;
        addButton("Skip 1 Day",()->{world.fastForward(WorldConstants.Settings.ticksToSecond*3600*24);},gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addLabel(()->"# Split Nodes   "+World.globalInnovations.splitNode.size(),gbc);
        gbc.gridy++;
        addLabel(()->"# Innovation Nodes   "+World.globalNodes.size(),gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"# Innovation Synapse   "+World.globalInnovations.size(),gbc);

        gbc.gridy++;
        addLabel(()->"# Creature   "+world.Creatures.size(),gbc);
        gbc.gridx++;
        addLabel(()->"# Egg   "+world.Eggs.size(),gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addLabel(()->"ticksPerSec "+WorldConstants.Settings.ticksPerSec,gbc);
        gbc.gridx++;
        addLabel(()->"Population "+WorldConstants.WorldGen.startingPopulation,gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addSlider(-1,999, WorldConstants.Settings.ticksPerSec,(int val)->{WorldConstants.Settings.ticksPerSec=val;},gbc);
        gbc.gridx++;
        addSlider(0,500, WorldConstants.WorldGen.startingPopulation,(int val)->{WorldConstants.WorldGen.startingPopulation=val;},gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addLabel(()->"Natural Spawning "+WorldConstants.WorldGen.naturalSpawningThreshold + " --- " + (world.Creatures.size()+world.Eggs.size()),gbc);
        gbc.gridx++;
        addLabel(()->"Berry Energy "+BushConstants.energy,gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addSlider(0,500, WorldConstants.WorldGen.naturalSpawningThreshold,(int val)->{WorldConstants.WorldGen.naturalSpawningThreshold=val;},gbc);
        gbc.gridx++;
        addSlider(0,2000, (int)(BushConstants.energy*100),(int val)->{BushConstants.energy=val/100.0;},gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"Max Berries "+BushConstants.maxBerries,gbc);
        gbc.gridx++;
        addLabel(()->"Herding Speed "+(int)(CreatureConstants.Boids.minHerdingSpeed*100)/100.0,gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addSlider(0,80, BushConstants.maxBerries,(int val)->{BushConstants.maxBerries=val;},gbc);
        gbc.gridx++;
        addSlider(0,3000, (int)(CreatureConstants.Boids.minHerdingSpeed*100),(int val)->{CreatureConstants.Boids.minHerdingSpeed=val*0.01;},gbc);
        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"# Bushes "+world.Bushes.size(),gbc);
        gbc.gridx++;
        addButtonPlusMinus(()->{int bushMaxWidth = BushConstants.initialMaxSize*ImageConstants.bushWidth/20;
            int bushMaxHeight = BushConstants.initialMaxSize*ImageConstants.bushHeight/20;
            int bushBoundx = WorldConstants.xBound-2*bushMaxWidth;
            int bushBoundy = WorldConstants.yBound-2*bushMaxHeight;
            for(int i=0;i<100;i++){
                Point coord = new Point((int) (Math.random() * bushBoundx + bushMaxWidth), (int) (Math.random() * bushBoundy + bushMaxHeight));
                boolean tooClose = false;
                for(Bush b : world.Bushes){
                    if(coord.distance(b.getX(),b.getY())<WorldConstants.WorldGen.bushRadius){
                        tooClose = true;
                        break;
                    }
                }
                if(!tooClose){
                    world.add.add(new Bush(coord));
                    return;
                }
            }},()->{
            world.Bushes.remove(world.Bushes.size()-1);},gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addLabel(()->"Dev Mode "+(WorldConstants.Settings.devMode?"On":"Off"),gbc);
        gbc.gridx++;
        addButton("Toggle",()->{WorldConstants.Settings.devMode=!WorldConstants.Settings.devMode;},gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addLabel(()->"Spawn Corpse "+(WorldConstants.WorldGen.spawnCorpse?"On":"Off"),gbc);
        gbc.gridx++;
        addButton("Toggle",()->{WorldConstants.WorldGen.spawnCorpse=!WorldConstants.WorldGen.spawnCorpse;},gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addButton("Close",()->setVisible(false),gbc);
    }

    private void addButton(String name,Runnable func,GridBagConstraints gbc){
        JButton button = new JButton(name);
        button.addActionListener(e -> func.run());
        add(button,gbc);
    }
    private void addButtonPlusMinus(Runnable funcAdd,Runnable funcSubtract,GridBagConstraints gbc){
        JButton button = new JButton("+/-");
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton()==MouseEvent.BUTTON1)funcAdd.run();
                else if(e.getButton()==MouseEvent.BUTTON3)funcSubtract.run();
            }
        });
        add(button,gbc);
    }
    private void addLabel(Supplier<String> iNeedToLearnLambdaStuff, GridBagConstraints gbc){
        JLabel label = new JLabel(iNeedToLearnLambdaStuff.get());
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        add(label,gbc);
        dataVals.put(label,iNeedToLearnLambdaStuff);
    }
    private void addSlider(int min, int max, int initVal, IntConsumer whatisthis,GridBagConstraints gbc){
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initVal);
        slider.setMajorTickSpacing((max-min)/10);
        slider.setFont(new Font("Arial", Font.PLAIN, 10));
        slider.setMinorTickSpacing((max-min)/50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension((int)(WindowConstants.settingsWidth*0.6),slider.getPreferredSize().height));
        slider.addChangeListener(e -> whatisthis.accept(slider.getValue()));
        add(slider,gbc);
    }
    public void updateVariables(){
        for(JLabel jl : dataVals.keySet()){
            jl.setText(dataVals.get(jl).get());
        }
    }
}
