package qupath.lib.gui;

import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.util.List;


/**
 * Class to complete all interactions with the list of imageData.
 *
 * @author Jaedyn Ward
 */
public class ImageSelector {

    private List<ImageData<BufferedImage>> imageDataList = null;

    /**
     * Return the list of image data when the first image is opened.
     *
     * @param firstImageData
     */
    public List<ImageData<BufferedImage>> initialiseImageDataList(ImageData<BufferedImage> firstImageData) {
        this.imageDataList.add(firstImageData);
        //TODO: update GUI when image is added
        return imageDataList;
    }

    /**
     * Return the current list of image data objects.
     *
     */
    public List<ImageData<BufferedImage>> getImageDataList() {
        return this.imageDataList;
    }

    /**
     * Add an image to the list of image data.
     *
     * @param imageData
     */
    public void addImage(ImageData<BufferedImage> imageData) {
        this.imageDataList.add(imageData);
        //TODO: update GUI when image is added
    }

    /**
     * Specify the image to be set as the current image.
     *
     * @param imageData
     * @param
     */
    public void setAsActiveImage(ImageData<BufferedImage> imageData, QuPathViewer viewer) {
        viewer.setImageData(imageData);
        //TODO: update GUI when image is selected
    }

    /**
     * Delete the specified image from the list of image data.
     *
     * @param imageData
     */
    public boolean deleteImageFromList(ImageData<BufferedImage> imageData) {
        boolean didDelete = false;
        if(this.imageDataList.contains(imageData)) {
            this.imageDataList.remove(imageData);
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
    public boolean checkIfDuplicateImage(ImageData<BufferedImage> imageData) {
        return this.imageDataList.contains(imageData);
    }


    /**
     * Check if the image list of image data has already been initialised.
     *
     */
    public boolean checkIfImageListInitialised() {
        return !(imageDataList == null);
    }
}
