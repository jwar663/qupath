package qupath.lib.common;

import org.apache.commons.math3.linear.SingularMatrixException;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.WrappedBufferedImageServer;

import java.awt.image.BufferedImage;
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

        boolean error = false;

        ArrayList<Integer> notDuplicates = new ArrayList<>();

        ArrayList<ImageChannel> channels = new ArrayList<>();

        for(int i = 0; i < 7; i++) {
            notDuplicates.add(i);
            channels.add(imageData.getServer().getChannel(i));
        }


        BufferedImage resultImage = RemoveDuplicate.createNewBufferedImage(notDuplicates, oldImage);

        Thread DAPIThread = new MultiThreadManualUnmix("DAPI", imageData, proportionArray, DAPIChannels);

        Thread opal780Thread = new MultiThreadManualUnmix("Opal780", imageData, proportionArray, opal780Channels);

        Thread opal480Thread = new MultiThreadManualUnmix("Opal480", imageData, proportionArray, opal480Channels);

        Thread opal690Thread = new MultiThreadManualUnmix("Opal690", imageData, proportionArray, opal690Channels);

        Thread FITCThread = new MultiThreadManualUnmix("FITC", imageData, proportionArray, FITCChannels);

        Thread cy3Thread = new MultiThreadManualUnmix("Cy3", imageData, proportionArray, cy3Channels);

        Thread texasRedThread = new MultiThreadManualUnmix("TexasRed", imageData, proportionArray, texasRedChannels);

        try {
            DAPIThread.start();
            opal780Thread.start();
            opal480Thread.start();
            opal690Thread.start();
            FITCThread.start();
            cy3Thread.start();
            texasRedThread.start();

            DAPIThread.join();
            opal780Thread.join();
            opal480Thread.join();
            opal690Thread.join();
            FITCThread.join();
            cy3Thread.join();
            texasRedThread.join();
        } catch(InterruptedException ie) {
            error = true;
            ie.printStackTrace();
        } catch(SingularMatrixException sme) {
            error = true;
            sme.printStackTrace();
        }

        if(error) {
            return null;
        } else {
            BufferedImage DAPI_image = ((MultiThreadManualUnmix) DAPIThread).getImage();

            BufferedImage Opal780_image = ((MultiThreadManualUnmix) opal780Thread).getImage();

            BufferedImage Opal480_image = ((MultiThreadManualUnmix) opal480Thread).getImage();

            BufferedImage Opal690_image = ((MultiThreadManualUnmix) opal690Thread).getImage();

            BufferedImage FITC_image = ((MultiThreadManualUnmix) FITCThread).getImage();

            BufferedImage Cy3_image = ((MultiThreadManualUnmix) cy3Thread).getImage();

            BufferedImage TexasRed_image = ((MultiThreadManualUnmix) texasRedThread).getImage();

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
}
