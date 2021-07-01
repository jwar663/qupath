package qupath.lib.common;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.WrappedBufferedImageServer;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Unmixing {

    /**
     * This method completes regression using OLSMultipleLinearRegression. The results didn't
     * seem correct, so use completeManualRegression method.
     *
     * @param pixelIntensity
     * @param referenceEmission
     */
    public static double[] completeRegression(double[] pixelIntensity, double[][] referenceEmission) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(pixelIntensity, referenceEmission);
        double[] beta = regression.estimateRegressionParameters();
        return beta;
    }


    /**
     * This method uses the proportionArray to find the most prominent filter for
     * each channel and returns those values
     *
     * @param possibleChannels
     * @param proportionArray
     */
    public static ArrayList<Integer>[] findFiltersAndChannels(ArrayList<Integer> possibleChannels, double[][] proportionArray) {
        ArrayList<Integer>[] returnLists = new ArrayList[3];
        ArrayList<Integer> chosenChannels = new ArrayList<>();
        ArrayList<Integer> chosenFilters = new ArrayList<>();
        ArrayList<Integer> peak = new ArrayList<>();

        for(int channel : possibleChannels) {
            for(int filter = 0; filter < 7; filter++) {
                if(!chosenFilters.contains(filter)) {
                    if(proportionArray[filter][channel] > 0) {
                        chosenFilters.add(filter);
                        break;
                    }
                }
            }
        }

        double maxValue;
        int countChosen = 0;
        int countPeak = 0;
        for(int filter : chosenFilters) {
            maxValue = 0;
            for(int channel : possibleChannels) {
                if(!chosenChannels.contains(channel)) {
                    if(proportionArray[filter][channel] > maxValue) {
                        maxValue = proportionArray[filter][channel];
                        if(chosenChannels.size() == countChosen + 1) {
                            chosenChannels.remove(countChosen);
                        }
                        chosenChannels.add(channel);
                        countChosen++;
                    }
                }
                if(proportionArray[filter][channel] >= maxValue) {
                    maxValue = proportionArray[filter][channel];
                    if(peak.size() == countPeak + 1) {
                        peak.remove(countPeak);
                    }
                    peak.add(channel);
                    countPeak++;
                    System.out.println("max value: " + maxValue + " (" + filter + ", " + channel + ")");
                }
            }
        }
        returnLists[0] = chosenChannels;
        returnLists[1] = chosenFilters;
        returnLists[2] = peak;
        return returnLists;
    }

    /**
     * This method completes regression manually, without any external packages. Uses the chosen channels and chosen
     * filters to create the relevant matrices and completes matrix operations following the formula: f = (((M^T)M)^-1)((M^T)x)
     *
     * @param pixelIntensity
     * @param proportionArray
     * @param chosenFilters
     * @param chosenChannels
     */
    public static double[] completeManualRegression(ArrayList<Double> pixelIntensity, double[][] proportionArray, ArrayList<Integer> chosenFilters, ArrayList<Integer> chosenChannels) {

        //Instantiate identity matrix
        double [][] rhs = new double[pixelIntensity.size()][pixelIntensity.size()];
        for(int i = 0; i < pixelIntensity.size(); i++) {
            for(int j = 0; j < pixelIntensity.size(); j++) {
                if(i == j) {
                    rhs[i][j] = 1;
                } else {
                    rhs[i][j] = 0;
                }
            }
        }
        RealMatrix I = new Array2DRowRealMatrix(rhs);

        double[] pixelIntensityArray = new double[pixelIntensity.size()];
        double[][] referenceEmission = new double[pixelIntensity.size()][pixelIntensity.size()];

        for(int i = 0; i < pixelIntensity.size(); i++) {
            for(int j = 0; j < pixelIntensity.size(); j++) {
                referenceEmission[i][j] = proportionArray[chosenFilters.get(j)][chosenChannels.get(i)];
            }

            pixelIntensityArray[i] = pixelIntensity.get(i);
        }

        RealMatrix referenceEmissionMatrix = new Array2DRowRealMatrix(referenceEmission);
        RealMatrix pixelIntensityMatrix = new Array2DRowRealMatrix(pixelIntensityArray);

        //M^t
        RealMatrix referenceEmissionMatrixTransposed = referenceEmissionMatrix.transpose();

        //M^t.x
        RealMatrix transposeBySignal = referenceEmissionMatrixTransposed.multiply(pixelIntensityMatrix);

        //M^t.M
        RealMatrix transposeByNormal = referenceEmissionMatrixTransposed.multiply(referenceEmissionMatrix);

        //invert matrix
        DecompositionSolver solver = new LUDecomposition(transposeByNormal).getSolver();
        RealMatrix toPowerOfNegativeOne = solver.solve(I);

        RealMatrix solution = toPowerOfNegativeOne.multiply(transposeBySignal);
        double[][] contribution = solution.getData();
        double[] result = new double[contribution.length * contribution[0].length];
        int count = 0;
        for(int i = 0; i < contribution.length; i++) {
            for(int j = 0; j < contribution[0].length; j++) {
                result[count] = contribution[i][j];
                count++;
            }
        }
        return result;
    }

    /**
     * Uses completeManualRegression method to unmix a single fluorophore. The method will work for any fluorophore,
     * it just needs to know which channel to start from and end at, then it will consider all of these channels.
     * The method will automatically find the heaviest contributing filters for each channel and use those values
     *
     *
     * @param imageData
     * @param proportionArray
     * @param startChannel
     * @param endChannel
     * @param fluorophore
     */
    public static ImageData unmixFluorophore(ImageData imageData, double[][] proportionArray, int startChannel, int endChannel, String fluorophore) {
        ArrayList<Integer> possibleChannels = new ArrayList<>();

        for(int i = startChannel - 1; i < endChannel; i++) {
            possibleChannels.add(i);
        }

        BufferedImage overallImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer>[] lists = findFiltersAndChannels(possibleChannels, proportionArray);
        ArrayList<Integer> chosenChannels = lists[0];
        ArrayList<Integer> chosenFilters = lists[1];
        ArrayList<Integer> peak = lists[2];

        ArrayList<ImageChannel> channels = new ArrayList<>();
        for(int i = 0; i < chosenChannels.size(); i++) {
            channels.add(imageData.getServer().getChannel(i));
        }

        BufferedImage limitedImage = RemoveDuplicate.createNewBufferedImage(chosenChannels, overallImage);
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
                    samplePixel = overallImage.getRaster().getSample(x, y, channel);
                    pixelIntensity.add(samplePixel);
                }
                double[] beta = completeManualRegression(pixelIntensity, proportionArray, chosenFilters, chosenChannels);

                aValues[count] = beta;
                count++;
                //different method
                for(int i = 0; i < chosenChannels.size(); i++) {
                    double result = beta[i] * proportionArray[chosenFilters.get(i)][peak.get(i)];
                    if(result < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, i, result);
                    }
                }
                pixelIntensity.clear();
            }
        }

        try {
            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + fluorophore + ".csv");
            for(int i = 0; i < width * height; i++) {
                for(int j = 0; j < chosenChannels.size(); j++) {
                    writer.append((aValues[i][j] + ","));
                    if(j == chosenChannels.size() - 1) {
                        writer.append((Double.toString(aValues[i][j])));
                    }
                }
                writer.append("\n");
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

    /**
     * Runs the unmixFluorophore method for the specified Opal690 fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixOpal690(ImageData imageData, double[][] proportionArray) {
        //channels 18-20
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 18, 20, "opal690");
        return resultImageData;
    }

    /**
     * Runs the unmixFluorophore method for the specified Opal780 fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixOpal780(ImageData imageData, double[][] proportionArray) {
        //channels 10-11
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 10, 11, "opal780");
        return resultImageData;
    }

    /**
     * Runs the unmixFluorophore method for the specified Cy3 fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixCy3(ImageData imageData, double[][] proportionArray) {
        //channels 30-36
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 30, 36, "Cy3");
        return resultImageData;
    }

    /**
     * Runs the unmixFluorophore method for the specified Opal480 fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixOpal480(ImageData imageData, double[][] proportionArray) {
        //channels 12-17
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 12, 17, "opal480");
        return resultImageData;
    }

    /**
     * Runs the unmixFluorophore method for the specified TexasRed fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixTexasRed(ImageData imageData, double[][] proportionArray) {
        //channels 37-43
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 37, 43, "TexasRed");
        return resultImageData;
    }

    /**
     * Runs the unmixFluorophore method for the specified FITC fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixFITC(ImageData imageData, double[][] proportionArray) {
        //channels 21-29
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 21, 29, "FITC");
        return resultImageData;
    }

    /**
     * Runs the unmixFluorophore method for the specified DAPI fluorophore, given the relevant channels
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixDAPI(ImageData imageData, double[][] proportionArray) {
        //channels 1-9
        ImageData resultImageData = unmixFluorophore(imageData, proportionArray, 1, 9, "DAPI");
        return resultImageData;
    }
}
