package qupath.lib.gui.commands;

import qupath.lib.common.AutoUnmixing;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;

import java.io.File;

public class AutoUnmixingDialog {

    public static void createAutoUnmix(QuPathGUI qupath) {


            double[][] proportionArray = new double[7][43];
            System.out.println("in auto unmixing");
            try {
                File file = Dialogs.promptForFile("Select indirect data csv file", null, null);
                proportionArray = DuplicateMatrixCommand.readCSV(file.toString(), proportionArray);
                ImageData newImageData = AutoUnmixing.unmixAll_Crossed(qupath.getImageData(), proportionArray);
                qupath.getViewer().setImageData(newImageData);
                File exportDirectory = Dialogs.promptForDirectory(null);
                String filePath = exportDirectory.toString();
                System.out.println(filePath);
                DuplicateMatrixCommand.exportImage(qupath.getViewer(),  filePath + File.separator + "auto-unmixed-image", qupath.getStage());
            } catch (NullPointerException npe){
                Dialogs.showErrorMessage("Error", "No directory/file was chosen");
                npe.printStackTrace();
            }
    }
}
