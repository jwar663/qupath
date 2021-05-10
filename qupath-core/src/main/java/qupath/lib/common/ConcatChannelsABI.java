package qupath.lib.common;

import com.google.common.primitives.Ints;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServerMetadata;
import qupath.lib.images.servers.WrappedBufferedImageServer;
import qupath.lib.images.writers.ImageWriter;
import qupath.lib.objects.PathObject;
import qupath.lib.projects.Project;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.interfaces.ROI;

import java.awt.*;
import java.awt.image.*;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.sql.Time;
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


    //find the contribution of filter for each pixel in a single channel.
//    public static BufferedImage unmixSingleChannel(BufferedImage img, float[] imgPixelValues, double[][] proportionArray, int band) {
//        ArrayList<Integer> channel = new ArrayList<>();
//        channel.add(band);
//        double[] referenceEmission = new double[proportionArray.length];
//        double[] filterContribution = new double[referenceEmission.length];
//        for (int i = 0; i < proportionArray.length; i++) {
//            referenceEmission[i] = proportionArray[i][band];
//        }
//
//        for(int x = 0; x < img.getWidth(); x++) {
//            for(int y = 0; y < img.getHeight(); y++) {
//                for(int referenceEmissionValue = 0; referenceEmissionValue < referenceEmission.length; referenceEmissionValue++) {
//                    //insert equation here... filterContribution[] = ...
//                }
//            }
//        }
//        BufferedImage resultImage = createNewBufferedImage(channel, img);
//
//        return resultImage;
//    }

    //completely unmix the whole image linearly by calling various sub methods
    //at this point just hard coding for channels 18-20 to test
    public static ImageData unmixFullImage(ImageData imageData, double[][] proportionArray) {
        BufferedImage overallImage = convertImageDataToImage(imageData);
        ArrayList<Integer> keptChannels = new ArrayList<>();
        ArrayList<ImageChannel> channels = new ArrayList<>();
        //only includes channels  18-20 (-1 from original value)
//        for(int i = 17; i < 20; i++) {
//            keptChannels.add(i);
//            channels.add(imageData.getServer().getChannel(i));
//        }
        for(int i = 0; i < 3; i++) {
            keptChannels.add(i);
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = createNewBufferedImage(keptChannels, overallImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        double[] pixelIntensity = new double[9];
        double[][] referenceEmission = new double[9][keptChannels.size()];
        double[] beta;
        double[][] aValues = new double[width * height][3];
//        double channel18Value = 0;
//        double channel19Value = 0;
//        double channel20Value = 0;
        double channel1Value = 0;
        double channel2Value = 0;
        double channel3Value = 0;
        int count = 0;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel = 0; channel < 9; channel++) {
                    pixelIntensity[channel] = overallImage.getRaster().getSample(x, y, channel);
                    referenceEmission[channel][0] = proportionArray[4][channel];
                    referenceEmission[channel][1] = proportionArray[3][channel];
                    referenceEmission[channel][2] = proportionArray[6][channel];
                }
                //equation1 -> pixelIntensity[0] = A1 * referenceEmission[0][0] + A2 * referenceEmission[1][0] + A3 * referenceEmission[2][0]
                //equation2 -> pixelIntensity[1] = A1 * referenceEmission[0][1] + A2 * referenceEmission[1][1] + A3 * referenceEmission[2][1]
                //equation3 -> pixelIntensity[2] = A1 * referenceEmission[0][2] + A2 * referenceEmission[1][2] + A3 * referenceEmission[2][2]
                //todo: OLSMultipleLinearRegression
                OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
                regression.newSampleData(pixelIntensity, referenceEmission);
                beta = regression.estimateRegressionParameters();
                aValues[count] = beta;
                channel1Value = 0.0;
                channel2Value = 0.0;
                channel3Value = 0.0;
                count++;
                for(int i = 0; i < 9; i++) {
                    channel1Value += beta[0] * referenceEmission[i][0];
                    channel2Value += beta[1] * referenceEmission[i][1];
                    channel3Value += beta[2] * referenceEmission[i][2];
                }
//                channel18Value = beta[0] * (referenceEmission[0][0] + referenceEmission[1][0] + referenceEmission[2][0]);
//                channel19Value = beta[1] * (referenceEmission[0][1] + referenceEmission[1][1] + referenceEmission[2][1]);
//                channel20Value = beta[2] * (referenceEmission[0][2] + referenceEmission[1][2] + referenceEmission[2][2]);
//                resultImage.getRaster().setSample(x, y, 0, channel18Value);
//                resultImage.getRaster().setSample(x, y, 1, channel19Value);
//                resultImage.getRaster().setSample(x, y, 2, channel20Value);
                resultImage.getRaster().setSample(x, y, 0, channel1Value);
                resultImage.getRaster().setSample(x, y, 1, channel2Value);
                resultImage.getRaster().setSample(x, y, 2, channel3Value);
            }
        }

        try {
            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\a-values.csv");
            for(int i = 0; i < width * height; i++) {
                writer.append(aValues[i][0] + "," + aValues[i][1] + "," + aValues[i][2] + "\n");
            }
            writer.flush();
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        ImageServer newServer = new WrappedBufferedImageServer(imageData.getServer().getOriginalMetadata().getName(), resultImage, channels);
        ImageData resultImageData = new ImageData<BufferedImage>(newServer);
        return resultImageData;
    }

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
    public static void mapFilters(List<ImageData> imageDataList) {
//    public static float[][] mapFilters(List<ImageData> imageDataList) {
        //note: this will only work if you open a project and there are at least two images in the project
        //and it only uses the first two images
        String filePath = "D:\\Desktop\\QuPath\\Compare Images\\MappedFilters.csv";
        BufferedImage img1 = convertImageDataToImage(imageDataList.get(0));
        BufferedImage img2 = convertImageDataToImage(imageDataList.get(1));
        int width = img1.getWidth();
        int height = img1.getHeight();
        float[][] img1Channels = new float[7][width * height];
        float[][] img2Channels = new float[7][width * height];
        for(int band = 0; band < 7; band++) {
            img1.getRaster().getSamples(0,0, width, height, band, img1Channels[band]);
            img2.getRaster().getSamples(0,0, width, height, band, img2Channels[band]);
        }
        float[][] correlationMatrix = new float[7][7];
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                correlationMatrix[i][j] = normCrossCorrelationFloat(img1Channels[i], img2Channels[j]);
                System.out.println("img1channel size: " + img1Channels.length + ", i: " + i + ", j: " + j);
            }
        }
        Object[] tempOrderedArray;
        List<Float> tempUnorderedList = new ArrayList<>();
        List<Integer> finalList = new ArrayList<>();
        for(int i = 0; i < 7; i++) {
            tempUnorderedList.clear();
            for(int l = 0; l < 7; l++) {
                tempUnorderedList.add(correlationMatrix[i][l]);
                System.out.println("count: " + l + ", float value: " + tempUnorderedList.get(l));
            }
            tempOrderedArray = tempUnorderedList.toArray();
            Arrays.sort(tempOrderedArray, Collections.reverseOrder());
            for(int j = 0; j < 7; j++) {
                //if this channel is not in the list already then add it
                if(!finalList.contains(tempUnorderedList.indexOf(tempOrderedArray[j]))) {
                    finalList.add(tempUnorderedList.indexOf(tempOrderedArray[j]));
                    break;
                }
            }
        }

        try {
            FileWriter writer = new FileWriter(filePath);
            for(int i = 0; i < 7; i++) {
                    writer.append(Integer.toString(finalList.get(i)));
                    if(i != 6) {
                        writer.append(",");
                    }
            }
            writer.flush();
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void compareImages(List<ImageData> imageDataList) {
        String filePath = "D:\\Desktop\\QuPath\\Compare Images\\im3_vs_Duplicate_Scaled.csv";
        List<BufferedImage> images = new ArrayList<>();
        float[] maxIntensities = new float[imageDataList.size()];
        for(int i = 0; i < imageDataList.size(); i++) {
            images.add(convertImageDataToImage(imageDataList.get(i)));
            maxIntensities[i] = findMaximumPixelIntensity(images.get(i));
        }
        float[] pixelIntensities1;
        float[] pixelIntensities2;

        //use this to identify which channels are which filters
        //it should be DAPI, DL755, ATTO425, DL680, Alexa488, Alexa555, Alexa594
        List<List<Integer>> orderChannels = new ArrayList<>();
        //order for unmodified im3 image
        List<Integer> im3Order = new ArrayList<>();
        im3Order.add(3);
        im3Order.add(10);
        im3Order.add(13);
        im3Order.add(19);
        im3Order.add(21);
        im3Order.add(30);
        im3Order.add(38);
        //order for image altered by duplicate matrix
        List<Integer> duplicateOrder = new ArrayList<>();
        duplicateOrder.add(0);
        duplicateOrder.add(1);
        duplicateOrder.add(2);
        duplicateOrder.add(3);
        duplicateOrder.add(4);
        duplicateOrder.add(5);
        duplicateOrder.add(6);
        //order for image altered by algorithm from proportions
        List<Integer> algorithmOrder = new ArrayList<>();
        algorithmOrder.add(4);
        algorithmOrder.add(6);
        algorithmOrder.add(3);
        algorithmOrder.add(5);
        algorithmOrder.add(0);
        algorithmOrder.add(1);
        algorithmOrder.add(2);
        //order for image unmixed with imageJ/LUMoS
        List<Integer> lumosOrder = new ArrayList<>();
        lumosOrder.add(4);
        lumosOrder.add(1);
        lumosOrder.add(2);
        lumosOrder.add(3);
        lumosOrder.add(6);
        lumosOrder.add(5);
        lumosOrder.add(0);
        //order for image unmixed with imageJ/LUMoS
        List<Integer> lumos1000Order = new ArrayList<>();
        lumos1000Order.add(1);
        lumos1000Order.add(4);
        lumos1000Order.add(2);
        lumos1000Order.add(3);
        lumos1000Order.add(5);
        lumos1000Order.add(0);
        lumos1000Order.add(6);
        //order for image unmixed with imageJ/LUMoS
        List<Integer> lumosAbsoluteOrder = new ArrayList<>();
        lumosAbsoluteOrder.add(6);
        lumosAbsoluteOrder.add(3);
        lumosAbsoluteOrder.add(2);
        lumosAbsoluteOrder.add(5);
        lumosAbsoluteOrder.add(0);
        lumosAbsoluteOrder.add(4);
        lumosAbsoluteOrder.add(1);
        List<Integer> noBackgroundOrder1 = new ArrayList<>();
        List<Integer> noBackgroundOrder2 = new ArrayList<>();
        for(int i = 0; i < 43; i++) {
            noBackgroundOrder1.add(i);
            noBackgroundOrder2.add(i);
        }
//        orderChannels.add(im3Order);
//        orderChannels.add(duplicateOrder);
//        orderChannels.add(lumosOrder);
//        orderChannels.add(lumos1000Order);
//        orderChannels.add(lumosAbsoluteOrder);
//        orderChannels.add(algorithmOrder);
        orderChannels.add(noBackgroundOrder1);
        orderChannels.add(noBackgroundOrder2);

        String[] values = new String[orderChannels.get(0).size()];
        float maxIntensity1;
        float maxIntensity2;

        for(int img1 = 0; img1 < images.size() - 1; img1++) {
            for(int img2 = img1 + 1; img2 < images.size(); img2++) {
                for(int band = 0; band < orderChannels.get(0).size(); band++) {
                    filePath = "D:\\Desktop\\QuPath\\Compare Images\\" + img1 + "_vs_" + img2 + "_compare" + ".csv";
                    maxIntensity1 = maxIntensities[img1];
                    maxIntensity2 = maxIntensities[img2];
                    pixelIntensities1 = convertAllMaximumPixelIntensities(images.get(img1), orderChannels.get(img1).get(band), maxIntensity1);
                    pixelIntensities2 = convertAllMaximumPixelIntensities(images.get(img2), orderChannels.get(img2).get(band), maxIntensity2);
                    values[band] = Float.toString(normCrossCorrelationFloat(pixelIntensities1, pixelIntensities2));
                }
                //write values to csv file
                try {
                    FileWriter writer = new FileWriter(filePath);
                    for(int i = 0; i < values.length; i++) {
                        writer.append(values[i]);
                        if(i != (values.length - 1)) {
                            writer.append(",");
                        }
                    }
                    writer.flush();
                    writer.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    public static double[] getAllThresholdValues(float[][] crossCorrelationMatrix) {
        double[] result = new double[crossCorrelationMatrix.length - 1];
        double thresholdValue = 0.50;
        for(int i = 1; i < crossCorrelationMatrix.length; i++) {
            result[i - 1] = getThresholdFromChannels(crossCorrelationMatrix, i, thresholdValue);
            thresholdValue = result[i - 1];
        }
        return result;
    }

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
}
