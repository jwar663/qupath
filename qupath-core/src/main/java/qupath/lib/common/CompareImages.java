package qupath.lib.common;

import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompareImages {

    /**
     * Find which image corresponds to which filter. Enter an image with known filters so we can map them to
     * the most similar image and report the values.
     *
     * @param imageDataList
     */
    public static void mapFilters(List<ImageData> imageDataList) {
        String filePath = "D:\\Desktop\\QuPath\\Compare Images\\MappedFilters.csv";
        BufferedImage img1 = RemoveDuplicate.convertImageDataToImage(imageDataList.get(0));
        BufferedImage img2 = RemoveDuplicate.convertImageDataToImage(imageDataList.get(1));
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
                correlationMatrix[i][j] = RemoveDuplicate.normCrossCorrelationFloat(img1Channels[i], img2Channels[j]);
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

    /**
     * Use normalised cross correlation to compare different images, to give a numerical
     * value. This allows easier evaluation of different unmixing methods.
     *
     * @param imageDataList
     */
    public static void compareImages(List<ImageData> imageDataList) {
        String filePath = "D:\\Desktop\\QuPath\\Compare Images\\im3_vs_Duplicate_Scaled.csv";
        List<BufferedImage> images = new ArrayList<>();
        float[] maxIntensities = new float[imageDataList.size()];
        for(int i = 0; i < imageDataList.size(); i++) {
            images.add(RemoveDuplicate.convertImageDataToImage(imageDataList.get(i)));
            maxIntensities[i] = RemoveDuplicate.findMaximumPixelIntensity(images.get(i));
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
                    pixelIntensities1 = RemoveDuplicate.convertAllMaximumPixelIntensities(images.get(img1), orderChannels.get(img1).get(band), maxIntensity1);
                    pixelIntensities2 = RemoveDuplicate.convertAllMaximumPixelIntensities(images.get(img2), orderChannels.get(img2).get(band), maxIntensity2);
                    values[band] = Float.toString(RemoveDuplicate.normCrossCorrelationFloat(pixelIntensities1, pixelIntensities2));
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
}
