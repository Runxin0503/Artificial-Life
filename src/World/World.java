package World;

import Entity.Entity;
import Menu.MainMenu;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import Constants.Constants.*;
import Entity.Immovable.Bush;
import Entity.Immovable.Egg;
import Entity.Movable.Corpse;
import Entity.Movable.Creature;
import Screens.Graph;
import Screens.ZoomableWindow;
import globalGenomes.*;

public class World implements Serializable{
    public static globalNodes globalNodes= null;
    public static globalInnovations globalInnovations = null;
    public GridList Plants = new GridList((int)Math.ceil(BushConstants.initialMaxSize*BushConstants.sizeToWidth),(int)Math.ceil(BushConstants.initialMaxSize*BushConstants.sizeToHeight));
    public GridList Animals = new GridList(CreatureConstants.Reproduce.maxSize,CreatureConstants.Reproduce.maxSize);
    public ArrayList<Creature> Creatures = new ArrayList<Creature>();
    public ArrayList<Corpse> Corpses = new ArrayList<Corpse>();
    public ArrayList<Egg> Eggs = new ArrayList<Egg>();
    public ArrayList<Bush> Bushes = new ArrayList<Bush>();
    public transient ArrayList<Entity> remove=new ArrayList<Entity>(),add=new ArrayList<Entity>(),exceptionAvoidance=new ArrayList<Entity>();
    public transient ZoomableWindow window;
    public long day=0,hour=0,minute=0,second=0,tick=0;
    public transient boolean exists = false;
    public transient String name;
    public double tickRate=1;
    private ArrayList<Long> tickRates = new ArrayList<Long>();

    public World(MainMenu mm,String name){
        this.name=name;
        this.window = new ZoomableWindow(this,mm);
        this.window.setVisible(true);

        int bushBoundx = WorldConstants.xBound-2*BushConstants.initialMaxSize;
        int bushBoundy = WorldConstants.yBound-2*BushConstants.initialMaxSize;
        for(int i=0;i<WorldConstants.WorldGen.numBushes;i++) {
            Point coord = new Point((int) (Math.random() * bushBoundx + BushConstants.initialMaxSize), (int) (Math.random() * bushBoundy + BushConstants.initialMaxSize));
            boolean tooClose = false;
            for(Bush b : Bushes){
                if(coord.distance(b.getX(),b.getY())<WorldConstants.WorldGen.bushRadius){
                    tooClose = true;
                    break;
                }
            }
            if(tooClose)i--;
            else Bushes.add(new Bush(coord));
        }
        for(int i=0;i<WorldConstants.WorldGen.startingPopulation;i++)
            Eggs.add(new Egg(new Creature((int)(Math.random()*WorldConstants.xBound),(int)(Math.random()*WorldConstants.yBound))));

        sortIntoGrid(true,true);

        this.exists = true;
    }

