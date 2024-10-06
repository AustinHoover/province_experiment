package electrosphere.state;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.province.Province;
import electrosphere.util.Point;

/**
 * A state
 */
public class State {

    /**
     * The id of the state
     */
    int id;
    
    /**
     * The list of province IDs within this state
     */
    List<Integer> provinces;

    /**
     * true if is a land state, false otherwise
     */
    boolean isLand;

    /**
     * The list of building locations
     */
    List<Building> buildings = new LinkedList<Building>();

    /**
     * Creates a state
     * @param id The id of the state
     * @param provinces The province IDs in the state
     * @param isLand true if is a land state, false otherwise
     */
    public State(int id, List<Integer> provinces, boolean isLand){
        this.id = id;
        this.provinces = provinces;
        this.isLand = isLand;
    }

    /**
     * Generates the buildings within this state
     * @param provinces The map of all provinces
     * @param idProvinceMap The map of province id -> province
     */
    public void generateBuildings(List<Province> provinces, Map<Integer,Province> idProvinceMap){
        Province firstProvince = idProvinceMap.get(provinces.get(0).getId());
        buildings.add(new Building(new Point(firstProvince.getX(),firstProvince.getY()), "arms_factory"));
        buildings.add(new Building(new Point(firstProvince.getX(),firstProvince.getY()), "industrial_complex"));
        buildings.add(new Building(new Point(firstProvince.getX(),firstProvince.getY()), "anti_air_building"));
    }

    /**
     * Gets the province IDs within this state
     * @return The list of province IDs
     */
    public List<Integer> getProvinces(){
        return provinces;
    }

    /**
     * Gets whether this is a land state or a naval state
     * @return true if is land, false if is naval
     */
    public boolean isLand(){
        return isLand;
    }

    /**
     * Gets the buildings in this state
     * @return The buildings
     */
    public List<Building> getBuildings(){
        return this.buildings;
    }

    /**
     * Gets the id of the state
     * @return The id of the state
     */
    public int getId(){
        return id;
    }

}
