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
        pane.setPrefSize(900.0, 800.0);

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
//        HBox imageHBox = new HBox(10);
//        VBox image1VBox = new VBox();
//        VBox image2VBox = new VBox();
//        Pane imagePane1 = new Pane();
//        Pane imagePane2 = new Pane();
//        imageHBox.getChildren().addAll(image1VBox, image2VBox);
//        Label image1Label = new Label("Image 1");
//        Label image2Label = new Label("Image 2");
//        ImageView imageView1 = new ImageView();
//        ImageView imageView2 = new ImageView();
//        imagePane1.getChildren().add(imageView1);
//        imagePane2.getChildren().add(imageView2);
//        image1VBox.getChildren().addAll(image1Label, imagePane1);
//        image2VBox.getChildren().addAll(image2Label, imagePane2);
//        overallPane.setBottom(imageHBox);
//
//        //matrix part
//        BorderPane matrixPane = new BorderPane();
//        GridPane matrix = new GridPane();
//        GridPane labelVertical = new GridPane();
//        GridPane labelHorizontal = new GridPane();;
//        for(int i = 0; i < size; i++) {
//            for(int j = 0; j < size; j++) {
//                if(j == 0 && i== 0) {
//                    Label tempLabel1 = new Label(Integer.toString(j + 1));
//                    Label tempLabel2 = new Label(Integer.toString(i + 1));
//                    labelHorizontal.add(tempLabel1, i, j);
//                    labelVertical.add(tempLabel2, i, j);
//                }
//                else if(i == 0) {
//                    Label tempLabel = new Label(Integer.toString(j + 1));
//                    labelHorizontal.add(tempLabel, i, j);
//                }
//                else if(j == 0) {
//                    Label tempLabel = new Label(Integer.toString(i + 1));
//                    labelVertical.add(tempLabel, i, j);
//                }
//                String tempString = String.format("%.2f", duplicateMatrix[i][j]);
//                //set buttons to be the corresponding matrix
//                Button tempButton = new Button(tempString);
//                int tempI = i + 1;
//                int tempJ = j + 1;
//                tempButton.setOnAction(e -> {
//                    //set the correct images depending on button click
//                    image1Label.setText("Channel " + tempI);
//                    image2Label.setText("Channel " + tempJ);
//                    if(imageData != null) {
//                        imageView1.setImage(SwingFXUtils.toFXImage(ConcatChannelsABI.singleChannelImage(imageData, tempI), null));
//                        imageView2.setImage(SwingFXUtils.toFXImage(ConcatChannelsABI.singleChannelImage(imageData, tempJ), null));
//                    }
//                });
//                matrix.add(tempButton, i, j);
//            }
//        }
//        overallPane.setCenter(matrixPane);


        //pane.getChildren().add(overallPane);

        Scene scene = new Scene(pane, 900, 800);
        dialog.setScene(scene);
        dialog.setMinWidth(900);
        dialog.setMinHeight(800);
        dialog.setMaxWidth(900);
        dialog.setMaxHeight(800);

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
