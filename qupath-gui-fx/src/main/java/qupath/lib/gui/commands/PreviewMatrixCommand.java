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

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import qupath.lib.common.ConcatChannelsABI;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Command to show a Duplicate Matrix widget to preview and decide which threshold
 * is the best to properly represent the image.
 *
 * @author Jaedyn Ward
 *
 */
public class PreviewMatrixCommand {

    private QuPathGUI qupath;
    private QuPathViewer viewer;

    private Stage dialog;

    private BufferedImage img;
    private Double confirmDouble = 0.0;

    public ImageData<BufferedImage> imageData;

    //CONSTANT MACROS
    private static final double BUTTON_WIDTH = 42.0;
    private static final double BUTTON_LABEL_HEIGHT = 25.0;
    private static final double SCROLL_BAR_FONT_SIZE = 12.0;
    private static final double TAB_SIZE = 30.0;

    //MAX/PREF

    //OVERALL MACROS MAX/PREF
    private static final double OVERALL_WIDTH_MAX = 900.0;
    private static final double OVERALL_HEIGHT_MAX = 800.0;

    //THRESHOLD MACROS MAX/PREF
    private static final double THRESHOLD_WIDTH_MAX = OVERALL_WIDTH_MAX - 20.0;
    private static final double THRESHOLD_HEIGHT_MAX = 25.0;

    private static final double THRESHOLD_BUTTONS_WIDTH_MAX = 80.0;
    private static final double THRESHOLD_TEXT_FIELD_WIDTH_MAX = 40.0;
    private static final double THRESHOLD_LABEL_WIDTH_MAX = 620.0;

    private static final double THRESHOLD_FIELD_COLUMN_MAX = THRESHOLD_TEXT_FIELD_WIDTH_MAX * 2;
    private static final double THRESHOLD_BUTTON_COLUMN_MAX = THRESHOLD_BUTTONS_WIDTH_MAX + 10.0;

    //MATRIX MACROS MAX/PREF
    private static final double MATRIX_BORDER_WIDTH_MAX = OVERALL_WIDTH_MAX - 20.0;
    private static final double MATRIX_BORDER_HEIGHT_MAX = 384.0;

    private static final double MATRIX_LABELS_VERTICAL_WIDTH_MAX = 25.0;
    private static final double MATRIX_LABELS_VERTICAL_HEIGHT_MAX = MATRIX_BORDER_HEIGHT_MAX - BUTTON_LABEL_HEIGHT - SCROLL_BAR_FONT_SIZE;
    private static final double MATRIX_LABELS_HORIZONTAL_WIDTH_MAX = MATRIX_BORDER_WIDTH_MAX - SCROLL_BAR_FONT_SIZE;
    private static final double MATRIX_LABELS_HORIZONTAL_HEIGHT_MAX = 25.0;
    private static final double MATRIX_SCROLL_HEIGHT_MAX = MATRIX_BORDER_HEIGHT_MAX - MATRIX_LABELS_HORIZONTAL_HEIGHT_MAX;
    private static final double MATRIX_SCROLL_WIDTH_MAX = MATRIX_BORDER_WIDTH_MAX - MATRIX_LABELS_VERTICAL_WIDTH_MAX;

    //IMAGE MACROS MAX/PREF
    private static final double TAB_WIDTH_MAX = OVERALL_WIDTH_MAX - 20.0;
    private static final double TAB_HEIGHT_MAX = 326.0;

    private static final double IMAGE_HBOX_WIDTH_MAX = TAB_WIDTH_MAX - TAB_SIZE;
    private static final double IMAGE_HBOX_HEIGHT_MAX = TAB_HEIGHT_MAX;

    private static final double IMAGE_VBOX_WIDTH_MAX = IMAGE_HBOX_WIDTH_MAX/2 - 10.0;
    private static final double IMAGE_VBOX_HEIGHT_MAX = TAB_HEIGHT_MAX;

    private static final double IMAGE_LABEL_WIDTH_MAX = IMAGE_VBOX_WIDTH_MAX;
    private static final double IMAGE_LABEL_HEIGHT_MAX = 25.0;

