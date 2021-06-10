package qupath.lib.gui.commands;

import qupath.lib.common.AutoUnmixing;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;

import java.io.File;

public class AutoUnmixingDialog {

    public static void createAutoUnmix(QuPathGUI qupath) {
        File file = Dialogs.promptForFile("Select indirect data csv file", null, null);
        double[][] proportionArray = new double[7][43];
        System.out.println("in auto unmixing");
        try {
            proportionArray = DuplicateMatrixCommand.readCSV(file.toString(), proportionArray);
            ImageData newImageData = AutoUnmixing.unmixAll_Crossed(qupath.getImageData(), proportionArray);
            qupath.getViewer().setImageData(newImageData);
            DuplicateMatrixCommand.exportImage(qupath.getViewer(),  "D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\unmixed-All_Crossed", qupath.getStage());
        } catch (NullPointerException npe){
            npe.printStackTrace();
        }
    }
}
