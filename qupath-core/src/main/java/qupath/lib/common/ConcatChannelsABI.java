package qupath.lib.common;

import com.google.common.primitives.Ints;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServerMetadata;
import qupath.lib.images.servers.WrappedBufferedImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.URI;
import java.nio.Buffer;
import java.util.*;
import java.util.List;

/**
 * Functions to help with combining fluorescent channels that are the same or similar.
 *
 * @author Jaedyn Ward
 *
 */

public class ConcatChannelsABI {

    //Macros
    private static final int[] ALEXA_488 = {0, 204, 0}; //GREEN
    private static final int[] ALEXA_555 = {255, 255, 0}; //YELLOW
    private static final int[] ALEXA_594 = {255, 0, 0}; //RED
    private static final int[] ATTO_425 = {0, 255, 255}; //CYAN
    private static final int[] DAPI = {0, 0, 255}; //BLUE
    private static final int[] DL680_DUNBAR = {255, 255, 255}; //WHITE
    private static final int[] DL755_DUNBAR = {233, 150, 122}; //DARK SALMON

    /**
     * This method is used to compare two channels together to see if they are similar or not using normalised cross-correlation.
     *
     * @param firstChannel
     * @param secondChannel
     */
    public static float normCrossCorrelationFloat(float[] firstChannel, float[] secondChannel) {
        float nominator = 0;
        float firstDenominator = 0;
        float secondDenominator = 0;
        for(int i = 0; i < firstChannel.length; i++) {
            nominator += firstChannel[i] * secondChannel[i];
            firstDenominator += (firstChannel[i] * firstChannel[i]);
            secondDenominator += (secondChannel[i] * secondChannel[i]);
        }
        return nominator/(float)(Math.sqrt((firstDenominator * secondDenominator)));
    }

    /**
     * This method uses the Normalised Cross Correlation Matrix to return which channels are
     * distinct channels.
     *
     * @param crossCorrelationMatrix
     * @param similarityThreshold
     */
    public static ArrayList<Integer> distinctChannels(float[][] crossCorrelationMatrix, double similarityThreshold) {
        ArrayList<Integer> duplicates = new ArrayList<>();
        ArrayList<Integer> distinct = new ArrayList<>();
        int nChannels = crossCorrelationMatrix.length;
        for(int i = 0; i < nChannels - 1; i++) {
            //only check for duplicates in channels that aren't already considered duplicates
            if(!duplicates.contains(i)) {
                for(int j = i + 1; j < crossCorrelationMatrix.length; j++) {
                    if(!duplicates.contains(j)) {
                        if(crossCorrelationMatrix[i][j] > similarityThreshold) {
                            duplicates.add(j);
                        }
                    }
                }
            }
        }
        for(int i = 0; i < nChannels; i++) {
            if(!duplicates.contains(i)) {
                distinct.add(i);
            }
        }
        return distinct;
    }

    /**
     * @author Pete Bankhead
     * Set the channel colors for the specified ImageData.
     * It is not essential to pass names for all channels:
     * by passing n values, the first n channel names will be set.
     * Any name that is null will be left unchanged.
     *
     * @param imageData
     * @param colors
     */
    public static void setChannelColors(ImageData<?> imageData, Integer... colors) {
        List<ImageChannel> oldChannels = imageData.getServer().getMetadata().getChannels();
        List<ImageChannel> newChannels = new ArrayList<>(oldChannels);
        for (int i = 0; i < colors.length; i++) {
            Integer color = colors[i];
            if (color == null)
                continue;
            newChannels.set(i, ImageChannel.getInstance(newChannels.get(i).getName(), color));
            if (i >= newChannels.size()) {
                break;
            }
        }
        setChannels(imageData, newChannels.toArray(ImageChannel[]::new));
    }

    /**
     * @author Pete Bankhead
     * Set the channels for the specified ImageData.
     * Note that number of channels provided must match the number of channels of the current image.
     * <p>
     * Also, currently it is not possible to set channels for RGB images - attempting to do so
     * will throw an IllegalArgumentException.
     *
     * @param imageData
     * @param channels
     */
    public static void setChannels(ImageData<?> imageData, ImageChannel... channels) {
        ImageServer<?> server = imageData.getServer();
        if (server.isRGB()) {
            throw new IllegalArgumentException("Cannot set channels for RGB images");
        }
        List<ImageChannel> oldChannels = server.getMetadata().getChannels();
        List<ImageChannel> newChannels = Arrays.asList(channels);
        if (oldChannels.size() != newChannels.size())
            throw new IllegalArgumentException("Cannot set channels - require " + oldChannels.size() + " channels but you provided " + channels.length);

        // Set the metadata
        var metadata = server.getMetadata();
        var metadata2 = new ImageServerMetadata.Builder(metadata)
                .channels(newChannels)
                .build();
        imageData.updateServerMetadata(metadata2);
    }

