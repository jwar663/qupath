package qupath.lib.gui.commands;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.math3.linear.SingularMatrixException;
import qupath.lib.common.ManualUnmixing;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    public static int numberOfStainsEntered = 0;

    public static void resetChannelLists() {
        DAPIChannels.clear();
        opal780Channels.clear();
        opal480Channels.clear();
        opal690Channels.clear();
        FITCChannels.clear();
        cy3Channels.clear();
        texasRedChannels.clear();
    }

    public static void setNumberOfStainsEntered(int value) {
        numberOfStainsEntered = value;
    }

    public static int getNumberOfStainsEntered() {
        return numberOfStainsEntered;
    }

    public static void createManualUnmix(QuPathGUI qupath) {
        try {
            File file = Dialogs.promptForFile("Select indirect data csv file", null, null);
            double[][] proportionArray = new double[7][43];
            System.out.println("in manual unmixing");
            Stage dialog;
            proportionArray = DuplicateMatrixCommand.readCSV(file.toString(), proportionArray);
            dialog = createUnmixingDialog(qupath.getImageData(), qupath.getStage(), proportionArray, qupath);
            dialog.showAndWait();
        } catch(NullPointerException npe) {
            Dialogs.showErrorMessage("Error", "No file was chosen, or file was invalid");
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

    protected static void setChannels(CheckBox[] checkBoxes) {
        for(int i = 0; i < DAPIOptions; i++) {
            if(checkBoxes[i].isSelected()) {
                DAPIChannels.add(i);
            }
        }
        for(int i = 9; i < 9 + opal780Options; i++) {
            if(checkBoxes[i].isSelected()) {
                opal780Channels.add(i);
            }
        }
        for(int i = 11; i < 11 + opal480Options; i++) {
            if(checkBoxes[i].isSelected()) {
                opal480Channels.add(i);
            }
        }
        for(int i = 17; i < 17 + opal690Options; i++) {
            if(checkBoxes[i].isSelected()) {
                opal690Channels.add(i);
            }
        }
        for(int i = 20; i < 20 + FITCOptions; i++) {
            if(checkBoxes[i].isSelected()) {
                FITCChannels.add(i);
            }
        }
        for(int i = 29; i < 29 + cy3Options; i++) {
            if(checkBoxes[i].isSelected()) {
                cy3Channels.add(i);
            }
        }
        for(int i = 36; i < 36 + texasRedOptions; i++) {
            if(checkBoxes[i].isSelected()) {
                texasRedChannels.add(i);
            }
        }
    }

    protected static Stage createUnmixingDialog(ImageData<BufferedImage> imageData, Stage duplicateDialog, double[][] proportionArray, QuPathGUI qupath) throws  NullPointerException {

        Stage unmixDialog = new Stage();
        unmixDialog.setTitle("Unmix Options");
        unmixDialog.initOwner(qupath.getStage());

        Pane overallPane = new Pane();

        VBox overallVBox = new VBox();

        overallPane.getChildren().add(overallVBox);

        Label infoLabel = new Label("Please select which channels to use for unmixing");

        HBox numberOfStainsHBox = new HBox();
        numberOfStainsHBox.setPrefHeight(40.0);
        numberOfStainsHBox.setPrefWidth(450.0);
        numberOfStainsHBox.setSpacing(20.0);
        Label numberOfStainsLabel = new Label("Number of stains to unmix:");

        TextField numberOfStainsText = new TextField("7");
        numberOfStainsText.setPrefSize(40,25);
        // force the field to be numeric only
        numberOfStainsText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    numberOfStainsText.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        numberOfStainsHBox.getChildren().add(numberOfStainsLabel);
        numberOfStainsHBox.getChildren().add(numberOfStainsText);

        overallVBox.getChildren().add(numberOfStainsHBox);

        overallVBox.getChildren().add(infoLabel);

        HBox dapiHBox = createHBox("DAPI");
        HBox opal780HBox = createHBox("OPAL 780");
        HBox opal480HBox = createHBox("OPAL 480");
        HBox opal690HBox = createHBox("OPAL 690");
        HBox fitcHBox = createHBox("FITC");
        HBox cy3HBox = createHBox("Cy3");
        HBox texasRedHBox = createHBox("TEXAS RED");

        HBox bottomHBox = new HBox();
        bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setSpacing(40.0);

        Label noteLabel = new Label("Note: Please select less channels per filter than specified number of stains above");

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

        Button submitButton = new Button("Next");
        submitButton.setPrefSize(60.0, 25.0);
        submitButton.setOnAction(e -> {
            setChannels(checkBoxes);
            if(checkValidCheckBoxes()) {
                setNumberOfStainsEntered(Integer.parseInt(numberOfStainsText.getText()));
                Stage stainDialog = createStainDialog(imageData, proportionArray, qupath, unmixDialog);
                stainDialog.showAndWait();
            } else {
                Dialogs.showErrorMessage("Error", "Please select a maximum of 7 channels for each filter");
                resetChannelLists();
            }
        });

        bottomHBox.getChildren().add(noteLabel);
        bottomHBox.getChildren().add(submitButton);

        overallVBox.getChildren().add(bottomHBox);

        Scene scene = new Scene(overallPane);
        unmixDialog.setScene(scene);

        return unmixDialog;
    }

    private static HBox createStainHBox(int number, ObservableList<Integer> choices) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(10));
        hBox.setSpacing(20.0);
        Label label = new Label("Stain " + number);
        TextField textField = new TextField("Stain " + number);
        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.setItems(choices);
        if(choices.size() >= number) {
            choiceBox.setValue(choiceBox.getItems().get(number - 1));
        } else {
            choiceBox.setValue(choiceBox.getItems().get(0));
        }


        hBox.getChildren().add(label);
        hBox.getChildren().add(textField);
        hBox.getChildren().add(choiceBox);
        return hBox;
    }

    private static ArrayList<Integer> setPossibleChannelsArrayList() {
        ArrayList<Integer> possibleChannels = new ArrayList<>();
        for(int i = 0; i < DAPIChannels.size(); i++) {
            possibleChannels.add(DAPIChannels.get(i) + 1);
        }
        for(int i = 0; i < opal780Channels.size(); i++) {
            possibleChannels.add(opal780Channels.get(i) + 1);
        }
        for(int i = 0; i < opal480Channels.size(); i++) {
            possibleChannels.add(opal480Channels.get(i) + 1);
        }
        for(int i = 0; i < opal690Channels.size(); i++) {
            possibleChannels.add(opal690Channels.get(i) + 1);
        }
        for(int i = 0; i < FITCChannels.size(); i++) {
            possibleChannels.add(FITCChannels.get(i) + 1);
        }
        for(int i = 0; i < cy3Channels.size(); i++) {
            possibleChannels.add(cy3Channels.get(i) + 1);
        }
        for(int i = 0; i < texasRedChannels.size(); i++) {
            possibleChannels.add(texasRedChannels.get(i) + 1);
        }
        return possibleChannels;
    }

    protected static Stage createStainDialog(ImageData<BufferedImage> imageData, double[][] proportionArray, QuPathGUI qupath, Stage unmixDialog) throws  NullPointerException {

        Stage stainDialog = new Stage();
        stainDialog.setTitle("Stain Options");
        stainDialog.initOwner(qupath.getStage());
        stainDialog.initModality(Modality.WINDOW_MODAL);

        Pane overallPane = new Pane();

        VBox overallVBox = new VBox();

        int numberOfStainsEntered = getNumberOfStainsEntered();

        HBox[] hBoxes = new HBox[numberOfStainsEntered];

        overallPane.getChildren().add(overallVBox);

        ArrayList<Integer> possibleChannels = setPossibleChannelsArrayList();


        ObservableList<Integer> possibleChannelsObservable = FXCollections.observableArrayList(possibleChannels);

        for(int i = 0; i < numberOfStainsEntered; i++) {
            hBoxes[i] = createStainHBox(i + 1, possibleChannelsObservable);
            overallVBox.getChildren().add(hBoxes[i]);
        }

        Button submitButton = new Button("Submit");
        submitButton.setPrefSize(60.0, 25.0);
        submitButton.setOnAction(e -> {
                try {
                    ImageData newImageData = ManualUnmixing.unmixAll(imageData, proportionArray, DAPIChannels, opal780Channels, opal480Channels, opal690Channels, FITCChannels, cy3Channels, texasRedChannels);
                    resetChannelLists();
                    unmixDialog.close();
                    stainDialog.close();
                    qupath.getViewer().setImageData(newImageData);
                    File file = Dialogs.promptForDirectory(null);
                    String filePath = file.toString();
                    DuplicateMatrixCommand.exportImage(qupath.getViewer(),  filePath + File.separator +"manual-unmixed-image", qupath.getStage());
                } catch (SingularMatrixException sme) {
                    Dialogs.showErrorMessage("Error", "One or more values create a singular matrix");
                    resetChannelLists();
                    sme.printStackTrace();
                } catch (NullPointerException npe) {
                    Dialogs.showErrorMessage("Error", "No export directory was chosen");
                    resetChannelLists();
                    npe.printStackTrace();
                }
        });

        HBox bottomHBox = new HBox();
        bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setSpacing(40.0);

        bottomHBox.getChildren().add(submitButton);

        overallVBox.getChildren().add(bottomHBox);

        Scene scene = new Scene(overallPane);
        stainDialog.setScene(scene);

        return stainDialog;
    }
}