    public static void save(World world) throws IOException{
        if(world.name.endsWith("unnamed")) world.name = world.name.substring(0,world.name.length()-8);

        File directory = new File("resources/worlds/" + world.name);
        if (!directory.exists()) directory.mkdirs();

        String time = world.day+"-"+world.hour+"."+world.minute+"."+world.second+"-"+world.tick;
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("resources/worlds/"+world.name+"/"+time+".ser"));
        oos.writeObject(world);
        oos.close();
    }

    public static World read(MainMenu mm, File file) throws IOException, ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        World world = (World) ois.readObject();
        System.out.println("World has been deserialized from "+file.getName());
        world.name = file.getParentFile().getName();
        for(Creature c : world.Creatures) c.reload(ImageConstants.bird);
        for(Corpse c : world.Corpses) c.reload(ImageConstants.corpse);
        for(Bush b : world.Bushes) b.reload(ImageConstants.bush);
        for(Egg egg : world.Eggs) egg.reload(ImageConstants.egg);
        world.window = new ZoomableWindow(world,mm);
        world.window.setVisible(true);
        world.remove=new ArrayList<Entity>();
        world.add=new ArrayList<Entity>();
        world.exceptionAvoidance=new ArrayList<Entity>();
        world.sortIntoGrid(true,true);
        world.exists = true;
        return world;
    }

    public void repaint(){
        if(!exists)return;
        this.window.repaint();
    }

    public void tick(ExecutorService executorService) throws InterruptedException {
        if(!exists)return;
        updateTime();
        if(!exceptionAvoidance.isEmpty()) {
            add.addAll(exceptionAvoidance);
            exceptionAvoidance.clear();
        }

        if(Creatures.size()+Eggs.size()<WorldConstants.WorldGen.naturalSpawningThreshold){
            if(Math.random()<WorldConstants.WorldGen.naturalSpawningProbability){
                Creature naturallyGenerated = new Creature((int)(Math.random()*WorldConstants.xBound),(int)(Math.random()*WorldConstants.yBound));
                Eggs.add(new Egg(naturallyGenerated));
//                System.out.println("Spawned new Egg at Coord "+naturallyGenerated.getCoord().x+","+naturallyGenerated.getCoord().y);
            }
        }

        if(Creatures.isEmpty()&&Eggs.isEmpty()&&WorldConstants.WorldGen.startingPopulation>0){//restart simulation if all creatures are dead
            System.out.println("====================================================================================================\nRESTARTING SIM...\n====================================================================================================");
            for(int i=0;i<WorldConstants.WorldGen.startingPopulation;i++)
                Eggs.add(new Egg(new Creature((int)(Math.random()*WorldConstants.xBound),(int)(Math.random()*WorldConstants.yBound))));
        }

        if(!Creatures.isEmpty()){
            CountDownLatch tickLatch = new CountDownLatch(Creatures.size());
            for (Creature c : Creatures) {
                if(!Double.isFinite(c.getEnergy())){
                    System.out.println("ERROR GATE ID 8 WORLD");
                    WorldConstants.Settings.ticksPerSec=0;
                }
                executorService.submit(() -> {
                    try {
                        c.tick(getVisibleGrids(c),getPhysicsGrids(c),c.getNN().output,this);
                    } finally {
                        tickLatch.countDown();
                    }
                });
            }

            CountDownLatch NNlatch = new CountDownLatch(Creatures.size());
            for (Creature c : Creatures)
                if(!(tick % NeuralNet.PromptInterval == 0)) NNlatch.countDown();
                else executorService.submit(()-> {
                    try{
                        c.thinkThonk();
                    }finally{
                        NNlatch.countDown();
                    }
                });

            tickLatch.await(); // Wait for all tick tasks to complete

            CountDownLatch updatePosLatch = new CountDownLatch(Creatures.size());
            for (Creature c : Creatures) {
                if(c.getVelocity()==0&&c.getAngularSpeed()==0) updatePosLatch.countDown();
                else executorService.submit(() -> {
                    try {
                        c.updatePos();
                        c.friction();
                    } finally {
                        updatePosLatch.countDown();
                    }
                });
            }
            updatePosLatch.await(); // Wait for all updatePos tasks to complete
            NNlatch.await();
        }
        if(!Corpses.isEmpty()){
                CountDownLatch updatePosLatch = new CountDownLatch(Corpses.size());
                for (Corpse c : Corpses) {
                    if (c.getVelocity() == 0) updatePosLatch.countDown();
                    else executorService.submit(() -> {
                        try {
                            c.updatePos();
                            c.friction();
                        } finally {
                            updatePosLatch.countDown();
                        }
                    });
                }
                updatePosLatch.await(); // Wait for all updatePos tasks to complete
        }

        for(Corpse c : Corpses)c.tick(this);
        for(Bush b : Bushes)b.tick();
        for(Egg egg : Eggs)egg.tick(this);

        boolean bushChange=false;
        for(int i=remove.size()-1;i>=0;i--){
            Entity e = remove.remove(i);
            switch (e) {
                case Corpse corpse -> Corpses.remove(corpse);
                case Creature creature -> Creatures.remove(creature);
                case Egg egg -> Eggs.remove(egg);
                case Bush bush -> {
                    bushChange = true;
                    Bushes.remove(bush);
                }
                case null, default -> System.out.println("ERROR GATE ID 2 WORLD");
            }
        }
        for(int i=add.size()-1;i>=0;i--){
            Entity e = add.remove(i);
            if(e instanceof Corpse) Corpses.add((Corpse)e);
            else if(e instanceof Creature) Creatures.add((Creature)e);
            else if(e instanceof Egg) Eggs.add((Egg)e);
            else if(e instanceof Bush){
                bushChange=true;
                Bushes.add((Bush)e);
            }
        }

        purge();
        sortIntoGrid(bushChange,true,executorService);

        if(!Creatures.isEmpty()||!Corpses.isEmpty()){//execute physics collisions
                CountDownLatch physicsLatch = new CountDownLatch(Creatures.size()+Corpses.size());
                for(Creature c : Creatures) {
                    executorService.submit(() -> {
                        try {
                            c.physics(getPhysicsGrids(c));
                        } finally {
                            physicsLatch.countDown();
                        }
                    });
                }
                for(Corpse c : Corpses) {
                    executorService.submit(() -> {
                        try {
                            c.physics(getPhysicsGrids(c));
                        } finally {
                            physicsLatch.countDown();
                        }
                    });
                }
                physicsLatch.await(); // Wait for all physics tasks to complete
        }
    }
    private void purge(){
        for(Creature c : Creatures) if(c.getX()==0&&c.getY()==0) remove.add(c);
        for(Corpse c : Corpses) if(c.getX()==0&&c.getY()==0) remove.add(c);
    }

    private void sortIntoGrid(boolean plant,boolean animal,ExecutorService executorService){
        CountDownLatch sortLatch = new CountDownLatch((plant?1:0)+(animal?1:0));
        if(plant) executorService.submit(()-> {
            try {
                Plants.sort(Bushes);
            } finally {
                sortLatch.countDown();
            }
        });
        if(animal) executorService.submit(()-> {
            try{
                Animals.sort(Creatures, Corpses, Eggs);
            } finally {
                sortLatch.countDown();
            }
        });
        try {
            sortLatch.await();
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    private void sortIntoGrid(boolean plant,boolean animal){
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(),4));

        sortIntoGrid(plant,animal,executorService);

        executorService.shutdown();
        try{
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }catch(Exception ignored){}
    }
    public Entity get(int x,int y){

        Entity a=Animals.get(x,y),p=Plants.get(x,y);

        if(a != null) return a;
        else return p;
    }
    public List<ArrayList<ArrayList<Entity>>> getVisibleGrids(Creature c){
        int[] visionPoints = c.getVisionRay();

        int minX=c.getX(),maxX=minX,minY=c.getY(),maxY=minY;

        for(int i=0;i<visionPoints.length;i+=2){
            if(visionPoints[i]<minX) minX=visionPoints[i];
            else if(visionPoints[i]>maxX) maxX=visionPoints[i];
            if(visionPoints[i+1]<minY) minY=visionPoints[i+1];
            else if(visionPoints[i+1]>maxY) maxY=visionPoints[i+1];
        }
        Rectangle visionBoundingBox = new Rectangle(minX,minY,maxX-minX,maxY-minY);
        return List.of(Plants.get(visionBoundingBox),Animals.get(visionBoundingBox));
    }
    public ArrayList<ArrayList<ArrayList<Entity>>> getPhysicsGrids(Entity e){
        ArrayList<ArrayList<ArrayList<Entity>>> intersectedGrids = new ArrayList<ArrayList<ArrayList<Entity>>>();

        int x=e.getX(),y=e.getY();
        intersectedGrids.add(Plants.getNeighborGrids(x,y));
        intersectedGrids.add(Animals.getNeighborGrids(x,y));

        return intersectedGrids;
    }

    public void addTickRate(long newTickRate){
        if(tick!=0)return;
        this.tickRate*=tickRates.size();
        this.tickRates.add(newTickRate);
        this.tickRate+=newTickRate;
        if(this.tickRates.size()>60)tickRate-=this.tickRates.remove(0);
        this.tickRate/=tickRates.size();
    }
    public void fastForward(int length){
        int temp = WorldConstants.Settings.ticksPerSec;
        WorldConstants.Settings.ticksPerSec=0;
        int count=0;
        try{
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        ExecutorService executorService = Executors.newFixedThreadPool(WorldConstants.Settings.maxThread);
        while(count++<length){
            try {
                tick(executorService);
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            };
            if(count%1000==0)System.out.println(count+" out of "+length+" ticks. "+count*100.0/length+"% Completed");
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        WorldConstants.Settings.ticksPerSec=temp;
    }
    private void updateTime(){
        tick++;
        if(tick>=WorldConstants.Settings.ticksToSecond){
            tick-=WorldConstants.Settings.ticksToSecond;
            second++;
        }
        if(second>=60){
            second-=60;
            minute++;
            System.out.println("Creature Size   "+Creatures.size()+"   Egg Size   "+Eggs.size()+"   "+(Creatures.size()+Eggs.size()));
            System.out.println("Corpse Size   "+Corpses.size());
        }
        if(minute>=60){
            minute-=60;
            hour++;
        }
        if(hour>=24){
            hour-=24;
            day++;
        }
        if((day*3600*24+hour*3600+minute*60+second)%(4L * window.graph.intervalOfAccepting)==0&&tick==0) report();
    }

    private void report(){
        double[] min=new double[WorldConstants.Settings.reportSize],max=new double[WorldConstants.Settings.reportSize],avg=new double[WorldConstants.Settings.reportSize];
        if(!Creatures.isEmpty()) Arrays.fill(min, 1e9);
        int herbivoreCount=0;
        for(Creature c : Creatures){
            double[] report = c.getReport();
            for(int i=0;i<report.length;i++){
                avg[i]+=report[i];
                if(min[i]>report[i]) min[i]=report[i];
                if(max[i]<report[i]) max[i]=report[i];
            }
            if(c.getGenome().carnivoryAffinity<c.getGenome().herbivoryAffinity)herbivoreCount++;
        }
        if(!Creatures.isEmpty()) for(int i=0;i<avg.length;i++) avg[i]/=Creatures.size();
        int time = (int)(second + minute*60 + hour*3600 + day*24*3600);
        for(int i=0;i<WorldConstants.Settings.reportSize;i++){
            String str = WorldConstants.Settings.reportInfo[i];
            window.graph.addPoint(Graph.VALUES,"avg"+str,avg[i],time);
            window.graph.addPoint(Graph.VALUES,"min"+str,min[i],time);
            window.graph.addPoint(Graph.VALUES,"max"+str,max[i],time);
        }
        window.graph.addPoint(Graph.ENTITIES,"# Carnivore",Creatures.size()-herbivoreCount,time);
        window.graph.addPoint(Graph.ENTITIES,"# Herbivore",herbivoreCount,time);
        window.graph.addPoint(Graph.ENTITIES,"# Creatures",Creatures.size(),time);
        window.graph.addPoint(Graph.ENTITIES,"# Eggs",Eggs.size(),time);
        double berriesEnergy = 0;
        for(Bush b : Bushes)berriesEnergy+=b.getEnergyIfConsumed();
        window.graph.addPoint(Graph.ENTITIES,"# Berries",berriesEnergy,time);
        double corpseEnergy = 0;
        for(Corpse c : Corpses)corpseEnergy+=c.getEnergyIfConsumed();
        window.graph.addPoint(Graph.ENTITIES,"# Corpses",corpseEnergy,time);
        window.graph.addPoint(Graph.ENTITIES,"Ticks/Sec",Math.max(1,(int)Math.round(1000.0/tickRate)),time);
        window.graph.updateDataSets();
        if(window.graph.isVisible()) window.graph.repaint();
    }
}