    /**
     * Call setChannelColors method with the channel colours used regularly with 7 channels
     *
     * @param imageData
     */
    public static void setRegularChannelColours(ImageData imageData){
        Integer[] regularChannelColourArray = new Integer[7];
        regularChannelColourArray[0] = ColorTools.makeRGB(ALEXA_488[0], ALEXA_488[1], ALEXA_488[2]); //Alexa 488
        regularChannelColourArray[1] = ColorTools.makeRGB(ALEXA_555[0], ALEXA_555[1], ALEXA_555[2]); //Alexa 555
        regularChannelColourArray[2] = ColorTools.makeRGB(ALEXA_594[0], ALEXA_594[1], ALEXA_594[2]); //Alexa 594
        regularChannelColourArray[3] = ColorTools.makeRGB(ATTO_425[0], ATTO_425[1], ATTO_425[2]); //ATTO 425
        regularChannelColourArray[4] = ColorTools.makeRGB(DAPI[0], DAPI[1], DAPI[2]); //DAPI
        regularChannelColourArray[5] = ColorTools.makeRGB(DL680_DUNBAR[0], DL680_DUNBAR[1], DL680_DUNBAR[2]); //DL680_Dunbar
        regularChannelColourArray[6] = ColorTools.makeRGB(DL755_DUNBAR[0], DL755_DUNBAR[1], DL755_DUNBAR[2]); //DL755_Dunbar
        setChannelColors(imageData, regularChannelColourArray);
    }

    /**
     * Call setChannelNames method with the channel names
     *
     * @param imageData
     */
    public static void setRegularChannelNames(ImageData imageData){
        int nChannels = imageData.getServer().nChannels();
        String[] regularChannelNameArray = new String[nChannels];
        for(int i = 1; i < nChannels + 1; i++) {
            regularChannelNameArray[i - 1] = "Channel " + i;
        }
        setChannelNames(imageData, regularChannelNameArray);
    }

