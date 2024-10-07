package electrosphere.building;

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
     * The id of the province this building falls within
     */
    int provinceId;

    /**
     * The adjacent naval province
     */
    int adjacentNavalProvince = 0;

    /**
     * Constructor
     * @param location The location of the building
     * @param type The type of building
     */
    public Building(int provinceId, Point location, String type){
        this.provinceId = provinceId;
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

    /**
     * Gets the id of the province this building falls within
     * @return The id of the province
     */
    public int getProvinceId(){
        return provinceId;
    }

    /**
     * Sets the adjacent naval province id
     * @param provinceId The id of the sea province
     */
    public void setAdjacentNavalProvince(int provinceId){
        this.adjacentNavalProvince = provinceId;
    }

    /**
     * Gets the adjacent naval province's id
     * @return The province id
     */
    public int getAdjacentNavalProvince(){
        return this.adjacentNavalProvince;
    }
    
}
