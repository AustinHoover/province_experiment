package electrosphere.bmp;

import java.awt.image.BufferedImage;

import electrosphere.bmp.BMPInfo.BMP_FORMAT;

public class BMPHeader {

    public static final int BMP_HEADER_SIZE_BYTES = 14;

    public static final int COLOR_MAP_SIZE = 256 * 4;



    int fileSize = 0;
    int dataOffset = BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES;


    public BMPHeader(BufferedImage image, BMP_FORMAT format){
        switch(format){
            case BITS_24_RGB: {
                fileSize = BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES + image.getWidth() * image.getHeight() * 3;
            } break;
            case BITS_8_GRAYSCALE: {
                fileSize = BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES + image.getWidth() * image.getHeight() * 1 + COLOR_MAP_SIZE;
                dataOffset = BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES + COLOR_MAP_SIZE;
            } break;
        }
    }

    public int getFileSize(){
        return fileSize;
    }

    public int getDataOffset(){
        return dataOffset;
    }

}
