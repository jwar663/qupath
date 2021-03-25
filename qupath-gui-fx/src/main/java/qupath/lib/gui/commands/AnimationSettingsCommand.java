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


import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;

import java.io.File;
import java.io.IOException;


/**
 * Command to show a Duplicate Matrix widget to preview and decide which threshold
 * is the best to properly represent the image.
 *
 * @author Jaedyn Ward
 *
 */
public class AnimationSettingsCommand implements Runnable {

    private QuPathGUI qupath;

    private Stage dialog;

    private String oldStackFilePath;

    private boolean oldIsStack;

    private int oldDelay;

    private String newStackFilePath;

    private boolean newIsStack;

    private int newDelay;


    private double BASE_WIDTH = 100;
    private double BASE_HEIGHT = 25;

    private double PADDING = 5;

    private double OVERALL_WIDTH = ((BASE_WIDTH + PADDING) * 5) + PADDING;
    private double OVERALL_HEIGHT = (2 * (BASE_HEIGHT + PADDING)) + PADDING;


    /**
     * Constructor.
     * @param qupath
     */
    public AnimationSettingsCommand(final QuPathGUI qupath) {
        this.qupath = qupath;
    }

    protected Stage createDialog() throws IOException, NullPointerException {

        Stage dialog = new Stage();
        dialog.initOwner(qupath.getStage());
        dialog.setTitle("Animation Settings");

        oldStackFilePath = qupath.getStackFilePath();
        newStackFilePath = oldStackFilePath;

        oldIsStack = qupath.getIsStack();
        newIsStack = oldIsStack;

        oldDelay = qupath.getStackDelay();
        newDelay = oldDelay;

        GridPane overallPane = new GridPane();
        overallPane.setMaxSize(OVERALL_WIDTH - (PADDING * 2), OVERALL_HEIGHT - (PADDING * 2));
        overallPane.setMinSize(OVERALL_WIDTH - (PADDING * 2), OVERALL_HEIGHT - (PADDING * 2));
        overallPane.setPrefSize(OVERALL_WIDTH - (PADDING * 2), OVERALL_HEIGHT - (PADDING * 2));
        overallPane.setHgap(PADDING);
        overallPane.setVgap(PADDING);
        overallPane.setPadding(new Insets(PADDING));

        ToggleButton singleStackToggle = new ToggleButton("Single");
        GridPane.setHalignment(singleStackToggle, HPos.CENTER);
        GridPane.setValignment(singleStackToggle, VPos.CENTER);
        singleStackToggle.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        singleStackToggle.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        singleStackToggle.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        singleStackToggle.setOnAction(e -> {
            if(singleStackToggle.getText().equals("Single")) {
                singleStackToggle.setText("Stack");
                newIsStack = true;
            } else {
                singleStackToggle.setText("Single");
                newIsStack = false;
            }
        });

        Label delayLabel = new Label("Delay(ms)");
        GridPane.setHalignment(delayLabel, HPos.CENTER);
        GridPane.setValignment(delayLabel, VPos.CENTER);
        delayLabel.setAlignment(Pos.CENTER);
        delayLabel.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        delayLabel.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        delayLabel.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        TextField delayField = new TextField("250");
        GridPane.setHalignment(delayField, HPos.CENTER);
        GridPane.setValignment(delayField, VPos.CENTER);
        delayField.setAlignment(Pos.CENTER);
        delayField.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        delayField.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        delayField.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        Button chooseFolderButton = new Button("Choose Folder");
        GridPane.setHalignment(chooseFolderButton, HPos.CENTER);
        GridPane.setValignment(chooseFolderButton, VPos.CENTER);
        chooseFolderButton.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        chooseFolderButton.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        chooseFolderButton.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        Label exportLabel = new Label("Export to: " + oldStackFilePath);
        GridPane.setHalignment(exportLabel, HPos.LEFT);
        GridPane.setValignment(exportLabel, VPos.CENTER);
        exportLabel.setMaxSize(OVERALL_WIDTH - (PADDING * 3) - BASE_WIDTH, BASE_HEIGHT);
        exportLabel.setMinSize(OVERALL_WIDTH - (PADDING * 3) - BASE_WIDTH, BASE_HEIGHT);
        exportLabel.setPrefSize(OVERALL_WIDTH - (PADDING * 3) - BASE_WIDTH, BASE_HEIGHT);

        chooseFolderButton.setOnAction(e -> {
            //TODO: implement or connect a function to choose where to export file
            File filePath = Dialogs.promptForDirectory(null);
            newStackFilePath = filePath.toString();
            exportLabel.setText("Export to: " + filePath);
        });

        Button confirmButton = new Button("Confirm");
        GridPane.setHalignment(confirmButton, HPos.CENTER);
        GridPane.setValignment(confirmButton, VPos.CENTER);
        confirmButton.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        confirmButton.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        confirmButton.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        confirmButton.setOnAction(e -> {
            int delayFieldInt = oldDelay;
            try{
                delayFieldInt = Integer.parseInt(delayField.getText());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            newDelay = delayFieldInt;
            qupath.setIsStack(newIsStack);
            qupath.setStackDelay(newDelay);
            qupath.setStackFilePath(newStackFilePath);
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        GridPane.setHalignment(cancelButton, HPos.CENTER);
        GridPane.setValignment(cancelButton, VPos.CENTER);
        cancelButton.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        cancelButton.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        cancelButton.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        cancelButton.setOnAction(e -> {
            qupath.setIsStack(oldIsStack);
            qupath.setStackDelay(oldDelay);
            qupath.setStackFilePath(oldStackFilePath);
            exportLabel.setText("Export to: " + oldStackFilePath);
            dialog.close();
        });

        overallPane.add(singleStackToggle, 0, 1, 1, 1);
        overallPane.add(delayLabel, 1, 1, 1, 1);
        overallPane.add(delayField, 2, 1, 1, 1);
        overallPane.add(chooseFolderButton, 0, 0, 1, 1);
        overallPane.add(exportLabel, 1, 0, 4, 1);
        overallPane.add(confirmButton, 3, 1, 1, 1);
        overallPane.add(cancelButton, 4, 1, 1, 1);

        Scene scene = new Scene(overallPane, OVERALL_WIDTH, OVERALL_HEIGHT);
        dialog.setScene(scene);
//        dialog.setMinWidth(OVERALL_WIDTH);
//        dialog.setMinHeight(OVERALL_HEIGHT);
//        dialog.setMaxWidth(OVERALL_WIDTH);
//        dialog.setMaxHeight(OVERALL_HEIGHT);

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
        dialog.showAndWait();
    }
}
