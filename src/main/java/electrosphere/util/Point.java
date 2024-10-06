package electrosphere.util;

/**
 * A 2d point
 */
public class Point {
    
    /**
     * The x location
     */
    double x;

    /**
     * The y location
     */
    double y;

    /**
     * Constructor
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate of the point
     * @return The x coordinate
     */
    public double getX(){
        return x;
    }

    /**
     * Gets the y coordinate of the point
     * @return The y coordinate
     */
    public double getY(){
        return y;
    }

    /**
     * Gets the distance between two points
     * @param p2 The second point
     * @return The distance
     */
    public double distance(Point p2){
        return Math.sqrt((this.x - p2.x) * (this.x - p2.x) + (this.y - p2.y) * (this.y - p2.y));
    }

}
