package electrosphere.cache;

import java.util.List;
import java.util.Map;

import electrosphere.province.Province;
import electrosphere.util.Point;

/**
 * Caches data about the map between runs so the heavy work doesn't have to be evaluated every run.
 */
public class MapDataCache {
    
    /**
     * List of the centers of all land provinces
     */
    List<Point> provinceCenterList;

    /**
     * List of centers of all ocean provinces
     */
    List<Point> oceanCenterList;
    
    /**
     * Used for performance speedup on province lookups (essentially a quad tree)
     */
    Map<String,List<Point>> provinceCenterListMap;

    /**
     * Used for performance speedup on province lookups (essentially a quad tree)
     */
    Map<String,List<Point>> oceanCenterListMap;

    /**
     * The list of continents discovered
     */
    List<Integer> continentsDiscovered;

    /**
     * The list of all parsed provinces
     */
    List<Province> provinceList;

    /**
     * Map of province id -> color
     */
    Map<Integer, Integer> provinceIdColorMap;

    /**
     * Width of the map
     */
    int mapWidth;

    /**
     * Height of the map
     */
    int mapHeight;

    public List<Point> getProvinceCenterList() {
        return provinceCenterList;
    }

    public void setProvinceCenterList(List<Point> provinceCenterList) {
        this.provinceCenterList = provinceCenterList;
    }

    public List<Point> getOceanCenterList() {
        return oceanCenterList;
    }

    public void setOceanCenterList(List<Point> oceanCenterList) {
        this.oceanCenterList = oceanCenterList;
    }

    public Map<String, List<Point>> getProvinceCenterListMap() {
        return provinceCenterListMap;
    }

    public void setProvinceCenterListMap(Map<String, List<Point>> provinceCenterListMap) {
        this.provinceCenterListMap = provinceCenterListMap;
    }

    public Map<String, List<Point>> getOceanCenterListMap() {
        return oceanCenterListMap;
    }

    public void setOceanCenterListMap(Map<String, List<Point>> oceanCenterListMap) {
        this.oceanCenterListMap = oceanCenterListMap;
    }

    public List<Integer> getContinentsDiscovered() {
        return continentsDiscovered;
    }

    public void setContinentsDiscovered(List<Integer> continentsDiscovered) {
        this.continentsDiscovered = continentsDiscovered;
    }

    public List<Province> getProvinceList() {
        return provinceList;
    }

    public void setProvinceList(List<Province> provinceList) {
        this.provinceList = provinceList;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public Map<Integer, Integer> getProvinceIdColorMap() {
        return provinceIdColorMap;
    }

    public void setProvinceIdColorMap(Map<Integer, Integer> provinceIdColorMap) {
        this.provinceIdColorMap = provinceIdColorMap;
    }

    

}