    private static final double IMAGE_WIDTH_MAX = IMAGE_VBOX_WIDTH_MAX;
    private static final double IMAGE_HEIGHT_MAX = IMAGE_VBOX_HEIGHT_MAX - IMAGE_LABEL_HEIGHT_MAX;

    //MIN

    //OVERALL MACROS MIN
    private static final double OVERALL_WIDTH_MIN = 500.0;
    private static final double OVERALL_HEIGHT_MIN = 565.0;

    //THRESHOLD MACROS MIN
    private static final double THRESHOLD_WIDTH_MIN = OVERALL_WIDTH_MIN - 20.0;
    private static final double THRESHOLD_HEIGHT_MIN = 25.0;

    private static final double THRESHOLD_BUTTONS_WIDTH_MIN = 80.0;
    private static final double THRESHOLD_TEXT_FIELD_WIDTH_MIN = 40.0;
    private static final double THRESHOLD_LABEL_WIDTH_MIN = 220.0;
    private static final double THRESHOLD_FIELD_COLUMN_MIN = THRESHOLD_TEXT_FIELD_WIDTH_MIN * 2;
    private static final double THRESHOLD_BUTTON_COLUMN_MIN = THRESHOLD_BUTTONS_WIDTH_MIN + 10.0;

    //MATRIX MACROS MIN
    private static final double MATRIX_BORDER_WIDTH_MIN = OVERALL_WIDTH_MIN - 20.0;
    private static final double MATRIX_BORDER_HEIGHT_MIN = 250.0;

    private static final double MATRIX_LABELS_VERTICAL_WIDTH_MIN = 25.0;
    private static final double MATRIX_LABELS_VERTICAL_HEIGHT_MIN = MATRIX_BORDER_HEIGHT_MIN - SCROLL_BAR_FONT_SIZE;
    private static final double MATRIX_LABELS_HORIZONTAL_WIDTH_MIN = MATRIX_BORDER_WIDTH_MIN - BUTTON_WIDTH - SCROLL_BAR_FONT_SIZE;
    private static final double MATRIX_LABELS_HORIZONTAL_HEIGHT_MIN = 25.0;
    private static final double MATRIX_SCROLL_HEIGHT_MIN = MATRIX_BORDER_HEIGHT_MIN - MATRIX_LABELS_HORIZONTAL_HEIGHT_MIN;
    private static final double MATRIX_SCROLL_WIDTH_MIN = MATRIX_BORDER_WIDTH_MIN - MATRIX_LABELS_VERTICAL_WIDTH_MIN;

    //IMAGE MACROS MIN
    private static final double TAB_WIDTH_MIN = OVERALL_WIDTH_MIN - 20.0;
    private static final double TAB_HEIGHT_MIN = 250.0;

    private static final double IMAGE_HBOX_WIDTH_MIN = TAB_WIDTH_MIN - TAB_SIZE;
    private static final double IMAGE_HBOX_HEIGHT_MIN = TAB_HEIGHT_MIN;

    private static final double IMAGE_VBOX_WIDTH_MIN = IMAGE_HBOX_WIDTH_MIN/2 - 10.0;
    private static final double IMAGE_VBOX_HEIGHT_MIN = TAB_HEIGHT_MIN;

    private static final double IMAGE_LABEL_WIDTH_MIN = IMAGE_VBOX_WIDTH_MIN;
    private static final double IMAGE_LABEL_HEIGHT_MIN = 25.0;

    private static final double IMAGE_WIDTH_MIN = IMAGE_VBOX_WIDTH_MIN;
    private static final double IMAGE_HEIGHT_MIN = IMAGE_VBOX_HEIGHT_MIN - IMAGE_LABEL_HEIGHT_MIN;

    private static final String START_THRESHOLD = "0.90";

    String thresholdValue = START_THRESHOLD;


