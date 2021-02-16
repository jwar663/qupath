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
        //thresholdPane.setAlignment(Pos.CENTER);
        ColumnConstraints labelColumn = new ColumnConstraints(750.0, 750.0, 750.0);
        ColumnConstraints fieldColumn = new ColumnConstraints(80.0, 80.0, 80.0);
        ColumnConstraints buttonColumn = new ColumnConstraints(50.0, 50.0, 50.0);
        RowConstraints rowConstraints = new RowConstraints(25.0, 25.0, 25.0);

        //Threshold Part
        Label thresholdLabel = new Label("Please enter the correct threshold value:");
        thresholdLabel.setPrefHeight(25.0);
        thresholdLabel.setMinHeight(25.0);
        thresholdLabel.setMaxHeight(25.0);
        GridPane.setHalignment(thresholdLabel, HPos.CENTER);
        TextField thresholdTextField = new TextField("0.90");
        thresholdTextField.setPrefSize(40.0, 25.0);
        thresholdTextField.setMinSize(40.0, 25.0);
        thresholdTextField.setMaxSize(40.0, 25.0);
        GridPane.setHalignment(thresholdTextField, HPos.CENTER);
        Button thresholdConfirm = new Button("OK");
        thresholdConfirm.setPrefSize(40.0, 25.0);
        thresholdConfirm.setMinSize(40.0, 25.0);
        thresholdConfirm.setMaxSize(40.0, 25.0);
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
        thresholdPane.add(thresholdLabel, 0, 0);
        thresholdPane.add(thresholdTextField, 1, 0);
        thresholdPane.add(thresholdConfirm, 2, 0);
        thresholdPane.getColumnConstraints().addAll(labelColumn, fieldColumn, buttonColumn);
        thresholdPane.getRowConstraints().add(rowConstraints);
        overallPane.setTop(thresholdPane);



        //preview image section
        HBox imageHBox = new HBox();
        imageHBox.setPrefSize(880.0, 346.0);
        imageHBox.setMaxSize(880.0, 346.0);
        imageHBox.setMinSize(880.0, 346.0);
        BorderPane.setAlignment(imageHBox, Pos.BOTTOM_CENTER);
        VBox image1VBox = new VBox();
        image1VBox.setPrefSize(435.0, 346.0);
        image1VBox.setMaxSize(435.0, 346.0);
        image1VBox.setMinSize(435.0, 346.0);
        VBox image2VBox = new VBox();
        image2VBox.setPrefSize(435.0, 346.0);
        image2VBox.setMaxSize(435.0, 346.0);
        image2VBox.setMinSize(435.0, 346.0);
        image2VBox.setTranslateX(10);
        ScrollPane image1Scroll = new ScrollPane();
        image1Scroll.setPrefSize(435.0, 326.0);
        image1Scroll.setMaxSize(435.0, 326.0);
        image1Scroll.setMinSize(435.0, 326.0);
        image1Scroll.setPannable(true);
        ScrollPane image2Scroll = new ScrollPane();
        image2Scroll.setPrefSize(435.0, 326.0);
        image2Scroll.setMaxSize(435.0, 326.0);
        image2Scroll.setMinSize(435.0, 326.0);
        image2Scroll.setPannable(true);
        imageHBox.getChildren().addAll(image1VBox, image2VBox);
        Label image1Label = new Label("Image 1");
        image1Label.setPrefSize(435.0, 25.0);
        image1Label.setMaxSize(435.0, 25.0);
        image1Label.setMinSize(435.0, 25.0);
        image1Label.setAlignment(Pos.CENTER);
        VBox.setVgrow(image1Label, Priority.NEVER);
        Label image2Label = new Label("Image 2");
        image2Label.setPrefSize(435.0, 25.0);
        image2Label.setMaxSize(435.0, 25.0);
        image2Label.setMinSize(435.0, 25.0);
        image2Label.setAlignment(Pos.CENTER);
        VBox.setVgrow(image2Label, Priority.NEVER);
        ImageView imageView1 = new ImageView();
        ImageView imageView2 = new ImageView();
        image1Scroll.setContent(imageView1);
        image2Scroll.setContent(imageView2);
        image1VBox.getChildren().addAll(image1Label, image1Scroll);
        image2VBox.getChildren().addAll(image2Label, image2Scroll);
        overallPane.setBottom(imageHBox);

        //matrix part
        ScrollPane matrixScrollPane = new ScrollPane();
        matrixScrollPane.setPrefSize(880.0, 384.0);
        matrixScrollPane.setMaxSize(880.0, 384.0);
        matrixScrollPane.setMinSize(880.0, 384.0);
        BorderPane.setAlignment(matrixScrollPane, Pos.TOP_CENTER);
        GridPane matrix = new GridPane();
        matrix.setGridLinesVisible(true);
        for(int i = 0; i < size + 1; i++) {
            for(int j = 0; j < size + 1; j++) {
                if(j == 0 && i== 0) {
//                    Label tempLabel1 = new Label(Integer.toString(j + 1));
//                    Label tempLabel2 = new Label(Integer.toString(i + 1));
//                    labelHorizontal.add(tempLabel1, i, j);
//                    labelVertical.add(tempLabel2, i, j);
                }
                else if(i == 0) {
                    Label tempLabel = new Label(Integer.toString(j));
                    tempLabel.setPrefSize(40.0, 25.0);
                    tempLabel.setMaxSize(40.0, 25.0);
                    tempLabel.setMinSize(40.0, 25.0);
                    tempLabel.setAlignment(Pos.CENTER);
                    matrix.add(tempLabel, j, 0);
                    //labelHorizontal.add(tempLabel, i, j);
                }
                else if(j == 0) {
                    Label tempLabel = new Label(Integer.toString(i));
                    tempLabel.setPrefSize(25.0, 25.0);
                    tempLabel.setMaxSize(25.0, 25.0);
                    tempLabel.setMinSize(25.0, 25.0);
                    tempLabel.setAlignment(Pos.CENTER);
                    matrix.add(tempLabel, 0, i);
                    //labelVertical.add(tempLabel, i, j);
                } else {
                    String tempString = String.format("%.2f", duplicateMatrix[i - 1][j - 1]);
                    //set buttons to be the corresponding matrix
                    Button tempButton = new Button(tempString);
                    tempButton.setPrefSize(40.0, 25.0);
                    tempButton.setMaxSize(40.0, 25.0);
                    tempButton.setMinSize(40.0, 25.0);
                    //tempButton.setStyle("-fx-background-color: #FFFFFF");
                    int tempI = i + 1;
                    int tempJ = j + 1;
                    tempButton.setOnAction(e -> {
                        //set the correct images depending on button click
                        image1Label.setText("Channel " + tempI);
                        image2Label.setText("Channel " + tempJ);
                        if(imageData != null) {
                            imageView1.setImage(SwingFXUtils.toFXImage(ConcatChannelsABI.singleChannelImage(imageData, tempI), null));
                            imageView2.setImage(SwingFXUtils.toFXImage(ConcatChannelsABI.singleChannelImage(imageData, tempJ), null));
                        }
                    });
                    matrix.add(tempButton, i, j);
                }
            }
        }
        matrixScrollPane.setContent(matrix);
        overallPane.setCenter(matrixScrollPane);


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
