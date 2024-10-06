package electrosphere.util;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Utilities
 */
public class Utils {
    
    /**
     * Gets a color from a packed int
     * @param index The packed int
     * @param isLand true if this is land, false otherwise
     * @return The color
     */
    public static int[] getColorFromIndex(int index, boolean isLand){
        int red = 0;
        int green = 0;
        int blue = 0;
        if(isLand){
            blue = 200 - index % 200;
            green = 200 - index / 200;
            red = 255 - index / 200 / 200;
        } else {
            red = 200 - index % 200;
            green = 200 - index / 200;
            blue = 255 - index / 200 / 200;
        }
        // System.out.println(red + " " + green + " " + blue);
        // System.out.println(bestIndex);
        return new int[]{red,green,blue};
    }

    /**
     * Deep copies a buffered image
     * @param bi The input image
     * @return The copy
     */
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Writes a png with no compression
     * @param image The input buffered image
     * @param outputPath The path to write to
     */
    public static void writePngNoCompression(BufferedImage image, String outputPath){
        try (ImageOutputStream out = ImageIO.createImageOutputStream(Files.newOutputStream(Paths.get(outputPath)))) {
            ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
            ImageWriter writer = ImageIO.getImageWriters(type, "png").next();
        
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_DISABLED);
                // param.setCompressionQuality(1.0f);
            }
        
            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //from https://stackoverflow.com/questions/18022364/how-to-convert-rgb-color-to-int-in-java
    /**
     * Converts a red, green, and blue value into a packed int
     * @param Red The red value
     * @param Green The green value
     * @param Blue The blue value
     * @return The packed int
     */
    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.
    
        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    /**
     * Converts a red, green, blue, and alpha value into a packed int
     * @param Red The red value
     * @param Green The green value
     * @param Blue The blue value
     * @param Alpha The alpha value
     * @return The packed int
     */
    public static int getIntFromColor(int Red, int Green, int Blue, int Alpha){
        Alpha = (Alpha << 24) & 0xFF000000;
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.
    
        return 0x00000000 | Alpha | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

}
