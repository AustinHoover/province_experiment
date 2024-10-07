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
                            //delete already existing supply nodes -- TODO: make more generic
                            // if(buildingType.isSupplyNode()){
                            //     this.buildings = buildings.stream().filter(building -> !building.getType().contentEquals("supply_node") || building.getProvinceId() != provinceId).collect(Collectors.toList());
                            // }
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
        // boolean placedDockyard = false;
        // boolean placedStatewide = false;
        // for(int provinceId : this.provinces){
        //     Province province = idProvinceMap.get(provinceId);
        //     if(placedStatewide == false){
        //         placedStatewide = true;
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "synthetic_refinery"));
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "fuel_silo"));
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "radar_station"));
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "rocket_site"));
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "nuclear_reactor"));
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "air_base"));
        //         buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "anti_air_building"));
        //     }
        //     buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "arms_factory"));
        //     buildings.add(new Building(province.getId(),new Point(province.getX(),province.getY()), "industrial_complex"));
        //     buildings.add(new Building(province.getId(),new Point(province.getX(), province.getY()),"bunker"));
        //     if(province.getCoastalStatus()){
        //         //get adjacent naval province
        //         Province seaProvince = null;
        //         for(int neighborId : province.getNeighbors()){
        //             if(!idProvinceMap.get(neighborId).getType().contentEquals("land")){
        //                 seaProvince = idProvinceMap.get(neighborId);
        //                 break;
        //             }
        //         }
        //         if(placedDockyard == false){
        //             placedDockyard = true;
        //             buildings.add(new Building(province.getId(),new Point(province.getX(), province.getY()),"dockyard"));
        //         }
        //         Building floatingHarbor = new Building(province.getId(),new Point(province.getX(), province.getY()),"floating_harbor");
        //         floatingHarbor.setAdjacentNavalProvince(seaProvince.getId());
        //         buildings.add(floatingHarbor);
        //         Building navalBase = new Building(province.getId(),new Point(province.getX(), province.getY()),"naval_base");
        //         navalBase.setAdjacentNavalProvince(seaProvince.getId());
        //         buildings.add(navalBase);
        //         buildings.add(new Building(province.getId(),new Point(province.getX(), province.getY()),"coastal_bunker"));
        //     } else {
        //         buildings.add(new Building(province.getId(),new Point(province.getX(), province.getY()),"supply_node"));
        //     }
        // }
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
