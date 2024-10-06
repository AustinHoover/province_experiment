package electrosphere.config;

/**
 * Configuration for the program
 */
public class Config {

    /**
     * Default value for the source directory
     */
    public static final String DEFAULT_SORUCE_DIRECTORY = "~/Documents/Paradox Interactive/Hearts of Iron IV/mod/project1";

    /**
     * Default value for the live mod directory
     */
    public static final String DEFAULT_MOD_DIRECTORY = "~/Documents/Paradox Interactive/Hearts of Iron IV/mod/project1";

    /**
     * Default path to the image converter jar
     */
    public static final String DEFAULT_IMAGE_CONVERTER_JAR_PATH = "./imageconverter.jar";

    /**
     * Default filename for the province points image
     */
    public static final String DEFAULT_PROVINCE_POINTS_FILENAME = "province_points.png";

    /**
     * Default filename for the ocean points image
     */
    public static final String DEFAULT_OCEAN_POINTS_FILENAME = "ocean_points.png";

    /**
     * Default filename for the land vs ocean image
     */
    public static final String DEFAULT_LAND_OCEAN_FILENAME = "land_vs_ocean.png";

    /**
     * Default filename for the output provinces image
     */
    public static final String DEFAULT_OUT_PROVINCES_FILENAME = "provinces.png";

    /**
     * Default filename for the high contrast output provinces image
     */
    public static final String DEFAULT_OUT_PROVINCES_HIGH_CONTRAST_FILENAME = "provinces_high_contrast.png";

    /**
     * Default filename for the terrain texture map
     */
    public static final String DEFAULT_TERRAIN_TEXTURE_MAP_FILENAME = "terrainTextureMap.json";

    /**
     * Default filename for the terrain type image
     */
    public static final String DEFAULT_TERRAIN_TYPE_FILENAME = "terrain.png";

    /**
     * Default filename for the continents image
     */
    public static final String DEFAULT_CONTINENTS_FILENAME = "continents.png";

    /**
     * Default filename for the heightmap image
     */
    public static final String DEFAULT_HEIGHTMAP_FILENAME = "heightmap.png";

    /**
     * Default filename for the terrain colors image
     */
    public static final String DEFAULT_TERRAIN_COLORS_FILENAME = "colors.png";

    /**
     * Default filename for the terrain lightmap image
     */
    public static final String DEFAULT_TERRAIN_LIGHTMAP_FILENAME = "lightmap.png";

    /**
     * Default filename for the ocean colors image
     */
    public static final String DEFAULT_OCEAN_COLORS_FILENAME = "ocean_colors.png";
    
    /**
     * Path to the source project files directory
     */
    String sourceDirectory;

    /**
     * Path to the directory that contains actual live mod files
     */
    String modDirectory;

    /**
     * Path to the image converter jar
     */
    String imageConverterJarPath;

    /**
     * Filename of the province points image
     */
    String provincePointsFilename;

    /**
     * Filename of the ocean points image
     */
    String oceanPointsFilename;

    /**
     * Filename of the image that differentiates ocean from land
     */
    String landOceanFilename;

    /**
     * Filename of the provinces output image
     */
    String outProvincesFilename;

    /**
     * Filename of the high contrast provinces output image
     */
    String outProvincesHighContrastFilename;

    /**
     * Filename of the terrain texture map
     */
    String terrainTextureMapFilename;

    /**
     * Filename of the terrain type image
     */
    String terrainTypeFilename;

    /**
     * Filename of the continents image
     */
    String continentsFilename;

    /**
     * Filename of the heightmap image
     */
    String heightmapFilename;

    /**
     * Filename of the terrain color image
     */
    String terrainColorsFilename;

    /**
     * Filename of the terrain lightmap image
     */
    String terrainLightmapFilename;

    /**
     * Filename of the ocean colors image
     */
    String oceanColorsFilename;

