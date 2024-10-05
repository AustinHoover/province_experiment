package electrosphere.bmp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import electrosphere.bmp.BMPInfo.BMP_FORMAT;

import java.awt.image.BufferedImage;

//reference: http://www.ece.ualberta.ca/~elliott/ee552/studentAppNotes/2003_w/misc/bmp_file_format/bmp_file_format.htm

/**
 * Writes BMPs
 */
public class BMPWriter {

    /**
     * Writes a bmp to disk
     * @param file The file to write to
     * @param image The buffered image to write
     * @param formatRaw The format
     * @param colorMap The color map
     */
    public static void writeBMP(File file, BufferedImage image, String formatRaw, ByteBuffer colorMap) {
        //determine format if relevant
        BMP_FORMAT format = BMP_FORMAT.BITS_24_RGB;
        int numBits = 24;
        int colorMapOffset = 0;
        if(formatRaw != null){
            switch(formatRaw){
                case "8bitgrayscale":
                    numBits = 8;
                    colorMapOffset = 256 * 4;
                    format = BMP_FORMAT.BITS_8_GRAYSCALE;
                break;
                case "24bitrgb":
                    format = BMP_FORMAT.BITS_24_RGB;
                break;
            }
        }
        //allocate buffer
        int pixelByteCount = image.getHeight() * image.getWidth() * numBits / 8;
        System.out.println(image.getWidth() + " " + image.getHeight());
        ByteBuffer buffer = ByteBuffer.allocate(BMPHeader.BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES + pixelByteCount + colorMapOffset);
        System.out.println("Size: " + (BMPHeader.BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES + pixelByteCount + colorMapOffset));
        System.out.println(buffer.limit());
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        //create header
        BMPHeader header = new BMPHeader(image, format);

        //create info
        BMPInfo info = null;
        if(format == null){
            info = new BMPInfo(image, BMP_FORMAT.BITS_24_RGB);
        } else {
            info = new BMPInfo(image, format);
        }

        //
        //write header
        //

        //write "signature" (magic words)
        buffer.put((byte)66);
        buffer.put((byte)77);

        //get int buffer view

        IntBuffer intBuffer = buffer.asIntBuffer();

        //write rest of the header
        intBuffer.put(header.getFileSize());
        intBuffer.put(0); // unused
        intBuffer.put(header.getDataOffset());

        //
        //write info section
        //

        intBuffer.put(info.getSize());
        intBuffer.put(info.getWidth());
        intBuffer.put(info.getHeight());

        //construct short buffer
        buffer.position(26); //+2 for the initial signature that was written manually
        ShortBuffer shortBuffer = buffer.asShortBuffer();

        shortBuffer.put(info.planes);
        shortBuffer.put(info.bitsPerPixel);

        //set int buffer position again
        buffer.position(30);
        intBuffer = buffer.asIntBuffer();

        intBuffer.put(info.compression);
        intBuffer.put(info.imageSize);
        intBuffer.put(info.xPixelsPerM);
        intBuffer.put(info.yPixelsPerM);
        intBuffer.put(info.getColorsUsed());
        intBuffer.put(info.getImportantColors());

        //if 8 bit, write color map first
        buffer.position(BMPHeader.BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES);
        switch(format){
            case BITS_8_GRAYSCALE: {
                if(colorMap == null){
                    for(int i = 0; i < 256; i++){
                        buffer.put((byte)i);
                        buffer.put((byte)i);
                        buffer.put((byte)i);
                        buffer.put((byte)0);
                    }
                    colorMapOffset = 256 * 4;
                } else {
                    System.out.println("Buffer pos: " + buffer.position());
                    int offset = 0;
                    while(colorMap.hasRemaining()){
                        offset++;
                        buffer.put(colorMap.get());
                    }
                    colorMapOffset = offset;
                    System.out.println("Offset: " + offset);
                }
            } break;
            case BITS_24_RGB:
            break;
        }

        //
        //Write image content
        //
        buffer.position(BMPHeader.BMP_HEADER_SIZE_BYTES + BMPInfo.BMP_INFO_SIZE_BYTES + colorMapOffset);
        for(int y = info.getHeight() - 1; y > -1; y--){
            // System.out.println(y);
            for(int x = 0; x < info.getWidth(); x++){
                int colorRaw = image.getRGB(x, y);
                switch(format){
                    case BITS_24_RGB: {
                        buffer.put((byte)((colorRaw)&0xFF));
                        buffer.put((byte)((colorRaw>>8)&0xFF));
                        buffer.put((byte)((colorRaw>>16)&0xFF));
                    } break;
                    case BITS_8_GRAYSCALE: {
                        int blue = ((colorRaw)&0xFF);
                        int green = ((colorRaw>>8)&0xFF);
                        int red = ((colorRaw>>16)&0xFF);
                        int average = (red + green + blue) / 3;
                        buffer.put((byte)average);
                    } break;
                }
            }
        }

        //reposition buffer
        // buffer.position(0);
        buffer.flip();
        System.out.println(buffer.limit());

        //write buffer to file
        try(FileOutputStream outStream = new FileOutputStream(file)) {
            // int i = 0;
            outStream.write(buffer.array());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
