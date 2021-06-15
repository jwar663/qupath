/*-
 * #%L
 * This file is part of QuPath.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * Copyright (C) 2018 - 2020 QuPath developers, The University of Edinburgh
 * %%
 * QuPath is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * QuPath is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QuPath.  If not, see <https://www.gnu.org/licenses/>.
 * #L%
 */

package qupath.lib.gui.commands;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.lib.common.AutoUnmixing;
import qupath.lib.common.RemoveDuplicate;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.*;
import qupath.lib.images.writers.ImageWriter;
import qupath.lib.images.writers.ImageWriterTools;

/**
 * Command to show a Duplicate Matrix widget to preview and decide which threshold
 * is the best to properly represent the image.
 *
 * @author Jaedyn Ward
 *
 */
public class DuplicateMatrixCommand implements Runnable {

    private QuPathGUI qupath;
    private QuPathViewer viewer;

    private Stage dialog;

    private float[][] duplicateMatrix;
    private BufferedImage img;
    private Double confirmDouble = 0.0;
    double[] thresholdValues;
    private float maxPixelIntensity = 0;

    public ImageData<BufferedImage> imageData;

    //CONSTANT MACROS
    private static final double BUTTON_WIDTH = 42.0;
    private static final double BUTTON_LABEL_HEIGHT = 25.0;
    private static final double SCROLL_BAR_FONT_SIZE = 12.0;
    private static final double TAB_SIZE = 30.0;


    //MAX/PREF

    //OVERALL MACROS MAX/PREF
    private static final double OVERALL_WIDTH = 900.0;
    private static final double OVERALL_HEIGHT = 700.0;

    //THRESHOLD MACROS MAX/PREF
    private static final double THRESHOLD_WIDTH = OVERALL_WIDTH - 20.0;
    private static final double THRESHOLD_HEIGHT = 25.0;

    private static final double THRESHOLD_BUTTONS_WIDTH = 80.0;
    private static final double THRESHOLD_TEXT_FIELD_WIDTH = 40.0;

    private static final double THRESHOLD_FIELD_COLUMN = THRESHOLD_TEXT_FIELD_WIDTH * 2;
    private static final double THRESHOLD_BUTTON_COLUMN = THRESHOLD_BUTTONS_WIDTH + 10.0;

    private static final double THRESHOLD_LABEL_WIDTH = 620.0 - THRESHOLD_BUTTON_COLUMN;

    //MATRIX MACROS MAX/PREF
    private static final double MATRIX_BORDER_WIDTH = OVERALL_WIDTH - 20.0;
    private static final double MATRIX_BORDER_HEIGHT = 329.0;
    private static final double MATRIX_BORDER_WIDTH_PREF = BUTTON_LABEL_HEIGHT + BUTTON_WIDTH;
    private static final double MATRIX_BORDER_HEIGHT_PREF = BUTTON_LABEL_HEIGHT * 2;

    private static final double MATRIX_LABELS_VERTICAL_WIDTH = 25.0;
    private static final double MATRIX_LABELS_VERTICAL_HEIGHT = MATRIX_BORDER_HEIGHT - BUTTON_LABEL_HEIGHT - SCROLL_BAR_FONT_SIZE - 2;
    private static final double MATRIX_LABELS_HORIZONTAL_WIDTH = MATRIX_BORDER_WIDTH - SCROLL_BAR_FONT_SIZE - 2;
    private static final double MATRIX_LABELS_HORIZONTAL_HEIGHT = 25.0;
    private static final double MATRIX_SCROLL_HEIGHT = MATRIX_BORDER_HEIGHT - MATRIX_LABELS_HORIZONTAL_HEIGHT;
    private static final double MATRIX_SCROLL_WIDTH = MATRIX_BORDER_WIDTH - MATRIX_LABELS_VERTICAL_WIDTH;

    //IMAGE MACROS MAX/PREF
    private static final double TAB_WIDTH = OVERALL_WIDTH - 20.0;
    private static final double TAB_HEIGHT = 306.0;

    private static final double IMAGE_HBOX_WIDTH = TAB_WIDTH - TAB_SIZE - 10.0;
    private static final double IMAGE_HBOX_HEIGHT = TAB_HEIGHT;

    private static final double IMAGE_VBOX_WIDTH = (IMAGE_HBOX_WIDTH - 10.0)/2;
    private static final double IMAGE_VBOX_HEIGHT = TAB_HEIGHT;

    private static final double IMAGE_LABEL_WIDTH = IMAGE_VBOX_WIDTH;
    private static final double IMAGE_LABEL_HEIGHT = 25.0;

    private static final double IMAGE_WIDTH = IMAGE_VBOX_WIDTH;
    private static final double IMAGE_HEIGHT = IMAGE_VBOX_HEIGHT - IMAGE_LABEL_HEIGHT - 25;

    private static final String START_THRESHOLD = "0.90";
    private static final String START_CHANNEL = "7";

    String thresholdValue = START_THRESHOLD;

