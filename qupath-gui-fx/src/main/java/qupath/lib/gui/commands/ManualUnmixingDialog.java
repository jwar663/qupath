package qupath.lib.gui.commands;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import qupath.lib.common.ManualUnmixing;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class ManualUnmixingDialog {

    private Stage dialog;

    //global variables for choosing channels
    public static ArrayList<Integer> DAPIChannels = new ArrayList<>();
    public static ArrayList<Integer> opal780Channels = new ArrayList<>();
    public static ArrayList<Integer> opal480Channels = new ArrayList<>();
    public static ArrayList<Integer> opal690Channels = new ArrayList<>();
    public static ArrayList<Integer> FITCChannels = new ArrayList<>();
    public static ArrayList<Integer> cy3Channels = new ArrayList<>();
    public static ArrayList<Integer> texasRedChannels = new ArrayList<>();

    public static final int DAPIOptions = 9;
    public static final int opal780Options = 2;
    public static final int opal480Options = 6;
    public static final int opal690Options = 3;
    public static final int FITCOptions = 9;
    public static final int cy3Options = 7;
    public static final int texasRedOptions = 7;

    public static void resetChannelLists() {
        DAPIChannels.clear();
        opal780Channels.clear();
        opal480Channels.clear();
        opal690Channels.clear();
        FITCChannels.clear();
        cy3Channels.clear();
        texasRedChannels.clear();
    }

    public static void createManualUnmix(QuPathGUI qupath) {
        File file = Dialogs.promptForFile("Select indirect data csv file", null, null);
        double[][] proportionArray = new double[7][43];
        System.out.println("in manual unmixing");
        Stage dialog;
        try {
            proportionArray = DuplicateMatrixCommand.readCSV(file.toString(), proportionArray);
           dialog = createUnmixingDialog(qupath.getImageData(), qupath.getStage(), proportionArray, qupath);
           dialog.showAndWait();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    protected static HBox createHBox(String filter) {
        HBox hBox = new HBox();
        hBox.setPrefHeight(40.0);
        hBox.setPrefWidth(450.0);
        Label label = new Label(filter);
        label.setPadding(new Insets(0,0,0,10));
        hBox.getChildren().add(label);
        return hBox;
    }

    protected static boolean checkValidCheckBoxes() {
        if(DAPIChannels.size() > 7) {
            return false;
        } else if(FITCChannels.size() > 7) {
            return false;
        } else {
            return true;
        }
    }

    protected static Stage createUnmixingDialog(ImageData<BufferedImage> imageData, Stage duplicateDialog, double[][] proportionArray, QuPathGUI qupath) throws  NullPointerException {

        Stage unmixDialog = new Stage();
        unmixDialog.setTitle("Unmix Options");

        Pane overallPane = new Pane();

        VBox overallVBox = new VBox();
//        overallVBox.setPrefSize(470.0, 780.0);

        overallPane.getChildren().add(overallVBox);

        HBox dapiHBox = createHBox("DAPI");
        HBox opal780HBox = createHBox("OPAL 780");
        HBox opal480HBox = createHBox("OPAL 480");
        HBox opal690HBox = createHBox("OPAL 690");
        HBox fitcHBox = createHBox("FITC");
        HBox cy3HBox = createHBox("Cy3");
        HBox texasRedHBox = createHBox("TEXAS RED");

        overallVBox.getChildren().addAll(dapiHBox, opal780HBox, opal480HBox, opal690HBox, fitcHBox, cy3HBox, texasRedHBox);
        overallVBox.setPadding(new Insets(10));
        overallVBox.setSpacing(10);

        dapiHBox.setSpacing(10);
        dapiHBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        dapiHBox.setAlignment(Pos.CENTER_LEFT);
        opal780HBox.setSpacing(10);
        opal780HBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        opal780HBox.setAlignment(Pos.CENTER_LEFT);
        opal480HBox.setSpacing(10);
        opal480HBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        opal480HBox.setAlignment(Pos.CENTER_LEFT);
        opal690HBox.setSpacing(10);
        opal690HBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        opal690HBox.setAlignment(Pos.CENTER_LEFT);
        fitcHBox.setSpacing(10);
        fitcHBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        fitcHBox.setAlignment(Pos.CENTER_LEFT);
        cy3HBox.setSpacing(10);
        cy3HBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        cy3HBox.setAlignment(Pos.CENTER_LEFT);
        texasRedHBox.setSpacing(10);
        texasRedHBox.setStyle("-fx-border-style: solid inside; -fx-border-color: #000000");
        texasRedHBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox[] checkBoxes = new CheckBox[43];

        for(int i = 1; i <= DAPIOptions; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            dapiHBox.getChildren().add(checkBoxes[i - 1]);
        }

        for(int i = 10; i <= opal780Options + 9; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            opal780HBox.getChildren().add(checkBoxes[i - 1]);
        }

        for(int i = 12; i <= opal480Options + 11; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            opal480HBox.getChildren().add(checkBoxes[i - 1]);
        }

        for(int i = 18; i <= opal690Options + 17; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            opal690HBox.getChildren().add(checkBoxes[i - 1]);
        }

        for(int i = 21; i <= FITCOptions + 20; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            fitcHBox.getChildren().add(checkBoxes[i - 1]);
        }

        for(int i = 30; i <= cy3Options + 29; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            cy3HBox.getChildren().add(checkBoxes[i - 1]);
        }

        for(int i = 37; i <= texasRedOptions + 36; i++) {
            checkBoxes[i - 1] = new CheckBox(Integer.toString(i));
            checkBoxes[i - 1].setSelected(false);
            texasRedHBox.getChildren().add(checkBoxes[i - 1]);
        }

        Button submitButton = new Button("Submit");
        submitButton.setPrefSize(60.0, 25.0);
        submitButton.setOnAction(e -> {

            if(checkValidCheckBoxes()) {
                ImageData newImageData = ManualUnmixing.unmixAll(imageData, proportionArray, DAPIChannels, opal780Channels, opal480Channels, opal690Channels, FITCChannels, cy3Channels, texasRedChannels);
                qupath.getViewer().setImageData(newImageData);
                DuplicateMatrixCommand.exportImage(qupath.getViewer(),  "D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\Unmixed-Manual", qupath.getStage());
            } else {
                Dialogs.showErrorMessage("Error", "Please select a maximum of 7 channels for each filter");
                resetChannelLists();
            }
        });

        Scene scene = new Scene(overallPane);
        unmixDialog.setScene(scene);

        return unmixDialog;
    }
}
