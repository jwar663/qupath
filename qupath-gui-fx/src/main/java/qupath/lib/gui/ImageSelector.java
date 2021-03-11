package qupath.lib.gui;

import qupath.lib.images.ImageData;

import java.util.List;


/**
 * Class to complete all interactions with the list of imageData.
 *
 * @author Jaedyn Ward
 */
public class ImageSelector {

    private List<ImageData> imageDataList = null;

    /**
     * Return the list of image data when the first image is opened.
     */
    public List<ImageData> initialiseImageDataList(ImageData firstImageData) {
        this.imageDataList.add(firstImageData);
        return imageDataList;
    }

    public List<ImageData> getImageDataList() {
        return this.imageDataList;
    }

    public void addImage(ImageData imageData) {
        this.imageDataList.add(imageData);
    }

}
