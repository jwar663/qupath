package qupath.lib.common;

import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.WrappedBufferedImageServer;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AutoUnmixing {

    /**
     * Uses other unmixing_crossed methods specific to each fluorophore in this class.
     * The channels that are used have been chosen by deciding which channels work best
     * for having the panel graphs where the lines have crossed over.
     *
     * @param imageData
     * @param proportionArray
     *
     */
    public static ImageData unmixAll_Crossed(ImageData imageData, double[][] proportionArray) {
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

        Thread DAPIThread = new MultiThreadAutoUnmix("DAPI", imageData, proportionArray);

        Thread opal780Thread = new MultiThreadAutoUnmix("Opal780", imageData, proportionArray);

        Thread opal480Thread = new MultiThreadAutoUnmix("Opal480", imageData, proportionArray);

        Thread opal690Thread = new MultiThreadAutoUnmix("Opal690", imageData, proportionArray);

        Thread FITCThread = new MultiThreadAutoUnmix("FITC", imageData, proportionArray);

        Thread cy3Thread = new MultiThreadAutoUnmix("Cy3", imageData, proportionArray);

        Thread texasRedThread = new MultiThreadAutoUnmix("TexasRed", imageData, proportionArray);

        DAPIThread.start();
        opal780Thread.start();
        opal480Thread.start();
        opal690Thread.start();
        FITCThread.start();
        cy3Thread.start();
        texasRedThread.start();

        try {
            DAPIThread.join();
            opal780Thread.join();
            opal480Thread.join();
            opal690Thread.join();
            FITCThread.join();
            cy3Thread.join();
            texasRedThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage DAPI_image = ((MultiThreadAutoUnmix) DAPIThread).getImage();

        BufferedImage Opal780_image = ((MultiThreadAutoUnmix) opal780Thread).getImage();

        BufferedImage Opal480_image = ((MultiThreadAutoUnmix) opal480Thread).getImage();

        BufferedImage Opal690_image = ((MultiThreadAutoUnmix) opal690Thread).getImage();

        BufferedImage FITC_image = ((MultiThreadAutoUnmix) FITCThread).getImage();

        BufferedImage Cy3_image = ((MultiThreadAutoUnmix) cy3Thread).getImage();

        BufferedImage TexasRed_image = ((MultiThreadAutoUnmix) texasRedThread).getImage();


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
}
