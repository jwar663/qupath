package qupath.lib.gui.commands;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import qupath.lib.common.AutoUnmixing;
import qupath.lib.common.ManualUnmixing;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public ArrayList<Integer> DAPIOptions = new ArrayList<>();
    public ArrayList<Integer> opal780Options = new ArrayList<>();
    public ArrayList<Integer> opal480Options = new ArrayList<>();
    public ArrayList<Integer> opal690Options = new ArrayList<>();
    public ArrayList<Integer> FITCOptions = new ArrayList<>();
    public ArrayList<Integer> cy3Options = new ArrayList<>();
    public ArrayList<Integer> texasRedOptions = new ArrayList<>();

    public static int[] numberOfChannelsArray = new int[7];

    public void initialiseChannelOptions() {
        for(int i = 1; i <= 9; i++) {
            DAPIOptions.add(i);
        }

        for(int i = 10; i <= 11; i++) {
            opal780Options.add(i);
        }

        for(int i = 12; i <= 17; i++) {
            opal480Options.add(i);
        }

        for(int i = 18; i <= 20; i++) {
            opal690Options.add(i);
        }

        for(int i = 21; i <= 29; i++) {
            FITCOptions.add(i);
        }

        for(int i = 30; i <= 36; i++) {
            cy3Options.add(i);
        }

        for(int i = 37; i <= 43; i++) {
            texasRedOptions.add(i);
        }
    }

    public static List<Integer> getChannels(String filter) {
        if(filter.equals("DAPI")) {
            return DAPIChannels;
        } else if(filter.equals("Opal780")) {
            return opal780Channels;
        } else if(filter.equals("Opal480")) {
            return opal480Channels;
        } else if(filter.equals("Opal690")) {
            return opal690Channels;
        } else if(filter.equals("FITC")) {
            return FITCChannels;
        } else if(filter.equals("Cy3")) {
            return cy3Channels;
        } else if(filter.equals("TexasRed")) {
            return texasRedChannels;
        } else {
            return null;
        }
    }

    public void setChannels(String filter, ArrayList<Integer> chosenChannels) {
        if(filter.equals("DAPI")) {
            DAPIChannels = chosenChannels;
        } else if(filter.equals("Opal780")) {
            opal780Channels = chosenChannels;
        } else if(filter.equals("Opal480")) {
            opal480Channels = chosenChannels;
        } else if(filter.equals("Opal690")) {
            opal690Channels = chosenChannels;
        } else if(filter.equals("FITC")) {
            FITCChannels = chosenChannels;
        } else if(filter.equals("Cy3")) {
            cy3Channels = chosenChannels;
        } else if(filter.equals("TexasRed")) {
            texasRedChannels = chosenChannels;
        }
    }

    public void editChannel(String filter, int index, int channel) {
        if(filter.equals("DAPI")) {
            DAPIChannels.add(index, channel);
        } else if(filter.equals("Opal780")) {
            opal780Channels.add(index, channel);
        } else if(filter.equals("Opal480")) {
            opal480Channels.add(index, channel);
        } else if(filter.equals("Opal690")) {
            opal690Channels.add(index, channel);
        } else if(filter.equals("FITC")) {
            FITCChannels.add(index, channel);
        } else if(filter.equals("Cy3")) {
            cy3Channels.add(index, channel);
        } else if(filter.equals("TexasRed")) {
            texasRedChannels.add(index, channel);
        }
    }

    public void initialiseChannelLists(int numberOfDAPI, int numberOfOpal780, int numberOfOpal480, int numberOfOpal690, int numberOfFITC, int numberOfCy3, int numberOfTexasRed) {
        for(int i = 0; i < numberOfDAPI; i++) {
            DAPIChannels.add(i);
        }

        for(int i = 0; i < numberOfOpal780; i++) {
            opal780Channels.add(i);
        }

        for(int i = 0; i < numberOfOpal480; i++) {
            opal480Channels.add(i);
        }

        for(int i = 0; i < numberOfOpal690; i++) {
            opal690Channels.add(i);
        }

        for(int i = 0; i < numberOfFITC; i++) {
            FITCChannels.add(i);
        }

        for(int i = 0; i < numberOfCy3; i++) {
            cy3Channels.add(i);
        }

        for(int i = 0; i < numberOfTexasRed; i++) {
            texasRedChannels.add(i);
        }
    }

    public void resetChannelLists() {
        DAPIChannels.clear();
        opal780Channels.clear();
        opal480Channels.clear();
        opal690Channels.clear();
        FITCChannels.clear();
        cy3Channels.clear();
        texasRedChannels.clear();
    }

    protected static ChoiceBox createUnmixChoiceBox(int numberOfPossibleChannels, String filter) {
        ChoiceBox<Integer> choiceBox = new ChoiceBox<>();
        for(int i = 0; i <= numberOfPossibleChannels; i++) {
            choiceBox.getItems().add(i);
        }
        choiceBox.setPrefSize(80.0, 25.0);
        choiceBox.setMinSize(80.0, 25.0);
        choiceBox.setMaxSize(80.0, 25.0);
        choiceBox.setValue(getChannels(filter).size());

        return choiceBox;
    }

    protected static Label createUnmixLabel(String fluor) {
        Label label = new Label(fluor);
        label.setPrefSize(80.0, 25.0);
        label.setMinSize(80.0, 25.0);
        label.setMaxSize(80.0, 25.0);
        return label;
    }

    protected ChoiceBox createChannelSelectionChoiceBox(int index, String filter) {
        ChoiceBox choiceBox = new ChoiceBox();
        List<Integer> channelOptions;
        channelOptions = getChannels(filter);
        for(int i = 0; i < channelOptions.size(); i++) {
            choiceBox.getItems().add(channelOptions.get(i));
        }

        choiceBox.setValue(channelOptions.get(index));

        choiceBox.setOnAction(e -> {
            editChannel(filter, index, (Integer) choiceBox.getValue());
        });

        System.out.println("creating choice box");
        return choiceBox;
    }

    protected Label createChannelSelectionLabel(int index) {
        Label label = new Label("Channel " + (index + 1));
        System.out.println("creating label");
        return label;
    }

    protected ColumnConstraints createColumnConstraintsChannelSelection(int numberOfChannels) {
        double width = 420.0/numberOfChannels;
        ColumnConstraints columnConstraints = new ColumnConstraints(width, width, width);
        columnConstraints.setHalignment(HPos.CENTER);

        return columnConstraints;
    }
