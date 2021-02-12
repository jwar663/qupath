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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import qupath.lib.analysis.stats.Histogram;
import qupath.lib.common.ConcatChannelsABI;
import qupath.lib.display.ChannelDisplayInfo;
import qupath.lib.display.DirectServerChannelInfo;
import qupath.lib.display.ImageDisplay;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.charts.HistogramPanelFX;
import qupath.lib.gui.charts.HistogramPanelFX.HistogramData;
import qupath.lib.gui.charts.HistogramPanelFX.ThresholdedChartWrapper;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.prefs.PathPrefs;
import qupath.lib.gui.tools.ColorToolsFX;
import qupath.lib.gui.tools.PaneTools;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageChannel;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServerMetadata;

import javax.imageio.ImageIO;

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

    private double[][] fakeMatrix = new double[42][42];
    private int size;

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
        size = 5;
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                fakeMatrix[i][j] = Math.random();
            }
        }
        //for testing images
        InputStream stream = new FileInputStream("D:\\Desktop\\QuPath\\testimage.jpg");
        Image img = new Image(stream);

        //to visualise and allow for dimensions
        Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,CornerRadii.EMPTY, BorderWidths.DEFAULT));

        //larger panes
        Pane pane = new Pane();
        BorderPane overallPane = new BorderPane();
        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Duplicate Matrix");
        pane.setPadding(new Insets(10, 10, 10, 10));

        //Threshold Part
        Label thresholdLabel = new Label("Please enter the correct threshold value:");
        TextField thresholdValue = new TextField("0.90");
        thresholdValue.setPrefSize(40,10);
        Button thresholdConfirm = new Button("OK");
        HBox thresholdHBox = new HBox();
        thresholdHBox.getChildren().addAll(thresholdLabel, thresholdValue, thresholdConfirm);
        overallPane.setTop(thresholdHBox);

        //matrix part
        BorderPane matrixPane = new BorderPane();
        //TableView<Double> matrixTable = new TableView<Double>();
        GridPane matrix = new GridPane();
        for(int i = 0; i < size + 1; i++) {
            for(int j = 0; j < size + 1; j++) {
                if(i == 0 && j == 0) {

                } else if(i == 0) {
                    Label tempLabel = new Label(Integer.toString(j));
                    tempLabel.setAlignment(Pos.CENTER);
                    matrix.add(tempLabel, i, j);
                } else if(j == 0) {
                    Label tempLabel = new Label(Integer.toString(i));
                    matrix.add(tempLabel, i, j);
                } else {
                    String tempString = String.format("%.2f", fakeMatrix[i - 1][j - 1]);
                    //set buttons to be the corresponding matrix
                    Button tempButton = new Button(tempString);
                    tempButton.setAlignment(Pos.CENTER);
                    tempButton.setOnAction(e -> {
                        //set the correct images depending on button click
                        System.out.println(tempString);
                    });
                    matrix.add(tempButton, i, j);
                }
            }
        }
        matrix.setAlignment(Pos.BOTTOM_RIGHT);
        System.out.println(matrix.getAlignment().toString());
        ScrollBar verticalScrollBar = new ScrollBar();
        ScrollBar horizontalScrollBar = new ScrollBar();
        verticalScrollBar.setOrientation(Orientation.VERTICAL);
        matrixPane.setBottom(horizontalScrollBar);
        matrixPane.setRight(verticalScrollBar);
        matrixPane.setCenter(matrix);
        overallPane.setCenter(matrixPane);

        //preview image section
        HBox imageHBox = new HBox();
        VBox image1VBox = new VBox();
        VBox image2VBox = new VBox();
        imageHBox.getChildren().addAll(image1VBox, image2VBox);
        Label image1Label = new Label("Image 1");
        Label image2Label = new Label("Image 2");
        ImageView imageView1 = new ImageView();
        ImageView imageView2 = new ImageView();
        imageView1.setImage(img);
        imageView2.setImage(img);
        image1VBox.getChildren().addAll(image1Label, imageView1);
        image2VBox.getChildren().addAll(image2Label, imageView2);
        overallPane.setBottom(imageHBox);

        //set borders
        //overallPane.setBorder(border);

        pane.getChildren().add(overallPane);

        //image data is bad
        //image1 = ConcatChannelsABI.singleChannelImage(imageData, 0);
        //Canvas canvas1 = new Canvas();
        //canvas1.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(image1, null), 0,0);
        //largePane.getChildren().add(canvas1);


        Scene scene = new Scene(pane, 800, 800);
        dialog.setScene(scene);
        dialog.setMinWidth(800);
        dialog.setMinHeight(800);
        dialog.setMaxWidth(800);
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
