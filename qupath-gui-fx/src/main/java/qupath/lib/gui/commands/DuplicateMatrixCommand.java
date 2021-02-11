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
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
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

    protected Stage createDialog() {
        //for testing matrix without image data
        size = 5;
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                fakeMatrix[i][j] = Math.random();
            }
        }

        //to visualise and allow for dimensions
        Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,CornerRadii.EMPTY, BorderWidths.DEFAULT));

        //larger panes
        Pane pane = new Pane();
        VBox overallPane = new VBox();
        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Duplicate Matrix");
        pane.setPadding(new Insets(10, 10, 10, 10));

        //Threshold Part
        Label thresholdLabel = new Label("Please enter the correct threshold value:");
        TextField thresholdValue = new TextField("0.90");
        Button thresholdConfirm = new Button("OK");
        HBox thresholdHBox = new HBox();
        thresholdHBox.getChildren().addAll(thresholdLabel, thresholdValue, thresholdConfirm);

        //matrix part
        BorderPane matrixPane = new BorderPane();
        //TableView<Double> matrixTable = new TableView<Double>();
        GridPane matrix = new GridPane();
        matrix.setGridLinesVisible(true);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                String tempString = String.format("%.2f", fakeMatrix[i][j]);
                Button tempButton = new Button(tempString);

                tempButton.setOnAction(e -> {
                    System.out.println(tempString);
                });
                matrix.add(tempButton, i, j);
            }
        }
        ScrollBar verticalScrollBar = new ScrollBar();
        ScrollBar horizontalScrollBar = new ScrollBar();
        verticalScrollBar.setOrientation(Orientation.VERTICAL);
        matrixPane.setBottom(horizontalScrollBar);
        matrixPane.setRight(verticalScrollBar);

        //set borders
        overallPane.setBorder(border);

        overallPane.getChildren().addAll(matrix);
        pane.getChildren().add(overallPane);

        //overallPane.maxWidth(20);

        //image data is bad
        //image1 = ConcatChannelsABI.singleChannelImage(imageData, 0);
        //Canvas canvas1 = new Canvas();
        //canvas1.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(image1, null), 0,0);
        //largePane.getChildren().add(canvas1);


        Scene scene = new Scene(pane, 350, 500);
        dialog.setScene(scene);
        dialog.setMinWidth(300);
        dialog.setMinHeight(400);
        dialog.setMaxWidth(600);
        dialog.setMaxHeight(800);

        return dialog;
    }

    @Override
    public void run() {
        if (dialog == null)
            dialog = createDialog();
        dialog.show();
    }
}
