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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.lib.display.ImageDisplay;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;

import static java.lang.Math.round;

/**
 * Command to show a Duplicate Matrix widget to preview and decide which threshold
 * is the best to properly represent the image.
 *
 * @author Jaedyn Ward
 *
 */
public class DuplicateMatrixCommand implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(DuplicateMatrixCommand.class);

    private static DecimalFormat df = new DecimalFormat("#.###");

    private QuPathGUI qupath;
    private QuPathViewer viewer;
    private ImageDisplay imageDisplay;

    private BufferedImage image1;
    private BufferedImage image2;

    private Stage dialog;

    private int size = 5;
    private double[][] fakeMatrix = new double[size][size];


    private ImageData imageData;

    /**
     * Constructor.
     * @param qupath
     */
    public DuplicateMatrixCommand(final QuPathGUI qupath, ImageData<?> imageData) {
        this.qupath = qupath;
        this.imageData = imageData;
    }

    protected Stage createDialog() throws IOException {
        //for testing matrix without image data
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                fakeMatrix[i][j] = Math.random();
            }
        }
        //for testing images
        InputStream stream = new FileInputStream("D:\\Desktop\\QuPath\\testimage.jpg");
        Image img = new Image(stream);

        //to visualise and allow for dimensions
        //Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,CornerRadii.EMPTY, BorderWidths.DEFAULT));

        //larger panes
        Pane pane = new Pane();
        pane.setPrefSize(900, 800);
        BorderPane overallPane = new BorderPane();
        overallPane.setPrefSize(880,780);
        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Duplicate Matrix");
        overallPane.setPadding(new Insets(10, 10, 10, 10));

        //Threshold Part
        Label thresholdLabel = new Label("Please enter the correct threshold value:");
        thresholdLabel.setPrefHeight(20);
        TextField thresholdTextField = new TextField("0.90");
        thresholdTextField.setPrefSize(40,20);
        Button thresholdConfirm = new Button("OK");
        thresholdConfirm.setOnAction(event -> {
            String thresholdValue = thresholdTextField.getText();
            try{
                if(Double.parseDouble(thresholdValue) >= -1 && Double.parseDouble(thresholdValue) <= 1) {
                    //TODO: Call ConcatChannelsABI.concatDuplicateChannels when imageData is gathered correctly
                    System.out.println(thresholdValue);
                } else {
                    System.out.println("error");
                }
            } catch(Exception e) {
                System.out.println("Exception: " + e);
            }
        });
        thresholdConfirm.setPrefSize(40,20);
        GridPane thresholdGrid = new GridPane();
        thresholdGrid.add(thresholdLabel, 0, 0);
        thresholdGrid.add(thresholdTextField, 1, 0);
        thresholdGrid.add(thresholdConfirm, 2, 0);
        thresholdGrid.setPrefSize(880, 20);
        thresholdGrid.setHgap(10);
        overallPane.setTop(thresholdGrid);

        //preview image section
        HBox imageHBox = new HBox(10);
        imageHBox.setPrefSize(880, 346);
        imageHBox.setAlignment(Pos.CENTER);
        VBox image1VBox = new VBox();
        image1VBox.setPrefSize(435, 346);
        VBox image2VBox = new VBox();
        image2VBox.setPrefSize(435, 346);
        Pane imagePane1 = new Pane();
        imagePane1.setPrefSize(435, 326);
        Pane imagePane2 = new Pane();
        imagePane2.setPrefSize(435, 326);
        imageHBox.getChildren().addAll(image1VBox, image2VBox);
        Label image1Label = new Label("Image 1");
        image1Label.setPrefHeight(20);
        Label image2Label = new Label("Image 2");
        image2Label.setPrefHeight(20);
        ImageView imageView1 = new ImageView();
        imageView1.setFitHeight(326);
        imageView1.setFitWidth(435);
        imageView1.setPreserveRatio(true);
        ImageView imageView2 = new ImageView();
        imageView2.setFitHeight(326);
        imageView2.setFitWidth(435);
        imageView2.setPreserveRatio(true);
        imageView1.setImage(img);
        imageView2.setImage(img);
        imagePane1.getChildren().add(imageView1);
        imagePane2.getChildren().add(imageView2);
        image1VBox.getChildren().addAll(image1Label, imagePane1);
        image2VBox.getChildren().addAll(image2Label, imagePane2);
        overallPane.setBottom(imageHBox);

        //matrix part
        BorderPane matrixPane = new BorderPane();
        matrixPane.setPrefSize(880, 394);
        //TableView<Double> matrixTable = new TableView<Double>();
        GridPane matrix = new GridPane();
        GridPane labelVertical = new GridPane();
        GridPane labelHorizontal = new GridPane();
        labelHorizontal.setPrefSize(860, 20);
        labelHorizontal.setMaxSize(860, 20);
        labelVertical.setMaxSize(20, 374);
        matrix.setPrefSize(860,374);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(j == 0 && i== 0) {
                    Label tempLabel1 = new Label(Integer.toString(j + 1));
                    Label tempLabel2 = new Label(Integer.toString(i + 1));
                    tempLabel1.setPrefSize(40,20);
                    tempLabel1.setMaxSize(40,20);
                    tempLabel1.setAlignment(Pos.CENTER);
                    tempLabel2.setPrefSize(40,20);
                    tempLabel2.setMaxSize(40,20);
                    tempLabel2.setAlignment(Pos.CENTER);
                    labelHorizontal.add(tempLabel1, i, j);
                    labelVertical.add(tempLabel2, i, j);
                }
                else if(i == 0) {
                    Label tempLabel = new Label(Integer.toString(j + 1));
                    tempLabel.setPrefSize(40,20);
                    tempLabel.setMaxSize(40,20);
                    tempLabel.setAlignment(Pos.CENTER);
                    labelHorizontal.add(tempLabel, i, j);
                }
                else if(j == 0) {
                    Label tempLabel = new Label(Integer.toString(i + 1));
                    tempLabel.setPrefSize(40,20);
                    tempLabel.setMaxSize(40,20);
                    tempLabel.setAlignment(Pos.CENTER);
                    labelVertical.add(tempLabel, i, j);
                }
                String tempString = String.format("%.2f", fakeMatrix[i][j]);
                //set buttons to be the corresponding matrix
                Button tempButton = new Button(tempString);
                tempButton.setPrefSize(40,20);
                tempButton.setAlignment(Pos.CENTER);
                int tempI = i;
                int tempJ = j;
                tempButton.setOnAction(e -> {
                    //set the correct images depending on button click
                    image1Label.setText("Channel " + tempI);
                    image2Label.setText("Channel " + tempJ);
                    //imageView1.setImage(SwingFXUtils.toFXImage(ConcatChannelsABI.singleChannelImage(imageData, tempI), null));
                    //imageView2.setImage(SwingFXUtils.toFXImage(ConcatChannelsABI.singleChannelImage(imageData, tempJ), null));
                    System.out.println(tempString);
                });
                matrix.add(tempButton, i, j);
            }
        }
        matrix.setAlignment(Pos.BOTTOM_RIGHT);
        System.out.println(matrix.getAlignment().toString());
        ScrollBar verticalScrollBar = new ScrollBar();
        ScrollBar horizontalScrollBar = new ScrollBar();
        horizontalScrollBar.setPrefSize(860, 20);
        verticalScrollBar.setOrientation(Orientation.VERTICAL);
        verticalScrollBar.setPrefSize(20, 374);
        matrixPane.setBottom(horizontalScrollBar);
        matrixPane.setRight(verticalScrollBar);
        matrixPane.setCenter(matrix);
        matrixPane.setTop(labelHorizontal);
        matrixPane.setLeft(labelVertical);
        overallPane.setCenter(matrixPane);

        //set borders
        //overallPane.setBorder(border);

        pane.getChildren().add(overallPane);

        //image data is bad
        //image1 = ConcatChannelsABI.singleChannelImage(imageData, 0);
        //Canvas canvas1 = new Canvas();
        //canvas1.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(image1, null), 0,0);
        //largePane.getChildren().add(canvas1);

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
        if (dialog == null)
            try{
                dialog = createDialog();
            } catch (IOException e) {

            }
        dialog.show();
    }
}
