package electrosphere;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Edge;
import de.alsclo.voronoi.graph.Graph;
import electrosphere.bmp.BMPWriter;
import electrosphere.building.Building;
import electrosphere.cache.MapDataCache;
import electrosphere.config.Config;
import electrosphere.province.Province;
import electrosphere.province.TerrainMap;
import electrosphere.province.TerrainType;
import electrosphere.state.State;
import electrosphere.threads.PixelWorkerThread;
import electrosphere.util.Point;
import electrosphere.util.Utils;

import java.awt.Color;

public class Main {

    
    public static AtomicInteger progressIncrementer = new AtomicInteger(0);

    static int horizontalThird = 0;
    static int verticalThird = 0;

    static List<Point> provinceCenterList = new LinkedList<Point>();
    static List<Point> oceanCenterList = new LinkedList<Point>();
    //bootleg quad trees lmao
    static Map<String,List<Point>> provinceCenterListMap = new HashMap<String,List<Point>>();
    static Map<String,List<Point>> oceanCenterListMap = new HashMap<String,List<Point>>();

    static Random colorRandom = new Random();
    static Map<String, Boolean> colorUseMap = new HashMap<String, Boolean>();
    static Map<Integer, Integer> provinceIdColorMap = new HashMap<Integer, Integer>();

    //the continents that were discovered
    static List<Integer> continentsDiscovered = new LinkedList<Integer>();

    static List<Province> provinceList = new LinkedList<Province>();

    /**
     * Default path for the map data cache file
     */
    static final String MAP_DATA_CACHE_DEFAULT_PATH = "./mapDataCache.json";

    /**
     * The config file path
     */
    static final String CONFIG_FILEPATH = "./provinceExperiment.json";

    /**
     * The target number of provinces per state
     */
    static final int TARGET_PROVINCES_PER_STATE = 6;

