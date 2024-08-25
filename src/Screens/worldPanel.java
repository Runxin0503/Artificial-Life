package Screens;

import Entity.Immovable.*;
import Entity.Movable.*;
import Evolution.NN;
import Entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Constants.Constants.*;
import Constants.Constants.WindowConstants;
import java.util.List;
import World.*;

public class worldPanel extends JPanel {
    private Rectangle cameraBoundingBox;
    private double zoomFactor = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private double mouseX = 0;
    private double mouseY = 0;
    private boolean dragging = false;
    private World world;

    private PopupMenu contextMenu;
    private final overlay overlay;
    public Entity selected = null;
    private boolean following=false;
    private sidePanel controlPanel;
    private ExecutorService executorService;
    public worldPanel(World world,sidePanel controlPanel){
        this.world = world;
        this.executorService = Executors.newFixedThreadPool(WindowConstants.maxThread);
        this.controlPanel = controlPanel;
        setLayout(new BorderLayout());
        this.overlay = new overlay(world){{setPreferredSize(new Dimension(getWidth(),getHeight()));}};
        add(overlay,BorderLayout.CENTER);

        createContextMenu();

        addMouseWheelListener(e -> {
            double delta = 1.1;
            double scaleFactor = (e.getWheelRotation() < 0) ? delta : 1 / delta;
            if((zoomFactor== WindowConstants.maxZoom&&scaleFactor>1)||(zoomFactor== WindowConstants.minZoom&&scaleFactor<1)){
                return;
            }

            // Get the mouse coordinates relative to the panel
            double px = e.getX() - translateX;
            double py = e.getY() - translateY;

            // Update the translation to zoom in/out centered on the mouse pointer
            translateX -= px * (scaleFactor - 1);
            translateY -= py * (scaleFactor - 1);

            zoomFactor *= scaleFactor;
            zoomFactor = Math.min(WindowConstants.maxZoom,Math.max(WindowConstants.minZoom,zoomFactor));
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                if (e.isPopupTrigger()) {
                    select(e);
                    showContextMenu(e);
                }else if(selected!=null&&!selected.getBoundingBox().contains((int)Math.round((e.getX() - translateX) / zoomFactor),(int)Math.round((e.getY() - translateY) / zoomFactor))){
                    if(selected instanceof Creature){
                        if(((Creature)selected).isPlayer)((Creature)selected).isPlayer=false;
                        ((Creature)selected).isSelected=false;
                    }
                    selected=null;
                    controlPanel.setVisible(false);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 || e.isPopupTrigger()) {
                    select(e);
                    if(e.isPopupTrigger()) showContextMenu(e);
                }

            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    double dx = e.getX() - mouseX;
                    double dy = e.getY() - mouseY;
                    translateX += dx;
                    translateY += dy;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(following)centerOnSelected();

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(translateX, translateY);
        g2d.scale(zoomFactor, zoomFactor);
        g2d.setColor(Color.CYAN);
        g2d.fillRect(0,0, WorldConstants.xBound, WorldConstants.yBound);
        g2d.setColor(Color.black);
        calculateCameraBoundingBox();

        try{
            List<Creature> cr= new ArrayList<>(world.Creatures);
            List<Corpse> cp= new ArrayList<>(world.Corpses);
            List<Egg> e= new ArrayList<>(world.Eggs);
            List<Bush> b= new ArrayList<>(world.Bushes);

            if(!b.isEmpty()) {
                CountDownLatch bushLatch = new CountDownLatch(b.size());
                for (Bush bush : b) {
                    if(bush==null || (!cameraBoundingBox.contains(bush.getBoundingBox())&&!cameraBoundingBox.intersects(bush.getBoundingBox()))) bushLatch.countDown();
                    else executorService.submit(() -> {
                        try {
                            drawBush(bush, g2d);
                        } finally {
                            bushLatch.countDown();
                        }
                    });
                }
                bushLatch.await(); // Wait for all physics tasks to complete
            }

            if(!e.isEmpty()) {
                CountDownLatch eggLatch = new CountDownLatch(e.size());
                for (Egg egg : e) {
                    if(egg==null || (!cameraBoundingBox.contains(egg.getBoundingBox())&&!cameraBoundingBox.intersects(egg.getBoundingBox()))) eggLatch.countDown();
                    else executorService.submit(() -> {
                        try {
                            drawEgg(egg, g2d);
                        } finally {
                            eggLatch.countDown();
                        }
                    });
                }
                eggLatch.await(); // Wait for all physics tasks to complete
            }

            if(!cp.isEmpty()) {
                CountDownLatch corpseLatch = new CountDownLatch(cp.size());
                for (Corpse c : cp) {
                    if(c==null || (!cameraBoundingBox.contains(c.getBoundingBox())&&!cameraBoundingBox.intersects(c.getBoundingBox()))) corpseLatch.countDown();
                    else executorService.submit(() -> {
                        try {
                            drawCorpse(c, g2d);
                        } finally {
                            corpseLatch.countDown();
                        }
                    });
                }
                corpseLatch.await(); // Wait for all physics tasks to complete
            }

            if(!cr.isEmpty()) {
                CountDownLatch creatureLatch = new CountDownLatch(cr.size());
                for (Creature c : cr) {
                    if(c==null || (!cameraBoundingBox.contains(c.getBoundingBox())&&!cameraBoundingBox.intersects(c.getBoundingBox()))) creatureLatch.countDown();
                    else executorService.submit(() -> {
                        try {
                            drawCreature(c, g2d);
                        } finally {
                            creatureLatch.countDown();
                        }
                    });
                }
                creatureLatch.await(); // Wait for all physics tasks to complete
            }

        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        g2d.drawRect(0,0, WorldConstants.xBound, WorldConstants.yBound);

        g2d.scale(1/zoomFactor, 1/zoomFactor);
        g2d.translate(-translateX, -translateY);

        overlay.setVisible(WorldConstants.Settings.devMode);
        if(WorldConstants.Settings.devMode) overlay.repaint();

        contextMenu.drawGenome();
    }

    public void calculateCameraBoundingBox(){
        Dimension size = getSize();

        // Calculate the top-left corner
        int left = (int)Math.floor(-translateX / zoomFactor);
        int top = (int)Math.floor(-translateY / zoomFactor);

        cameraBoundingBox = new Rectangle(left,top,(int)Math.ceil(size.getWidth()/zoomFactor),(int)Math.ceil(size.getHeight()/zoomFactor));
    }
    private void drawBush(Bush bush,Graphics2D g2d){
        int x = bush.getBoundingBox().x;
        int y = bush.getBoundingBox().y;
        g2d.drawImage(bush.getImage(),x,y,this);
        if (WorldConstants.Settings.devMode){
            g2d.drawRect(bush.getBoundingBox().x,bush.getBoundingBox().y,bush.getBoundingBox().width,bush.getBoundingBox().height);
            g2d.fillRect(bush.getX()-2,bush.getY()-2,4,4);
        }
        ArrayList<Point> berries = bush.getBerries();
        for (Point berry : berries) g2d.drawImage(ImageConstants.berries, x + berry.x, y + berry.y, this);
        if(selected instanceof Bush && bush.equals(selected)){
            g2d.setColor(Color.red);
            g2d.drawRect(bush.getBoundingBox().x,bush.getBoundingBox().y,bush.getBoundingBox().width,bush.getBoundingBox().height);
            g2d.setColor(Color.black);
        }
    }
    private void drawCreature(Creature c,Graphics2D g2d){
        g2d.drawImage(c.getImage(), c.getBoundingBox().x, c.getBoundingBox().y, this);
        if (WorldConstants.Settings.devMode) {
            g2d.drawRect(c.getBoundingBox().x, c.getBoundingBox().y, c.getBoundingBox().width, c.getBoundingBox().height);
            Rectangle temp = c.getEatingHitbox();
            if (temp != null) g2d.drawRect(temp.x, temp.y, temp.width, temp.height);
            g2d.fillRect(c.getX() - 2, c.getY() - 2, 4, 4);
            int[] visionRay = c.getVisionRay();
            int x = c.getX(),y = c.getY();
            g2d.drawLine(visionRay[0],visionRay[1],visionRay[2],visionRay[3]);
            g2d.drawLine(visionRay[2],visionRay[3],visionRay[4],visionRay[5]);
            g2d.drawLine(visionRay[0],visionRay[1],x,y);
            g2d.drawLine(visionRay[2],visionRay[3],x,y);
        }

        if (selected instanceof Creature && c.equals(selected)) {
            g2d.setColor(Color.red);
            g2d.drawRect(c.getBoundingBox().x, c.getBoundingBox().y, c.getBoundingBox().width, c.getBoundingBox().height);
            g2d.setColor(Color.black);
        }
    }
    private void drawCorpse(Corpse c,Graphics2D g2d){
        int x = c.getBoundingBox().x;
        int y = c.getBoundingBox().y;
        g2d.drawImage(c.getImage(),x,y,this);
        if (WorldConstants.Settings.devMode){
            g2d.drawRect(c.getBoundingBox().x,c.getBoundingBox().y,c.getBoundingBox().width,c.getBoundingBox().height);
            g2d.fillRect(c.getX()-2,c.getY()-2,4,4);
        }
        if(selected instanceof Corpse && c.equals(selected)){
            g2d.setColor(Color.red);
            g2d.drawRect(c.getBoundingBox().x,c.getBoundingBox().y,c.getBoundingBox().width,c.getBoundingBox().height);
            g2d.setColor(Color.black);
        }
    }
    private void drawEgg(Egg egg,Graphics2D g2d){
        int x = egg.getBoundingBox().x;
        int y = egg.getBoundingBox().y;
        g2d.drawImage(egg.getImage(),x,y,this);
        if (WorldConstants.Settings.devMode){
            g2d.drawRect(x,y,egg.getBoundingBox().width,egg.getBoundingBox().height);
            g2d.fillRect(egg.getX()-2,egg.getY()-2,4,4);
        }
        if(selected instanceof Egg && egg.equals(selected)){
            g2d.setColor(Color.red);
            g2d.drawRect(x,y,egg.getBoundingBox().width,egg.getBoundingBox().height);
            g2d.setColor(Color.black);
        }
    }
    private void select(MouseEvent e){
        double x = (e.getX() - translateX) / zoomFactor;
        double y = (e.getY() - translateY) / zoomFactor;
        Entity newlySelected = world.get((int)Math.round(x),(int)Math.round(y));
        if(selected!=null && !selected.equals(newlySelected) && selected instanceof Creature){
            if(((Creature)selected).isPlayer) ((Creature)selected).isPlayer=false;
            ((Creature)selected).isSelected=false;
        }
        if(newlySelected!=null){
            selected=newlySelected;
            if(selected instanceof Creature)((Creature)selected).isSelected=true;
            controlPanel.displayData(selected);
            controlPanel.updateVariables();
            controlPanel.setVisible(true);
            revalidate();
            repaint();
        }else{
            selected=null;
            controlPanel.setVisible(false);
        }
    }
    public void centerOnSelected(){
        if(selected==null || !(selected instanceof Creature || selected instanceof Corpse)){
            following=false;
            return;
        }
        double width = getWidth();
        translateX = (width / 2.0) - (selected.getX() * zoomFactor);
        translateY = (getHeight() / 2.0) - (selected.getY() * zoomFactor);
    }
    private void createContextMenu() {
        contextMenu = new PopupMenu();
        JMenuItem createEgg = new JMenuItem("Create Egg Here");
        createEgg.addActionListener(e -> {
            Creature c = new Creature(getAbsolutePos(mouseX,mouseY));
            world.exceptionAvoidance.add(new Egg(c));
        });

        JMenuItem createCreature = new JMenuItem("Create Creature Here");
        createCreature.addActionListener(e -> {
            Creature c = new Creature(getAbsolutePos(mouseX,mouseY));
            c.setNN(NN.godGenome());
            world.exceptionAvoidance.add(c);
        });

        JMenuItem respawnPopulation = new JMenuItem("Respawn All");
        respawnPopulation.addActionListener(e -> {
            for(int i = 0; i< WorldConstants.WorldGen.startingPopulation; i++)
                world.exceptionAvoidance.add(new Creature((int)(Math.random()* WorldConstants.xBound),(int)(Math.random()* WorldConstants.yBound)));
        });

        JMenuItem controlCreature = new JMenuItem("Take Control");
        controlCreature.addActionListener(e -> ((Creature)selected).isPlayer=true);

        JMenuItem viewGenome = new JMenuItem("View Genome");
        viewGenome.addActionListener(e -> contextMenu.viewGenome(selected));

        JMenuItem Follow = new JMenuItem("Follow");
        Follow.addActionListener(e -> {
            following=true;
            zoomFactor = WindowConstants.followZoom;
            centerOnSelected();
        });

        JMenuItem FindCarnivore = new JMenuItem("Find Carnivore");
        FindCarnivore.addActionListener(e -> {
            for(Creature c : world.Creatures)if(c.getGenome().herbivoryAffinity<c.getGenome().carnivoryAffinity){
                selected=c;
                following=true;
                zoomFactor = WindowConstants.followZoom;
                controlPanel.displayData(selected);
                controlPanel.updateVariables();
                controlPanel.setVisible(true);
                centerOnSelected();
            }
        });

        contextMenu.add("Empty",createEgg);
        contextMenu.add("Empty",createCreature);
        contextMenu.add("Empty",respawnPopulation);
        contextMenu.add("Empty",FindCarnivore);
        contextMenu.add("Creature",controlCreature);
        contextMenu.add("Creature",viewGenome);
        contextMenu.add("Creature",Follow);
        contextMenu.add("Corpse",Follow);
    }
    private void showContextMenu(MouseEvent e) {
        switch (selected) {
            case Creature creature -> contextMenu.show(e.getComponent(), e.getX(), e.getY(), "Creature");
            case Corpse corpse -> contextMenu.show(e.getComponent(), e.getX(), e.getY(), "Corpse");
            case null -> contextMenu.show(e.getComponent(), e.getX(), e.getY(), "Empty");
            default -> contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    public Point getAbsolutePos(double x,double y){
        return new Point((int)Math.round((mouseX - translateX) / zoomFactor),(int)Math.round((mouseY- translateY) / zoomFactor));
    }
}