//
public static void createManualUnmix(QuPathGUI qupath) {
        File file = Dialogs.promptForFile("Select indirect data csv file", null, null);
        double[][] proportionArray = new double[7][43];
        System.out.println("in manual unmixing");
        Stage dialog;
        try {
            proportionArray = DuplicateMatrixCommand.readCSV(file.toString(), proportionArray);
           dialog = createUnmixingDialog(qupath.getImageData(), qupath.getStage(), proportionArray);
           dialog.showAndWait();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    protected static Stage createUnmixingDialog(ImageData<BufferedImage> imageData, Stage duplicateDialog, double[][] proportionArray) throws  NullPointerException {

        Stage unmixDialog = new Stage();
        unmixDialog.setTitle("Unmix Options");

        int[] numberOfChannels = new int[7];
        for(int i = 0; i < numberOfChannelsArray.length; i++) {
            numberOfChannelsArray[i] = 0;
        }

        Pane overallPane = new Pane();

        //larger panes
        GridPane gridPane = new GridPane();
        gridPane.setMaxSize(400.0, 410.0);
        gridPane.setPrefSize(400.0, 410.0);
        gridPane.setMinSize(400.0, 410.0);
        overallPane.getChildren().add(gridPane);

        Label DAPILabel = createUnmixLabel("DAPI");
        DAPILabel.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> DAPIBox = createUnmixChoiceBox(7, "DAPI");
        DAPIBox.setOnAction(e -> {
            numberOfChannelsArray[0] = DAPIBox.getValue();
        });
        gridPane.add(DAPILabel, 0,1);
        gridPane.add(DAPIBox, 0, 2);

        Label Opal780Label = createUnmixLabel("Opal780");
        Opal780Label.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> Opal780Box = createUnmixChoiceBox(2, "Opal780");
        Opal780Box.setOnAction(e -> {
            numberOfChannelsArray[1] = Opal780Box.getValue();
        });
        gridPane.add(Opal780Label, 1,1);
        gridPane.add(Opal780Box, 1, 2);

        Label Opal480Label = createUnmixLabel("Opal480");
        Opal480Label.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> Opal480Box = createUnmixChoiceBox(6, "Opal480");
        Opal480Box.setOnAction(e -> {
            numberOfChannelsArray[2] = Opal480Box.getValue();
        });
        gridPane.add(Opal480Label, 2,1);
        gridPane.add(Opal480Box, 2, 2);

        Label Opal690Label = createUnmixLabel("Opal690");
        Opal690Label.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> Opal690Box = createUnmixChoiceBox(3, "Opal690");
        Opal690Box.setOnAction(e -> {
            numberOfChannelsArray[3] = Opal690Box.getValue();
        });
        gridPane.add(Opal690Label, 0,3);
        gridPane.add(Opal690Box, 0, 4);

        Label FITCLabel = createUnmixLabel("FITC");
        FITCLabel.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> FITCBox = createUnmixChoiceBox(7, "FITC");
        FITCBox.setOnAction(e -> {
            numberOfChannelsArray[4] = FITCBox.getValue();
        });
        gridPane.add(FITCLabel, 1,3);
        gridPane.add(FITCBox, 1, 4);

        Label Cy3Label = createUnmixLabel("Cy3");
        Cy3Label.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> Cy3Box = createUnmixChoiceBox(7, "Cy3");
        Cy3Box.setOnAction(e -> {
            numberOfChannelsArray[5] = Cy3Box.getValue();
        });
        gridPane.add(Cy3Label, 2,3);
        gridPane.add(Cy3Box, 2, 4);

        Label TexasRedLabel = createUnmixLabel("TexasRed");
        TexasRedLabel.setAlignment(Pos.CENTER);
        ChoiceBox<Integer> TexasRedBox = createUnmixChoiceBox(7, "TexasRed");
        TexasRedBox.setOnAction(e -> {
            numberOfChannelsArray[6] = TexasRedBox.getValue();
        });
        gridPane.add(TexasRedLabel, 1,5);
        gridPane.add(TexasRedBox, 1, 6);

        Button nextButton = new Button("Next");
        nextButton.setPrefSize(100.0, 50.0);
        nextButton.setMinSize(100.0, 50.0);
        nextButton.setMaxSize(100.0, 50.0);
        gridPane.add(nextButton, 2, 5, 1, 2);


        nextButton.setOnAction(e -> {
            //Go to the next option
//            System.out.println(DAPIBox.getValue() + " " + Opal780Box.getValue() + " " + Opal480Box.getValue() + " " + Opal690Box.getValue() + " " + FITCBox.getValue() + " " + Cy3Box.getValue() + " " + TexasRedBox.getValue());
//            initialiseChannelLists(DAPIBox.getValue(), Opal780Box.getValue(), Opal480Box.getValue(), Opal690Box.getValue(), FITCBox.getValue(), Cy3Box.getValue(), TexasRedBox.getValue());
//            initialiseChannelOptions();
//            Stage channelSelectionDialog;
//
//            channelSelectionDialog = createChannelSelectionDialog(imageData, "DAPI", getChannels("DAPI").size(), duplicateDialog, proportionArray);
//            channelSelectionDialog.initOwner(duplicateDialog);
//            channelSelectionDialog.initModality(Modality.WINDOW_MODAL);
//
//            unmixDialog.close();
//
//            channelSelectionDialog.showAndWait();

        });

        ColumnConstraints column1 = new ColumnConstraints(134.0, 134.0, 134.0);
        column1.setHalignment(HPos.CENTER);
        ColumnConstraints column2 = new ColumnConstraints(134.0, 134.0, 134.0);
        column2.setHalignment(HPos.CENTER);
        ColumnConstraints column3 = new ColumnConstraints(134.0, 134.0, 134.0);
        column3.setHalignment(HPos.CENTER);

        RowConstraints row1 = new RowConstraints(58.0, 58.0, 58.0);
        RowConstraints row2 = new RowConstraints(58.0, 58.0, 58.0);
        row2.setValignment(VPos.BOTTOM);
        RowConstraints row3 = new RowConstraints(58.0, 58.0, 58.0);
        row3.setValignment(VPos.TOP);
        RowConstraints row4 = new RowConstraints(58.0, 58.0, 58.0);
        row4.setValignment(VPos.BOTTOM);
        RowConstraints row5 = new RowConstraints(58.0, 58.0, 58.0);
        row5.setValignment(VPos.TOP);
        RowConstraints row6 = new RowConstraints(58.0, 58.0, 58.0);
        row6.setValignment(VPos.BOTTOM);
        RowConstraints row7 = new RowConstraints(58.0, 58.0, 58.0);
        row7.setValignment(VPos.TOP);

        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.getRowConstraints().addAll(row1, row2, row3, row4, row5, row6, row7);



        Scene scene = new Scene(overallPane, 402.0, 406.0);
        unmixDialog.setScene(scene);
        unmixDialog.setMinWidth(402.0);
        unmixDialog.setMinHeight(406.0);
        unmixDialog.setMaxWidth(402.0);
        unmixDialog.setMaxHeight(406.0);

        return unmixDialog;
    }
//
//    protected Stage createChannelSelectionDialog(ImageData<BufferedImage> imageData, String filter, int numberOfChannels, Stage duplicateDialog, double[][] proportionArray) throws  NullPointerException {
//
//        Stage channelSelectDialog = new Stage();
//        channelSelectDialog.setTitle(filter);
//
//        Pane overallPane = new Pane();
//
//
//        GridPane grid = new GridPane();
//        overallPane.getChildren().add(grid);
//
//        Label[] labels = new Label[numberOfChannels];
//        ColumnConstraints[] columnConstraints = new ColumnConstraints[numberOfChannels];
//
//        for(int i = 0; i < numberOfChannels; i++) {
//            labels[i] = createChannelSelectionLabel(i);
//            columnConstraints[i] = createColumnConstraintsChannelSelection(numberOfChannels);
//
//            grid.add(labels[i], i, 0);
//            grid.add(createChannelSelectionChoiceBox(i, filter), i, 1);
//            grid.getColumnConstraints().add(columnConstraints[i]);
//        }
//
//        RowConstraints row1 = new RowConstraints(35, 35, 35);
//        row1.setValignment(VPos.CENTER);
//        RowConstraints row2 = new RowConstraints(35, 35, 35);
//        row2.setValignment(VPos.CENTER);
//        RowConstraints row3 = new RowConstraints(35, 35, 35);
//        row3.setValignment(VPos.CENTER);
//        grid.getRowConstraints().addAll(row1, row2, row3);
//
//
//        Button nextButton = new Button();
//        if(filter.equals("TexasRed")) {
//            nextButton.setText("Submit");
//        } else {
//            nextButton.setText("Next");
//        }
//        nextButton.setPrefSize(60.0, 30.0);
//        nextButton.setMinSize(60.0, 30.0);
//        nextButton.setMaxSize(60.0, 30.0);
//        grid.add(nextButton, (numberOfChannels - 1), 2, 1, 2);
//
//
//        nextButton.setOnAction(e -> {
//            //Go to the next option
//            for(int i = 0; i < getChannels(filter).size(); i++) {
//                System.out.println(getChannels(filter).get(i));
//            }
//            Stage newStage = new Stage();
//            if(filter.equals("DAPI")) {
//                newStage = createChannelSelectionDialog(imageData, "Opal780", getChannels("Opal780").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Opal780")) {
//                newStage = createChannelSelectionDialog(imageData, "Opal480", getChannels("Opal480").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Opal480")) {
//                newStage = createChannelSelectionDialog(imageData, "Opal690", getChannels("Opal690").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Opal690")) {
//                newStage = createChannelSelectionDialog(imageData, "FITC", getChannels("FITC").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("FITC")) {
//                newStage = createChannelSelectionDialog(imageData, "Cy3", getChannels("Cy3").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Cy3")) {
//                newStage = createChannelSelectionDialog(imageData, "TexasRed", getChannels("TexasRed").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("TexasRed")) {
//                ImageData newImageData = ManualUnmixing.unmixAll(imageData, proportionArray, DAPIChannels, opal780Channels, opal480Channels, opal690Channels, FITCChannels, cy3Channels, texasRedChannels);
//                viewer.setImageData(newImageData);
//                exportImage(viewer, "D:\\Desktop\\QuPath\\Indirect Panel\\indirect panel data\\unmixed-All_Crossed", dialog);
//                channelSelectDialog.close();
//                duplicateDialog.close();
//            }
//
//            newStage.initOwner(duplicateDialog);
//            newStage.initModality(Modality.WINDOW_MODAL);
//
//            channelSelectDialog.close();
//
//            newStage.showAndWait();
//        });
//
//        Button backButton = new Button("Back");
//        backButton.setPrefSize(60.0, 30.0);
//        backButton.setMinSize(60.0, 30.0);
//        backButton.setMaxSize(60.0, 30.0);
//        grid.add(backButton, 0, 2, 1, 2);
//
//
//        backButton.setOnAction(e -> {
//            //Go to the next option
//            for(int i = 0; i < DAPIChannels.size(); i++) {
//                System.out.println(DAPIChannels.get(i));
//            }
//            Stage newStage = new Stage();
//            if(filter.equals("DAPI")) {
//                newStage = createUnmixingDialog(imageData, duplicateDialog, proportionArray);
//            } else if(filter.equals("Opal780")) {
//                newStage = createChannelSelectionDialog(imageData, "DAPI", getChannels("DAPI").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Opal480")) {
//                newStage = createChannelSelectionDialog(imageData, "Opal780", getChannels("Opal780").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Opal690")) {
//                newStage = createChannelSelectionDialog(imageData, "Opal480", getChannels("Opal480").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("FITC")) {
//                newStage = createChannelSelectionDialog(imageData, "Opal690", getChannels("Opal690").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("Cy3")) {
//                newStage = createChannelSelectionDialog(imageData, "FITC", getChannels("FITC").size(), duplicateDialog, proportionArray);
//            } else if(filter.equals("TexasRed")) {
//                newStage = createChannelSelectionDialog(imageData, "Cy3", getChannels("Cy3").size(), duplicateDialog, proportionArray);
//            }
//
//            newStage.initOwner(duplicateDialog);
//            newStage.initModality(Modality.WINDOW_MODAL);
//
//            channelSelectDialog.close();
//
//            newStage.showAndWait();
//        });
//
//        Scene scene = new Scene(overallPane);
//        channelSelectDialog.setWidth(420.0);
//        channelSelectDialog.setHeight(180.0);
//        channelSelectDialog.setScene(scene);
//        channelSelectDialog.setResizable(false);
//
//        return channelSelectDialog;
//    }
//
//    @Override
//    public void run() {
//        if (dialog == null) {
//            try{
//                dialog = createChannelSelectionChoiceBox();
//            } catch (IOException e) {
//            }
//        } else if(!dialog.isShowing()) {
//            try {
//                dialog = createChannelSelectionChoiceBox();
//            } catch (IOException e) {
//            }
//        }
//        dialog.show();
//    }
//
}
