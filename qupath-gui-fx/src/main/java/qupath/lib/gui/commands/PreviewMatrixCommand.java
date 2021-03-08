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

    //CONSTANT MACROS
    private static final double BUTTON_WIDTH = 42.0;
    private static final double BUTTON_LABEL_HEIGHT = 25.0;
    private static final double SCROLL_BAR_FONT_SIZE = 12.0;
    private static final double TAB_SIZE = 30.0;

    //MAX/PREF

    //OVERALL MACROS MAX/PREF
    private static final double OVERALL_WIDTH = 900.0;
    private static final double OVERALL_HEIGHT = 800.0;

    //THRESHOLD MACROS MAX/PREF
    private static final double THRESHOLD_WIDTH = OVERALL_WIDTH - 20.0;
    private static final double THRESHOLD_HEIGHT = 25.0;

    private static final double THRESHOLD_BUTTONS_WIDTH = 80.0;
    private static final double THRESHOLD_TEXT_FIELD_WIDTH = 40.0;
    private static final double THRESHOLD_LABEL_WIDTH = 620.0;

    private static final double THRESHOLD_FIELD_COLUMN = THRESHOLD_TEXT_FIELD_WIDTH * 2;
    private static final double THRESHOLD_BUTTON_COLUMN = THRESHOLD_BUTTONS_WIDTH + 10.0;

    //MATRIX MACROS MAX/PREF
    private static final double MATRIX_BORDER_WIDTH = OVERALL_WIDTH - 20.0;
    private static final double MATRIX_BORDER_HEIGHT = 384.0;

    private static final double MATRIX_LABELS_VERTICAL_WIDTH = 25.0;
    private static final double MATRIX_LABELS_VERTICAL_HEIGHT = MATRIX_BORDER_HEIGHT - BUTTON_LABEL_HEIGHT - SCROLL_BAR_FONT_SIZE;
    private static final double MATRIX_LABELS_HORIZONTAL_WIDTH = MATRIX_BORDER_WIDTH - SCROLL_BAR_FONT_SIZE;
    private static final double MATRIX_LABELS_HORIZONTAL_HEIGHT = 25.0;
    private static final double MATRIX_SCROLL_HEIGHT = MATRIX_BORDER_HEIGHT - MATRIX_LABELS_HORIZONTAL_HEIGHT;
    private static final double MATRIX_SCROLL_WIDTH = MATRIX_BORDER_WIDTH - MATRIX_LABELS_VERTICAL_WIDTH;

    //IMAGE MACROS MAX/PREF
    private static final double TAB_WIDTH = OVERALL_WIDTH - 20.0;
    private static final double TAB_HEIGHT = 326.0;

    private static final double IMAGE_HBOX_WIDTH = TAB_WIDTH - TAB_SIZE;
    private static final double IMAGE_HBOX_HEIGHT = TAB_HEIGHT;

    private static final double IMAGE_VBOX_WIDTH = IMAGE_HBOX_WIDTH/2 - 10.0;
    private static final double IMAGE_VBOX_HEIGHT = TAB_HEIGHT;

    private static final double IMAGE_LABEL_WIDTH = IMAGE_VBOX_WIDTH;
    private static final double IMAGE_LABEL_HEIGHT = 25.0;

    private static final double IMAGE_WIDTH = IMAGE_VBOX_WIDTH;
    private static final double IMAGE_HEIGHT = IMAGE_VBOX_HEIGHT - IMAGE_LABEL_HEIGHT;

    /**
     * Constructor.
     * @param qupath
     */
    public PreviewMatrixCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
    }

    protected Stage createDialog(float[][] duplicateMatrix, Double thresholdValue, ImageData<BufferedImage> imageData, BufferedImage img, ArrayList<Integer> distinctChannels, Stage duplicateDialog) throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.setTitle("Preview");
        int size = duplicateMatrix.length;

        viewer = qupath.getViewer();

        //larger panes

        BorderPane overallPane = new BorderPane();
        overallPane.setPrefSize(OVERALL_WIDTH, OVERALL_HEIGHT);

        GridPane thresholdPane = new GridPane();
        thresholdPane.setPadding(new Insets(10,10,5,10));
        thresholdPane.setPrefSize(THRESHOLD_WIDTH, THRESHOLD_HEIGHT);
        ColumnConstraints labelColumn = new ColumnConstraints(THRESHOLD_LABEL_WIDTH, THRESHOLD_LABEL_WIDTH, THRESHOLD_LABEL_WIDTH
        );
        ColumnConstraints fieldColumn = new ColumnConstraints(THRESHOLD_FIELD_COLUMN, THRESHOLD_FIELD_COLUMN, THRESHOLD_FIELD_COLUMN);
        ColumnConstraints confirmColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        ColumnConstraints previewColumn = new ColumnConstraints(THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN, THRESHOLD_BUTTON_COLUMN);
        RowConstraints rowConstraints = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);


        //Threshold Part
        Label thresholdLabel = new Label("Threshold value selected: " + String.format("%.2f", thresholdValue));
        thresholdLabel.setPrefHeight(THRESHOLD_HEIGHT);
        thresholdLabel.setMinHeight(THRESHOLD_HEIGHT);
        thresholdLabel.setMaxHeight(THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdLabel, HPos.RIGHT);
        Button thresholdConfirm = new Button("Submit");
        thresholdConfirm.setPrefSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdConfirm.setMinSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdConfirm.setMaxSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
        thresholdConfirm.setOnAction(event -> {
            String filePath = DuplicateMatrixCommand.getFilePath(viewer, thresholdValue);
            viewer.setImageData(ConcatChannelsABI.concatDuplicateChannels(imageData, img, duplicateMatrix, thresholdValue));
            viewer.repaintEntireImage();
            DuplicateMatrixCommand.exportImage(viewer, filePath, dialog);
            duplicateDialog.close();
            dialog.close();
            try {
                QuPathGUI.getInstance().openImage(viewer, filePath, false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Button thresholdPreview = new Button("End Preview");
        thresholdPreview.setPrefSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdPreview.setMinSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        thresholdPreview.setMaxSize(THRESHOLD_BUTTONS_WIDTH, THRESHOLD_HEIGHT);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
        thresholdPreview.setOnAction(event -> {
            duplicateDialog.show();
            dialog.close();
        });
        thresholdPane.add(thresholdLabel, 0, 0);
        thresholdPane.add(thresholdPreview, 2, 0);
        thresholdPane.add(thresholdConfirm, 3, 0);
        thresholdPane.getColumnConstraints().addAll(labelColumn, fieldColumn, previewColumn, confirmColumn);
        thresholdPane.getRowConstraints().add(rowConstraints);
        overallPane.setTop(thresholdPane);



        //preview image section
        HBox imageScrollBox = new HBox();
        imageScrollBox.setPrefSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageScrollBox.setMaxSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageScrollBox.setMinSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        BorderPane.setAlignment(imageScrollBox, Pos.BOTTOM_CENTER);

        VBox image1ScrollVBox = new VBox();
        image1ScrollVBox.setPrefSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image1ScrollVBox.setMaxSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image1ScrollVBox.setMinSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image1ScrollVBox.setPadding(new Insets(0,10,0,10));

        VBox image2ScrollVBox = new VBox();
        image2ScrollVBox.setPrefSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image2ScrollVBox.setMaxSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image2ScrollVBox.setMinSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);

        ScrollPane image1ScrollPane = new ScrollPane();
        image1ScrollPane.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image1ScrollPane.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image1ScrollPane.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image1ScrollPane.setPannable(true);

        ScrollPane image2ScrollPane = new ScrollPane();
        image2ScrollPane.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image2ScrollPane.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image2ScrollPane.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);

        imageScrollBox.getChildren().addAll(image1ScrollVBox, image2ScrollVBox);

        Label image1ScrollLabel = new Label("Image 1");
        image1ScrollLabel.setPrefSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image1ScrollLabel.setMaxSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image1ScrollLabel.setMinSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image1ScrollLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1ScrollLabel, Priority.NEVER);

        Label image2ScrollLabel = new Label("Image 2");
        image2ScrollLabel.setPrefSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image2ScrollLabel.setMaxSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image2ScrollLabel.setMinSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
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
        imageThumbnailBox.setPrefSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageThumbnailBox.setMaxSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        imageThumbnailBox.setMinSize(IMAGE_HBOX_WIDTH, IMAGE_HBOX_HEIGHT);
        BorderPane.setAlignment(imageThumbnailBox, Pos.BOTTOM_CENTER);
        VBox image1ThumbnailVBox = new VBox();
        image1ThumbnailVBox.setPrefSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image1ThumbnailVBox.setMaxSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image1ThumbnailVBox.setMinSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image1ThumbnailVBox.setPadding(new Insets(0,10,0,10));
        VBox image2ThumbnailVBox = new VBox();
        image2ThumbnailVBox.setPrefSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image2ThumbnailVBox.setMaxSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        image2ThumbnailVBox.setMinSize(IMAGE_VBOX_WIDTH, IMAGE_VBOX_HEIGHT);
        Pane image1ThumbnailPane = new Pane();
        image1ThumbnailPane.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image1ThumbnailPane.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image1ThumbnailPane.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        Pane image2ThumbnailPane = new Pane();
        image2ThumbnailPane.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image2ThumbnailPane.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        image2ThumbnailPane.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageThumbnailBox.getChildren().addAll(image1ThumbnailVBox, image2ThumbnailVBox);
        Label image1ThumbnailLabel = new Label("Image 1");
        image1ThumbnailLabel.setPrefSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image1ThumbnailLabel.setMaxSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image1ThumbnailLabel.setMinSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image1ThumbnailLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1ThumbnailLabel, Priority.NEVER);
        Label image2ThumbnailLabel = new Label("Image 2");
        image2ThumbnailLabel.setPrefSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image2ThumbnailLabel.setMaxSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
        image2ThumbnailLabel.setMinSize(IMAGE_LABEL_WIDTH, IMAGE_LABEL_HEIGHT);
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
        imageTabPane.setPrefSize(TAB_WIDTH, TAB_HEIGHT);
        imageTabPane.setMaxSize(TAB_WIDTH, TAB_HEIGHT);
        imageTabPane.setMinSize(TAB_WIDTH, TAB_HEIGHT);
        imageTabPane.setPadding(new Insets(5, 10,10,10));
        overallPane.setBottom(imageTabPane);

        //matrix part
        BorderPane matrixBorder = new BorderPane();
        matrixBorder.setPadding(new Insets(5,10,5,10));

        AnchorPane verticalAnchor = new AnchorPane();
        AnchorPane horizontalAnchor = new AnchorPane();

        ScrollPane horizontalLabelScroll = new ScrollPane();
        horizontalLabelScroll.setPrefSize(MATRIX_LABELS_HORIZONTAL_WIDTH, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        horizontalLabelScroll.setMaxSize(MATRIX_LABELS_HORIZONTAL_WIDTH, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        horizontalLabelScroll.setMinSize(MATRIX_LABELS_HORIZONTAL_WIDTH, MATRIX_LABELS_HORIZONTAL_HEIGHT);
        horizontalLabelScroll.setPannable(false);
        horizontalLabelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontalLabelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        ScrollPane verticalLabelScroll = new ScrollPane();
        verticalLabelScroll.setPrefSize(MATRIX_LABELS_VERTICAL_WIDTH, MATRIX_LABELS_VERTICAL_HEIGHT);
        verticalLabelScroll.setMaxSize(MATRIX_LABELS_VERTICAL_WIDTH, MATRIX_LABELS_VERTICAL_HEIGHT);
        verticalLabelScroll.setMinSize(MATRIX_LABELS_VERTICAL_WIDTH, MATRIX_LABELS_VERTICAL_HEIGHT);
        verticalLabelScroll.setPannable(false);
        verticalLabelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        verticalLabelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        matrixBorder.setPrefSize(MATRIX_BORDER_WIDTH, MATRIX_BORDER_HEIGHT);
        matrixBorder.setMaxSize(MATRIX_BORDER_WIDTH, MATRIX_BORDER_HEIGHT);
        matrixBorder.setMinSize(MATRIX_BORDER_WIDTH, MATRIX_BORDER_HEIGHT);
        GridPane verticalLabelPane = new GridPane();

        GridPane horizontalLabelPane = new GridPane();


        RowConstraints labelRowConstraint = new RowConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        horizontalLabelPane.getRowConstraints().add(labelRowConstraint);
        ColumnConstraints labelColumnConstraint = new ColumnConstraints(BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT, BUTTON_LABEL_HEIGHT);
        verticalLabelPane.getColumnConstraints().add(labelColumnConstraint);
        ScrollPane matrixScrollPane = new ScrollPane();
        matrixScrollPane.setPrefSize(MATRIX_SCROLL_WIDTH, MATRIX_SCROLL_HEIGHT);
        matrixScrollPane.setMaxSize(MATRIX_SCROLL_WIDTH, MATRIX_SCROLL_HEIGHT);
        matrixScrollPane.setMinSize(MATRIX_SCROLL_WIDTH, MATRIX_SCROLL_HEIGHT);

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
            Label tempVerticalLabel = new Label(Integer.toString(distinctChannels.get(i) + 1));
            Label tempHorizontalLabel = new Label(Integer.toString(distinctChannels.get(i) + 1));
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
                //alter for previews
                int tempI = distinctChannels.get(i);
                int tempJ = distinctChannels.get(j);
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
        scrollTab.setTooltip(new Tooltip("View real size image with scroll"));
        thumbnailTab.setTooltip(new Tooltip("View a thumbnail of the real image"));
        thresholdPreview.setTooltip(new Tooltip("Go back to the previous window"));
        thresholdConfirm.setTooltip(new Tooltip("Apply this threshold value to project"));


        Scene scene = new Scene(overallPane, OVERALL_WIDTH, OVERALL_HEIGHT);
        dialog.setScene(scene);
        dialog.setMinWidth(OVERALL_WIDTH);
        dialog.setMinHeight(OVERALL_HEIGHT);
        dialog.setMaxWidth(OVERALL_WIDTH);
        dialog.setMaxHeight(OVERALL_HEIGHT);

        return dialog;
    }
}
