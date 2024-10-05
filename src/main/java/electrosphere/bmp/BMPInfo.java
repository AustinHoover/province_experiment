package electrosphere.bmp;

import java.awt.image.BufferedImage;

public class BMPInfo {

    public static final int BMP_INFO_SIZE_BYTES = 40;

    public static enum BMP_FORMAT {
        BITS_8_GRAYSCALE,
        BITS_24_RGB,
    }

    int size = 40;
    int width = 0;
    int height = 0;
    short planes = 1;
    short bitsPerPixel = 24;
    int compression = 0;
    int imageSize = 0; //valid to set = 0 if the image is not compressed AND rgb according to wikipedia
    int xPixelsPerM = 100;
    int yPixelsPerM = 100;
    int colorsUsed = 0;
    int importantColors = 0; //0 means all of them

    public BMPInfo(BufferedImage image){
        width = image.getWidth();
        height = image.getHeight();
    }

    public BMPInfo(BufferedImage image, BMP_FORMAT format){
        width = image.getWidth();
        height = image.getHeight();
        switch(format){
            case BITS_8_GRAYSCALE: {
                bitsPerPixel = 8;
                imageSize = width * height * 1;
            } break;
            case BITS_24_RGB: {

            } break;
        }
    }

    public int getSize(){
        return size;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public short getPlanes(){
        return planes;
    }

    public short getBitsPerPixel(){
        return bitsPerPixel;
    }

    public int getCompression(){
        return compression;
    }

    public int getImageSize(){
        return imageSize;
    }

    public int getXPixelsPerM(){
        return xPixelsPerM;
    }

    public int getYPixelsPerM(){
        return yPixelsPerM;
    }

    public int getColorsUsed(){
        return colorsUsed;
    }

    public int getImportantColors(){
        return importantColors;
    }


    
}
