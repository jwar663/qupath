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
import java.util.concurrent.atomic.AtomicReference;

import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.lib.common.ConcatChannelsABI;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;

/**
 * Command to show a Duplicate Matrix widget to preview and decide which threshold
 * is the best to properly represent the image.
 *
 * @author Jaedyn Ward
 *
 */
public class RemoveAFCommand implements Runnable {

    private QuPathGUI qupath;
    private QuPathViewer viewer;

    private Stage dialog;

    public ImageData<BufferedImage> imageData;


    /**
     * Constructor.
     * @param qupath
     */
    public RemoveAFCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
        this.viewer = qupath.getViewer();
    }

    protected Stage createDialog() throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Remove AF");


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

        GridPane overallPane = DuplicateMatrixCommand.createThresholdPane();
        Button buttonTiffMinusAll = DuplicateMatrixCommand.createThresholdConfirm();
        Button buttonTiffMinusSome = DuplicateMatrixCommand.createThresholdConfirm();
        buttonTiffMinusAll.setText("Tiff Minus All");
        buttonTiffMinusSome.setText("Tiff Minus Some");

        buttonTiffMinusAll.setMaxWidth(120);
        buttonTiffMinusAll.setPrefWidth(120);
        buttonTiffMinusAll.setMinWidth(120);

        buttonTiffMinusSome.setMaxWidth(120);
        buttonTiffMinusSome.setPrefWidth(120);
        buttonTiffMinusSome.setMinWidth(120);

        overallPane.add(buttonTiffMinusAll, 0, 0);
        overallPane.add(buttonTiffMinusSome, 1, 0);
        buttonTiffMinusAll.setOnAction(e -> {
            String filePath = DuplicateMatrixCommand.getFilePath(viewer, 0.101);
            viewer.setImageData(ConcatChannelsABI.removeAF(true, imageData));
            DuplicateMatrixCommand.exportImage(viewer, filePath);
            if(dialog.isShowing()) {
                dialog.close();
            }
            try {
                qupath.openImage(viewer, filePath + ".tif", false, false);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        buttonTiffMinusSome.setOnAction(e -> {
            String filePath = DuplicateMatrixCommand.getFilePath(viewer, 0.010);
            viewer.setImageData(ConcatChannelsABI.removeAF(false, imageData));
            DuplicateMatrixCommand.exportImage(viewer, filePath);
            if(dialog.isShowing()) {
                dialog.close();
            }
            try {
                qupath.openImage(viewer, filePath + ".tif", false, false);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        Button buttonIm3MinusAll = DuplicateMatrixCommand.createThresholdConfirm();
        Button buttonIm3MinusSome = DuplicateMatrixCommand.createThresholdConfirm();
        buttonIm3MinusAll.setText("Tiff Minus All");
        buttonIm3MinusSome.setText("Tiff Minus Some");

        Button removeAFMapChannels = DuplicateMatrixCommand.createThresholdConfirm();
        removeAFMapChannels.setText("Two im3 images");

        removeAFMapChannels.setMaxWidth(120);
        removeAFMapChannels.setPrefWidth(120);
        removeAFMapChannels.setMinWidth(120);

        removeAFMapChannels.setOnAction(e -> {
            try {
                ImageData autofluorescenceData = qupath.getProject().getImageList().get(1).readImageData();
                String filePath = DuplicateMatrixCommand.getFilePath(viewer, 1.1);
                viewer.setImageData(ConcatChannelsABI.removeMappedAF(imageData, autofluorescenceData));
                DuplicateMatrixCommand.exportImage(viewer, filePath);
            } catch (IOException ioException) {
                Dialogs.showErrorMessage("Error", "Please open a valid project");
                ioException.printStackTrace();
            }
            if(dialog.isShowing()) {
                dialog.close();
            }
//            try {
//                qupath.openImage(viewer, filePath + ".tif", false, false);
//            } catch (IOException exception) {
//                exception.printStackTrace();
//            }
        });

        buttonIm3MinusAll.setMaxWidth(120);
        buttonIm3MinusAll.setPrefWidth(120);
        buttonIm3MinusAll.setMinWidth(120);

        buttonIm3MinusSome.setMaxWidth(120);
        buttonIm3MinusSome.setPrefWidth(120);
        buttonIm3MinusSome.setMinWidth(120);

        overallPane.add(buttonIm3MinusAll, 0, 1);
        overallPane.add(buttonIm3MinusSome, 1, 1);
        overallPane.add(removeAFMapChannels, 0, 2, 2, 1);


        //kind of pointless at this point
        buttonIm3MinusAll.setOnAction(e -> {
            String filePath = DuplicateMatrixCommand.getFilePath(viewer, 0.101);
            viewer.setImageData(ConcatChannelsABI.removeAF(true, imageData));
            DuplicateMatrixCommand.exportImage(viewer, filePath);
            if(dialog.isShowing()) {
                dialog.close();
            }
            try {
                qupath.openImage(viewer, filePath + ".tif", false, false);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });


        //kind of pointless at this point
        buttonIm3MinusSome.setOnAction(e -> {
            String filePath = DuplicateMatrixCommand.getFilePath(viewer, 0.010);
            viewer.setImageData(ConcatChannelsABI.removeAF(false, imageData));
            DuplicateMatrixCommand.exportImage(viewer, filePath);
            if(dialog.isShowing()) {
                dialog.close();
            }
            try {
                qupath.openImage(viewer, filePath + ".tif", false, false);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        Scene scene = new Scene(overallPane, 400, 400);
        dialog.setScene(scene);
        dialog.setMinWidth(400);
        dialog.setMinHeight(400);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(400);

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
