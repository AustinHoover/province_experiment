package electrosphere;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Collection;
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

import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Edge;
import de.alsclo.voronoi.graph.Graph;
import electrosphere.bmp.BMPWriter;
import electrosphere.province.Province;
import electrosphere.province.TerrainMap;
import electrosphere.province.TerrainType;
import electrosphere.state.State;
import electrosphere.threads.PixelWorkerThread;

import java.awt.Color;
import java.awt.Point;

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
    static Map<Integer, Color> provinceIdColorMap = new HashMap<Integer, Color>();

    static List<Province> provinceList = new LinkedList<Province>();

    public static void main(String[] args){
        System.out.println("it lives!");

        Gson gson = new Gson();
        
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

        try {
            provinceImg = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\province_points.png"));
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
                            provinceList.add(new Province(newPoint.x,newPoint.y,provinceList.size()+1,colors[0],colors[1],colors[2],"land",false,"unknown",0));
                            provinceIdColorMap.put(provinceList.size(),getUnusedColor());
                        }
                    }
                }
            }
            System.out.println("Discovered " + provinceCenterList.size() + " provinces!");

            int incrementerForColor = 0;
            oceanImg = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\ocean_points.png"));
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
                            provinceList.add(new Province(newPoint.x,newPoint.y,provinceList.size()+1,colors[0],colors[1],colors[2],"sea",false,"unknown",0));
                            provinceIdColorMap.put(provinceList.size(),getUnusedColor());
                            incrementerForColor++;
                        }
                    }
                }
            }
            System.out.println("Discovered " + oceanCenterList.size() + " ocean tiles!");

            rawTerrainImg = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\land_vs_ocean.png"));
            BufferedImage outImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            BufferedImage highContrastOutImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
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
            Utils.writePngNoCompression(outImage,"C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\provinces.png");
            ImageIO.write(highContrastOutImage,"png",Files.newOutputStream(new File("C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\provinces_high_contrast.png").toPath()));
            ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-jar",
                "~/Documents/hoi4ide-imagemanipulator/target/hoi4ide-imagemanipulator-1.0-SNAPSHOT-jar-with-dependencies.jar",
                "-i",
                "C:/Users/satellite/Documents/Paradox Interactive/Hearts of Iron IV/mod/overhaul1/map/provinces.png",
                "-o",
                "C:/Users/satellite/Documents/Paradox Interactive/Hearts of Iron IV/mod/overhaul1/map/provinces.bmp"
            );
            builder.redirectErrorStream(true);
            builder.start();
            System.out.println("Finished writing image");


            //
            //terrain type
            //
            //read terrain map
            TerrainMap terrainMap = gson.fromJson(Files.readString(new File("C:\\Users\\satellite\\p\\overhaul1\\terrainTextureMap.json").toPath()), TerrainMap.class);
            Map<Integer,TerrainType> terrainTypeMap = new HashMap<Integer,TerrainType>();
            List<TerrainType> validClamps = new LinkedList<TerrainType>();
            for(TerrainType type : terrainMap.getMap()){
                terrainTypeMap.put(type.getId(), type);
                if(type.getSourceColor().size() > 0){
                    validClamps.add(type);
                }
            }
            //load terrain images
            BufferedImage terrainImage = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\terrain.png"));
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
            BMPWriter.writeBMP(new File("C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain.bmp"), terrainImage, "8bitgrayscale", terrainColorMapBuffer);

            //set continents
            continentsImage = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\continents.png"));
            List<Integer> continentsDiscovered = new LinkedList<Integer>();
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
        String output = provinceCSVBuilder.toString();//.substring(0, provinceCSVBuilder.length() - 2);
        try {
            Files.write(new File("C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\definition.csv").toPath(), output.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }



        //
        //Heightmap
        //
        {
            ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-jar",
                "~/Documents/hoi4ide-imagemanipulator/target/hoi4ide-imagemanipulator-1.0-SNAPSHOT-jar-with-dependencies.jar",
                "-i",
                "C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\heightmap.png",
                "-f",
                "8bitgrayscale",
                "-o",
                "C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\heightmap.bmp"
            );
            builder.redirectErrorStream(true);
            try {
                builder.start();
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
            colorsImage = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\colors.png"));
            lightMapImage = ImageIO.read(new File("C:\\Users\\satellite\\p\\overhaul1\\lightmap.png"));
        } catch (IOException e){
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
                ImageIO.write(colorsImage, "png", Files.newOutputStream(new File("C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain\\colors.png").toPath()));
                //convert to 32bitargb in converter helper
                ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    "-jar",
                    "~/Documents/hoi4/hoi4ide-imagemanipulator/target/hoi4ide-imagemanipulator-1.0-SNAPSHOT-jar-with-dependencies.jar",
                    "-i",
                    "C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain\\colors.png",
                    "-f",
                    "32bitargb",
                    "-o",
                    "C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain\\colormap_rgb_cityemissivemask_a.dds"
                );
                builder.redirectErrorStream(true);
                builder.start();
            } catch (IOException e) {
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
                ImageIO.write(oceanColorMap, "png", Files.newOutputStream(new File("C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain\\ocean_colors.png").toPath()));
                //convert to 32bitargb in converter helper
                ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    "-jar",
                    "~/Documents/hoi4/hoi4ide-imagemanipulator/target/hoi4ide-imagemanipulator-1.0-SNAPSHOT-jar-with-dependencies.jar",
                    "-i",
                    "C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain\\ocean_colors.png",
                    "-f",
                    "32bitargb",
                    "-o",
                    "C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\map\\terrain\\colormap_water_0.dds"
                );
                builder.redirectErrorStream(true);
                builder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




















        //
        // Calculate states
        //

        //build province point list
        List<de.alsclo.voronoi.graph.Point> provinceVoronoiPoints = provinceCenterList.stream().map(point -> new de.alsclo.voronoi.graph.Point(point.x,point.y)).toList();
        int landCutoff = provinceVoronoiPoints.size();
        List<de.alsclo.voronoi.graph.Point> oceanVoronoiPoints = oceanCenterList.stream().map(point -> new de.alsclo.voronoi.graph.Point(point.x,point.y)).toList();
        Collection<de.alsclo.voronoi.graph.Point> voronoiPoints = new LinkedList<de.alsclo.voronoi.graph.Point>();
        voronoiPoints.addAll(provinceVoronoiPoints);
        voronoiPoints.addAll(oceanVoronoiPoints);

        //build voronoi -> original point index map
        Map<de.alsclo.voronoi.graph.Point,Integer> pointIndexMap = new HashMap<de.alsclo.voronoi.graph.Point,Integer>();
        int voronoiIndex = 0;
        for(de.alsclo.voronoi.graph.Point voronoiPoint : voronoiPoints){
            pointIndexMap.put(voronoiPoint,voronoiIndex);
            voronoiIndex++;
        }

        //fortune's algorithm
        Voronoi voronoi = new Voronoi(voronoiPoints);
        Graph graph = voronoi.getGraph();

        //get adjacencies in more convenient datastructures
        int edgeCount = 0;
        Map<Integer,List<Integer>> adjacencyMap = new HashMap<Integer,List<Integer>>();
        List<Edge> edgeList = graph.edgeStream().toList();
        for(Edge edge : edgeList){
            edgeCount++;
            int site1Index = pointIndexMap.get(edge.getSite1());
            int site2Index = pointIndexMap.get(edge.getSite2());

            if(adjacencyMap.containsKey(site1Index)){
                adjacencyMap.get(site1Index).add(site2Index);
            } else {
                List<Integer> adjacencies = new LinkedList<Integer>();
                adjacencies.add(site2Index);
                adjacencyMap.put(site1Index,adjacencies);
            }

            if(adjacencyMap.containsKey(site2Index)){
                adjacencyMap.get(site2Index).add(site1Index);
            } else {
                List<Integer> adjacencies = new LinkedList<Integer>();
                adjacencies.add(site1Index);
                adjacencyMap.put(site2Index,adjacencies);
            }
        }
        System.out.println("Edge count: " + edgeCount);

        List<State> states = new LinkedList<State>();
        //calculate states based on adjacencies
        //closed set of indices that have already been added to a state
        List<Integer> closedSet = new LinkedList<Integer>();
        //first try to construct ideal states of all neighbors where state is 6+ provinces
        for(int i = 0; i < voronoiPoints.size(); i++){
            if(!closedSet.contains(i)){
                List<Integer> adjacencies = adjacencyMap.get(i);
                if(i < landCutoff){
                    //it's land
                    if(adjacencies != null){
                        List<Integer> provinceList = new LinkedList<Integer>();
                        provinceList.add(i + 1);
                        for(int adjacentIndex : adjacencies){
                            if(!closedSet.contains(adjacentIndex) && adjacentIndex < landCutoff){
                                provinceList.add(adjacentIndex + 1);
                            }
                        }
                        if(provinceList.size() > 5){
                            for(int toClose : provinceList){
                                closedSet.add(toClose - 1);
                            }
                            states.add(new State(provinceList, true));
                        } else {
                            for(int adjacentIndex : adjacencies){
                                List<Integer> extendedNeighbors = adjacencyMap.get(adjacentIndex);
                                if(extendedNeighbors != null && adjacentIndex < landCutoff){
                                    for(int neighborsNeighbor : extendedNeighbors){
                                        if(!closedSet.contains(neighborsNeighbor) && !provinceList.contains(neighborsNeighbor) && neighborsNeighbor < landCutoff && provinceList.size() < 10){
                                            provinceList.add(neighborsNeighbor + 1);
                                            // System.out.println("Bonus province");
                                        }
                                    }
                                }
                            }
                            if(provinceList.size() > 5){
                                for(int toClose : provinceList){
                                    closedSet.add(toClose - 1);
                                }
                                states.add(new State(provinceList, true));
                            }
                        }
                    }
                }
                //  else {
                //     //it's ocean
                //     int numOceanAdjacent = 0;
                //     if(adjacencies != null){
                //         List<Integer> provinceList = new LinkedList<Integer>();
                //         provinceList.add(i + 1);
                //         for(int adjacentIndex : adjacencies){
                //             if(!closedSet.contains(adjacentIndex) && adjacentIndex >= landCutoff){
                //                 numOceanAdjacent++;
                //                 provinceList.add(adjacentIndex + 1);
                //             }
                //         }
                //         if(numOceanAdjacent > 5){
                //             for(int toClose : provinceList){
                //                 closedSet.add(toClose - 1);
                //             }
                //             states.add(new State(provinceList, false));
                //         }
                //     }
                // }
            }
        }
        System.out.println("Number of states ideal: " + states.size());
        //then add remaining combinations of provinces as states
        for(int i = 0; i < voronoiPoints.size(); i++){
            if(!closedSet.contains(i)){
                List<Integer> adjacencies = adjacencyMap.get(i);
                if(i < landCutoff){
                    //it's land
                    if(adjacencies != null){
                        List<Integer> provinceList = new LinkedList<Integer>();
                        provinceList.add(i + 1);
                        for(int adjacentIndex : adjacencies){
                            if(!closedSet.contains(adjacentIndex) && adjacentIndex < landCutoff){
                                provinceList.add(adjacentIndex + 1);
                            }
                        }
                        for(int toClose : provinceList){
                            closedSet.add(toClose - 1);
                        }
                        states.add(new State(provinceList, true));
                    }
                }
                //  else {
                //     //it's ocean
                //     if(adjacencies != null){
                //         List<Integer> provinceList = new LinkedList<Integer>();
                //         provinceList.add(i + 1);
                //         for(int adjacentIndex : adjacencies){
                //             if(!closedSet.contains(adjacentIndex) && adjacentIndex >= landCutoff){
                //                 provinceList.add(adjacentIndex + 1);
                //             }
                //         }
                //         for(int toClose : provinceList){
                //             closedSet.add(toClose - 1);
                //         }
                //         states.add(new State(provinceList, false));
                //     }
                // }
            }
        }
        System.out.println("Number of states final: " + states.size());
        //emit states as state files
        int stateId = 0;
        int defaultManpower = 500000;
        String defaultCategory = "town";
        //optional stuff
        String resources = "steel = 10 aluminium = 10 rubber = 10 tungsten = 10 chromium = 10 oil = 10";
        float localSupplies = 10;
        for(State state : states){
            if(state.isLand()){
                String provinces = "";
                for(int provinceId : state.getProvinces()){
                    provinces = provinces + "    " + provinceId;
                }
                StringBuilder builder = new StringBuilder("");
                builder.append("state = {\n");
                builder.append("    id=" + stateId + "\n");
                builder.append("    name=\"STATE_" + stateId + "\"\n");
                builder.append("    manpower=" + defaultManpower + "\n");
                builder.append("    state_category=" + defaultCategory + "\n");
                builder.append("    provinces={\n");
                builder.append("    " + provinces + "\n");
                builder.append("    }\n");
                builder.append("    resources={" + resources + "}\n");
                builder.append("    local_supplies=" + localSupplies + "\n");
                builder.append("}");
                //write to file
                try {
                    Files.write(new File("C:\\Users\\satellite\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\overhaul1\\history\\states\\STATE_" + stateId + ".txt").toPath(), builder.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //increment
                stateId++;
            }
        }

        //
        //generate strategic regions
        //

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

    

}