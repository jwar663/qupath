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
     */
    public List<ImageData<BufferedImage>> initialiseImageDataList(ImageData<BufferedImage> firstImageData) {
        this.imageDataList.add(firstImageData);
        //TODO: update GUI when image is added
        return imageDataList;
    }

    public List<ImageData<BufferedImage>> getImageDataList() {
        return this.imageDataList;
    }

    public void addImage(ImageData<BufferedImage> imageData) {
        this.imageDataList.add(imageData);
        //TODO: update GUI when image is added
    }

    public void setAsActiveImage(ImageData<BufferedImage> imageData, QuPathViewer viewer) {
        viewer.setImageData(imageData);
        //TODO: update GUI when image is selected
    }

    public boolean deleteImageFromList(ImageData<BufferedImage> imageData) {
        boolean didDelete = false;
        if(this.imageDataList.contains(imageData)) {
            this.imageDataList.remove(imageData);
            didDelete = true;
            //TODO: update GUI when image is deleted
        }
        return didDelete;
    }
}
