package qupath.lib.common;

import org.apache.commons.math3.linear.SingularMatrixException;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.WrappedBufferedImageServer;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ManualUnmixing {


    /**
     * Uses other unmixing methods specific to each fluorophore in this class.
     * The channels that are used have been manually chosen by the user.
     *
     * @param imageData
     * @param proportionArray
     * @param DAPIChannels
     * @param cy3Channels
     * @param FITCChannels
     * @param opal480Channels
     * @param opal690Channels
     * @param opal780Channels
     * @param texasRedChannels
     */
    public static ImageData unmixAll(ImageData imageData, double[][] proportionArray, ArrayList<Integer> DAPIChannels, ArrayList<Integer> opal780Channels, ArrayList<Integer> opal480Channels, ArrayList<Integer> opal690Channels, ArrayList<Integer> FITCChannels, ArrayList<Integer> cy3Channels, ArrayList<Integer> texasRedChannels) throws SingularMatrixException {
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();

        ArrayList<Integer> notDuplicates = new ArrayList<>();

        ArrayList<ImageChannel> channels = new ArrayList<>();

        for(int i = 0; i < 7; i++) {
            notDuplicates.add(i);
            channels.add(imageData.getServer().getChannel(i));
        }


        BufferedImage resultImage = RemoveDuplicate.createNewBufferedImage(notDuplicates, oldImage);

        BufferedImage DAPI_image = unmixDAPI(imageData, proportionArray, DAPIChannels);

        BufferedImage Opal780_image = unmixOpal780(imageData, proportionArray, opal780Channels);

        BufferedImage Opal480_image = unmixOpal480(imageData, proportionArray, opal480Channels);

        BufferedImage Opal690_image = unmixOpal690(imageData, proportionArray, opal690Channels);

        BufferedImage FITC_image = unmixFITC(imageData, proportionArray, FITCChannels);

        BufferedImage Cy3_image = unmixCy3(imageData, proportionArray, cy3Channels);

        BufferedImage TexasRed_image = unmixTexasRed(imageData, proportionArray, texasRedChannels);

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                resultImage.getRaster().setSample(x, y, 0, DAPI_image.getRaster().getSample(x, y, 0));

                resultImage.getRaster().setSample(x, y, 1, Opal780_image.getRaster().getSample(x, y, 0));

                resultImage.getRaster().setSample(x, y, 2, Opal480_image.getRaster().getSample(x, y, 0));

                resultImage.getRaster().setSample(x, y, 3, Opal690_image.getRaster().getSample(x, y, 0));

                resultImage.getRaster().setSample(x, y, 4, FITC_image.getRaster().getSample(x, y, 0));

                resultImage.getRaster().setSample(x, y, 5, Cy3_image.getRaster().getSample(x, y, 0));

                resultImage.getRaster().setSample(x, y, 6, TexasRed_image.getRaster().getSample(x, y, 0));
            }
        }

        ImageServer newServer = new WrappedBufferedImageServer(imageData.getServer().getOriginalMetadata().getName(), resultImage, channels);
        ImageData resultImageData = new ImageData<BufferedImage>(newServer);

        return resultImageData;
    }

    /**
     * Unmixes the Texas Red fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixTexasRed(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 36/38
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(2);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 3) {
            chosenFilters.add(1);
        }
        if(numberOfFilters >= 4) {
            chosenFilters.add(5);
        }
        if(numberOfFilters >= 5) {
            chosenFilters.add(4);
        }
        if(numberOfFilters >= 6) {
            chosenFilters.add(3);
        }
        if(numberOfFilters >= 7) {
            chosenFilters.add(0);
        }


        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(1) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(1)]);
                double result1 = pixelIntensity.get(0) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(0)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                pixelIntensity.clear();
            }
        }

//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "TexasRed_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }

    /**
     * Unmixes the Cy3 fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixCy3(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 30/34
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(1);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(2);
        }
        if(numberOfFilters >= 3) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 4) {
            chosenFilters.add(5);
        }
        if(numberOfFilters >= 5) {
            chosenFilters.add(4);
        }
        if(numberOfFilters >= 6) {
            chosenFilters.add(0);
        }
        if(numberOfFilters >= 7) {
            chosenFilters.add(3);
        }

        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(0) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(0)]);
                double result1 = pixelIntensity.get(1) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(1)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                pixelIntensity.clear();
            }
        }

        //un-comment this to export a-values
//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Cy3_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }

    /**
     * Unmixes the Opal780 fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixOpal780(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 9-10
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(5);
        }

        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(1) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(1)]);
                double result1 = pixelIntensity.get(0) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(0)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                pixelIntensity.clear();
            }
        }
//un-comment this to export a-values
//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Opal780_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }

    /**
     * Unmixes the FITC fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixFITC(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 21-29
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(0);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(1);
        }
        if(numberOfFilters >= 3) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 4) {
            chosenFilters.add(5);
        }
        if(numberOfFilters >= 5) {
            chosenFilters.add(2);
        }
        if(numberOfFilters >= 6) {
            chosenFilters.add(3);
        }
        if(numberOfFilters >= 7) {
            chosenFilters.add(4);
        }
        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(0) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(0)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(0)]);
                double result1 = pixelIntensity.get(2) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(2)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(2)]);
                double result2 = pixelIntensity.get(1) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(1)] + beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(1)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                if(result2 < 0) {
                    resultImage.getRaster().setSample(x, y, 2, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 2, result2);
                }
                pixelIntensity.clear();
            }
        }
        //un-comment this to export a-values
//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "FITC_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }

    /**
     * Unmixes the Opal480 fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixOpal480(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 21-29
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(3);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 3) {
            chosenFilters.add(0);
        }
        if(numberOfFilters >= 4) {
            chosenFilters.add(5);
        }
        if(numberOfFilters >= 5) {
            chosenFilters.add(1);
        }
        if(numberOfFilters >= 6) {
            chosenFilters.add(4);
        }

        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(0) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(0)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(0)] + beta[3] * proportionArray[chosenFilters.get(3)][chosenChannels.get(0)]);
                double result1 = pixelIntensity.get(2) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(2)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(2)] + beta[3] * proportionArray[chosenFilters.get(3)][chosenChannels.get(2)]);
                double result2 = pixelIntensity.get(1) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(1)] + beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(1)]  + beta[3] * proportionArray[chosenFilters.get(3)][chosenChannels.get(1)]);
                double result3 = pixelIntensity.get(3) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(3)] + beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(3)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(3)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                if(result2 < 0) {
                    resultImage.getRaster().setSample(x, y, 2, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 2, result2);
                }

                if(result3 < 0) {
                    resultImage.getRaster().setSample(x, y, 3, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 3, result3);
                }

                pixelIntensity.clear();
            }
        }
        //un-comment this to export a-values
//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Opal480_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }

    /**
     * Unmixes the Opal690 fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixOpal690(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 17-19
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(5);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 3) {
            chosenFilters.add(2);
        }

        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(2) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(2)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(2)]);
                double result1 = pixelIntensity.get(1) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(1)] + beta[2] * proportionArray[chosenFilters.get(2)][chosenChannels.get(1)]);
                double result2 = pixelIntensity.get(0) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(0)] + beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(0)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                if(result2 < 0) {
                    resultImage.getRaster().setSample(x, y, 2, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 2, result2);
                }

                pixelIntensity.clear();
            }
        }
        //un-comment this to export a-values
//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Opal690_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }

    /**
     * Unmixes the DAPI fluorophore with the channels selected by the user
     *
     * @param imageData
     * @param proportionArray
     * @param chosenChannels
     *
     */
    public static BufferedImage unmixDAPI(ImageData imageData, double[][] proportionArray, ArrayList<Integer> chosenChannels) {
        //channels 21-29
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        int numberOfFilters = chosenChannels.size();

        //number of filters needs to be the same as number of channels. Add filters in order of importance
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        if(numberOfFilters >= 1) {
            chosenFilters.add(4);
        }
        if(numberOfFilters >= 2) {
            chosenFilters.add(3);
        }
        if(numberOfFilters >= 3) {
            chosenFilters.add(1);
        }
        if(numberOfFilters >= 4) {
            chosenFilters.add(6);
        }
        if(numberOfFilters >= 5) {
            chosenFilters.add(5);
        }
        if(numberOfFilters >= 6) {
            chosenFilters.add(0);
        }
        if(numberOfFilters >= 7) {
            chosenFilters.add(2);
        }

        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, oldImage);
        BufferedImage resultImage = limitedImage;
        int width = imageData.getServer().getWidth();
        int height = imageData.getServer().getHeight();
        ArrayList<Double> pixelIntensity = new ArrayList<>();
        double[][] aValues = new double[width * height][chosenChannels.size()];
        int count = 0;
        double samplePixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int channel : chosenChannels) {
                    samplePixel = oldImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = Unmixing.completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;

                double result0 = pixelIntensity.get(1) - (beta[1] * proportionArray[chosenFilters.get(1)][chosenChannels.get(1)]);
                double result1 = pixelIntensity.get(1) - (beta[0] * proportionArray[chosenFilters.get(0)][chosenChannels.get(0)]);

                if(result0 < 0) {
                    resultImage.getRaster().setSample(x, y, 0, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 0, result0);
                }

                if(result1 < 0) {
                    resultImage.getRaster().setSample(x, y, 1, 0);
                } else {
                    resultImage.getRaster().setSample(x, y, 1, result1);
                }

                pixelIntensity.clear();
            }
        }
        //un-comment this to export a-values
//        try {
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "DAPI_A" + ".csv");
//            for(int i = 0; i < width * height; i++) {
//                for(int j = 0; j < chosenChannels.size(); j++) {
//                    writer.append((aValues[i][j] + ","));
//                    if(j == chosenChannels.size() - 1) {
//                        writer.append((Double.toString(aValues[i][j])));
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//            writer.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        return resultImage;
    }
}
