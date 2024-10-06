package electrosphere.province;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A province
 */
public class Province {

    int x;
    int y;
    int id;
    int red;
    int green;
    int blue;
    String type;
    boolean coastalStatus;
    String terrain;
    int continent;
    List<Integer> neighbors;

    public Province(int x, int y, int id, int red, int green, int blue, String type, boolean coastalStatus, String terrain, int continent){
        this.x = x;
        this.y = y;
        this.id = id;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.type = type;
        this.coastalStatus = coastalStatus;
        this.terrain = terrain;
        this.continent = continent;
        this.neighbors = new LinkedList<Integer>();
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getId(){
        return id;
    }

    public int getRed(){
        return red;
    }

    public int getGreen(){
        return green;
    }

    public int getBlue(){
        return blue;
    }

    public String getType(){
        return type;
    }

    public boolean getCoastalStatus(){
        return coastalStatus;
    }

    /**
     * Sets the coastal status of the province
     * @param isCoastal true if is coastal, false otherwise
     */
    public void setCoastalStatus(boolean isCoastal){
        this.coastalStatus = isCoastal;
    }

    public String getTerrain(){
        return terrain;
    }

    public int getContinent(){
        return continent;
    }

    public void setContinent(int continent){
        this.continent = continent;
    }

    /**
     * Gets the neighbor province ids of this province
     * @return The list of neighbors
     */
    public List<Integer> getNeighbors(){
        return Collections.unmodifiableList(this.neighbors);
    }

    /**
     * Adds a neighbor to this province
     * @param neighborId The neighbor
     */
    public void addNeighbor(int neighborId){
        this.neighbors.add(neighborId);
    }

}