    public static void main(String[] args){
        System.out.println("it lives!");

        Gson gson = new Gson();

        //
        //read config
        //
        Config config = null;
        File configFile = new File(CONFIG_FILEPATH);
        if(Files.exists(new File(CONFIG_FILEPATH).toPath())){
            try {
                System.out.println("Reading config");
                config = gson.fromJson(Files.readString(configFile.toPath()), Config.class);
                config.fillInMissingValues();
            } catch (JsonSyntaxException e) {
                System.err.println("Failed to read config! " + configFile);
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Failed to read config! " + configFile);
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.err.println("Failed to find config, writing an example one to " + configFile);
            config = new Config();
            config.fillInMissingValues();
            try {
                Files.writeString(configFile.toPath(),gson.toJson(config));
            } catch (IOException e) {
                System.err.println("Failed to write example config to " + configFile);
                e.printStackTrace();
                System.exit(1);
            }
            System.exit(1);
        }
        
        //error check the config
        if(!Files.exists(new File(config.getSourceDirectory()).toPath())){
            throw new Error("Sorurce folder does not exist! " + config.getSourceDirectory());
        }
        if(!Files.exists(new File(config.getModDirectory()).toPath())){
            throw new Error("Mod folder does not exist! " + config.getModDirectory());
        }

        //make sure the mod folder has all required subfolders
        {
            String targetSubfolder = config.getModDirectory() + "/map/terrain";
            try {
                Files.createDirectories(new File(targetSubfolder).toPath());
            } catch (IOException e) {
                System.err.println("Failed to create required directories in mod folder " + targetSubfolder);
                e.printStackTrace();
                System.exit(1);
            }
            targetSubfolder = config.getModDirectory() + "/map/strategicregions";
            try {
                Files.createDirectories(new File(targetSubfolder).toPath());
            } catch (IOException e) {
                System.err.println("Failed to create required directories in mod folder " + targetSubfolder);
                e.printStackTrace();
                System.exit(1);
            }
        }

        //read map data cache
        MapDataCache mapDataCache = null;
        if(Files.exists(new File(MAP_DATA_CACHE_DEFAULT_PATH).toPath())){
            try {
                System.out.println("Reading map data cache");
                mapDataCache = gson.fromJson(Files.readString(new File(MAP_DATA_CACHE_DEFAULT_PATH).toPath()), MapDataCache.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){
                provinceCenterListMap.put(x+""+y,new LinkedList<Point>());
                oceanCenterListMap.put(x+""+y,new LinkedList<Point>());
            }
        }
        int width = 0;
        int height = 0;

        BufferedImage provinceImg = null; // the points for each land province
        BufferedImage oceanImg = null; // the points for each ocean province
        BufferedImage rawTerrainImg = null; // the mask for ocean
        BufferedImage continentsImage = null; // the mask for each continents

        if(shouldRunProvinceParsing(config, mapDataCache)){
            try {
                if(mapDataCache == null || mapDataCache.getProvinceCenterList() == null || mapDataCache.getProvinceCenterListMap() == null || mapDataCache.getProvinceIdColorMap() == null){
                    String provincesImageFilepath = config.getSourceDirectory() + "/" + config.getProvincePointsFilename();
                    System.out.println("Reading provinces points image file " + provincesImageFilepath);
                    provinceImg = ImageIO.read(new File(provincesImageFilepath));
                    width = provinceImg.getWidth();
                    height = provinceImg.getHeight();
                    horizontalThird = provinceImg.getWidth() / 3;
                    verticalThird = provinceImg.getHeight() / 3;
                    for(int x = 0; x < provinceImg.getWidth(); x++){
                        for(int y = 0; y < provinceImg.getHeight(); y++){
                            int rgb = provinceImg.getRGB(x, y);
                            int blue = rgb & 0xff;
                            int green = (rgb & 0xff00) >> 8;
                            int red = (rgb & 0xff0000) >> 16;
                            if(red + blue + green > 400){
                                Point newPoint = new Point(x,y);
                                boolean shouldAdd = true;
                                for(Point extantPoint: provinceCenterList){
                                    if(extantPoint.distance(newPoint) < 3){
                                        shouldAdd = false;
                                        break;
                                    }
                                }
                                if(shouldAdd){
                                    provinceCenterList.add(newPoint);
                                    provinceCenterListMap.get((x/horizontalThird)+""+(y/verticalThird)).add(newPoint);
                                    int[] colors = Utils.getColorFromIndex(provinceList.size(),true);
                                    provinceList.add(new Province((int)newPoint.getX(),(int)newPoint.getY(),provinceList.size()+1,colors[0],colors[1],colors[2],"land",false,"unknown",0));
                                    provinceIdColorMap.put(provinceList.size(),getUnusedColor().getRGB());
                                }
                            }
                        }
                    }
                    System.out.println("Discovered " + provinceCenterList.size() + " provinces!");
                } else {
                    System.out.println("Using cached province data");
                    provinceCenterList = mapDataCache.getProvinceCenterList();
                    provinceCenterListMap = mapDataCache.getProvinceCenterListMap();
                    width = mapDataCache.getMapWidth();
                    height = mapDataCache.getMapHeight();
                    horizontalThird = width / 3;
                    verticalThird = height / 3;
                }

                if(mapDataCache == null || mapDataCache.getOceanCenterList() == null || mapDataCache.getOceanCenterListMap() == null || mapDataCache.getProvinceIdColorMap() == null){
                    int incrementerForColor = 0;
                    String oceanPointsPath = config.getSourceDirectory() + "/" + config.getOceanPointsFilename();
                    System.out.println("Reading ocean points file " + oceanPointsPath);
                    oceanImg = ImageIO.read(new File(oceanPointsPath));
                    for(int x = 0; x < oceanImg.getWidth(); x++){
                        for(int y = 0; y < oceanImg.getHeight(); y++){
                            int rgb = oceanImg.getRGB(x, y);
                            int blue = rgb & 0xff;
                            int green = (rgb & 0xff00) >> 8;
                            int red = (rgb & 0xff0000) >> 16;
                            if(red + blue + green > 400){
                                Point newPoint = new Point(x,y);
                                boolean shouldAdd = true;
                                for(Point extantPoint: oceanCenterList){
                                    if(extantPoint.distance(newPoint) < 4){
                                        shouldAdd = false;
                                        break;
                                    }
                                }
                                if(shouldAdd){
                                    oceanCenterList.add(newPoint);
                                    oceanCenterListMap.get((x/horizontalThird)+""+(y/verticalThird)).add(newPoint);
                                    int[] colors = Utils.getColorFromIndex(incrementerForColor,false);
                                    provinceList.add(new Province((int)newPoint.getX(),(int)newPoint.getY(),provinceList.size()+1,colors[0],colors[1],colors[2],"sea",false,"unknown",0));
                                    provinceIdColorMap.put(provinceList.size(),getUnusedColor().getRGB());
                                    incrementerForColor++;
                                }
                            }
                        }
                    }
                    System.out.println("Discovered " + oceanCenterList.size() + " ocean tiles!");
                } else {
                    System.out.println("Using cached ocean province data");
                    oceanCenterList = mapDataCache.getOceanCenterList();
                    oceanCenterListMap = mapDataCache.getOceanCenterListMap();
                }

                String landOceanFilepath = config.getSourceDirectory() + "/" + config.getLandOceanFilename();
                String provincesOutputPath = config.getModDirectory() + "/map/" + config.getOutProvincesFilename();
                String highContrastOutputPath = config.getModDirectory() + "/map/" + config.getOutProvincesHighContrastFilename();
                BufferedImage outImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                BufferedImage highContrastOutImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                if(new File(provincesOutputPath).exists() == false || new File(highContrastOutputPath).exists() == false || mapDataCache == null || mapDataCache.getProvinceIdColorMap() == null){
                    System.out.println("Reading land ocean file " + landOceanFilepath);
                    rawTerrainImg = ImageIO.read(new File(landOceanFilepath));
                    int totalPixels = rawTerrainImg.getWidth() * rawTerrainImg.getHeight();
                    ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
                    for(int x = 0; x < rawTerrainImg.getWidth(); x++){
                        for(int y = 0; y < rawTerrainImg.getHeight(); y++){
                            PixelWorkerThread thread = new PixelWorkerThread(
                                x,
                                y,
                                rawTerrainImg,
                                outImage,
                                highContrastOutImage,
                                horizontalThird,
                                verticalThird,
                                provinceCenterList,
                                oceanCenterList,
                                provinceCenterListMap,
                                oceanCenterListMap,
                                provinceIdColorMap
                            );
                            executorService.submit(thread);
                        }
                    }
                    while(executorService.getQueue().size() > 0){
                        System.out.print("\rProgress: " + ((float)progressIncrementer.get() / (float)totalPixels) + " (queue size: " + executorService.getQueue().size() + ")     ");
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("\rProgress: 1.0                                             ");
                    //
                    //second phase to clean up x-crossings
                    //x-crossings are when four provinces share the same corner. We're gonna fix this by merging the upper two
                    for(int x = 0; x < width; x++){
                        for(int y = 0; y < height; y++){
                            //if there can be an X in the first place
                            if(x < width - 1 && y < height - 1){
                                int color00 = outImage.getRGB(x, y);
                                int color10 = outImage.getRGB(x + 1, y);
                                int color01 = outImage.getRGB(x, y + 1);
                                int color11 = outImage.getRGB(x + 1, y + 1);
                                if(color00 != color10 && 
                                color00 != color01 && 
                                color00 != color11 && 
                                color10 != color01 &&
                                color10 != color11 &&
                                color01 != color11){
                                    outImage.setRGB(x+1, y, outImage.getRGB(x, y));
                                }
                            }
                        }
                    }
                    //
                    //emit image to file
                    System.out.println("Finished!");
                    executorService.shutdown();

                    System.out.println("Writing " + provincesOutputPath);
                    Utils.writePngNoCompression(outImage,provincesOutputPath);

                    System.out.println("Writing " + highContrastOutputPath);
                    ImageIO.write(highContrastOutImage,"png",Files.newOutputStream(new File(highContrastOutputPath).toPath()));
                } else {
                    System.out.println("Reading province images");
                    System.out.println("Reading " + provincesOutputPath);
                    outImage = ImageIO.read(new File(provincesOutputPath));
                    System.out.println("Reading " + highContrastOutputPath);
                    highContrastOutImage = ImageIO.read(new File(highContrastOutputPath));
                }

                String finalProvincesPath = config.getModDirectory() + "/map/provinces.bmp";
                {
                    System.out.println("Converting " + provincesOutputPath + " -> " + finalProvincesPath);
                    ProcessBuilder builder = new ProcessBuilder(
                        "java",
                        "-jar",
                        "\"" + config.getImageConverterJarPath() + "\"",
                        "-i",
                        "\"" + provincesOutputPath + "\"",
                        "-o",
                        "\"" + finalProvincesPath + "\""
                    );
                    builder.inheritIO().start();
                    System.out.println("Finished writing image");
                }


                //
                //terrain type
                //
                //read terrain map
                String textureMapPath = config.getSourceDirectory() + "/" + config.getTerrainTextureMapFilename();
                System.out.println("Reading " + textureMapPath);
                TerrainMap terrainMap = gson.fromJson(Files.readString(new File(textureMapPath).toPath()), TerrainMap.class);
                Map<Integer,TerrainType> terrainTypeMap = new HashMap<Integer,TerrainType>();
                List<TerrainType> validClamps = new LinkedList<TerrainType>();
                for(TerrainType type : terrainMap.getMap()){
                    terrainTypeMap.put(type.getId(), type);
                    if(type.getSourceColor().size() > 0){
                        validClamps.add(type);
                    }
                }
                //load terrain images
                String terrainTextureImagePath = config.getSourceDirectory() + "/" + config.getTerrainTypeFilename();
                System.out.println("Reading terrain texture image " + terrainTextureImagePath);
                BufferedImage terrainImage = ImageIO.read(new File(terrainTextureImagePath));
                //clamp output image
                for(int x = 0; x < terrainImage.getWidth(); x++){
                    for(int y = 0; y < terrainImage.getHeight(); y++){
                        int rgb = terrainImage.getRGB(x, y);
                        //get color of source terrain image
                        int blue = rgb & 0xff;
                        int green = (rgb & 0xff00) >> 8;
                        int red = (rgb & 0xff0000) >> 16;
                        int resistance = 99999;
                        TerrainType leastResistanceClamp = null;
                        //find terrain type that is closes to this color
                        for(TerrainType type : validClamps){
                            int calculatedResistance = Math.abs(type.getSourceColor().get(0) - red) + Math.abs(type.getSourceColor().get(1) - green) + Math.abs(type.getSourceColor().get(2) - blue);
                            if(calculatedResistance < resistance){
                                resistance = calculatedResistance;
                                leastResistanceClamp = type;
                            }
                        }
                        //set color to closest terrain type
                        red = leastResistanceClamp.getBaseColor().get(0);
                        green = leastResistanceClamp.getBaseColor().get(1);
                        blue = leastResistanceClamp.getBaseColor().get(2);
                        //set output color
                        terrainImage.setRGB(x, y, Utils.getIntFromColor(leastResistanceClamp.getId(), leastResistanceClamp.getId(), leastResistanceClamp.getId()));
                    }
                }
                //construct colormap
                ByteBuffer terrainColorMapBuffer = ByteBuffer.allocate(256 * 4);
                for(int i = 0; i < 256; i++){
                    if(terrainTypeMap.containsKey(i)){
                        TerrainType type = terrainTypeMap.get(i);
                        terrainColorMapBuffer.put((byte)(int)type.getBaseColor().get(2));
                        terrainColorMapBuffer.put((byte)(int)type.getBaseColor().get(1));
                        terrainColorMapBuffer.put((byte)(int)type.getBaseColor().get(0));
                        terrainColorMapBuffer.put((byte)0);
                    } else {
                        terrainColorMapBuffer.put((byte)i);
                        terrainColorMapBuffer.put((byte)i);
                        terrainColorMapBuffer.put((byte)i);
                        terrainColorMapBuffer.put((byte)0);
                    }
                }
                terrainColorMapBuffer.flip();
                //write
                String terrainTextureFinalPath = config.getModDirectory() + "/map/terrain.bmp";
                System.out.println("Writing terrain texture image " + terrainTextureFinalPath);
                BMPWriter.writeBMP(new File(terrainTextureFinalPath), terrainImage, "8bitgrayscale", terrainColorMapBuffer);

                //set continents
                String continentsImagePath = config.getSourceDirectory() + "/" + config.getContinentsFilename();
                System.out.println("Reading continents image " + continentsImagePath);
                continentsImage = ImageIO.read(new File(continentsImagePath));
                for(Province province : provinceList){
                    if(province.getType().equals("land")){
                        int rgb = continentsImage.getRGB(province.getX(), province.getY());
                        int continent = 0;
                        boolean newContinent = true;
                        for(int currentContinent : continentsDiscovered){
                            if(rgb == currentContinent){
                                newContinent = false;
                                continent = continentsDiscovered.indexOf(currentContinent)+1;
                            }
                        }
                        if(newContinent){
                            continentsDiscovered.add(rgb);
                            continent = continentsDiscovered.indexOf(rgb)+1;
                        }
                        province.setContinent(continent);
                    }
                }
                System.out.println("Continents: " + continentsDiscovered.size());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //set width/height if they haven't already been
            if(width == 0 && mapDataCache != null){
                width = mapDataCache.getMapWidth();
            }
            if(height == 0 && mapDataCache != null){
                height = mapDataCache.getMapHeight();
            }

            //write map data cache now that we have parsed once
            System.out.println("Writing map data cache");
            mapDataCache = new MapDataCache();
            mapDataCache.setProvinceCenterList(provinceCenterList);
            mapDataCache.setOceanCenterList(oceanCenterList);
            mapDataCache.setProvinceCenterListMap(provinceCenterListMap);
            mapDataCache.setOceanCenterListMap(oceanCenterListMap);
            mapDataCache.setContinentsDiscovered(continentsDiscovered);
            mapDataCache.setProvinceList(provinceList);
            mapDataCache.setProvinceIdColorMap(provinceIdColorMap);
            mapDataCache.setMapWidth(width);
            mapDataCache.setMapHeight(height);
            try {
                System.out.println("Writing map data cache " + MAP_DATA_CACHE_DEFAULT_PATH);
                Files.writeString(new File(MAP_DATA_CACHE_DEFAULT_PATH).toPath(),gson.toJson(mapDataCache));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Loading map data cache");
            provinceCenterList = mapDataCache.getProvinceCenterList();
            oceanCenterList = mapDataCache.getOceanCenterList();
            provinceCenterListMap = mapDataCache.getProvinceCenterListMap();
            oceanCenterListMap = mapDataCache.getOceanCenterListMap();
            continentsDiscovered = mapDataCache.getContinentsDiscovered();
            provinceList = mapDataCache.getProvinceList();
            provinceIdColorMap = mapDataCache.getProvinceIdColorMap();
            width = mapDataCache.getMapWidth();
            height = mapDataCache.getMapHeight();
        }
        
        //get province list
        if(mapDataCache != null && mapDataCache.getProvinceList() != null){
            provinceList = mapDataCache.getProvinceList();
        }

        //get province color map
        if(mapDataCache != null && mapDataCache.getProvinceIdColorMap() != null){
            provinceIdColorMap = mapDataCache.getProvinceIdColorMap();
        }



        //
        //Heightmap
        //
        {
            String heightmapInPath = config.getSourceDirectory() + "/" + config.getHeightmapFilename();
            String heightmapOutPath = config.getModDirectory() + "/map/heightmap.bmp";
            System.out.println("Converting " + heightmapInPath + " -> " + heightmapOutPath);
            ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-jar",
                "\"" + config.getImageConverterJarPath() + "\"",
                "-i",
                "\"" + heightmapInPath + "\"",
                "-f",
                "8bitgrayscale",
                "-o",
                "\"" + heightmapOutPath + "\""
            );
            try {
                builder.inheritIO().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }





        //
        //Color map
        //
        BufferedImage colorsImage = null;
        BufferedImage lightMapImage = null;
        try {
            File colorsFile = new File(config.getSourceDirectory() + "/" + config.getTerrainColorsFilename());
            System.out.println("Reading colors file " + colorsFile);
            colorsImage = ImageIO.read(colorsFile);

            File lightmapFile = new File(config.getSourceDirectory() + "/" + config.getTerrainLightmapFilename());
            System.out.println("Reading lightmap file " + lightmapFile);
            lightMapImage = ImageIO.read(lightmapFile);
        } catch (IOException e){
            System.err.println("Failed to read file");
            e.printStackTrace();
        }
        if(colorsImage != null & lightMapImage != null){
            for(int x = 0; x < colorsImage.getWidth(); x++){
                for(int y = 0; y < colorsImage.getHeight(); y++){
                    int rgb = colorsImage.getRGB(x, y);
                    //get color of source terrain image
                    int blue = rgb & 0xff;
                    int green = (rgb & 0xff00) >> 8;
                    int red = (rgb & 0xff0000) >> 16;
                    //color map alpha should be light map red value
                    int alpha = (lightMapImage.getRGB(x,y) & 0xff0000) >> 16;
                    colorsImage.setRGB(x, y, Utils.getIntFromColor(red, green, blue, alpha));
                }
            }
            //write color map
            try {
                String colorsInFilePath = config.getModDirectory() + "/map/terrain/" + config.getTerrainColorsFilename();
                String colorsOutFilepath = config.getModDirectory() + "/map/terrain/colormap_rgb_cityemissivemask_a.dds";

                System.out.println("Writing " + colorsInFilePath);
                ImageIO.write(colorsImage, "png", Files.newOutputStream(new File(colorsInFilePath).toPath()));

                System.out.println("Converting " + colorsInFilePath + " -> " + colorsOutFilepath);
                //convert to 32bitargb in converter helper
                ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    "-jar",
                    "\"" + config.getImageConverterJarPath() + "\"",
                    "-i",
                    "\"" + colorsInFilePath + "\"",
                    "-f",
                    "32bitargb",
                    "-o",
                    "\"" + colorsOutFilepath + "\""
                );
                builder.inheritIO().start();
            } catch (IOException e) {
                System.err.println("Failed to write terrain colors");
                e.printStackTrace();
            }
        }












        //
        //ocean color map
        //
        if(rawTerrainImg != null){
            BufferedImage oceanColorMap = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            Color landColor = new Color(247, 192, 141);
            Color oceanColor = new Color(54, 92, 145);
            for(int x = 0; x < oceanColorMap.getWidth(); x++){
                for(int y = 0; y < oceanColorMap.getHeight(); y++){
                    Color c = new Color(rawTerrainImg.getRGB(x, y), true);
                    if(c.getAlpha() < 10){
                        oceanColorMap.setRGB(x,y,landColor.getRGB());
                    } else {
                        oceanColorMap.setRGB(x,y,oceanColor.getRGB());
                    }
                }
            }
            //write color map
            try {
                String oceanColorsInFilepath = config.getModDirectory() + "/map/terrain/" + config.getOceanColorsFilename();
                String oceanColorsOutFilepath = config.getModDirectory() + "/map/terrain/colormap_water_0.dds";

                System.out.println("Writing " + oceanColorsInFilepath);
                ImageIO.write(oceanColorMap, "png", Files.newOutputStream(new File(oceanColorsInFilepath).toPath()));

                System.out.println("Converting " + oceanColorsInFilepath + " -> " + oceanColorsOutFilepath);
                //convert to 32bitargb in converter helper
                ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    "-jar",
                    "\"" + config.getImageConverterJarPath() + "\"",
                    "-i",
                    "\"" + oceanColorsInFilepath + "\"",
                    "-f",
                    "32bitargb",
                    "-o",
                    "\"" + oceanColorsOutFilepath + "\""
                );
                builder.redirectErrorStream(true);
                builder.start();
            } catch (IOException e) {
                System.err.println("Failed to write ocean colors");
                e.printStackTrace();
            }
        }




















        //
        // Calculate states
        //

        //build province point list
        List<de.alsclo.voronoi.graph.Point> provinceVoronoiPoints = provinceList.stream().map(province -> new de.alsclo.voronoi.graph.Point(province.getX(),province.getY())).toList();

        //build voronoi -> original point index map
        Map<de.alsclo.voronoi.graph.Point,Integer> pointIndexMap = new HashMap<de.alsclo.voronoi.graph.Point,Integer>();
        int voronoiIndex = 0;
        for(de.alsclo.voronoi.graph.Point voronoiPoint : provinceVoronoiPoints){
            pointIndexMap.put(voronoiPoint,voronoiIndex);
            voronoiIndex++;
        }

        //fortune's algorithm
        Voronoi voronoi = new Voronoi(provinceVoronoiPoints);
        Graph graph = voronoi.getGraph();

        //get adjacencies in more convenient datastructures
        int edgeCount = 0;
        List<Edge> edgeList = graph.edgeStream().toList();
        for(Edge edge : edgeList){
            edgeCount++;
            int site1Index = pointIndexMap.get(edge.getSite1());
            int site2Index = pointIndexMap.get(edge.getSite2());
            provinceList.get(site1Index).addNeighbor(provinceList.get(site2Index).getId());
            provinceList.get(site2Index).addNeighbor(provinceList.get(site1Index).getId());
        }
        System.out.println("Edge count: " + edgeCount);

        //construct province id->province map
        Map<Integer,Province> idProvinceMap = new HashMap<Integer,Province>();
        for(Province province : provinceList){
            idProvinceMap.put(province.getId(),province);
        }

        //evaluate province data based on neighbors
        for(Province province : provinceList){
            //evaluate coastal status before emitting
            if(province.getType().contains("land")){
                for(int neighborId : province.getNeighbors()){
                    Province neighbor = idProvinceMap.get(neighborId);
                    if(!neighbor.getType().contains("land")){
                        province.setCoastalStatus(true);
                    }
                }
            }
        }

        List<State> states = new LinkedList<State>();
        //calculate states based on adjacencies
        //closed set of indices that have already been added to a state
        List<Integer> closedSet = new LinkedList<Integer>();
        //first try to construct ideal states of all neighbors where state is 6+ provinces
        for(int i = 0; i < provinceVoronoiPoints.size(); i++){
            //IDs start at 1, ergo must add 1 to the incremented value
            Province currentProvince = idProvinceMap.get(i+1);
            if(currentProvince.getType().contains("land") && !closedSet.contains(currentProvince.getId())){
                List<Integer> neighbors = currentProvince.getNeighbors();
                if(neighbors != null){
                    List<Integer> stateMemberList = new LinkedList<Integer>();
                    stateMemberList.add(currentProvince.getId());
                    closedSet.add(currentProvince.getId());

                    //add all neighbors of the province that was just added
                    for(int neighborId : neighbors){
                        Province currentNeighbor = idProvinceMap.get(neighborId);
                        if(
                            !closedSet.contains(currentNeighbor.getId()) &&
                            currentNeighbor.getType().contains("land")
                        ){
                            if(!closedSet.contains(currentNeighbor.getId()) && stateMemberList.contains(currentNeighbor.getId())){
                                throw new Error("Closed set does not contain index that was already assigned!");
                            }
                            stateMemberList.add(currentNeighbor.getId());
                            closedSet.add(currentNeighbor.getId());
                        }
                    }

                    if(stateMemberList.size() < TARGET_PROVINCES_PER_STATE){
                        //have not added enough provinces yet, try adding more
                        for(int neighborId : neighbors){
                            Province currentNeighbor = idProvinceMap.get(neighborId);
                            if(stateMemberList.size() >= TARGET_PROVINCES_PER_STATE){
                                break;
                            }
                            List<Integer> extendedNeighbors = idProvinceMap.get(currentNeighbor.getId()).getNeighbors();
                            if(
                                extendedNeighbors != null &&
                                currentNeighbor.getType().contains("land")
                            ){
                                for(int neighborsNeighbor : extendedNeighbors){
                                    Province neighborsNeighborObj = idProvinceMap.get(neighborsNeighbor);
                                    if(
                                        !closedSet.contains(neighborsNeighborObj.getId()) &&
                                        !stateMemberList.contains(neighborsNeighborObj.getId()) &&
                                        neighborsNeighborObj.getType().contains("land") &&
                                        stateMemberList.size() < 10
                                    ){
                                        if(!closedSet.contains(neighborsNeighborObj.getId()) && stateMemberList.contains(neighborsNeighborObj.getId())){
                                            throw new Error("Closed set does not contain index that was already assigned!");
                                        }
                                        stateMemberList.add(neighborsNeighborObj.getId());
                                        closedSet.add(neighborsNeighborObj.getId());
                                        // System.out.println("Bonus province");
                                    }
                                }
                            }
                        }
                    }
                    states.add(new State(states.size() + 1,stateMemberList, true));
                }
            }
        }
        System.out.println("Number of states final: " + states.size());
        //emit states as state files
        int defaultManpower = 500000;
        String defaultCategory = "town";
        //optional stuff
        String resources = "steel = 10 aluminium = 10 rubber = 10 tungsten = 10 chromium = 10 oil = 10";
        float localSupplies = 10;
        //clear existing states
        for(File childFile : new File(config.getModDirectory() + "/history/states").listFiles()){
            if(!childFile.isDirectory()){
                childFile.delete();
            }
        }
        for(State state : states){
            if(state.isLand()){
                String provinces = "";
                for(int provinceId : state.getProvinces()){
                    provinces = provinces + "    " + provinceId;
                }
                StringBuilder builder = new StringBuilder("");
                builder.append("state = {\n");
                builder.append("    id=" + state.getId() + "\n");
                builder.append("    name=\"STATE_" + state.getId() + "\"\n");
                builder.append("    manpower=" + defaultManpower + "\n");
                builder.append("    state_category=" + defaultCategory + "\n");
                builder.append("    provinces={\n");
                builder.append("    " + provinces + "\n");
                builder.append("    }\n");
                builder.append("    history={\n");
                builder.append("        owner = AFG\n");
                builder.append("    }\n");
                builder.append("    resources={" + resources + "}\n");
                builder.append("    local_supplies=" + localSupplies + "\n");
                builder.append("}");
                //write to file
                try {
                    Files.write(new File(config.getModDirectory() + "/history/states/STATE_" + state.getId() + ".txt").toPath(), builder.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }









        //
        //Emit province definitions
        //
        //error check province list
        if(
            provinceList.size() != provinceCenterList.size() + oceanCenterList.size()
        ){
            throw new Error("Province list does not include correct centers! " + provinceList.size() + " " + provinceCenterList.size() + " " + oceanCenterList.size());
        }
        if(provinceList.size() != provinceIdColorMap.values().size()){
            throw new Error("Province list does not contain correct colors! " + provinceList.size() + " " + provinceIdColorMap.values().size());
        }
        //output province csv
        StringBuilder provinceCSVBuilder = new StringBuilder("0;0;0;0;land;false;unknown;0\r\n");
        for(Province province : provinceList){
            provinceCSVBuilder.append(province.getId());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getRed());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getGreen());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getBlue());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getType());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getCoastalStatus());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getTerrain());
            provinceCSVBuilder.append(";");
            provinceCSVBuilder.append(province.getContinent());
            provinceCSVBuilder.append("\r\n");
        }
        String output = provinceCSVBuilder.toString();
        try {
            String provinceDefinitionPath = config.getModDirectory() + "/map/definition.csv";
            System.out.println("Writing province definition " + provinceDefinitionPath);
            Files.write(new File(provinceDefinitionPath).toPath(), output.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }













        //
        //generate strategic regions
        //
        {
            StringBuilder builder = new StringBuilder("");
            builder.append("strategic_region={\n");
            builder.append("    id=1\n");
            builder.append("    name=\"STRATEGICREGION_1\"\n");
            builder.append("    provinces = {\n");
            builder.append("        ");
            for(int i = 0; i <= provinceList.size(); i++){
                builder.append(i + " ");
            }
            builder.append("\n");
            builder.append("    }\n");
            builder.append("    weather={\n");
            builder.append("        period={\n");
            builder.append("            between={ 0.0 30.0 }\n");
            builder.append("            temperature={ -5.0 14.0 }\n");
            builder.append("            no_phenomenon=0.700\n");
            builder.append("            rain_light=0.200\n");
            builder.append("            rain_heavy=0.080\n");
            builder.append("            snow=0.020\n");
            builder.append("            blizzard=0.000\n");
            builder.append("            arctic_water=0.000\n");
            builder.append("            mud=0.000\n");
            builder.append("            sandstorm=0.000\n");
            builder.append("            min_snow_level=0.000\n");
            builder.append("        }\n");
            builder.append("    }\n");
            builder.append("}\n");
            try {
                String stratRegionFilepath = config.getModDirectory() + "/map/strategicregions/0-STRAT_REGION_LAND.txt";
                System.out.println("Writing strategic region! " + stratRegionFilepath);
                Files.writeString(new File(stratRegionFilepath).toPath(),builder.toString());
            } catch (IOException ex){
                ex.printStackTrace();
            }

            //try writing weather positions
            try {
                String content = "1;2781.24;9.90;1571.49;small";
                String weatherPositionsFilepath = config.getModDirectory() + "/map/weatherpositions.txt";
                System.out.println("Writing weather positions! " + weatherPositionsFilepath);
                Files.writeString(new File(weatherPositionsFilepath).toPath(),content);
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }

        //
        //generate buildings for each state
        //
        for(State state : states){
            state.generateBuildings(provinceList, idProvinceMap);
        }

        //
        //emit buildings
        //
        {

            StringBuilder buildingCSVBuilder = new StringBuilder("");
            for(State state : states){
                for(Building building : state.getBuildings()){
                    int buildingHeight = 10;
                    double buildingRotation = 0.0;
                    int adjacentSeaProvince = 0;
                    buildingCSVBuilder.append(state.getId());
                    buildingCSVBuilder.append(";");
                    buildingCSVBuilder.append(building.getType());
                    buildingCSVBuilder.append(";");
                    buildingCSVBuilder.append(building.getLocation().getX());
                    buildingCSVBuilder.append(";");
                    buildingCSVBuilder.append(buildingHeight);
                    buildingCSVBuilder.append(";");
                    buildingCSVBuilder.append(height - building.getLocation().getY());
                    buildingCSVBuilder.append(";");
                    buildingCSVBuilder.append(buildingRotation);
                    buildingCSVBuilder.append(";");
                    buildingCSVBuilder.append(adjacentSeaProvince);
                    buildingCSVBuilder.append("\r\n");
                }
            }
            output = buildingCSVBuilder.toString();
            try {
                String buildingsFilePath = config.getModDirectory() + "/map/buildings.txt";
                System.out.println("Writing buildings file " + buildingsFilePath);
                Files.writeString(new File(buildingsFilePath).toPath(),output);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //
        //Emit air bases
        //
        {
            StringBuilder airbaseBuilder = new StringBuilder("");
            for(State state : states){
                for(Building building : state.getBuildings()){
                    if(building.getType().contentEquals("air_base")){
                        airbaseBuilder.append(state.getId());
                        airbaseBuilder.append("=");
                        airbaseBuilder.append("{" + building.getProvinceId() + "}");
                        airbaseBuilder.append("\r\n");
                    }
                }
            }
            output = airbaseBuilder.toString();
            try {
                String buildingsFilePath = config.getModDirectory() + "/map/airports.txt";
                System.out.println("Writing airports file " + buildingsFilePath);
                Files.writeString(new File(buildingsFilePath).toPath(),output);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //
        //Emit rocket sites
        //
        {
            StringBuilder airbaseBuilder = new StringBuilder("");
            for(State state : states){
                for(Building building : state.getBuildings()){
                    if(building.getType().contentEquals("rocket_site")){
                        airbaseBuilder.append(state.getId());
                        airbaseBuilder.append("=");
                        airbaseBuilder.append("{" + building.getProvinceId() + "}");
                        airbaseBuilder.append("\r\n");
                    }
                }
            }
            output = airbaseBuilder.toString();
            try {
                String buildingsFilePath = config.getModDirectory() + "/map/rocketsites.txt";
                System.out.println("Writing rocketsites file " + buildingsFilePath);
                Files.writeString(new File(buildingsFilePath).toPath(),output);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //add all building files as skeletons
        try {
            Files.writeString(new File(config.getModDirectory() + "/map/adjacencies.csv").toPath(),"");
            Files.writeString(new File(config.getModDirectory() + "/map/adjacency_rules.txt").toPath(),"");
            Files.writeString(new File(config.getModDirectory() + "/map/railways.txt").toPath(),"");
            Files.writeString(new File(config.getModDirectory() + "/map/supply_nodes.txt").toPath(),"");
            Files.writeString(new File(config.getModDirectory() + "/map/unitstacks.txt").toPath(),"");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static Color getUnusedColor(){
        int red = 0;
        int green = 0;
        int blue = 0;
        boolean colorValid = false;
        while(!colorValid){
            red = colorRandom.nextInt(255);
            green = colorRandom.nextInt(255);
            blue = colorRandom.nextInt(255);
            if(!colorUseMap.containsKey(red + "-" + green + "-" + blue)){
                colorValid = true;
            }
        }
        colorUseMap.put(red + "-" + green + "-" + blue, true);
        Color rVal = new Color(red, green, blue);
        return rVal;
    }

    /**
     * Checks if the province parsing should be running
     * @return true if should run, false otherwise
     */
    static boolean shouldRunProvinceParsing(Config config, MapDataCache mapDataCache){
        String provincesOutputPath = config.getModDirectory() + "/map/" + config.getOutProvincesFilename();
            String highContrastOutputPath = config.getModDirectory() + "/map/" + config.getOutProvincesHighContrastFilename();
            String finalProvincesPath = config.getModDirectory() + "/map/provinces.bmp";
        return
        mapDataCache == null ||
        mapDataCache.getProvinceCenterList() == null ||
        mapDataCache.getProvinceCenterListMap() == null ||
        mapDataCache.getOceanCenterList() == null ||
        mapDataCache.getOceanCenterListMap() == null ||
        new File(provincesOutputPath).exists() == false ||
        new File(highContrastOutputPath).exists() == false ||
        new File(finalProvincesPath).exists() == false
        ;
    }

    

}