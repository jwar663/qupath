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
import java.util.*;
import java.util.List;

/**
 * Functions to help with combining fluorescent channels that are the same or similar.
 *
 * @author Jaedyn Ward
 *
 */


public class RemoveDuplicate {

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
     * This method scales all pixel values in an image to what the maximum pixel intensity is for every image.
     * This allows for a more fair comparison as values seem more normalised.
     *
     * @param img
     * @param band
     * @param maxIntensity
     */
    public static float[] convertAllMaximumPixelIntensities(BufferedImage img, int band, float maxIntensity) {
        int width = img.getWidth();
        int height = img.getHeight();
        float[] pixelIntensities = new float[width * height];
        img.getRaster().getSamples(0, 0, width, height, band, pixelIntensities);
        for(int i = 0; i < pixelIntensities.length; i++) {
            pixelIntensities[i] = pixelIntensities[i]/maxIntensity;
        }
        return pixelIntensities;
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
     * Find out the threshold value for every single different channel value. This allows this information to be calculated
     * when opening the DuplicateMatrixCommand so it seems much quicker when given any different number of channels.
     *
     * @param crossCorrelationMatrix
     */
    public static double[] getAllThresholdValues(float[][] crossCorrelationMatrix) {
        double[] result = new double[crossCorrelationMatrix.length - 1];
        double thresholdValue = 0.50;
        for(int i = 1; i < crossCorrelationMatrix.length; i++) {
            result[i - 1] = getThresholdFromChannels(crossCorrelationMatrix, i, thresholdValue);
            thresholdValue = result[i - 1];
        }
        return result;
    }


    /**
     * Find out how what the threshold value is for a specified number of channels. You can give a channel number between 1 and 43 and
     * this will find the threshold value corresponding to it. This makes it easier to enter the values in a different method, which is
     * only interested in the threshold value.
     *
     * @param crossCorrelationMatrix
     * @param numberOfChannelsRequired
     * @param startThreshold
     */
    public static double getThresholdFromChannels(float[][] crossCorrelationMatrix, int numberOfChannelsRequired, double startThreshold) {
        double result = startThreshold;
        double upperValue = 1;
        double lowerValue = 0;
        int returnedChannels;
        int iteration = 0;
        while(true) {
            returnedChannels = distinctChannels(crossCorrelationMatrix, result).size();
            if(returnedChannels == numberOfChannelsRequired || iteration >= 100) {
                return result;
            } else if(returnedChannels > numberOfChannelsRequired) {
                upperValue = result;
                result = result - (result - lowerValue)/2;
            } else {
                lowerValue = result;
                result = result + (upperValue - result)/2;
            }
            iteration++;
        }
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
        for (int i = 0; i < oldChannels.size(); i++) {
            if (i >= newChannels.size()) {
                break;
            }
            Integer color = colors[i];
            if (color == null)
                continue;
            newChannels.set(i, ImageChannel.getInstance(newChannels.get(i).getName(), color));
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
        //setChannelColors(imageData, regularChannelColourArray);
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
     * Run through the whole image to find the greatest pixel intensity, so it is
     * possible to find the correct ratio.
     *
     * @param img
     */
    public static float findMaximumPixelIntensity(BufferedImage img) {
        float maxValue = 0;
        float sample = 0;
        for(int height = 0; height < img.getHeight(); height++) {
            for(int width = 0; width < img.getWidth(); width++) {
                for(int band = 0; band < img.getRaster().getNumBands(); band++) {
                    sample = img.getRaster().getSample(width, height, band);
                    if(sample > maxValue) {
                        maxValue = sample;
                    }
                }
            }
        }
        return maxValue;
    }

    /**
     * Use the image data to create an associated image with the specified channel.
     * This image will be used to compare to see if the channels are actually different.
     *
     * @param imageData
     * @param channel
     */
    public static BufferedImage[] singleChannelImage(ImageData<BufferedImage> imageData, int channel, int desiredWidth, int desiredHeight, float maxValue) {
        RegionRequest request = RegionRequest.createInstance(imageData.getServer());
        int width = imageData.getServer().getMetadata().getWidth();
        int height = imageData.getServer().getMetadata().getHeight();
        BufferedImage bufferedImage;
        BufferedImage[] grayScaleImages = new BufferedImage[2];
        BufferedImage img = null;
        try {
            img = imageData.getServer().readBufferedImage(request);
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    //set the red colour, leave the other colours as 0.
                    bufferedImage.getRaster().setSample(i, j, 0, (img.getRaster().getSample(i, j, channel)/maxValue) * 255);
                }
            }
            grayScaleImages[1] = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics graphics = grayScaleImages[1].getGraphics();
            graphics.drawImage(bufferedImage, 0, 0, null);
            graphics.dispose();
            grayScaleImages[0] = createThumbnailImage(grayScaleImages[1], desiredHeight, desiredWidth);
            return grayScaleImages;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Use a buffered image to create a thumbnail of that image given the requested size of the thumbnail.
     *
     * @param img
     * @param height
     * @param width
     */
    public static BufferedImage createThumbnailImage(BufferedImage img, int height, int width) {
        BufferedImage thumbnailImage = new BufferedImage(width, height, img.getType());
        Graphics2D graphics2D = thumbnailImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(img, 0, 0, width, height, null);
        graphics2D.dispose();
        return thumbnailImage;
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
        ImageServer newServer = new WrappedBufferedImageServer(imageData.getServer().getOriginalMetadata().getName(), finalImg, channels);
        ImageData imageData1 = new ImageData<BufferedImage>(newServer);
        imageData1.setImageType(ImageData.ImageType.FLUORESCENCE);
        setRegularChannelColours(imageData1);
        resultImageData = imageData1;
        setRegularChannelNames(resultImageData);
        return resultImageData;
    }

    public static ImageData createSingleChannelImageData(ImageData imageData, int channel) {
        ImageData resultImageData;
        int height = imageData.getServer().getHeight();
        int width = imageData.getServer().getWidth();
        List<ImageChannel> channels = new ArrayList<>();
        channels.add(imageData.getServer().getChannel(channel));
        float maxValue = findMaximumPixelIntensity(convertImageDataToImage(imageData));
        BufferedImage returnImage = singleChannelImage(imageData, channel, width, height, maxValue)[1];
        ImageServer newServer = new WrappedBufferedImageServer(imageData.getServer().getOriginalMetadata().getName(), returnImage, channels);
        ImageData imageData1 = new ImageData<BufferedImage>(newServer);
        imageData1.setImageType(ImageData.ImageType.FLUORESCENCE);
        setRegularChannelColours(imageData1);
        resultImageData = imageData1;
        setRegularChannelNames(resultImageData);
        return resultImageData;
    }
}