    /**
     * Fills in values that are null (ie if the config is incomplete or failed to load)
     */
    public void fillInMissingValues(){
        if(this.sourceDirectory == null){
            this.sourceDirectory = DEFAULT_SORUCE_DIRECTORY;
        }
        if(this.modDirectory == null){
            this.modDirectory = DEFAULT_MOD_DIRECTORY;
        }
        if(this.imageConverterJarPath == null){
            this.imageConverterJarPath = DEFAULT_IMAGE_CONVERTER_JAR_PATH;
        }
        if(this.provincePointsFilename == null){
            this.provincePointsFilename = DEFAULT_PROVINCE_POINTS_FILENAME;
        }
        if(this.oceanPointsFilename == null){
            this.oceanPointsFilename = DEFAULT_OCEAN_POINTS_FILENAME;
        }
        if(this.landOceanFilename == null){
            this.landOceanFilename = DEFAULT_LAND_OCEAN_FILENAME;
        }
        if(this.outProvincesFilename == null){
            this.outProvincesFilename = DEFAULT_OUT_PROVINCES_FILENAME;
        }
        if(this.outProvincesHighContrastFilename == null){
            this.outProvincesHighContrastFilename = DEFAULT_OUT_PROVINCES_HIGH_CONTRAST_FILENAME;
        }
        if(this.terrainTextureMapFilename == null){
            this.terrainTextureMapFilename = DEFAULT_TERRAIN_TEXTURE_MAP_FILENAME;
        }
        if(this.terrainTypeFilename == null){
            this.terrainTypeFilename = DEFAULT_TERRAIN_TYPE_FILENAME;
        }
        if(this.continentsFilename == null){
            this.continentsFilename = DEFAULT_CONTINENTS_FILENAME;
        }
        if(this.heightmapFilename == null){
            this.heightmapFilename = DEFAULT_HEIGHTMAP_FILENAME;
        }
        if(this.terrainColorsFilename == null){
            this.terrainColorsFilename = DEFAULT_TERRAIN_COLORS_FILENAME;
        }
        if(this.terrainLightmapFilename == null){
            this.terrainLightmapFilename = DEFAULT_TERRAIN_LIGHTMAP_FILENAME;
        }
        if(this.oceanColorsFilename == null){
            this.oceanColorsFilename = DEFAULT_OCEAN_COLORS_FILENAME;
        }
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getModDirectory() {
        return modDirectory;
    }

    public void setModDirectory(String modDirectory) {
        this.modDirectory = modDirectory;
    }

    public String getProvincePointsFilename() {
        return provincePointsFilename;
    }

    public void setProvincePointsFilename(String provincePointsFilename) {
        this.provincePointsFilename = provincePointsFilename;
    }

    public String getOceanPointsFilename() {
        return oceanPointsFilename;
    }

    public void setOceanPointsFilename(String oceanPointsFilename) {
        this.oceanPointsFilename = oceanPointsFilename;
    }

    public String getLandOceanFilename() {
        return landOceanFilename;
    }

    public void setLandOceanFilename(String landOceanFilename) {
        this.landOceanFilename = landOceanFilename;
    }

    public String getOutProvincesFilename() {
        return outProvincesFilename;
    }

    public void setOutProvincesFilename(String outProvincesFilename) {
        this.outProvincesFilename = outProvincesFilename;
    }

    public String getOutProvincesHighContrastFilename() {
        return outProvincesHighContrastFilename;
    }

    public void setOutProvincesHighContrastFilename(String outProvincesHighContrastFilename) {
        this.outProvincesHighContrastFilename = outProvincesHighContrastFilename;
    }

    public String getTerrainTextureMapFilename() {
        return terrainTextureMapFilename;
    }

    public void setTerrainTextureMapFilename(String terrainTextureMapFilename) {
        this.terrainTextureMapFilename = terrainTextureMapFilename;
    }

    public String getTerrainTypeFilename() {
        return terrainTypeFilename;
    }

    public void setTerrainTypeFilename(String terrainTypeFilename) {
        this.terrainTypeFilename = terrainTypeFilename;
    }

    public String getContinentsFilename() {
        return continentsFilename;
    }

    public void setContinentsFilename(String continentsFilename) {
        this.continentsFilename = continentsFilename;
    }

    public String getHeightmapFilename() {
        return heightmapFilename;
    }

    public void setHeightmapFilename(String heightmapFilename) {
        this.heightmapFilename = heightmapFilename;
    }

    public String getTerrainColorsFilename() {
        return terrainColorsFilename;
    }

    public void setTerrainColorsFilename(String terrainColorsFilename) {
        this.terrainColorsFilename = terrainColorsFilename;
    }

    public String getTerrainLightmapFilename() {
        return terrainLightmapFilename;
    }

    public void setTerrainLightmapFilename(String terrainLightmapFilename) {
        this.terrainLightmapFilename = terrainLightmapFilename;
    }

    public String getOceanColorsFilename() {
        return oceanColorsFilename;
    }

    public void setOceanColorsFilename(String oceanColorsFilename) {
        this.oceanColorsFilename = oceanColorsFilename;
    }

    public String getImageConverterJarPath() {
        return imageConverterJarPath;
    }

    public void setImageConverterJarPath(String imageConverterJarPath) {
        this.imageConverterJarPath = imageConverterJarPath;
    }

    

}
