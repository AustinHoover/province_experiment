package electrosphere.threads;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import electrosphere.Main;
import electrosphere.util.Point;
import electrosphere.util.Utils;

import java.awt.Color;

/**
 * Thread for brute force calculation of delaunay triangulation of map to generate province bounds
 */
public class PixelWorkerThread implements Runnable {

    /**
     * Lock for writing to the final image
     */
    static Semaphore imageLock = new Semaphore(1);

    /**
     * The coordinates to consider
     */
    int x;
    int y;

    /**
     * The image containing the terrain data
     */
    BufferedImage rawTerrainImg;

    /**
     * The triangulated output image
     */
    BufferedImage outImage;

    /**
     * A high contrast image used for reviewing the emitted values
     */
    BufferedImage highContrastImage;

    int horizontalThird = 0;
    int verticalThird = 0;

    List<Point> provinceCenterList;
    List<Point> oceanCenterList;
    //bootleg quad trees lmao
    Map<String,List<Point>> provinceCenterListMap;
    Map<String,List<Point>> oceanCenterListMap;
    Map<Integer, Integer> provinceIdColorMap;

    public PixelWorkerThread(
        int x,
        int y,
        BufferedImage rawTerrainImg,
        BufferedImage outImage,
        BufferedImage highContrastImage,
        int horizontalThird,
        int verticalThird,
        List<Point> provinceCenterList,
        List<Point> oceanCenterList,
        Map<String,List<Point>> provinceCenterListMap,
        Map<String,List<Point>> oceanCenterListMap,
        Map<Integer, Integer> provinceIdColorMap
    ){
        this.x = x;
        this.y = y;
        this.rawTerrainImg = rawTerrainImg;
        this.outImage = outImage;
        this.highContrastImage = highContrastImage;
        this.horizontalThird = horizontalThird;
        this.verticalThird = verticalThird;
        this.provinceCenterList = provinceCenterList;
        this.oceanCenterList = oceanCenterList;
        this.provinceCenterListMap = provinceCenterListMap;
        this.oceanCenterListMap = oceanCenterListMap;
        this.provinceIdColorMap = provinceIdColorMap;
    }

    @Override
    public void run() {
        int rgb = rawTerrainImg.getRGB(x, y);
        int blue = rgb & 0xff;
        int green = (rgb & 0xff00) >> 8;
        int red = (rgb & 0xff0000) >> 16;
        int[] rgbArr;
        int index = -1;
        if(red + blue + green > 100){
            //land point
            index = getColor(provinceCenterListMap,provinceCenterList,true,horizontalThird,verticalThird,new Point(x,y));
            rgbArr = Utils.getColorFromIndex(index, true);
        } else {
            //ocean point
            index = getColor(oceanCenterListMap,oceanCenterList,false,horizontalThird,verticalThird,new Point(x,y));
            rgbArr = Utils.getColorFromIndex(index, false);
        }
        imageLock.acquireUninterruptibly();
        outImage.setRGB(x, y, new Color(rgbArr[0],rgbArr[1],rgbArr[2]).getRGB());
        highContrastImage.setRGB(x,y,provinceIdColorMap.get(index+1));
        imageLock.release();
        Main.progressIncrementer.addAndGet(1);
    }

    static int getColor(Map<String,List<Point>> listMap, List<Point> points, boolean isLand, int horizontalThird, int verticalThird, Point toTest){
        int index = 0;
        Point bestPoint = null;
        double closest = 9999999.99;
        int offsetX = (int)toTest.getX() / horizontalThird;
        int offsetY = (int)toTest.getY() / verticalThird;
        for(int x = -1; x < 2; x++){
            for(int y = -1; y < 2; y++){
                if(offsetX + x > -1 && offsetX + x < 3 && offsetY + y > -1 && offsetY + y < 3){
                    // List<Point> currentList = listMap.get((offsetX + x)+""+(offsetY + y));
                    for(Point point : points){
                        if(point.distance(toTest) < closest){
                            closest = point.distance(toTest);
                            bestPoint = point;
                        }
                    }
                }
            }
        }
        index = points.indexOf(bestPoint);
        return index;
    }

}