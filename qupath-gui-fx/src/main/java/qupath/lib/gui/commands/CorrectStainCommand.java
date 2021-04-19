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
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.writers.ImageWriter;
import qupath.lib.images.writers.ImageWriterTools;

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
public class CorrectStainCommand implements Runnable {

    private QuPathGUI qupath;
    private QuPathViewer viewer;

    private Stage dialog;

    public ImageData<BufferedImage> imageData;

    //dimensions
    private double BUTTON_WIDTH = 60.0;
    private double BUTTON_HEIGHT = 25.0;

    private double OVERALL_WIDTH = BUTTON_WIDTH*6 + 15;
    private double OVERALL_HEIGHT = BUTTON_HEIGHT*2 + 15;



    /**
     * Constructor.
     * @param qupath
     */
    public CorrectStainCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
        this.viewer = qupath.getViewer();
    }

    public static String getFilePath(QuPathViewer viewer, Double thresholdValue) {
        ImageServer<BufferedImage> imageServer = viewer.getServer();
        Collection<URI> uris = imageServer.getURIs();
        //remove "." from the name of the file
        String thresholdString = Double.toString(thresholdValue * 100).substring(0,2);
        String filePath = "";
        URI Uri;
        if(uris.iterator().hasNext()) {
            Uri = uris.iterator().next();
            filePath = GeneralTools.getNameWithoutExtension(Uri.getPath()) + "-distinct-" + thresholdString;
        }
        return filePath;
    }

    public static void exportImage(QuPathViewer viewer, String filePath, Stage dialog) {
        ImageServer<BufferedImage> imageServer = viewer.getServer();
        List<ImageWriter<BufferedImage>> writers = ImageWriterTools.getCompatibleWriters(imageServer, null);
        ImageWriter<BufferedImage> writer = writers.get(0);
        File file = new File(filePath + "." + writer.getDefaultExtension());
//        if(!file.exists()) {
            try{
                writer.writeImage(imageServer, file.getPath());
            } catch(Exception e) {
                e.printStackTrace();
            }
//        } else {
//            createFileExistsAlert(dialog, writer, imageServer, file).showAndWait();
//        }
    }

    private GridPane createExportGridPane(String[] stains) {
        int numberOfStains = stains.length;
        GridPane gridPane = new GridPane();
        for(int i = 0; i < numberOfStains; i++) {
            TextField textField = new TextField("0.0");
            Label label = new Label(stains[i]);

            textField.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            textField.setMaxSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            textField.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

            label.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            label.setMaxSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            label.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

            gridPane.add(label, i, 0);
            gridPane.add(textField, i, 1);
        }
        return gridPane;
    }


    public static Stage createInvalidInputStage(Stage dialog, boolean toggle, int numberOfChannels) {
        Stage invalidInput = new Stage();
        invalidInput.setTitle("Invalid Input");
        invalidInput.initModality(Modality.WINDOW_MODAL);
        invalidInput.initOwner(dialog);
        Button invalidInputConfirmButton = new Button("OK");
        invalidInputConfirmButton.setOnAction(ev -> {
            invalidInput.close();
        });
        VBox invalidInputVbox;
        invalidInputVbox = new VBox(new Text("Please enter a value between 0.0 and 1.0"), invalidInputConfirmButton);
        invalidInputVbox.setSpacing(10.0);
        invalidInputVbox.setAlignment(Pos.CENTER);
        invalidInputVbox.setPadding(new Insets(15));

        invalidInput.setScene(new Scene(invalidInputVbox));
        return invalidInput;
    }

    protected Stage createDialog() throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Export Correct Stains");

        GridPane overallPane = new GridPane();
        overallPane.setPrefSize(OVERALL_WIDTH - 10, OVERALL_HEIGHT - 10);

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


        Label instructionLabel = new Label("Please select the corresponding '.csv' file: ");
        Label fileLabel = new Label();
        Button fileButton = new Button("Choose file");
        Button submitButton = new Button("Submit");

        instructionLabel.setPrefSize(BUTTON_WIDTH * 4 + 5*3, BUTTON_HEIGHT);
        instructionLabel.setAlignment(Pos.CENTER);

        fileLabel.setPrefSize(BUTTON_WIDTH * 6 + 4*3, BUTTON_HEIGHT);
        fileLabel.setAlignment(Pos.CENTER);

        fileButton.setPrefSize(BUTTON_WIDTH * 2, BUTTON_HEIGHT);
        fileButton.setAlignment(Pos.CENTER);

        submitButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        submitButton.setAlignment(Pos.CENTER);

        overallPane.setVgap(5);
        overallPane.setHgap(5);
        overallPane.setPadding(new Insets(5));
        overallPane.setAlignment(Pos.CENTER);


        fileButton.setOnAction(e -> {
            File file = Dialogs.promptForFile("Choose Input File", null, "CSV", new String[]{"csv"});
            fileLabel.setText(file.toString());
        });

        submitButton.setOnAction(e -> {
            dialog.close();
        });

        overallPane.add(instructionLabel, 0, 0, 4, 1);
        overallPane.add(fileButton, 4, 0, 2, 1);
        overallPane.add(fileLabel, 0, 1, 6, 1);
        overallPane.add(submitButton, 0, 2, 6, 1);


        Scene scene = new Scene(overallPane);
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
