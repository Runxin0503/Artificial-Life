package Screens;

import Constants.Constants.*;
import Entity.*;
import Entity.Movable.*;
import Entity.Immovable.*;
import World.World;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.function.Supplier;

public class sidePanel extends JPanel {
    private HashMap<JLabel,Supplier<String>> dataVals;
    private World world;
    public sidePanel(World world){
        super();
        setBackground(Color.LIGHT_GRAY);
        setLayout(new GridBagLayout());
        this.world = world;
        this.dataVals=new HashMap<JLabel,Supplier<String>>();
    }

    public void displayData(Entity e){
        dataVals.clear();
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx=0;
        gbc.gridy=0;
        if(e instanceof Corpse)addCorpseComponents((Corpse)e,gbc);
        else if(e instanceof Creature)addCreatureComponents((Creature)e,gbc);
        else if(e instanceof Bush)addBushComponents((Bush)e,gbc);
        else if(e instanceof Egg)addEggComponents((Egg)e,gbc);
        revalidate();
        repaint();
    }
    private void addCorpseComponents(Corpse c,GridBagConstraints gbc){

        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"Coordinate   "+c.getCoord().x+","+c.getCoord().y,gbc);
        gbc.gridy++;
        addLabel(()->"Velocity   "+(int)(c.getVelocity()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Energy | Max Energy   "+(int)(c.getEnergy()*100)/100.0+","+ (int)(c.getInitialEnergy()*100)/100.0,gbc);
        gbc.gridx++;
        addButton("+",()->c.setEnergy(c.getEnergy()+1),gbc);
        gbc.gridx++;
        addButton("-",()->c.setEnergy(c.getEnergy()-1),gbc);

        gbc.gridx=0;
        gbc.gridy++;
        addButton("Rot",()->c.setEnergy(-1),gbc);
    }
    private void addCreatureComponents(Creature c,GridBagConstraints gbc){
        CreatureGenome g = c.getGenome();
        addLabel(()->"Vision Range   "+(int)(g.getVisionRange()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Min Size | Max Size   "+(int)(g.minSize*100)/100.0+","+ (int)(g.maxSize*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Size   "+(int)(c.getSize()*100)/100.0,gbc);
        gbc.gridx++;
        addButton("+",()->c.setSize(c.getSize()+1, ImageConstants.bird),gbc);
        gbc.gridx++;
        addButton("-",()->c.setSize(c.getSize()-1, ImageConstants.bird),gbc);

        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"Energy | Max Energy   "+(int)(c.getEnergy()*100)/100.0+","+ (int)c.getMaxEnergy(),gbc);
        gbc.gridx++;
        addButton("+",()->c.setEnergy(c.getEnergy()+1),gbc);
        gbc.gridx++;
        addButton("-",()->c.setEnergy(c.getEnergy()-1),gbc);
        gbc.gridx=0;

        gbc.gridy++;
        addLabel(()->"Health | Max Health   "+(int)(c.getHealth()*100)/100.0+","+ (int)c.getMaxHealth(),gbc);
        gbc.gridx++;
        addButton("+",()->c.setHealth(c.getHealth()+1),gbc);
        gbc.gridx++;
        addButton("-",()->c.setHealth(c.getHealth()-1),gbc);

        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"Coordinate   "+c.getCoord().x+","+c.getCoord().y,gbc);
        gbc.gridy++;
        addLabel(()->"Force   "+(int)(c.getForce()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Velocity   "+(int)(c.getVelocityVector().x*100)/100.0+","+(int)(c.getVelocityVector().y*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Rotation | Angular Vel   "+(int)(c.getRotation()*100)/100.0+","+(int)(c.getAngularSpeed()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Strength   "+(int)(c.getDamage()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Armour   "+(int)(c.getArmour()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Herbivore   "+(int)(g.herbivoryAffinity*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Carnivore   "+(int)(g.carnivoryAffinity*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Metabolism   "+(int)(c.getMetabolism()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Stomach %   "+(int)((c.getPlantMass()+c.getMeatMass())/c.getStomachSize()*10000)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Plant Mass | Meat Mass   "+(int)(c.getPlantMass()*100)/100.0+","+(int)(c.getMeatMass()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Stomach Size   "+(int)(c.getStomachSize()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Digestion Rate   "+(int)(c.getStomachFluid()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Starving?   "+c.isStarving(),gbc);
        gbc.gridy++;
        addLabel(()->"Seeking Mate?   "+c.isSeekingMate(),gbc);
        gbc.gridy++;
        addLabel(()->"Offspring Investment   "+(int)(g.offspringInvestment*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"~Reproduction Cost   "+(int)(g.getReproductionCost()*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"Maturity   "+c.getMaturity(),gbc);
        gbc.gridy++;
        addLabel(()->"BOID - Separation | Alignment | Cohesion   ",gbc);
        gbc.gridy++;
        addLabel(()->(int)(g.boidSeparationWeight*100)/100.0+" | "+(int)(g.boidAlignmentWeight)/100.0+" | "+(int)(g.boidCohesionWeight*100)/100.0,gbc);
        gbc.gridy++;
        addLabel(()->"isPlayer - "+c.isPlayer,gbc);
        gbc.gridy++;
        addButton("Mate",()->world.Eggs.add(new Egg(c.mate(c),c.getCoord().x-Math.cos(c.getRotation())*c.getSize()+g.minSize/2,c.getCoord().y-Math.sin(c.getRotation())*(c.getSize()+g.minSize/2))),gbc);
        gbc.gridy++;
        addButton("Death",()->c.setHealth(-100),gbc);
    }
    private void addBushComponents(Bush b,GridBagConstraints gbc){
        addLabel(()->"Size   "+b.getSize(),gbc);
        gbc.gridy++;
        addButton("+",()->b.setSize(b.getSize()+1,null),gbc);
        gbc.gridx++;
        addButton("-",()->b.setSize(b.getSize()-1,null),gbc);

        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"Berries | Max Berries   "+b.getBerries().size()+","+b.getMaxBerries(),gbc);
        gbc.gridy++;
        addButton("+", b::growBerry,gbc);
        gbc.gridx++;
        addButton("-",()->b.creatureInteract(null),gbc);

        gbc.gridx=0;
        gbc.gridy++;
        addLabel(()->"Stored Energy   "+b.getEnergyIfConsumed(),gbc);
    }
    private void addEggComponents(Egg egg,GridBagConstraints gbc){
        addLabel(()->"Incubation Time   "+egg.getTimeLeft(),gbc);
        gbc.gridy++;
        addButton("Hatch",()->egg.setInvIncubationTime(0),gbc);
    }
    public void updateVariables(){
        for(JLabel jl : dataVals.keySet()){
            jl.setText(dataVals.get(jl).get());
        }
    }

    private void addButton(String name,Runnable func,GridBagConstraints gbc){
        JButton button = new JButton(name);
        button.addActionListener(e -> func.run());
        add(button,gbc);
    }
    private void addLabel(Supplier<String> iNeedToLearnLambdaStuff, GridBagConstraints gbc){
        JLabel label = new JLabel(iNeedToLearnLambdaStuff.get());
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        add(label,gbc);
        dataVals.put(label,iNeedToLearnLambdaStuff);
    }
}