    /**
     * Constructor.
     * @param qupath
     */
    public PreviewMatrixCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
    }

    protected Stage createDialog(float[][] duplicateMatrix) throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.setTitle("Preview");
        int size = duplicateMatrix.length;

        viewer = qupath.getViewer();

        //larger panes

        BorderPane overallPane = new BorderPane();
        overallPane.setPrefSize(OVERALL_WIDTH_MAX, OVERALL_HEIGHT_MAX);

        GridPane thresholdPane = new GridPane();
        thresholdPane.setPadding(new Insets(10,10,5,10));
        thresholdPane.setPrefSize(THRESHOLD_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        ColumnConstraints labelColumn = new ColumnConstraints(THRESHOLD_LABEL_WIDTH_MIN, THRESHOLD_LABEL_WIDTH_MAX, THRESHOLD_LABEL_WIDTH_MAX
        );
        ColumnConstraints fieldColumn = new ColumnConstraints(THRESHOLD_FIELD_COLUMN_MIN, THRESHOLD_FIELD_COLUMN_MAX, THRESHOLD_FIELD_COLUMN_MAX);
        ColumnConstraints confirmColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN_MIN, THRESHOLD_BUTTON_COLUMN_MAX, THRESHOLD_BUTTON_COLUMN_MAX);
        ColumnConstraints previewColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN_MIN, THRESHOLD_BUTTON_COLUMN_MAX, THRESHOLD_BUTTON_COLUMN_MAX);
        RowConstraints rowConstraints = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);


        //Threshold Part
        Label thresholdLabel = new Label("Please enter a threshold value:");
        thresholdLabel.setPrefHeight(THRESHOLD_HEIGHT_MAX);
        thresholdLabel.setMinHeight(THRESHOLD_HEIGHT_MIN);
        thresholdLabel.setMaxHeight(THRESHOLD_HEIGHT_MAX);
        GridPane.setHalignment(thresholdLabel, HPos.RIGHT);
        TextField thresholdTextField = new TextField(START_THRESHOLD);
        thresholdTextField.setPrefSize(THRESHOLD_TEXT_FIELD_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        thresholdTextField.setMinSize(THRESHOLD_TEXT_FIELD_WIDTH_MIN, THRESHOLD_HEIGHT_MIN);
        thresholdTextField.setMaxSize(THRESHOLD_TEXT_FIELD_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        GridPane.setHalignment(thresholdTextField, HPos.CENTER);
        Button thresholdConfirm = new Button("Submit");
        thresholdConfirm.setPrefSize(THRESHOLD_BUTTONS_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        thresholdConfirm.setMinSize(THRESHOLD_BUTTONS_WIDTH_MIN, THRESHOLD_HEIGHT_MIN);
        thresholdConfirm.setMaxSize(THRESHOLD_BUTTONS_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
        thresholdConfirm.setOnAction(event -> {
            thresholdValue = thresholdTextField.getText();
            try{
                System.out.println(thresholdValue);
                confirmDouble = Double.parseDouble(thresholdValue);
                System.out.println(confirmDouble);
            } catch(Exception e) {
                confirmDouble = 1.01;
                System.out.println("Exception: " + e);
            }
            if(confirmDouble >= -1.0 && confirmDouble <= 1.0) {
                viewer.setImageData(ConcatChannelsABI.concatDuplicateChannels(imageData, img, duplicateMatrix, Double.parseDouble(thresholdValue)));
                viewer.repaintEntireImage();
                if(dialog.isShowing()) {
                    dialog.close();
                }
            } else {
                DuplicateMatrixCommand.createInvalidInputStage(dialog).showAndWait();
            }
        });
        Button thresholdPreview = new Button("Preview");
        thresholdPreview.setPrefSize(THRESHOLD_BUTTONS_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        thresholdPreview.setMinSize(THRESHOLD_BUTTONS_WIDTH_MIN, THRESHOLD_HEIGHT_MIN);
        thresholdPreview.setMaxSize(THRESHOLD_BUTTONS_WIDTH_MAX, THRESHOLD_HEIGHT_MAX);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
        thresholdPreview.setOnAction(event -> {
            ArrayList<Integer> distinctPreviewChannels;
            thresholdValue = thresholdTextField.getText();
            try{
                confirmDouble = Double.parseDouble(thresholdValue);
            } catch(Exception e) {
                confirmDouble = 1.01;
                System.out.println("Exception: " + e);
            }
            if(confirmDouble >= -1.0 && confirmDouble <= 1.0) {
                distinctPreviewChannels = ConcatChannelsABI.distinctChannels(duplicateMatrix, confirmDouble);
                float[][] previewMatrix = new float[distinctPreviewChannels.size()][distinctPreviewChannels.size()];
                previewMatrix = DuplicateMatrixCommand.createPreviewMatrix(duplicateMatrix, distinctPreviewChannels);
            } else {
            }
        });
        thresholdPane.add(thresholdLabel, 0, 0);
        thresholdPane.add(thresholdTextField, 1, 0);
        thresholdPane.add(thresholdPreview, 2, 0);
        thresholdPane.add(thresholdConfirm, 3, 0);
        thresholdPane.getColumnConstraints().addAll(labelColumn, fieldColumn, previewColumn, confirmColumn);
        thresholdPane.getRowConstraints().add(rowConstraints);
        overallPane.setTop(thresholdPane);



        //preview image section
        HBox imageScrollBox = new HBox();
        imageScrollBox.setPrefSize(IMAGE_HBOX_WIDTH_MAX, IMAGE_HBOX_HEIGHT_MAX);
        imageScrollBox.setMaxSize(IMAGE_HBOX_WIDTH_MAX, IMAGE_HBOX_HEIGHT_MAX);
        imageScrollBox.setMinSize(IMAGE_HBOX_WIDTH_MIN, IMAGE_HBOX_HEIGHT_MIN);
        BorderPane.setAlignment(imageScrollBox, Pos.BOTTOM_CENTER);

        VBox image1ScrollVBox = new VBox();
        image1ScrollVBox.setPrefSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image1ScrollVBox.setMaxSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image1ScrollVBox.setMinSize(IMAGE_VBOX_WIDTH_MIN, IMAGE_VBOX_HEIGHT_MIN);
        image1ScrollVBox.setPadding(new Insets(0,10,0,0));

        VBox image2ScrollVBox = new VBox();
        image2ScrollVBox.setPrefSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image2ScrollVBox.setMaxSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image2ScrollVBox.setMinSize(IMAGE_VBOX_WIDTH_MIN, IMAGE_VBOX_HEIGHT_MIN);

        ScrollPane image1ScrollPane = new ScrollPane();
        image1ScrollPane.setPrefSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image1ScrollPane.setMaxSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image1ScrollPane.setMinSize(IMAGE_WIDTH_MIN, IMAGE_HEIGHT_MIN);
        image1ScrollPane.setPannable(true);

        ScrollPane image2ScrollPane = new ScrollPane();
        image2ScrollPane.setPrefSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image2ScrollPane.setMaxSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image2ScrollPane.setMinSize(IMAGE_WIDTH_MIN, IMAGE_HEIGHT_MIN);

        imageScrollBox.getChildren().addAll(image1ScrollVBox, image2ScrollVBox);

        Label image1ScrollLabel = new Label("Image 1");
        image1ScrollLabel.setPrefSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image1ScrollLabel.setMaxSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image1ScrollLabel.setMinSize(IMAGE_LABEL_WIDTH_MIN, IMAGE_LABEL_HEIGHT_MIN);
        image1ScrollLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1ScrollLabel, Priority.NEVER);

        Label image2ScrollLabel = new Label("Image 2");
        image2ScrollLabel.setPrefSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image2ScrollLabel.setMaxSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image2ScrollLabel.setMinSize(IMAGE_LABEL_WIDTH_MIN, IMAGE_LABEL_HEIGHT_MIN);
        image2ScrollLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image2ScrollLabel, Priority.NEVER);

        ImageView imageScrollView1 = new ImageView();
        ImageView imageScrollView2 = new ImageView();

        AnchorPane image1Anchor = new AnchorPane();
        image1Anchor.getChildren().add(imageScrollView1);

        AnchorPane image2Anchor = new AnchorPane();
        image2Anchor.getChildren().add(imageScrollView2);

        image1ScrollPane.setContent(image1Anchor);
        image2ScrollPane.setContent(image2Anchor);
        image2ScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        image2ScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        image1ScrollVBox.getChildren().addAll(image1ScrollLabel, image1ScrollPane);
        image2ScrollVBox.getChildren().addAll(image2ScrollLabel, image2ScrollPane);
        Tab scrollTab = new Tab("Scroll", imageScrollBox);

        HBox imageThumbnailBox = new HBox();
        imageThumbnailBox.setPrefSize(IMAGE_HBOX_WIDTH_MAX, IMAGE_HBOX_HEIGHT_MAX);
        imageThumbnailBox.setMaxSize(IMAGE_HBOX_WIDTH_MAX, IMAGE_HBOX_HEIGHT_MAX);
        imageThumbnailBox.setMinSize(IMAGE_HBOX_WIDTH_MIN, IMAGE_HBOX_HEIGHT_MIN);
        BorderPane.setAlignment(imageThumbnailBox, Pos.BOTTOM_CENTER);
        VBox image1ThumbnailVBox = new VBox();
        image1ThumbnailVBox.setPrefSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image1ThumbnailVBox.setMaxSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image1ThumbnailVBox.setMinSize(IMAGE_VBOX_WIDTH_MIN, IMAGE_VBOX_HEIGHT_MIN);
        image1ThumbnailVBox.setPadding(new Insets(0,10,0,0));
        VBox image2ThumbnailVBox = new VBox();
        image2ThumbnailVBox.setPrefSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image2ThumbnailVBox.setMaxSize(IMAGE_VBOX_WIDTH_MAX, IMAGE_VBOX_HEIGHT_MAX);
        image2ThumbnailVBox.setMinSize(IMAGE_VBOX_WIDTH_MIN, IMAGE_VBOX_HEIGHT_MIN);
        Pane image1ThumbnailPane = new Pane();
        image1ThumbnailPane.setPrefSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image1ThumbnailPane.setMaxSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image1ThumbnailPane.setMinSize(IMAGE_WIDTH_MIN, IMAGE_HEIGHT_MIN);
        Pane image2ThumbnailPane = new Pane();
        image2ThumbnailPane.setPrefSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image2ThumbnailPane.setMaxSize(IMAGE_WIDTH_MAX, IMAGE_HEIGHT_MAX);
        image2ThumbnailPane.setMinSize(IMAGE_WIDTH_MIN, IMAGE_HEIGHT_MIN);
        imageThumbnailBox.getChildren().addAll(image1ThumbnailVBox, image2ThumbnailVBox);
        Label image1ThumbnailLabel = new Label("Image 1");
        image1ThumbnailLabel.setPrefSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image1ThumbnailLabel.setMaxSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image1ThumbnailLabel.setMinSize(IMAGE_LABEL_WIDTH_MIN, IMAGE_LABEL_HEIGHT_MIN);
        image1ThumbnailLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1ThumbnailLabel, Priority.NEVER);
        Label image2ThumbnailLabel = new Label("Image 2");
        image2ThumbnailLabel.setPrefSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image2ThumbnailLabel.setMaxSize(IMAGE_LABEL_WIDTH_MAX, IMAGE_LABEL_HEIGHT_MAX);
        image2ThumbnailLabel.setMinSize(IMAGE_LABEL_WIDTH_MIN, IMAGE_LABEL_HEIGHT_MIN);
        image2ThumbnailLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image2ThumbnailLabel, Priority.NEVER);
        ImageView imageThumbnailView1 = new ImageView();
        ImageView imageThumbnailView2 = new ImageView();
        image1ThumbnailPane.getChildren().add(imageThumbnailView1);
        image2ThumbnailPane.getChildren().add(imageThumbnailView2);
        image1ThumbnailVBox.getChildren().addAll(image1ThumbnailLabel, image1ThumbnailPane);
        image2ThumbnailVBox.getChildren().addAll(image2ThumbnailLabel, image2ThumbnailPane);
        Tab thumbnailTab = new Tab("Thumbnail", imageThumbnailBox);


        //tab pane
        TabPane imageTabPane = new TabPane(scrollTab, thumbnailTab);
        imageTabPane.setSide(Side.LEFT);
        imageTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        imageTabPane.setPrefSize(TAB_WIDTH_MAX, TAB_HEIGHT_MAX);
        imageTabPane.setMaxSize(TAB_WIDTH_MAX, TAB_HEIGHT_MAX);
        imageTabPane.setMinSize(TAB_WIDTH_MIN, TAB_HEIGHT_MIN);
        imageTabPane.setPadding(new Insets(5, 10,10,10));
        overallPane.setBottom(imageTabPane);

        //matrix part
        BorderPane matrixBorder = new BorderPane();
        matrixBorder.setPadding(new Insets(5,10,5,10));

        AnchorPane verticalAnchor = new AnchorPane();
        AnchorPane horizontalAnchor = new AnchorPane();

        ScrollPane horizontalLabelScroll = new ScrollPane();
        horizontalLabelScroll.setPrefSize(MATRIX_LABELS_HORIZONTAL_WIDTH_MAX, MATRIX_LABELS_HORIZONTAL_HEIGHT_MAX);
        horizontalLabelScroll.setMaxSize(MATRIX_LABELS_HORIZONTAL_WIDTH_MAX, MATRIX_LABELS_HORIZONTAL_HEIGHT_MAX);
        horizontalLabelScroll.setMinSize(MATRIX_LABELS_HORIZONTAL_WIDTH_MIN, MATRIX_LABELS_HORIZONTAL_HEIGHT_MIN);
        horizontalLabelScroll.setPannable(false);
        horizontalLabelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontalLabelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        ScrollPane verticalLabelScroll = new ScrollPane();
        verticalLabelScroll.setPrefSize(MATRIX_LABELS_VERTICAL_WIDTH_MAX, MATRIX_LABELS_VERTICAL_HEIGHT_MAX);
        verticalLabelScroll.setMaxSize(MATRIX_LABELS_VERTICAL_WIDTH_MAX, MATRIX_LABELS_VERTICAL_HEIGHT_MAX);
        verticalLabelScroll.setMinSize(MATRIX_LABELS_VERTICAL_WIDTH_MIN, MATRIX_LABELS_VERTICAL_HEIGHT_MIN);
        verticalLabelScroll.setPannable(false);
        verticalLabelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        verticalLabelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        matrixBorder.setPrefSize(MATRIX_BORDER_WIDTH_MAX, MATRIX_BORDER_HEIGHT_MAX);
        matrixBorder.setMaxSize(MATRIX_BORDER_WIDTH_MAX, MATRIX_BORDER_HEIGHT_MAX);
        matrixBorder.setMinSize(MATRIX_BORDER_WIDTH_MIN, MATRIX_BORDER_HEIGHT_MIN);
        GridPane verticalLabelPane = new GridPane();

        GridPane horizontalLabelPane = new GridPane();


        RowConstraints labelRowConstraint = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        horizontalLabelPane.getRowConstraints().add(labelRowConstraint);
        ColumnConstraints labelColumnConstraint = new ColumnConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        verticalLabelPane.getColumnConstraints().add(labelColumnConstraint);
        ScrollPane matrixScrollPane = new ScrollPane();
        matrixScrollPane.setPrefSize(MATRIX_SCROLL_WIDTH_MAX, MATRIX_SCROLL_HEIGHT_MAX);
        matrixScrollPane.setMaxSize(MATRIX_SCROLL_WIDTH_MAX, MATRIX_SCROLL_HEIGHT_MAX);
        matrixScrollPane.setMinSize(MATRIX_SCROLL_WIDTH_MIN, MATRIX_SCROLL_HEIGHT_MIN);

        matrixScrollPane.setStyle("-fx-font-size: " + SCROLL_BAR_FONT_SIZE + "px");
        BorderPane.setAlignment(matrixBorder, Pos.TOP_CENTER);
        matrixBorder.setCenter(matrixScrollPane);
        horizontalLabelScroll.setContent(horizontalAnchor);

        horizontalAnchor.getChildren().add(horizontalLabelPane);

        horizontalLabelScroll.setPadding(new Insets(0,0,0,25));

        matrixBorder.setTop(horizontalLabelScroll);
        verticalAnchor.getChildren().add(verticalLabelPane);
        verticalLabelScroll.setContent(verticalAnchor);
        matrixBorder.setLeft(verticalLabelScroll);
        GridPane matrix = new GridPane();
        Tooltip matrixButtonTooltip = new Tooltip("Select which channels to compare images");
        matrixButtonTooltip.setShowDelay(Duration.seconds(1));
        for(int i = 0; i < size; i++) {
            Label tempVerticalLabel = new Label(Integer.toString(i + 1));
            Label tempHorizontalLabel = new Label(Integer.toString(i + 1));
            tempVerticalLabel.setPrefSize(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
            tempVerticalLabel.setMinSize(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
            tempVerticalLabel.setMaxSize(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
            tempVerticalLabel.setAlignment(Pos.CENTER);
            tempHorizontalLabel.setPrefSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
            tempHorizontalLabel.setMaxSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
            tempHorizontalLabel.setMinSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
            tempHorizontalLabel.setAlignment(Pos.CENTER);
            horizontalLabelPane.add(tempHorizontalLabel, i, 0);
            verticalLabelPane.add(tempVerticalLabel, 0, i);
        }
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                String tempString = String.format("%.2f", duplicateMatrix[i][j]);
                //set buttons to be the corresponding matrix
                Button tempButton = new Button(tempString);
                tempButton.setPrefSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
                tempButton.setMaxSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
                tempButton.setMinSize(BUTTON_WIDTH, BUTTON_LABEL_HEIGHT);
                tempButton.setTooltip(matrixButtonTooltip);
                tempButton.setAlignment(Pos.CENTER_RIGHT);
                String tempButtonColour = DuplicateMatrixCommand.getHeatmapColour(duplicateMatrix[i][j]);
                tempButton.setStyle("-fx-border-color: #000000; -fx-border-radius: 0; -fx-background-color: " + tempButtonColour + "; -fx-background-radius: 0");
                int tempI = i;
                int tempJ = j;
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
                tempButton.setOnAction(e -> {
                    //set the correct images depending on button click
                    image1ScrollLabel.setText("Channel " + (tempI + 1));
                    image2ScrollLabel.setText("Channel " + (tempJ + 1));
                    image1ThumbnailLabel.setText("Channel " + (tempI + 1));
                    image2ThumbnailLabel.setText("Channel " + (tempJ + 1));
                    BufferedImage[] bufferedImages1 = ConcatChannelsABI.singleChannelImage(imageData, tempI, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight());
                    BufferedImage[] bufferedImages2 = ConcatChannelsABI.singleChannelImage(imageData, tempJ, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight());
                    imageScrollView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[1], null));
                    imageScrollView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[1], null));
                    imageThumbnailView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[0], null));
                    imageThumbnailView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[0], null));
                });
                matrix.add(tempButton, i, j);
            }
        }

        DuplicateMatrixCommand.bindImages(image1ScrollPane, imageScrollView2);
        DuplicateMatrixCommand.bindMatrixToHeaders(matrixScrollPane, horizontalLabelPane, verticalLabelPane, size);

        matrixScrollPane.setContent(matrix);
        overallPane.setCenter(matrixBorder);

        //tooltips
        thresholdTextField.setTooltip(new Tooltip("Select a value between -1.0 and 1.0"));
        scrollTab.setTooltip(new Tooltip("View real size image with scroll"));
        thumbnailTab.setTooltip(new Tooltip("View a thumbnail of the real image"));
        thresholdPreview.setTooltip(new Tooltip("I dont even know"));
        thresholdConfirm.setTooltip(new Tooltip("Apply this threshold value to project"));


        Scene scene = new Scene(overallPane, OVERALL_WIDTH_MAX, OVERALL_HEIGHT_MAX);
        dialog.setScene(scene);
        dialog.setMinWidth(OVERALL_WIDTH_MIN);
        dialog.setMinHeight(OVERALL_HEIGHT_MIN);
        dialog.setMaxWidth(OVERALL_WIDTH_MAX);
        dialog.setMaxHeight(OVERALL_HEIGHT_MAX);

        DuplicateMatrixCommand.bindResize(scene, imageThumbnailView1, imageThumbnailView2);

        return dialog;
    }

//    @Override
//    public void run() {
//        if (dialog == null) {
//            try{
//                dialog = createDialog();
//            } catch (IOException e) {
//            }
//        } else if(!dialog.isShowing()) {
//            try {
//                dialog = createDialog();
//            } catch (IOException e) {
//            }
//        }
//        dialog.show();
//    }
}