    /**
     * @author Pete Bankhead
     * Set the channel names for the specified ImageData.
     * It is not essential to pass names for all channels:
     * by passing n values, the first n channel names will be set.
     * Any name that is null will be left unchanged.
     *
     * @param imageData
     * @param names
     */
    public static void setChannelNames(ImageData<?> imageData, String... names) {
        List<ImageChannel> oldChannels = imageData.getServer().getMetadata().getChannels();
        List<ImageChannel> newChannels = new ArrayList<>(oldChannels);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name == null)
                continue;
            newChannels.set(i, ImageChannel.getInstance(name, newChannels.get(i).getColor()));
            if (i >= newChannels.size()) {
                break;
            }
        }
        setChannels(imageData, newChannels.toArray(ImageChannel[]::new));
    }


    /**
     * Use the channels that are not duplicates to create a new BufferedImage object.
     *
     * @param notDuplicates
     * @param img
     */
    public static BufferedImage createNewBufferedImage(ArrayList<Integer> notDuplicates, BufferedImage img) {

        int width = img.getWidth();
        int height = img.getHeight();
        int[] notDuplicatesArray = Ints.toArray(notDuplicates);
        float[] tempFloatArray = new float[width * height];
        SampleModel resultSampleModel = img.getSampleModel().createSubsetSampleModel(notDuplicatesArray);
        WritableRaster resultRaster = Raster.createWritableRaster(resultSampleModel, null);
        BufferedImage resultImage = new BufferedImage(img.getColorModel(), resultRaster, img.getColorModel().isAlphaPremultiplied(), null);
        for(int i = 0; i < notDuplicates.size(); i++) {
            img.getRaster().getSamples(0, 0, width, height, notDuplicates.get(i), tempFloatArray);
            resultImage.getRaster().setSamples(0, 0, width, height, i, tempFloatArray);
        }
        return resultImage;
    }

    /**
     * Use the original and new URI to create a new mapping.
     *
     * @param originalURI
     * @param newURIs
     */
    public static Map<URI, URI> getCorrectURIMap(URI originalURI, Collection<URI> newURIs) {
        URI[] newURIArray = newURIs.toArray(new URI[newURIs.size()]);
        Map<URI, URI> uriMap = new HashMap<URI, URI>();
        for(int i = 0; i < newURIs.size(); i++) {
            uriMap.put(originalURI, newURIArray[i]);
        }
        return uriMap;
    }

    /**
     * Perform normalised cross correlation for each channel of the image then put it into a matrix.
     *
     * @param img
     */
    public static float[][] createConcatMatrix(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        float[] channelOneArray = new float[width * height];
        float[] channelTwoArray = new float[width * height];
        int nChannels = img.getRaster().getNumBands();
        float[][] channelMatrix = new float[nChannels][nChannels];
        float result = 0;
        for(int i = 0; i < nChannels; i++) {
            for(int j = i; j < nChannels; j++) {
                if(i == j) {
                    channelMatrix[i][j] = 1;
                } else {
                    img.getRaster().getSamples(0, 0, width, height, i, channelOneArray);
                    img.getRaster().getSamples(0, 0, width, height, j, channelTwoArray);
                    result = normCrossCorrelationFloat(channelOneArray, channelTwoArray);
                    channelMatrix[i][j] = result;
                    channelMatrix[j][i] = result;
                }
            }
        }
        return channelMatrix;
    }

    /**
     * Use the image data to create an associated image with the specified channel.
     * This image will be used to compare to see if the channels are actually different.
     *
     * @param imageData
     * @param channel
     */
    public static BufferedImage singleChannelImage(ImageData<BufferedImage> imageData, int channel) {
        RegionRequest request = RegionRequest.createInstance(imageData.getServer());
        int width = imageData.getServer().getMetadata().getWidth();
        int height = imageData.getServer().getMetadata().getHeight();

        BufferedImage img = null;
        try {
            img = imageData.getServer().readBufferedImage(request);
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    //set the red colour, leave the other colours as 0.
                    newImage.getRaster().setSample(i, j, 0, img.getRaster().getSample(i, j, channel)/20);
                }
            }
            return newImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Use the image data to create the full buffered image for use in other methods.
     *
     * @param imageData
     */
    public static BufferedImage convertImageDataToImage(ImageData<BufferedImage> imageData) {
        RegionRequest request = RegionRequest.createInstance(imageData.getServer());
        BufferedImage img = null;
        try {
            img = imageData.getServer().readBufferedImage(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    /**
     * Create the associated image whilst removing the duplicate channels.
     *
     * @param imageData
     * @param img
     */
    public static ImageData concatDuplicateChannels(ImageData<BufferedImage> imageData, BufferedImage img, float[][] crossCorrelationMatrix, double similarityThreshold) {
        ImageData resultImageData;
        ArrayList<Integer> distinct = distinctChannels(crossCorrelationMatrix, similarityThreshold);
        List<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < distinct.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }
        BufferedImage finalImg = createNewBufferedImage(distinct, img);
            //writing a tiff file, not particularly useful
//            File file = new File("D:\\Desktop\\QuPath\\newImage.tiff");
//            try {
//                ImageIO.write(finalImg, "tiff", file);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        ImageServer newServer = new WrappedBufferedImageServer(imageData.getServer().getOriginalMetadata().getName(), finalImg, channels);
        ImageData imageData1 = new ImageData<BufferedImage>(newServer);
        imageData1.setImageType(ImageData.ImageType.FLUORESCENCE);
        //imageData1.setProperty()
        setRegularChannelColours(imageData1);
//            System.out.println("original URI: " + imageData.getServer().getURIs().toString());
//            System.out.println("new URI: " + imageData1.getServer().getURIs().toString());
//            System.out.println("original reference: " + imageData.getServer().getPath());
//            System.out.println("reference: " + imageData1.getServer().getPath());
//            try {
//                imageData1.getServer()
//                        .getBuilder()
//                        .updateURIs(getCorrectURIMap(new URI(imageData1.getServer().getPath()), imageData.getServer().getURIs()))
//                        .build();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.println("updated URI: " + imageData1.getServer().getURIs().toString());
        resultImageData = imageData1;
        setRegularChannelNames(resultImageData);
        return resultImageData;
    }
}
