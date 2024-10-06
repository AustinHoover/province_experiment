package electrosphere.state;

import electrosphere.util.Point;

/**
 * A building
 */
public class Building {

    /**
     * The location of the building
     */
    Point location;

    /**
     * The type of building
     */
    String type;

    /**
     * Constructor
     * @param location The location of the building
     * @param type The type of building
     */
    public Building(Point location, String type){
        this.location = location;
        this.type = type;
    }

    /**
     * Gets the location of the building
     * @return The location
     */
    public Point getLocation(){
        return location;
    }

    /**
     * Gets the type of building
     * @return The type
     */
    public String getType(){
        return type;
    }
    
}
