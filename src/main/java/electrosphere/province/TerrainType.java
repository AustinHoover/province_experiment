package electrosphere.province;

import java.util.List;

public class TerrainType {

    int id;
    String type;
    List<Integer> baseColor;
    List<Integer> sourceColor;

    public int getId(){
        return id;
    }

    public String getType(){
        return type;
    }

    public List<Integer> getBaseColor(){
        return baseColor;
    }

    public List<Integer> getSourceColor(){
        return sourceColor;
    }

}
