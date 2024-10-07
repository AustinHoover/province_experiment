package electrosphere.state;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.building.Building;
import electrosphere.building.BuildingType;
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
        List<BuildingType> buildingTypes = BuildingType.getDefaultTypes();
        for(BuildingType buildingType : buildingTypes){
            if(buildingType.isProvincial()){
                for(int provinceId : this.provinces){
                    Province province = idProvinceMap.get(provinceId);
                    if(buildingType.isOnlyCoastal()){
                        if(!province.getCoastalStatus()){
                            continue;
                        }
                        buildings.add(new Building(province.getId(), new Point(province.getX(), province.getY()), buildingType.getName()));
                    } else {
                        buildings.add(new Building(province.getId(), new Point(province.getX(), province.getY()), buildingType.getName()));
                    }
                }
            } else {
                int placed = 0;
                while(placed < buildingType.getShowOnMap()){
                    for(int provinceId : this.provinces){
                        if(placed >= buildingType.getShowOnMap()){
                            break;
                        }
                        Province province = idProvinceMap.get(provinceId);
                        if(buildingType.isOnlyCoastal()){
                            if(!province.getCoastalStatus()){
                                continue;
                            }
                            buildings.add(new Building(province.getId(), new Point(province.getX(), province.getY()), buildingType.getName()));
                            placed++;
                        } else {
                            buildings.add(new Building(province.getId(), new Point(province.getX(), province.getY()), buildingType.getName()));
                            placed++;
                        }
                    }
                    if(placed == 0){
                        break;
                    }
                }
            }
        }
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
