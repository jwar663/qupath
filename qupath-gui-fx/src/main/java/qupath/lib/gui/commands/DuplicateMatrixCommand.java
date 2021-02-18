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
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.lib.common.ConcatChannelsABI;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;

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

    private int size;
    private float[][] duplicateMatrix;
    private BufferedImage img;


    public ImageData<BufferedImage> imageData;

    /**
     * Constructor.
     * @param qupath
     */
    public DuplicateMatrixCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
    }

    protected Stage createDialog() throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Duplicate Matrix");

        viewer = qupath.getViewer();
        imageData = qupath.getImageData();
        if(imageData == null) {
            Stage error = new Stage();
            error.initModality(Modality.WINDOW_MODAL);
            Button confirmButton = new Button("OK");
            confirmButton.setOnAction(e -> {
                error.close();
            });
            VBox vbox = new VBox(new Text("Please open an image before selecting this feature"), confirmButton);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15));

            error.setScene(new Scene(vbox));
            return error;
        }
        size = imageData.getServer().nChannels();
        duplicateMatrix = new float[size][size];
        img = ConcatChannelsABI.convertImageDataToImage(imageData);
        duplicateMatrix = ConcatChannelsABI.createConcatMatrix(img);

        //to visualise and allow for dimensions
        //Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,CornerRadii.EMPTY, BorderWidths.DEFAULT));

        //larger panes
        Pane pane = new Pane();
        pane.setPrefSize(900.0, 805.0);

        BorderPane overallPane = new BorderPane();
        overallPane.setPrefSize(880.0, 780.0);
        overallPane.setLayoutX(10.0);
        overallPane.setLayoutY(10.0);
        pane.getChildren().add(overallPane);

        GridPane thresholdPane = new GridPane();
        thresholdPane.setPrefSize(880.0, 25.0);
        ColumnConstraints labelColumn = new ColumnConstraints(620.0, 620.0, 620.0);
        ColumnConstraints fieldColumn = new ColumnConstraints(80.0, 80.0, 80.0);
        ColumnConstraints confirmColumn = new ColumnConstraints(90.0, 90.0, 90.0);
        ColumnConstraints previewColumn = new ColumnConstraints(90.0, 90.0, 90.0);
        RowConstraints rowConstraints = new RowConstraints(25.0, 25.0, 25.0);

        //Threshold Part
        Label thresholdLabel = new Label("Please enter a threshold value:");
        thresholdLabel.setPrefHeight(25.0);
        thresholdLabel.setMinHeight(25.0);
        thresholdLabel.setMaxHeight(25.0);
        GridPane.setHalignment(thresholdLabel, HPos.RIGHT);
        TextField thresholdTextField = new TextField("0.90");
        thresholdTextField.setPrefSize(40.0, 25.0);
        thresholdTextField.setMinSize(40.0, 25.0);
        thresholdTextField.setMaxSize(40.0, 25.0);
        GridPane.setHalignment(thresholdTextField, HPos.CENTER);
        Button thresholdConfirm = new Button("Submit");
        thresholdConfirm.setPrefSize(80.0, 25.0);
        thresholdConfirm.setMinSize(80.0, 25.0);
        thresholdConfirm.setMaxSize(80.0, 25.0);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
        thresholdConfirm.setOnAction(event -> {
            String thresholdValue = thresholdTextField.getText();
            try{
                if(Double.parseDouble(thresholdValue) >= -1 && Double.parseDouble(thresholdValue) <= 1) {
                    viewer.setImageData(ConcatChannelsABI.concatDuplicateChannels(imageData, img, duplicateMatrix, Double.parseDouble(thresholdValue)));
                    viewer.repaintEntireImage();
                    if(dialog.isShowing())
                        dialog.close();
                }
            } catch(Exception e) {
                System.out.println("Exception: " + e);
            }
        });
        Button thresholdPreview = new Button("Preview");
        thresholdPreview.setPrefSize(80.0, 25.0);
        thresholdPreview.setMinSize(80.0, 25.0);
        thresholdPreview.setMaxSize(80.0, 25.0);
        GridPane.setHalignment(thresholdConfirm, HPos.CENTER);
        thresholdPreview.setOnAction(event -> {
            String thresholdValue = thresholdTextField.getText();
            try{
                if(Double.parseDouble(thresholdValue) >= -1 && Double.parseDouble(thresholdValue) <= 1) {

                }
            } catch(Exception e) {
                System.out.println("Exception: " + e);
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
        imageScrollBox.setPrefSize(840.0, 341.0);
        imageScrollBox.setMaxSize(840.0, 341.0);
        imageScrollBox.setMinSize(840.0, 341.0);
        BorderPane.setAlignment(imageScrollBox, Pos.BOTTOM_CENTER);
        VBox image1ScrollVBox = new VBox();
        image1ScrollVBox.setPrefSize(415.0, 341.0);
        image1ScrollVBox.setMaxSize(415.0, 341.0);
        image1ScrollVBox.setMinSize(415.0, 341.0);
        image1ScrollVBox.setTranslateX(10);
        VBox image2ScrollVBox = new VBox();
        image2ScrollVBox.setPrefSize(415.0, 341.0);
        image2ScrollVBox.setMaxSize(415.0, 341.0);
        image2ScrollVBox.setMinSize(415.0, 341.0);
        image2ScrollVBox.setTranslateX(20);
        ScrollPane image1ScrollPane = new ScrollPane();
        image1ScrollPane.setPrefSize(415.0, 321.0);
        image1ScrollPane.setMaxSize(415.0, 321.0);
        image1ScrollPane.setMinSize(415.0, 321.0);
        ScrollPane image2ScrollPane = new ScrollPane();
        image2ScrollPane.setPrefSize(415.0, 321.0);
        image2ScrollPane.setMaxSize(415.0, 321.0);
        image2ScrollPane.setMinSize(415.0, 321.0);
        imageScrollBox.getChildren().addAll(image1ScrollVBox, image2ScrollVBox);
        Label image1ScrollLabel = new Label("Image 1");
        image1ScrollLabel.setPrefSize(415.0, 25.0);
        image1ScrollLabel.setMaxSize(415.0, 25.0);
        image1ScrollLabel.setMinSize(415.0, 25.0);
        image1ScrollLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1ScrollLabel, Priority.NEVER);
        Label image2ScrollLabel = new Label("Image 2");
        image2ScrollLabel.setPrefSize(415.0, 25.0);
        image2ScrollLabel.setMaxSize(415.0, 25.0);
        image2ScrollLabel.setMinSize(415.0, 25.0);
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
        image2ScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        image1ScrollVBox.getChildren().addAll(image1ScrollLabel, image1ScrollPane);
        image2ScrollVBox.getChildren().addAll(image2ScrollLabel, image2ScrollPane);
        Tab scrollTab = new Tab("Scroll", imageScrollBox);

        HBox imageThumbnailBox = new HBox();
        imageThumbnailBox.setPrefSize(840.0, 341.0);
        imageThumbnailBox.setMaxSize(840.0, 341.0);
        imageThumbnailBox.setMinSize(840.0, 341.0);
        BorderPane.setAlignment(imageThumbnailBox, Pos.BOTTOM_CENTER);
        VBox image1ThumbnailVBox = new VBox();
        image1ThumbnailVBox.setPrefSize(415.0, 341.0);
        image1ThumbnailVBox.setMaxSize(415.0, 341.0);
        image1ThumbnailVBox.setMinSize(415.0, 341.0);
        image1ThumbnailVBox.setTranslateX(10);
        VBox image2ThumbnailVBox = new VBox();
        image2ThumbnailVBox.setPrefSize(415.0, 341.0);
        image2ThumbnailVBox.setMaxSize(415.0, 341.0);
        image2ThumbnailVBox.setMinSize(415.0, 341.0);
        image2ThumbnailVBox.setTranslateX(20);
        Pane image1ThumbnailPane = new Pane();
        image1ThumbnailPane.setPrefSize(415.0, 321.0);
        image1ThumbnailPane.setMaxSize(415.0, 321.0);
        image1ThumbnailPane.setMinSize(415.0, 321.0);
        Pane image2ThumbnailPane = new Pane();
        image2ThumbnailPane.setPrefSize(415.0, 321.0);
        image2ThumbnailPane.setMaxSize(415.0, 321.0);
        image2ThumbnailPane.setMinSize(415.0, 321.0);
        imageThumbnailBox.getChildren().addAll(image1ThumbnailVBox, image2ThumbnailVBox);
        Label image1ThumbnailLabel = new Label("Image 1");
        image1ThumbnailLabel.setPrefSize(415.0, 25.0);
        image1ThumbnailLabel.setMaxSize(415.0, 25.0);
        image1ThumbnailLabel.setMinSize(415.0, 25.0);
        image1ThumbnailLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1ThumbnailLabel, Priority.NEVER);
        Label image2ThumbnailLabel = new Label("Image 2");
        image2ThumbnailLabel.setPrefSize(415.0, 25.0);
        image2ThumbnailLabel.setMaxSize(415.0, 25.0);
        image2ThumbnailLabel.setMinSize(415.0, 25.0);
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
        imageTabPane.setPrefSize(880.0, 346.0);
        imageTabPane.setMaxSize(880.0, 346.0);
        imageTabPane.setMinSize(880.0, 346.0);
        overallPane.setBottom(imageTabPane);

        //matrix part
        BorderPane matrixBorder = new BorderPane();

        AnchorPane verticalAnchor = new AnchorPane();
        AnchorPane horizontalAnchor = new AnchorPane();

        ScrollPane horizontalLabelScroll = new ScrollPane();
        horizontalLabelScroll.setPrefSize(868.0, 25.0);
        horizontalLabelScroll.setMaxSize(868.0, 25.0);
        horizontalLabelScroll.setMinSize(868.0, 25.0);
        horizontalLabelScroll.setPannable(false);
        horizontalLabelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontalLabelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        ScrollPane verticalLabelScroll = new ScrollPane();
        verticalLabelScroll.setPrefSize(25.0, 347.0);
        verticalLabelScroll.setMaxSize(25.0, 347.0);
        verticalLabelScroll.setMinSize(25.0, 347.0);
        verticalLabelScroll.setPannable(false);
        verticalLabelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        verticalLabelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        matrixBorder.setPrefSize(880.0, 384.0);
        matrixBorder.setMaxSize(880.0, 384.0);
        matrixBorder.setMinSize(880.0, 384.0);
        GridPane verticalLabelPane = new GridPane();
        verticalLabelPane.setGridLinesVisible(true);

        GridPane horizontalLabelPane = new GridPane();
        horizontalLabelPane.setGridLinesVisible(true);


        RowConstraints labelRowConstraint = new RowConstraints(25.0, 25.0, 25.0);
        horizontalLabelPane.getRowConstraints().add(labelRowConstraint);
        ColumnConstraints labelColumnConstraint = new ColumnConstraints(25.0, 25.0, 25.0);
        verticalLabelPane.getColumnConstraints().add(labelColumnConstraint);
        ScrollPane matrixScrollPane = new ScrollPane();
        matrixScrollPane.setPrefSize(855.0, 359.0);
        matrixScrollPane.setMaxSize(855.0, 359.0);
        matrixScrollPane.setMinSize(855.0, 359.0);
        matrixScrollPane.setStyle("-fx-font-size: 12px");
        BorderPane.setAlignment(matrixBorder, Pos.TOP_CENTER);
        BorderPane.setMargin(matrixBorder, new Insets(10.0,0.0,0.0,0.0));
        matrixBorder.setCenter(matrixScrollPane);
        horizontalLabelScroll.setContent(horizontalAnchor);

        horizontalAnchor.getChildren().add(horizontalLabelPane);

        horizontalLabelScroll.setTranslateX(25.0);

        matrixBorder.setTop(horizontalLabelScroll);
        verticalAnchor.getChildren().add(verticalLabelPane);
        verticalLabelScroll.setContent(verticalAnchor);
        matrixBorder.setLeft(verticalLabelScroll);
        GridPane matrix = new GridPane();
        matrix.setGridLinesVisible(true);
        for(int i = 0; i < size; i++) {
            Label tempVerticalLabel = new Label(Integer.toString(i + 1));
            Label tempHorizontalLabel = new Label(Integer.toString(i + 1));
            tempVerticalLabel.setPrefSize(25.0, 25.0);
            tempVerticalLabel.setMinSize(25.0, 25.0);
            tempVerticalLabel.setMaxSize(25.0, 25.0);
            tempVerticalLabel.setAlignment(Pos.CENTER);
            tempHorizontalLabel.setPrefSize(40.0, 25.0);
            tempHorizontalLabel.setMaxSize(40.0, 25.0);
            tempHorizontalLabel.setMinSize(40.0, 25.0);
            tempHorizontalLabel.setAlignment(Pos.CENTER);
            horizontalLabelPane.add(tempHorizontalLabel, i, 0);
            verticalLabelPane.add(tempVerticalLabel, 0, i);
        }
        for(int i = 0; i < size + 1; i++) {
            for(int j = 0; j < size + 1; j++) {
                if(j == 0 && i== 0) {
                }
                else if(i == 0) {
//                    Label tempLabel = new Label(Integer.toString(j));
//                    tempLabel.setPrefSize(40.0, 25.0);
//                    tempLabel.setMaxSize(40.0, 25.0);
//                    tempLabel.setMinSize(40.0, 25.0);
//                    tempLabel.setAlignment(Pos.CENTER);
//                    matrix.add(tempLabel, j, 0);
                }
                else if(j == 0) {
//                    Label tempLabel = new Label(Integer.toString(i));
//                    tempLabel.setPrefSize(25.0, 25.0);
//                    tempLabel.setMaxSize(25.0, 25.0);
//                    tempLabel.setMinSize(25.0, 25.0);
//                    tempLabel.setAlignment(Pos.CENTER);
//                    matrix.add(tempLabel, 0, i);
                } else {
                    String tempString = String.format("%.2f", duplicateMatrix[i - 1][j - 1]);
                    //set buttons to be the corresponding matrix
                    Button tempButton = new Button(tempString);
                    tempButton.setPrefSize(40.0, 25.0);
                    tempButton.setMaxSize(40.0, 25.0);
                    tempButton.setMinSize(40.0, 25.0);
                    int tempI = i;
                    int tempJ = j;
                    tempButton.setOnAction(e -> {
                        //set the correct images depending on button click
                        AnchorPane.setTopAnchor(verticalLabelPane, -25.0);
                        AnchorPane.setLeftAnchor(horizontalLabelPane, -40.0);
                        image1ScrollLabel.setText("Channel " + tempI);
                        image2ScrollLabel.setText("Channel " + tempJ);
                        image1ThumbnailLabel.setText("Channel " + tempI);
                        image2ThumbnailLabel.setText("Channel " + tempJ);
                        BufferedImage[] bufferedImages1 = ConcatChannelsABI.singleChannelImage(imageData, tempI - 1, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight());
                        BufferedImage[] bufferedImages2 = ConcatChannelsABI.singleChannelImage(imageData, tempJ - 1, (int)image1ThumbnailPane.getWidth(), (int)image1ThumbnailPane.getHeight());
                        imageScrollView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[1], null));
                        imageScrollView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[1], null));
                        imageThumbnailView1.setImage(SwingFXUtils.toFXImage(bufferedImages1[0], null));
                        imageThumbnailView2.setImage(SwingFXUtils.toFXImage(bufferedImages2[0], null));
                    });
                    matrix.add(tempButton, i, j);
                }
            }
        }
        matrixScrollPane.setContent(matrix);
        overallPane.setCenter(matrixBorder);


        //pane.getChildren().add(overallPane);

        Scene scene = new Scene(pane, 900, 805);
        dialog.setScene(scene);
        dialog.setMinWidth(900);
        dialog.setMinHeight(805);
        dialog.setMaxWidth(900);
        dialog.setMaxHeight(805);

        return dialog;
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
