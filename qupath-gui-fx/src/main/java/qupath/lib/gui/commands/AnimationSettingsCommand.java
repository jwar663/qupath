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
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import qupath.lib.common.ConcatChannelsABI;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.writers.ImageWriter;
import qupath.lib.images.writers.ImageWriterTools;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Command to show a Duplicate Matrix widget to preview and decide which threshold
 * is the best to properly represent the image.
 *
 * @author Jaedyn Ward
 *
 */
public class AnimationSettingsCommand implements Runnable {

    private QuPathGUI qupath;
    private QuPathViewer viewer;

    private Stage dialog;

    private float[][] duplicateMatrix;
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
    private static final double OVERALL_WIDTH = 900.0;
    private static final double OVERALL_HEIGHT = 700.0;

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

    String thresholdValue = START_THRESHOLD;


    /**
     * Constructor.
     * @param qupath
     */
    public AnimationSettingsCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
        this.viewer = qupath.getViewer();
    }

    protected Stage createDialog() throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Duplicate Matrix");

        GridPane overallPane = new GridPane();

        ToggleButton singleStackToggle = new ToggleButton("Single");

        Label delayLabel = new Label("Delay(ms)");

        TextField delayField = new TextField("250");

        Button chooseFolderButton = new Button("Choose Folder");

        Label exportLabel = new Label("Export to: ");

        Button confirmButton = new Button("Confirm");

        Button cancelButton = new Button("Cancel");

        overallPane.add(singleStackToggle, 0, 0, 2, 1);
        overallPane.add(delayLabel, 0, 1, 1, 1);
        overallPane.add(delayField, 1, 1, 1, 1);
        overallPane.add(chooseFolderButton, 0, 2, 2, 1);
        overallPane.add(exportLabel, 0, 3, 2, 1);
        overallPane.add(confirmButton, 0, 4, 1, 1);
        overallPane.add(cancelButton, 1, 4, 1, 1);

        Scene scene = new Scene(overallPane, OVERALL_WIDTH, OVERALL_HEIGHT);
        dialog.setScene(scene);
        dialog.setMinWidth(OVERALL_WIDTH);
        dialog.setMinHeight(OVERALL_HEIGHT);
        dialog.setMaxWidth(OVERALL_WIDTH);
        dialog.setMaxHeight(OVERALL_HEIGHT);

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