    /**
     * Constructor.
     * @param qupath
     */
    public DuplicateMatrixCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
        this.viewer = qupath.getViewer();
    }

    public static String getFilePath(QuPathViewer viewer, Double thresholdValue) {
        ImageServer<BufferedImage> imageServer = viewer.getServer();
        Collection<URI> uris = imageServer.getURIs();
        //remove "." from the name of the file
        String thresholdString = Double.toString(thresholdValue * 100).substring(0,2);
        String filePath = "";
        URI Uri;
        if(uris.iterator().hasNext()) {
            Uri = uris.iterator().next();
            filePath = GeneralTools.getNameWithoutExtension(Uri.getPath()) + "-distinct-" + thresholdString;
        }
        return filePath;
    }

    public static void exportImage(QuPathViewer viewer, String filePath, Stage dialog) {
        ImageServer<BufferedImage> imageServer = viewer.getServer();
        List<ImageWriter<BufferedImage>> writers = ImageWriterTools.getCompatibleWriters(imageServer, null);
        ImageWriter<BufferedImage> writer = writers.get(0);
        File file = new File(filePath + "." + "tif");
//        if(!file.exists()) {
            try{
                writer.writeImage(imageServer, file.getPath());
            } catch(Exception e) {
                e.printStackTrace();
            }
//        } else {
//            createFileExistsAlert(dialog, writer, imageServer, file).showAndWait();
//        }
    }

    public static Stage createFileExistsAlert(Stage dialog, ImageWriter<BufferedImage> writer, ImageServer<BufferedImage> imageServer, File file) {
        Stage fileExistsAlert = new Stage();
        fileExistsAlert.setTitle("Alert");
        fileExistsAlert.initModality(Modality.WINDOW_MODAL);
        fileExistsAlert.initOwner(dialog);
        Button yesButton = new Button("Yes");
        yesButton.setOnAction(ev -> {
            Boolean fileDelete = file.delete();
            System.out.println("did it delete" + fileDelete);
            try {
                writer.writeImage(imageServer, file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileExistsAlert.close();
        });
        Button noButton = new Button("No");
        yesButton.setOnAction(ev -> {
            fileExistsAlert.close();
        });
        HBox buttonBox = new HBox(yesButton, noButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        VBox fileExistsAlertVbox = new VBox(new Text("This file already exists, do you want to overwrite it?"), buttonBox);
        fileExistsAlertVbox.setSpacing(10.0);
        fileExistsAlertVbox.setAlignment(Pos.CENTER);
        fileExistsAlertVbox.setPadding(new Insets(15));
        fileExistsAlert.setScene(new Scene(fileExistsAlertVbox));
        return fileExistsAlert;
    }

    public static Stage createInvalidInputStage(Stage dialog, boolean toggle, int numberOfChannels) {
        Stage invalidInput = new Stage();
        invalidInput.setTitle("Invalid Input");
        invalidInput.initModality(Modality.WINDOW_MODAL);
        invalidInput.initOwner(dialog);
        Button invalidInputConfirmButton = new Button("OK");
        invalidInputConfirmButton.setOnAction(ev -> {
            invalidInput.close();
        });
        VBox invalidInputVbox;
        if(!toggle) {
            invalidInputVbox = new VBox(new Text("Please enter a value between -1.0 and 1.0"), invalidInputConfirmButton);
        } else {
            invalidInputVbox = new VBox(new Text("Please enter an integer between 1 and " + numberOfChannels), invalidInputConfirmButton);
        }
        invalidInputVbox.setSpacing(10.0);
        invalidInputVbox.setAlignment(Pos.CENTER);
        invalidInputVbox.setPadding(new Insets(15));

        invalidInput.setScene(new Scene(invalidInputVbox));
        return invalidInput;
    }

    public static float[][] createPreviewMatrix(float[][] currentMatrix, ArrayList<Integer> selectedChannels) {
        int selectedChannelsSize = selectedChannels.size();
        float[][] previewMatrix = new float[selectedChannelsSize][selectedChannelsSize];
        for(int i = 0; i < selectedChannelsSize; i++) {
            for(int j = 0; j < selectedChannelsSize; j++) {
                previewMatrix[i][j] = currentMatrix[selectedChannels.get(i)][selectedChannels.get(j)];
            }
        }
        return previewMatrix;
    }

    public static void bindImages(ScrollPane image1Scroll, ImageView image2) {
        image1Scroll.vvalueProperty().addListener((ov, oldValue, newValue) -> {
            AnchorPane.setTopAnchor(image2, ((image2.getImage().getHeight() - image1Scroll.getHeight()) * newValue.doubleValue()) * -1.0);
        });
        image1Scroll.hvalueProperty().addListener((ov, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(image2, ((image2.getImage().getWidth() - image1Scroll.getWidth()) * newValue.doubleValue()) * -1.0);
        });
    }

    public static void counterMouseWheel(ScrollPane labelScroll) {
        labelScroll.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                scrollEvent.consume();
            }
        });
    }

    public static void bindMatrixToHeaders(ScrollPane matrix, GridPane horizontalLabels, GridPane verticalLabels, double size) {
        matrix.vvalueProperty().addListener((ov, oldValue, newValue) -> {
            AnchorPane.setTopAnchor(verticalLabels, ((size * BUTTON_LABEL_HEIGHT - matrix.getHeight() + SCROLL_BAR_FONT_SIZE) * newValue.doubleValue()) * -1.0);
        });
        matrix.hvalueProperty().addListener((ov, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(horizontalLabels, ((size * BUTTON_WIDTH - matrix.getWidth() + SCROLL_BAR_FONT_SIZE) * newValue.doubleValue()) * -1.0);
        });

    }

    public static String getHeatmapColour(double value) {
        double maxColour = 255;
        double minColour = 0;
        double maxValue = 0.5;
        value = value - 0.5;

        String redValue = "";
        String greenValue = "";

        if(value <= 0) {
            redValue = Integer.toHexString((int) maxColour);
            greenValue = Integer.toHexString((int) minColour);
        } else if(value > maxValue / 2) {
            redValue = Integer.toHexString((int) ((1 - 2 * (value - maxValue / 2) / maxValue) * maxColour));
            greenValue = Integer.toHexString((int) (maxColour));
        } else {
            redValue = Integer.toHexString((int) maxColour);
            greenValue = Integer.toHexString((int) ((2 * value / maxValue) * maxColour));
        }
        String blueValue = Integer.toHexString(0);
        if(redValue.length() < 2) {
            redValue = "0" + redValue;
        }
        if(greenValue.length() < 2) {
            greenValue = "0" + greenValue;
        }
        if(blueValue.length() < 2) {
            blueValue = "0" + blueValue;
        }
        return "#" + redValue + greenValue + blueValue;
    }

    protected BorderPane createOverallPane() {
        BorderPane overallPane = new BorderPane();
        overallPane.setPrefSize(OVERALL_WIDTH, OVERALL_HEIGHT);
        return overallPane;
    }

    protected GridPane createThresholdPane() {
        GridPane thresholdPane = new GridPane();
        thresholdPane.setPadding(new Insets(10,10,10,10));
        thresholdPane.setPrefSize(THRESHOLD_WIDTH, THRESHOLD_HEIGHT);
        return thresholdPane;
    }

    protected Label createThresholdLabel(String label) {
        Label thresholdLabel = new Label(label);
        thresholdLabel.setPrefHeight(THRESHOLD_HEIGHT);
        thresholdLabel.setMinHeight(THRESHOLD_HEIGHT);
        thresholdLabel.setMaxHeight(THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdLabel, HPos.RIGHT);
        return thresholdLabel;
    }

    protected TextField createThresholdTextField() {
        TextField thresholdTextField = new TextField(START_THRESHOLD);
        thresholdTextField.setPrefSize(THRESHOLD_TEXT_FIELD_WIDTH, THRESHOLD_HEIGHT);
        thresholdTextField.setMinSize(THRESHOLD_TEXT_FIELD_WIDTH, THRESHOLD_HEIGHT);
        thresholdTextField.setMaxSize(THRESHOLD_TEXT_FIELD_WIDTH, THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdTextField, HPos.CENTER);
        thresholdTextField.setTooltip(new Tooltip("Select a value between -1.0 and 1.0"));

        return thresholdTextField;
    }

    protected Button createThresholdConfirm() {
        Button thresholdConfirm = new Button("Submit");
        thresholdConfirm.setPrefSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdConfirm.setMinSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdConfirm.setMaxSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);

        thresholdConfirm.setTooltip(new Tooltip("Apply this threshold value to project"));
        return thresholdConfirm;
    }

    protected Button createThresholdPreview(String label) {
        Button thresholdPreview = new Button(label);
        thresholdPreview.setPrefSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdPreview.setMinSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdPreview.setMaxSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdPreview, HPos.CENTER);
        if(label.equals("Preview")) {
            thresholdPreview.setTooltip(new Tooltip("Show only the distinct channels with the selected threshold value"));
        } else {
            thresholdPreview.setTooltip(new Tooltip("Go back to the previous window"));
        }
        return thresholdPreview;
    }

    protected HBox createImageHBox() {
        HBox imageHBox = new HBox();
        imageHBox.setPrefSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageHBox.setMaxSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageHBox.setMinSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageHBox.setSpacing(10.0);
        imageHBox.setPadding(new Insets(0,0,0,10));
        BorderPane.setAlignment(imageHBox, Pos.BOTTOM_CENTER);
        return imageHBox;
    }

    protected VBox createImageVBox() {
        VBox imageVBox = new VBox();
        imageVBox.setPrefSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        imageVBox.setMaxSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        imageVBox.setMinSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        imageVBox.setSpacing(5);
        return imageVBox;
    }

    protected ScrollPane createImageScrollPane(Boolean isImage1) {
        ScrollPane imageScrollPane = new ScrollPane();
        imageScrollPane.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageScrollPane.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageScrollPane.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageScrollPane.setVisible(false);
        if(!isImage1) {
            imageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            imageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            imageScrollPane.setPannable(false);
        } else {
            imageScrollPane.setPannable(true);
        }
        return imageScrollPane;
    }

    protected Label createImageLabel(Boolean isImage1) {
        Label imageLabel = new Label();
        if(isImage1) {
            imageLabel.setText("Image 1");
        } else {
            imageLabel.setText("Image 2");
        }
        imageLabel.setPrefSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        imageLabel.setMaxSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        imageLabel.setMinSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        imageLabel.setAlignment(Pos.CENTER);
        return imageLabel;
    }

    protected TabPane createImageTabPane(HBox imageScrollBox, HBox imageThumbnailBox) {
        Tab scrollTab = new Tab("Scroll", imageScrollBox);
        Tab thumbnailTab = new Tab("Thumbnail", imageThumbnailBox);

        scrollTab.setTooltip(new Tooltip("View real size image with scroll"));
        thumbnailTab.setTooltip(new Tooltip("View a thumbnail of the real image"));

        TabPane imageTabPane = new TabPane(scrollTab, thumbnailTab);
        imageTabPane.setSide(Side.LEFT);
        imageTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        imageTabPane.setPrefSize(TAB_WIDTH, TAB_HEIGHT);
        imageTabPane.setMaxSize(TAB_WIDTH, TAB_HEIGHT);
        imageTabPane.setMinSize(TAB_WIDTH, TAB_HEIGHT);
        imageTabPane.setPadding(new Insets(10, 10,10,10));
        return imageTabPane;
    }

    protected ImageView createImageView() {
        ImageView imageView = new ImageView();
        return imageView;
    }

    protected AnchorPane createImageScrollAnchor() {
        AnchorPane imageScrollAnchor = new AnchorPane();
        return imageScrollAnchor;
    }

    protected Pane createImageThumbnailPane() {
        Pane imageThumbnailPane = new Pane();
        imageThumbnailPane.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageThumbnailPane.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageThumbnailPane.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        return imageThumbnailPane;
    }

    protected BorderPane createMatrixBorder(int size) {
        BorderPane matrixBorder = new BorderPane();
        BorderPane.setAlignment(matrixBorder, Pos.CENTER);
        matrixBorder.setPrefSize(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT, size * BUTTON_LABEL_HEIGHT + BUTTON_LABEL_HEIGHT);
        if(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT > MATRIX_BORDER_WIDTH) {
            if(size * BUTTON_LABEL_HEIGHT + BUTTON_LABEL_HEIGHT > MATRIX_BORDER_HEIGHT) {
                matrixBorder.setMaxSize(MATRIX_BORDER_WIDTH, MATRIX_BORDER_HEIGHT);
            } else {
                matrixBorder.setMaxSize(MATRIX_BORDER_WIDTH, size * BUTTON_LABEL_HEIGHT + BUTTON_LABEL_HEIGHT);
            }
        } else {
            if(size * BUTTON_LABEL_HEIGHT + BUTTON_LABEL_HEIGHT > MATRIX_BORDER_HEIGHT) {
                matrixBorder.setMaxSize(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT, MATRIX_BORDER_HEIGHT);
            } else {
                matrixBorder.setMaxSize(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT, size * BUTTON_LABEL_HEIGHT + BUTTON_LABEL_HEIGHT);
            }
        }

        matrixBorder.setMinSize(MATRIX_BORDER_WIDTH_PREF, MATRIX_BORDER_HEIGHT_PREF);
        return matrixBorder;
    }

    protected ScrollPane createHorizontalLabelConfinePane(int size) {
        ScrollPane horizontalLabelConfinePane = new ScrollPane();
        horizontalLabelConfinePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontalLabelConfinePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontalLabelConfinePane.setPrefSize(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        if(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT > MATRIX_LABELS_HORIZONTAL_WIDTH) {
            horizontalLabelConfinePane.setMaxSize(MATRIX_LABELS_HORIZONTAL_WIDTH, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        } else {
            horizontalLabelConfinePane.setMaxSize(size * BUTTON_WIDTH + BUTTON_LABEL_HEIGHT, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        }
        horizontalLabelConfinePane.setMinSize(BUTTON_WIDTH + BUTTON_LABEL_HEIGHT, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        counterMouseWheel(horizontalLabelConfinePane);
        return horizontalLabelConfinePane;
    }

    protected ScrollPane createVerticalLabelConfinePane(int size) {
        ScrollPane verticalLabelConfinePane = new ScrollPane();
        verticalLabelConfinePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        verticalLabelConfinePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        verticalLabelConfinePane.setPrefSize(MATRIX_LABELS_VERTICAL_WIDTH, size * BUTTON_LABEL_HEIGHT);
        if(size * BUTTON_LABEL_HEIGHT > MATRIX_LABELS_VERTICAL_HEIGHT) {
            verticalLabelConfinePane.setMaxSize(MATRIX_LABELS_VERTICAL_WIDTH, MATRIX_LABELS_VERTICAL_HEIGHT);
        } else {
            verticalLabelConfinePane.setMaxSize(MATRIX_LABELS_VERTICAL_WIDTH, size * BUTTON_LABEL_HEIGHT);
        }
        verticalLabelConfinePane.setMinSize(MATRIX_LABELS_VERTICAL_WIDTH, BUTTON_LABEL_HEIGHT);
        counterMouseWheel(verticalLabelConfinePane);
        return verticalLabelConfinePane;
    }

    protected AnchorPane createHorizontalAnchor(GridPane horizontalLabelPane) {
        AnchorPane horizontalAnchor = new AnchorPane(horizontalLabelPane);
        return horizontalAnchor;
    }

    protected AnchorPane createVerticalAnchor(GridPane verticalLabelPane) {
        AnchorPane verticalAnchor = new AnchorPane(verticalLabelPane);
        return verticalAnchor;
    }

    protected ScrollPane createMatrixScrollPane(int size) {
        ScrollPane matrixScrollPane = new ScrollPane();
        matrixScrollPane.setStyle("-fx-font-size: " + SCROLL_BAR_FONT_SIZE + "px");
        matrixScrollPane.setPrefSize(size * BUTTON_WIDTH, size * BUTTON_LABEL_HEIGHT);
        if(size * BUTTON_WIDTH > MATRIX_SCROLL_WIDTH) {
            if(size * BUTTON_LABEL_HEIGHT > MATRIX_SCROLL_HEIGHT) {
                matrixScrollPane.setMaxSize(MATRIX_SCROLL_WIDTH, MATRIX_SCROLL_HEIGHT);
            } else {
                matrixScrollPane.setMaxSize(MATRIX_SCROLL_WIDTH, size * BUTTON_LABEL_HEIGHT);
                matrixScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        } else {
            if(size * BUTTON_LABEL_HEIGHT > MATRIX_SCROLL_HEIGHT) {
                matrixScrollPane.setMaxSize(size * BUTTON_WIDTH, MATRIX_SCROLL_HEIGHT);
                matrixScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            } else {
                matrixScrollPane.setMaxSize(size * BUTTON_WIDTH, size * BUTTON_LABEL_HEIGHT);
                matrixScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                matrixScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        }
        matrixScrollPane.setMinSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
        return matrixScrollPane;
    }

    protected GridPane createMatrix() {
        GridPane matrix = new GridPane();
        return matrix;
    }

    protected Label createMatrixLabel(String labelName, Boolean isSquare) {
        Double width = BUTTON_WIDTH;
        Double height = BUTTON_LABEL_HEIGHT;
        Label label = new Label(labelName);
        if(isSquare) {
            width = BUTTON_LABEL_HEIGHT;
        }
        label.setPrefSize(width, height);
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    protected Button createMatrixButton(float[][] duplicateMatrix, int i, int j) {
        String tempString = String.format("%.2f", duplicateMatrix[i][j]);
        //set buttons to be the corresponding matrix
        Button tempButton = new Button(tempString);
        tempButton.setPrefSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
        tempButton.setMaxSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
        tempButton.setMinSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
        tempButton.setTooltip(new Tooltip("Select which channels to compare images"));
        tempButton.setAlignment(Pos.CENTER_RIGHT);
        String tempButtonColour = getHeatmapColour(duplicateMatrix[i][j]);
        tempButton.setStyle("-fx-border-color: #000000; -fx-border-radius: 0; -fx-background-color: " + tempButtonColour + "; -fx-background-radius: 0");
        tempButton.setOnMouseEntered(e -> {
            tempButton.setStyle("-fx-border-color: #000000; -fx-border-radius: 0; -fx-background-color: #C4C4C4; -fx-background-radius: 0");
        });
        tempButton.setOnMouseExited(e -> {
            tempButton.setStyle("-fx-border-color: #000000; -fx-border-radius: 0; -fx-background-color: " + tempButtonColour + "; -fx-background-radius: 0");
        });
        tempButton.setOnMousePressed(e -> {
            tempButton.setStyle("-fx-border-color: #0DD5FC; -fx-border-radius: 0; -fx-background-color: " + tempButtonColour + "; -fx-background-radius: 0");
        });
        tempButton.setOnMouseReleased(e -> {
            tempButton.setStyle("-fx-border-color: #000000; -fx-border-radius: 0; -fx-background-color: " + tempButtonColour + "; -fx-background-radius: 0");
        });
        return tempButton;
    }

    protected ToggleButton createToggleButton() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setPrefSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        toggleButton.setMinSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        toggleButton.setMaxSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        toggleButton.setText("Threshold");
        GridPane.setHalignment(toggleButton, HPos.CENTER);
        toggleButton.setSelected(false);
        return toggleButton;
    }

    public static double[][] readCSV(String file, double[][] array) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] lineArray;
            for(int i = 0; i < array.length; i++) {
                line = br.readLine();
                lineArray = line.split(",");
                for(int j = 0; j < array[0].length; j++) {
                    array[i][j] = Double.parseDouble(lineArray[j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Dialogs.showErrorMessage("Invalid File", "Please select a '.csv' file that has the correct number of channels, stains, and numerical values");
        }
        return array;
    }



    protected Stage createDialog() throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Duplicate Matrix");

        imageData = qupath.getImageData();
        if(imageData == null) {
            Stage error = new Stage();
            error.setTitle("Error");
            error.initModality(Modality.WINDOW_MODAL);
            Button confirmButton = new Button("OK");
            confirmButton.setOnAction(e -> {
                error.close();
            });
            VBox vbox = new VBox(new Text("Please open an image before selecting this feature"), confirmButton);
            vbox.setSpacing(10.0);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15));

            error.setScene(new Scene(vbox));
            return error;
        }
        int size = imageData.getServer().nChannels();
        duplicateMatrix = new float[size][size];
        img = RemoveDuplicate.convertImageDataToImage(imageData);
        duplicateMatrix = RemoveDuplicate.createConcatMatrix(img);
        thresholdValues = new double[size];
        thresholdValues = RemoveDuplicate.getAllThresholdValues(duplicateMatrix);
        maxPixelIntensity = RemoveDuplicate.findMaximumPixelIntensity(img);

        //larger panes

        BorderPane overallPane = createOverallPane();


        GridPane thresholdPane = createThresholdPane();
        ColumnConstraints compareColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        ColumnConstraints labelColumn = new ColumnConstraints(THRESHOLD_LABEL_WIDTH - THRESHOLD_BUTTON_COLUMN, THRESHOLD_LABEL_WIDTH - THRESHOLD_BUTTON_COLUMN, THRESHOLD_LABEL_WIDTH - THRESHOLD_BUTTON_COLUMN);
        ColumnConstraints fieldColumn = new ColumnConstraints(THRESHOLD_FIELD_COLUMN, THRESHOLD_FIELD_COLUMN, THRESHOLD_FIELD_COLUMN);
        ColumnConstraints confirmColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        ColumnConstraints previewColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        ColumnConstraints toggleColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        RowConstraints rowConstraints = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);

        Button splitButton = new Button("Split");
        splitButton.setOnAction(e -> {
            ImageData originalImageData = imageData;
            viewer.setImageData(RemoveDuplicate.createSingleChannelImageData(originalImageData, 0));
            String filePath0 = "D:\\Desktop\\QuPath\\channel0";
            exportImage(viewer, filePath0, dialog);
            viewer.setImageData(RemoveDuplicate.createSingleChannelImageData(originalImageData, 1));
            String filePath1 = "D:\\Desktop\\QuPath\\channel1";
            exportImage(viewer, filePath1, dialog);
        });

        //Threshold Part
        Label thresholdLabel = createThresholdLabel("Please enter a threshold value: ");
        TextField thresholdTextField = createThresholdTextField();
        Button thresholdConfirm = createThresholdConfirm();
        ToggleButton thresholdToggle = createToggleButton();
        thresholdConfirm.setOnAction(event -> {
            thresholdValue = thresholdTextField.getText();
            try {
                if(!thresholdToggle.isSelected()) {
                    confirmDouble = Double.parseDouble(thresholdValue);
                } else {
                    confirmDouble = thresholdValues[Integer.parseInt(thresholdValue) - 1];
                }
            } catch(Exception e) {
                confirmDouble = 1.01;
                System.out.println("Exception: " + e);
            }
            if(confirmDouble >= -1.0 && confirmDouble <= 1.0) {
                String filePath = getFilePath(viewer, confirmDouble);
                viewer.setImageData(RemoveDuplicate.concatDuplicateChannels(imageData, img, duplicateMatrix, confirmDouble));
                exportImage(viewer, filePath, dialog);
                if(dialog.isShowing()) {
                    dialog.close();
                }
                try {
                    qupath.openImage(viewer, filePath + ".tif", false, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                createInvalidInputStage(dialog, thresholdToggle.isSelected(), size).showAndWait();
            }
        });
        Button thresholdPreview = createThresholdPreview("Preview");
        thresholdPreview.setOnAction(event -> {
            Stage previewDialog;
            ArrayList<Integer> distinctPreviewChannels;
            thresholdValue = thresholdTextField.getText();
            try{
                if(!thresholdToggle.isSelected()) {
                    confirmDouble = Double.parseDouble(thresholdValue);
                } else {
                    confirmDouble = thresholdValues[Integer.parseInt(thresholdValue) - 1];
                }
            } catch(Exception e) {
                confirmDouble = 1.01;
                System.out.println("Exception: " + e);
            }
            if(confirmDouble >= -1.0 && confirmDouble <= 1.0) {
                distinctPreviewChannels = RemoveDuplicate.distinctChannels(duplicateMatrix, confirmDouble);
                float[][] previewMatrix = createPreviewMatrix(duplicateMatrix, distinctPreviewChannels);
                try {
                   previewDialog = createPreviewDialog(previewMatrix, confirmDouble, imageData, img, distinctPreviewChannels, dialog);
                   previewDialog.initOwner(dialog);
                   previewDialog.initModality(Modality.WINDOW_MODAL);
                   previewDialog.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                createInvalidInputStage(dialog, thresholdToggle.isSelected(), size).showAndWait();
            }
        });

        thresholdToggle.setOnAction(e -> {
            if(thresholdToggle.getText().equals("Channels")) {
                thresholdToggle.setText("Threshold");
                thresholdLabel.setText("Please enter a threshold value: ");
                thresholdTextField.setText(START_THRESHOLD);
            } else {
                thresholdToggle.setText("Channels");
                thresholdLabel.setText("Please enter the required number of channels: ");
                thresholdTextField.setText(START_CHANNEL);
            }
        });

        thresholdPane.add(splitButton, 0, 0);
        thresholdPane.add(thresholdLabel, 1, 0);
        thresholdPane.add(thresholdTextField, 2, 0);
        thresholdPane.add(thresholdPreview, 3, 0);
        thresholdPane.add(thresholdConfirm, 4, 0);
        thresholdPane.add(thresholdToggle, 5, 0);
        thresholdPane.getColumnConstraints().addAll(compareColumn, labelColumn, fieldColumn, previewColumn, confirmColumn, toggleColumn);
        thresholdPane.getRowConstraints().add(rowConstraints);
        overallPane.setTop(thresholdPane);



        //preview image section
        HBox imageScrollBox = createImageHBox();

        VBox image1ScrollVBox = createImageVBox();

        VBox image2ScrollVBox = createImageVBox();

        ScrollPane image1ScrollPane = createImageScrollPane(true);

        ScrollPane image2ScrollPane = createImageScrollPane(false);

        imageScrollBox.getChildren().addAll(image1ScrollVBox, image2ScrollVBox);

        Label image1ScrollLabel = createImageLabel(true);

        Label image2ScrollLabel = createImageLabel(false);

        ImageView imageScrollView1 = createImageView();
        ImageView imageScrollView2 = createImageView();

        AnchorPane image1Anchor = createImageScrollAnchor();
        image1Anchor.getChildren().add(imageScrollView1);

        AnchorPane image2Anchor = createImageScrollAnchor();
        image2Anchor.getChildren().add(imageScrollView2);

        image1ScrollPane.setContent(image1Anchor);
        image2ScrollPane.setContent(image2Anchor);
        image1ScrollVBox.getChildren().addAll(image1ScrollLabel, image1ScrollPane);
        image2ScrollVBox.getChildren().addAll(image2ScrollLabel, image2ScrollPane);

        HBox imageThumbnailBox = createImageHBox();
        VBox image1ThumbnailVBox = createImageVBox();
        VBox image2ThumbnailVBox = createImageVBox();

        Pane image1ThumbnailPane = createImageThumbnailPane();
        Pane image2ThumbnailPane = createImageThumbnailPane();
        imageThumbnailBox.getChildren().addAll(image1ThumbnailVBox, image2ThumbnailVBox);
        Label image1ThumbnailLabel = createImageLabel(true);
        Label image2ThumbnailLabel = createImageLabel(false);
        ImageView imageThumbnailView1 = createImageView();
        ImageView imageThumbnailView2 = createImageView();
        image1ThumbnailPane.getChildren().add(imageThumbnailView1);
        image2ThumbnailPane.getChildren().add(imageThumbnailView2);
        image1ThumbnailVBox.getChildren().addAll(image1ThumbnailLabel, image1ThumbnailPane);
        image2ThumbnailVBox.getChildren().addAll(image2ThumbnailLabel, image2ThumbnailPane);


        //tab pane
        TabPane imageTabPane = createImageTabPane(imageScrollBox, imageThumbnailBox);
        overallPane.setBottom(imageTabPane);

        //matrix part



        GridPane verticalLabelPane = new GridPane();

        GridPane horizontalLabelPane = new GridPane();


        RowConstraints labelRowConstraint = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        horizontalLabelPane.getRowConstraints().add(labelRowConstraint);
        ColumnConstraints labelColumnConstraint = new ColumnConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        verticalLabelPane.getColumnConstraints().add(labelColumnConstraint);

        GridPane matrix = createMatrix();

        Label placeholderLabel = createMatrixLabel("", true);
        horizontalLabelPane.add(placeholderLabel, 0, 0);
        for(int i = 0; i < size; i++) {
            Label tempVerticalLabel = createMatrixLabel(Integer.toString(i + 1), true);
            Label tempHorizontalLabel = createMatrixLabel(Integer.toString(i + 1), false);

            horizontalLabelPane.add(tempHorizontalLabel, i + 1, 0);
            verticalLabelPane.add(tempVerticalLabel, 0, i);
        }
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                Button tempButton = createMatrixButton(duplicateMatrix, i, j);
                int tempI = i;
                int tempJ = j;
                tempButton.setOnAction(e -> {
                    //set the correct images depending on button click
                    image1ScrollLabel.setText("Channel " + (tempI + 1));
                    image2ScrollLabel.setText("Channel " + (tempJ + 1));
                    image1ThumbnailLabel.setText("Channel " + (tempI + 1));
                    image2ThumbnailLabel.setText("Channel " + (tempJ + 1));
                    BufferedImage[] bufferedImages1 = RemoveDuplicate.singleChannelImage(imageData, tempI, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight(), maxPixelIntensity);
                    BufferedImage[] bufferedImages2 = RemoveDuplicate.singleChannelImage(imageData, tempJ, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight(), maxPixelIntensity);
                    imageScrollView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[1], null));
                    imageScrollView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[1], null));
                    imageThumbnailView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[0], null));
                    imageThumbnailView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[0], null));
                    image1ScrollPane.setVisible(true);
                    image2ScrollPane.setVisible(true);
                });
                matrix.add(tempButton, i, j);
            }
        }


        ScrollPane matrixScrollPane = createMatrixScrollPane(size);

        AnchorPane verticalAnchor = createVerticalAnchor(verticalLabelPane);
        AnchorPane horizontalAnchor = createHorizontalAnchor(horizontalLabelPane);

        ScrollPane horizontalLabelScroll = createHorizontalLabelConfinePane(size);
        horizontalLabelScroll.setContent(horizontalAnchor);

        ScrollPane verticalLabelScroll = createVerticalLabelConfinePane(size);
        verticalLabelScroll.setContent(verticalAnchor);

        BorderPane matrixBorder = createMatrixBorder(size);
        matrixBorder.setTop(horizontalLabelScroll);
        matrixBorder.setCenter(matrixScrollPane);
        matrixBorder.setLeft(verticalLabelScroll);

        bindImages(image1ScrollPane, imageScrollView2);
        bindImages(image2ScrollPane, imageScrollView1);
        bindMatrixToHeaders(matrixScrollPane, horizontalLabelPane, verticalLabelPane, size);

        matrixScrollPane.setContent(matrix);
        overallPane.setCenter(matrixBorder);


        Scene scene = new Scene(overallPane, OVERALL_WIDTH, OVERALL_HEIGHT);
        dialog.setScene(scene);
        dialog.setMinWidth(OVERALL_WIDTH);
        dialog.setMinHeight(OVERALL_HEIGHT);
        dialog.setMaxWidth(OVERALL_WIDTH);
        dialog.setMaxHeight(OVERALL_HEIGHT);

        return dialog;
    }

    protected Stage createPreviewDialog(float[][] duplicateMatrix, Double thresholdValue, ImageData<BufferedImage> imageData, BufferedImage img, ArrayList<Integer> distinctChannels, Stage duplicateDialog) throws IOException, NullPointerException {

        Stage previewDialog = new Stage();
        previewDialog.setTitle("Preview");
        int size = duplicateMatrix.length;

        //larger panes

        BorderPane overallPane = createOverallPane();

        GridPane thresholdPane = createThresholdPane();
        ColumnConstraints labelColumn = new ColumnConstraints(THRESHOLD_LABEL_WIDTH, THRESHOLD_LABEL_WIDTH, THRESHOLD_LABEL_WIDTH
        );
        ColumnConstraints fieldColumn = new ColumnConstraints(THRESHOLD_FIELD_COLUMN, THRESHOLD_FIELD_COLUMN, THRESHOLD_FIELD_COLUMN);
        ColumnConstraints confirmColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        ColumnConstraints previewColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        RowConstraints rowConstraints = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);


        //Threshold Part
        Label thresholdLabel = createThresholdLabel("Threshold value selected: " + String.format("%.2f", thresholdValue));
