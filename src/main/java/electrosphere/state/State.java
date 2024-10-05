package electrosphere.state;

import java.util.LinkedList;
import java.util.List;

import electrosphere.province.Province;

import java.awt.Point;

public class State {
    
    List<Integer> provinces;
    boolean isLand;

    List<Point> armsFactories = new LinkedList<Point>();
    List<Point> industrialComplex = new LinkedList<Point>();
    List<Point> airBase = new LinkedList<Point>();
    List<Point> navalBase = new LinkedList<Point>();
    List<Point> bunker = new LinkedList<Point>();
    List<Point> coastalBunker = new LinkedList<Point>();
    List<Point> dockyard = new LinkedList<Point>();
    List<Point> antiAir = new LinkedList<Point>();
    List<Point> syntheticRefiner = new LinkedList<Point>();
    List<Point> nuclearReactor = new LinkedList<Point>();

    public State(List<Integer> provinces, boolean isLand){
        this.provinces = provinces;
        this.isLand = isLand;
    }

    public void generateBuildings(List<Province> provinces){
        // for(int provinceIndex : this.provinces){
            //arms
            //industrial
            //naval potentially
            //bunker
            //coastal bunker
            //dockyard
            //AA
        // }
    }

    public List<Integer> getProvinces(){
        return provinces;
    }

    public boolean isLand(){
        return isLand;
    }

}
