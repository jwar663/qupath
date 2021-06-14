package qupath.lib.gui.commands;

import qupath.lib.common.AutoUnmixing;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;

import java.io.File;

public class AutoUnmixingDialog {

    public static void createAutoUnmix(QuPathGUI qupath) {
        File file = Dialogs.promptForFile("Select indirect data csv file", null, null);
        if(file.isFile()) {
            double[][] proportionArray = new double[7][43];
            System.out.println("in auto unmixing");
            try {
                proportionArray = DuplicateMatrixCommand.readCSV(file.toString(), proportionArray);
                ImageData newImageData = AutoUnmixing.unmixAll_Crossed(qupath.getImageData(), proportionArray);
                qupath.getViewer().setImageData(newImageData);
                File exportDirectory = Dialogs.promptForDirectory(null);
                if(exportDirectory.isFile()){
                    String filePath = exportDirectory.toString();
                    DuplicateMatrixCommand.exportImage(qupath.getViewer(),  filePath + "unmixed-image", qupath.getStage());
                }
            } catch (NullPointerException npe){
                npe.printStackTrace();
            }
        } else {
            Dialogs.showErrorMessage("Error", "No file was chosen");
        }
    }
}
