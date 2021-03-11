package qupath.lib.gui;

import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to complete all interactions with the list of imageData.
 *
 * @author Jaedyn Ward
 */
public class ImageSelector {

    private static List<ImageData<BufferedImage>> imageDataList = new ArrayList<ImageData<BufferedImage>>();

    /**
     * Return the list of image data when the first image is opened.
     *
     * @param firstImageData
     */
    public static List<ImageData<BufferedImage>> initialiseImageDataList(ImageData<BufferedImage> firstImageData) {
        imageDataList.add(firstImageData);
        //TODO: update GUI when image is added
        return imageDataList;
    }

    /**
     * Return the current list of image data objects.
     *
     */
    public static List<ImageData<BufferedImage>> getImageDataList() {
        return imageDataList;
    }

    /**
     * Add an image to the list of image data.
     *
     * @param imageData
     */
    public static void addImage(ImageData<BufferedImage> imageData) {
        imageDataList.add(imageData);
        //TODO: update GUI when image is added
    }

    /**
     * Specify the image to be set as the current image.
     *
     * @param imageData
     * @param
     */
    public static void setAsActiveImage(ImageData<BufferedImage> imageData, QuPathViewer viewer) {
        viewer.setImageData(imageData);
        //TODO: update GUI when image is selected
    }

    /**
     * Delete the specified image from the list of image data.
     *
     * @param imageData
     */
    public static boolean deleteImageFromList(ImageData<BufferedImage> imageData) {
        boolean didDelete = false;
        if(imageDataList.contains(imageData)) {
            imageDataList.remove(imageData);
            didDelete = true;
            //TODO: update GUI when image is deleted
        }
        return didDelete;
    }

    /**
     * Check if the image is already included in the list of image data.
     *
     * @param imageData
     */
    public static boolean checkIfDuplicateImage(ImageData<BufferedImage> imageData) {
        return imageDataList.contains(imageData);
    }


    /**
     * Check if the image list of image data has already been initialised.
     *
     */
    public static boolean checkIfImageListInitialised() {
        return !imageDataList.isEmpty();
    }
}
