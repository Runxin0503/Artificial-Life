package World;

import Constants.Constants.*;
import Entity.Entity;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class GridList implements Serializable {
    private final HashMap<Integer, ArrayList<Entity>> grid = new HashMap<>();
    private final int[] NEIGHBOR_OFFSETS;
    private final int GRID_NUM_X,GRID_NUM_Y,GRID_WIDTH,GRID_HEIGHT;
    public GridList(int maxWidth, int maxHeight){
        GRID_WIDTH=maxWidth+1;
        GRID_HEIGHT=maxHeight+1;
        GRID_NUM_X= (int)Math.ceil((double)WorldConstants.xBound/GRID_WIDTH);
        GRID_NUM_Y= (int)Math.ceil((double)WorldConstants.yBound/GRID_HEIGHT);
        NEIGHBOR_OFFSETS = new int[]{
                -GRID_NUM_X -1,-GRID_NUM_X,-GRID_NUM_X +1,
                -1,0,1,
                GRID_NUM_X -1, GRID_NUM_X, GRID_NUM_X +1
        };
    }

    @SafeVarargs//ExecutorService executorService,
    public final void sort(ArrayList<? extends Entity>... AllEntities){
        grid.clear();
        for(ArrayList<? extends Entity> Entities : AllEntities) for(Entity e : Entities){
//            executorService.submit(()->{
                int id = getID(e);
                if(grid.containsKey(id)) grid.get(id).add(e);
                else grid.put(id,new ArrayList<Entity>(List.of(e)));
//            });
        }
    }

    public Entity get(int x,int y){
        ArrayList<Entity> contained = new ArrayList<Entity>();
        int id = getID(x,y);
        for(int offsetVal : NEIGHBOR_OFFSETS){
            int newID = id+offsetVal;
            if(grid.containsKey(newID)) for(Entity e : grid.get(newID)){
                if ((e).getBoundingBox().contains(x,y)) contained.add(e);
            }
        }
        if(contained.isEmpty()) return null;
        if(contained.size() == 1) return contained.get(0);
        int smallestArea = Integer.MAX_VALUE;
        Entity smallestEntity = null;
        for(Entity e : contained) if(e.getBoundingBox().width*e.getBoundingBox().height<smallestArea) smallestEntity = e;

        return smallestEntity;
    }
    public ArrayList<ArrayList<Entity>> get(Rectangle box){
        ArrayList<ArrayList<Entity>> contained = new ArrayList<ArrayList<Entity>>();
        int minX = (int)Math.floor(box.getMinX()/GRID_WIDTH)-1;
        int maxX = (int)Math.ceil(box.getMaxX()/GRID_WIDTH)+1;
        int minY = (int)Math.floor(box.getMinY()/GRID_HEIGHT)-1;
        int maxY = (int)Math.ceil(box.getMaxY()/GRID_HEIGHT)+1;

        if(minX<0)minX=0;
        if(maxX>GRID_NUM_X)maxX=GRID_NUM_X;
        if(minY<0)minY=0;
        if(maxY>GRID_NUM_Y)maxY=GRID_NUM_Y;

//        System.out.println(minX*GRID_WIDTH+","+maxX*GRID_WIDTH+","+minY*GRID_HEIGHT+","+maxY*GRID_HEIGHT+"\t\t\t"+minX+","+maxX+","+minY+","+maxY);
        for(int x=minX;x<maxX;x++) for(int y=minY;y<maxY;y++)
            if(grid.containsKey(x+y*GRID_NUM_X))
                contained.add(grid.get(x+y*GRID_NUM_X));

        return contained;
    }
    public ArrayList<ArrayList<Entity>> getNeighborGrids(int x,int y){
        ArrayList<ArrayList<Entity>> contained = new ArrayList<ArrayList<Entity>>();
        int id = getID(x,y);
        for(int offsetVal : NEIGHBOR_OFFSETS){
            int newID = id+offsetVal;
            if(grid.containsKey(newID))
                contained.add(grid.get(newID));
        }
        return contained;
    }

    private int getID(int x,int y){
        return y / GRID_HEIGHT * GRID_NUM_X + x / GRID_WIDTH;
    }
    private int getID(Entity e){
        return getID(e.getX(),e.getY());
    }
}
