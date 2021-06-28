package qupath.lib.common;

import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MultiThreadManualUnmix extends Thread {

    String name;
    BufferedImage image;
    ImageData imgData;
    double[][] proportion;
    ArrayList<Integer> chosen;


    MultiThreadManualUnmix(String name, ImageData imgData, double[][] proportion, ArrayList<Integer> chosen) {
        this.name = name;
        this.imgData = imgData;
        this.proportion = proportion;
        this.chosen = chosen;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void run() {
        if(this.name.equals("DAPI")) {
            image = unmixDAPI(imgData, proportion, chosen);
        } else if(this.name.equals("Opal780")) {
            image = unmixOpal780(imgData, proportion, chosen);
        } else if(this.name.equals("Opal480")) {
            image = unmixOpal480(imgData, proportion, chosen);
        } else if(this.name.equals("Opal690")) {
            image = unmixOpal690(imgData, proportion, chosen);
        } else if(this.name.equals("FITC")) {
            image = unmixFITC(imgData, proportion, chosen);
        } else if(this.name.equals("Cy3")) {
            image = unmixCy3(imgData, proportion, chosen);
        } else if(this.name.equals("TexasRed")) {
            image = unmixTexasRed(imgData, proportion, chosen);
        }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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

        ArrayList<Integer> primaryChannels = new ArrayList<>();
        for(int i = 0; i < numberOfFilters; i++) {
            int bestChannel = chosenChannels.get(0);
            double bestChannelValue = 0;
            for(int j = 0; j < chosenChannels.size(); j++) {
                if(proportionArray[chosenFilters.get(i)][chosenChannels.get(j)] > bestChannelValue) {
                    bestChannelValue = proportionArray[chosenFilters.get(i)][chosenChannels.get(j)];
                    bestChannel = chosenChannels.get(j);
                }
            }
            primaryChannels.add(bestChannel);
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

                double[] result = new double[numberOfFilters];

                for(int i = 0 ; i < numberOfFilters; i++) {
                    result[i] = pixelIntensity.get(chosenChannels.indexOf(primaryChannels.get(i)));
                    for(int j = 0; j < numberOfFilters; j++) {
                        if(j != i) {
                            result[i] = result[i] - (beta[j] * proportionArray[chosenFilters.get(j)][primaryChannels.get(i)]);
                        }
                    }
                    if(result[i] < 0) {
                        resultImage.getRaster().setSample(x, y, i, 0);
                    } else {
                        resultImage.getRaster().setSample(x, y, 0, result[i]);
                    }
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
