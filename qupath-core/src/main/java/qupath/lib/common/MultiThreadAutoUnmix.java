package qupath.lib.common;

import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MultiThreadAutoUnmix extends Thread {

    String name;
    BufferedImage image;
    ImageData imgData;
    double[][] proportion;


    MultiThreadAutoUnmix(String name, ImageData imgData, double[][] proportion) {
        this.name = name;
        this.imgData = imgData;
        this.proportion = proportion;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void run() {
        if(this.name.equals("DAPI")) {
            image = unmixDAPI_Crossed(imgData, proportion);
        } else if(this.name.equals("Opal780")) {
            image = unmixOpal780_Crossed(imgData, proportion);
        } else if(this.name.equals("Opal480")) {
            image = unmixOpal480_Crossed(imgData, proportion);
        } else if(this.name.equals("Opal690")) {
            image = unmixOpal690_Crossed(imgData, proportion);
        } else if(this.name.equals("FITC")) {
            image = unmixFITC_Crossed(imgData, proportion);
        } else if(this.name.equals("Cy3")) {
            image = unmixCy3_Crossed(imgData, proportion);
        } else if(this.name.equals("TexasRed")) {
            image = unmixTexasRed_Crossed(imgData, proportion);
        }
    }

    /**
     * Unmixes the Texas Red fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixTexasRed_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 36/38
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(36);
        chosenChannels.add(38);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(2);
        chosenFilters.add(6);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "TexasRed_Crossed_A" + ".csv");
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
     * Unmixes the Cy3 fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixCy3_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 30/34
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(30);
        chosenChannels.add(34);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(1);
        chosenFilters.add(2);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Cy3_Crossed_A" + ".csv");
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
     * Unmixes the Opal780 fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixOpal780_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 9-10
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(9);
        chosenChannels.add(10);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(6);
        chosenFilters.add(5);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Opal780_Crossed_A" + ".csv");
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
     * Unmixes the FITC fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixFITC_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 21-29
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(21);
        chosenChannels.add(23);
        chosenChannels.add(25);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(0);
        chosenFilters.add(6);
        chosenFilters.add(1);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "FITC_Crossed_A" + ".csv");
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
     * Unmixes the Opal480 fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixOpal480_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 21-29
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(13);
        chosenChannels.add(14);
        chosenChannels.add(15);
        chosenChannels.add(16);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(3);
        chosenFilters.add(6);
        chosenFilters.add(0);
        chosenFilters.add(5);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Opal480_Crossed_A" + ".csv");
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
     * Unmixes the Opal690 fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixOpal690_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 17-19
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(17);
        chosenChannels.add(18);
        chosenChannels.add(19);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(5);
        chosenFilters.add(6);
        chosenFilters.add(2);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "Opal690_Crossed_A" + ".csv");
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
     * Unmixes the DAPI fluorophore with the best channels selected for crossing
     * the lines in the panel graph.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static BufferedImage unmixDAPI_Crossed(ImageData imageData, double[][] proportionArray) {
        //channels 21-29
        BufferedImage oldImage = RemoveDuplicate.convertImageDataToImage(imageData);

        ArrayList<Integer> chosenChannels = new ArrayList<>();
        chosenChannels.add(2);
        chosenChannels.add(3);

        ArrayList<Integer> chosenFilters = new ArrayList<>();
        chosenFilters.add(4);
        chosenFilters.add(3);

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
//            FileWriter writer = new FileWriter("D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\" + "DAPI_Crossed_A" + ".csv");
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
