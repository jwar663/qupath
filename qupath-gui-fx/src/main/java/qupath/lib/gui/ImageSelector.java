package qupath.lib.gui;

import qupath.lib.images.ImageData;

import java.util.List;


/**
 * Class to complete all interactions with the list of imageData.
 *
 * @author Jaedyn Ward
 */
public class ImageSelector {

    /**
     * Return the list of image data when the first image is opened.
     */
    public List<ImageData> initialiseImageDataList(ImageData firstImageData) {
        List<ImageData> imageDataList = null;
        imageDataList.add(firstImageData);
        return imageDataList;
    }

}
