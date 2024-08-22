package Screens;

import Entity.Movable.Creature;
import Entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PopupMenu extends JPopupMenu {
    public HashMap<String,ArrayList<JMenuItem>> packages;
    private GenomeFrame genomeFrame;
    public PopupMenu(){
        super();
        this.packages = new HashMap<String,ArrayList<JMenuItem>>();
        packages.put("defaultPackage",new ArrayList<JMenuItem>());
        genomeFrame=new GenomeFrame(){{setVisible(false);}};
    }

    public void add(String packageName,JMenuItem menuItem){
        if(packages.containsKey(packageName))packages.get(packageName).add(menuItem);
        else{
            packages.put(packageName,new ArrayList<JMenuItem>());
            packages.get(packageName).add(menuItem);
        }
    }

    public void show(Component invoker, int x, int y,String... packageNames){
        super.removeAll();
        for(JMenuItem menuItem : packages.get("defaultPackage"))super.add(menuItem);
        for(String packageName : packageNames)for(JMenuItem menuItem: packages.get(packageName))super.add(menuItem);
        super.show(invoker,x,y);
    }

    public void viewGenome(Entity e){
        if(e instanceof Creature && ((Creature)e).getNN()!=null){
            genomeFrame.setGenome(((Creature)e).getNN());
        }
        genomeFrame.setVisible(true);
    }

    @Override
    public void show(Component invoker, int x, int y){
        super.removeAll();
        for(JMenuItem menuItem : packages.get("defaultPackage"))super.add(menuItem);
        super.show(invoker,x,y);
    }

    public void drawGenome(){
        if(genomeFrame.isVisible()){
            genomeFrame.repaint();
        }
    }
}
