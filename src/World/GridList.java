package World;

import Constants.Constants.*;
import Entity.Entity;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GridList implements Serializable {
    private final List<Grid> grids = new ArrayList<Grid>();
    public GridList(){
        for(int x = 0; x <WorldConstants.xBound; x +=WorldConstants.GridWidth)
            for(int y = 0; y <WorldConstants.yBound; y +=WorldConstants.GridHeight)
                grids.add(new Grid(x,y));
    }

    @SafeVarargs
    public final void sort(ArrayList<? extends Entity>... AllEntities){
        for(ArrayList<? extends Entity> entities : AllEntities)
            for(Entity e : entities){
                if(e.isBoundingBoxChange()){

                }
            }
    }

    private void sort(Rectangle prevBoundingBox,Rectangle newBoundingBox){
        int prevMinX,prevMaxX,prevMinY,prevMaxY;
        int newMinX,newMaxX,newMinY,newMaxY;

        for(int y=prevMinY;y<newMinY;y++){
            for(int x=prevMinX;x<=prevMaxX;x++){
                //remove
            }
        }
        for(int x=newMaxX;x<prevMaxX;x++){
            for(int y=prevMaxY;y<prevMinY;y++){
                //remove
            }
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
        return getID(e.getCoord().x,e.getCoord().y);
    }

    public class Grid implements Serializable{
        private final Rectangle boundingBox;
        private final ArrayList<Entity> containedEntity = new ArrayList<Entity>();
        public Grid(int x,int y){
            this.boundingBox = new Rectangle(x,y,WorldConstants.GridWidth,WorldConstants.GridHeight);
        }

        
    }
}