//        Button thresholdConfirm = new Button("Submit");
//        thresholdConfirm.setPrefSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
//        thresholdConfirm.setMinSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
//        thresholdConfirm.setMaxSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
//        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
//        thresholdConfirm.setOnAction(event -> {
//            String filePath = getFilePath(viewer, thresholdValue);
//            viewer.setImageData(ConcatChannelsABI.concatDuplicateChannels(imageData, img, duplicateMatrix, thresholdValue));
//            viewer.repaintEntireImage();
//            exportImage(viewer, filePath, previewDialog);
//            try {
//                qupath.openImage(viewer, filePath  + ".tif", false, false);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            duplicateDialog.close();
//            previewDialog.close();
//        });
        Button thresholdPreview = createThresholdPreview("End Preview");
        thresholdPreview.setOnAction(event -> {
            previewDialog.close();
        });
        thresholdPane.add(thresholdLabel, 0, 0);
        thresholdPane.add(thresholdPreview, 2, 0);
//        thresholdPane.add(thresholdConfirm, 3, 0);
        thresholdPane.getColumnConstraints().addAll(labelColumn, fieldColumn, previewColumn, confirmColumn);
        thresholdPane.getRowConstraints().add(rowConstraints);
        overallPane.setTop(thresholdPane);



        //preview image section
        HBox imageScrollBox = createImageHBox();

        VBox image1ScrollVBox = createImageVBox();

        VBox image2ScrollVBox = createImageVBox();

        ScrollPane image1ScrollPane = createImageScrollPane(true);

        ScrollPane image2ScrollPane = createImageScrollPane(false);

        imageScrollBox.getChildren().addAll(image1ScrollVBox, image2ScrollVBox);

        Label image1ScrollLabel = createImageLabel(true);

        Label image2ScrollLabel = createImageLabel(false);

        ImageView imageScrollView1 = createImageView();
        ImageView imageScrollView2 = createImageView();

        AnchorPane image1Anchor = createImageScrollAnchor();
        image1Anchor.getChildren().add(imageScrollView1);

        AnchorPane image2Anchor = createImageScrollAnchor();
        image2Anchor.getChildren().add(imageScrollView2);

        image1ScrollPane.setContent(image1Anchor);
        image2ScrollPane.setContent(image2Anchor);
        image2ScrollPane.setPannable(false);
        image1ScrollVBox.getChildren().addAll(image1ScrollLabel, image1ScrollPane);
        image2ScrollVBox.getChildren().addAll(image2ScrollLabel, image2ScrollPane);

        HBox imageThumbnailBox = createImageHBox();
        VBox image1ThumbnailVBox = createImageVBox();
        VBox image2ThumbnailVBox = createImageVBox();

        Pane image1ThumbnailPane = createImageThumbnailPane();
        Pane image2ThumbnailPane = createImageThumbnailPane();
        imageThumbnailBox.getChildren().addAll(image1ThumbnailVBox, image2ThumbnailVBox);
        Label image1ThumbnailLabel = createImageLabel(true);
        Label image2ThumbnailLabel = createImageLabel(false);
        ImageView imageThumbnailView1 = createImageView();
        ImageView imageThumbnailView2 = createImageView();
        image1ThumbnailPane.getChildren().add(imageThumbnailView1);
        image2ThumbnailPane.getChildren().add(imageThumbnailView2);
        image1ThumbnailVBox.getChildren().addAll(image1ThumbnailLabel, image1ThumbnailPane);
        image2ThumbnailVBox.getChildren().addAll(image2ThumbnailLabel, image2ThumbnailPane);


        //tab pane
        TabPane imageTabPane = createImageTabPane(imageScrollBox, imageThumbnailBox);
        overallPane.setBottom(imageTabPane);

        //matrix part

        GridPane verticalLabelPane = new GridPane();

        GridPane horizontalLabelPane = new GridPane();


        RowConstraints labelRowConstraint = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        horizontalLabelPane.getRowConstraints().add(labelRowConstraint);
        ColumnConstraints labelColumnConstraint = new ColumnConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        verticalLabelPane.getColumnConstraints().add(labelColumnConstraint);

        GridPane matrix = createMatrix();
        Tooltip matrixButtonTooltip = new Tooltip("Select which channels to compare images");
        Label placeholderLabel = createMatrixLabel("", true);
        horizontalLabelPane.add(placeholderLabel, 0, 0);
        for(int i = 0; i < size; i++) {
            Label tempVerticalLabel = createMatrixLabel(Integer.toString(distinctChannels.get(i) + 1), true);
            Label tempHorizontalLabel = createMatrixLabel(Integer.toString(distinctChannels.get(i) + 1), false);

            horizontalLabelPane.add(tempHorizontalLabel, i + 1, 0);
            verticalLabelPane.add(tempVerticalLabel, 0, i);
        }
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                Button tempButton = createMatrixButton(duplicateMatrix, i, j);

                int tempI = distinctChannels.get(i);
                int tempJ = distinctChannels.get(j);
                tempButton.setOnAction(e -> {
                    //set the correct images depending on button click
                    image1ScrollLabel.setText("Channel " + (tempI + 1));
                    image2ScrollLabel.setText("Channel " + (tempJ + 1));
                    image1ThumbnailLabel.setText("Channel " + (tempI + 1));
                    image2ThumbnailLabel.setText("Channel " + (tempJ + 1));
                    BufferedImage[] bufferedImages1 = RemoveDuplicate.singleChannelImage(imageData, tempI, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight(), maxPixelIntensity);
                    BufferedImage[] bufferedImages2 = RemoveDuplicate.singleChannelImage(imageData, tempJ, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight(), maxPixelIntensity);
                    imageScrollView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[1], null));
                    imageScrollView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[1], null));
                    imageThumbnailView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[0], null));
                    imageThumbnailView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[0], null));
                    image1ScrollPane.setVisible(true);
                    image2ScrollPane.setVisible(true);
                });
                matrix.add(tempButton, i, j);
            }
        }


        ScrollPane matrixScrollPane = createMatrixScrollPane(size);

        AnchorPane verticalAnchor = createVerticalAnchor(verticalLabelPane);
        AnchorPane horizontalAnchor = createHorizontalAnchor(horizontalLabelPane);

        ScrollPane horizontalLabelScroll = createHorizontalLabelConfinePane(size);
        horizontalLabelScroll.setContent(horizontalAnchor);

        ScrollPane verticalLabelScroll = createVerticalLabelConfinePane(size);
        verticalLabelScroll.setContent(verticalAnchor);

        BorderPane matrixBorder = createMatrixBorder(size);
        matrixBorder.setTop(horizontalLabelScroll);
        matrixBorder.setCenter(matrixScrollPane);
        matrixBorder.setLeft(verticalLabelScroll);

        bindImages(image1ScrollPane, imageScrollView2);
        bindImages(image2ScrollPane, imageScrollView1);
        bindMatrixToHeaders(matrixScrollPane, horizontalLabelPane, verticalLabelPane, size);

        matrixScrollPane.setContent(matrix);
        overallPane.setCenter(matrixBorder);

        Scene scene = new Scene(overallPane, OVERALL_WIDTH, OVERALL_HEIGHT);
        previewDialog.setScene(scene);
        previewDialog.setMinWidth(OVERALL_WIDTH);
        previewDialog.setMinHeight(OVERALL_HEIGHT);
        previewDialog.setMaxWidth(OVERALL_WIDTH);
        previewDialog.setMaxHeight(OVERALL_HEIGHT);

        return previewDialog;
    }

    @Override
    public void run() {
        if (dialog == null) {
            try{
                dialog = createDialog();
            } catch (IOException e) {
            }
        } else if(!dialog.isShowing()) {
            try {
                dialog = createDialog();
            } catch (IOException e) {
            }
        }
        dialog.show();
    }
}
