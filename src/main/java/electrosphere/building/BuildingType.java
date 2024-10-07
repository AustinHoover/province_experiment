package electrosphere.building;

import java.util.LinkedList;
import java.util.List;

/**
 * A building type that should visually show
 */
public class BuildingType {

    /**
     * The name of the building type
     */
    String name;
    
    /**
     * The number of models to show on the map
     */
    int showOnMap;

    /**
     * true if is a provincial building, false if is a state-wide building
     */
    boolean provincial;

    /**
     * true if is only available in coastal provinces
     */
    boolean onlyCoastal;

    /**
     * true if is a supply node, false otherwise
     */
    boolean supplyNode;

    /**
     * Constructor
     * @param name The name of the building type
     * @param showOnMap the number of meshes to show on the map
     * @param provincial true if a provincial building
     * @param onlyCoastal true if a coastal building
     * @param supplyNode true if a supply node
     */
    public BuildingType(String name, int showOnMap, boolean provincial, boolean onlyCoastal, boolean supplyNode){
        this.name = name;
        this.showOnMap = showOnMap;
        this.provincial = provincial;
        this.onlyCoastal = onlyCoastal;
        this.supplyNode = supplyNode;
    }

    /**
     * Generate building types for the vanilla game building types
     * @return The list of building types
     */
    public static List<BuildingType> getDefaultTypes(){
        List<BuildingType> rVal = new LinkedList<BuildingType>();
        rVal.add(new BuildingType("arms_factory",6,false,false,false));
        rVal.add(new BuildingType("industrial_complex",6,false,false,false));
        rVal.add(new BuildingType("air_base",1,false,false,false));
        rVal.add(new BuildingType("supply_node",1,true,false,true));
        rVal.add(new BuildingType("naval_base",1,true,true,false));
        rVal.add(new BuildingType("floating_harbor",3,false,true,false));
        rVal.add(new BuildingType("bunker",1,true,false,false));
        rVal.add(new BuildingType("coastal_bunker",1,true,true,false));
        rVal.add(new BuildingType("dockyard",1,false,true,true));
        rVal.add(new BuildingType("anti_air_building",3,false,false,false));
        rVal.add(new BuildingType("synthetic_refinery",1,false,false,false));
        rVal.add(new BuildingType("fuel_silo",1,false,false,false));
        rVal.add(new BuildingType("radar_station",1,false,false,false));
        rVal.add(new BuildingType("rocket_site",1,false,false,false));
        rVal.add(new BuildingType("nuclear_reactor",1,false,false,false));
        return rVal;
    }

    public String getName() {
        return name;
    }

    public int getShowOnMap() {
        return showOnMap;
    }

    public boolean isProvincial() {
        return provincial;
    }

    public boolean isOnlyCoastal() {
        return onlyCoastal;
    }

    public boolean isSupplyNode() {
        return supplyNode;
    }

    

}
