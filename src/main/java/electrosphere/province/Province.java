package electrosphere.province;

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

    public String getTerrain(){
        return terrain;
    }

    public int getContinent(){
        return continent;
    }

    public void setContinent(int continent){
        this.continent = continent;
    }

}
