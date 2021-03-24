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

    private String stackFilePath;

    private boolean isStack;

    private int delay;


    private double BASE_WIDTH = 60;
    private double BASE_HEIGHT = 25;

    private double PADDING = 5;

    private double OVERALL_WIDTH = ((BASE_WIDTH + PADDING) * 2) + PADDING;
    private double OVERALL_HEIGHT = (5 * (BASE_HEIGHT + PADDING)) + PADDING;


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

        stackFilePath = qupath.getStackFilePath();

        isStack = qupath.getIsStack();

        delay = qupath.getStackDelay();

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
        singleStackToggle.setMaxSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);
        singleStackToggle.setMinSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);
        singleStackToggle.setPrefSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);

        singleStackToggle.setOnAction(e -> {
            if(singleStackToggle.getText().equals("Single")) {
                singleStackToggle.setText("Stack");
                qupath.setIsStack(true);
            } else {
                singleStackToggle.setText("Single");
                qupath.setIsStack(false);
            }
        });

        Label delayLabel = new Label("Delay(ms)");
        GridPane.setHalignment(delayLabel, HPos.CENTER);
        GridPane.setValignment(delayLabel, VPos.CENTER);
        delayLabel.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        delayLabel.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        delayLabel.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        TextField delayField = new TextField("250");
        GridPane.setHalignment(delayField, HPos.CENTER);
        GridPane.setValignment(delayField, VPos.CENTER);
        delayField.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        delayField.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        delayField.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        Button chooseFolderButton = new Button("Choose Folder");
        GridPane.setHalignment(chooseFolderButton, HPos.CENTER);
        GridPane.setValignment(chooseFolderButton, VPos.CENTER);
        chooseFolderButton.setMaxSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);
        chooseFolderButton.setMinSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);
        chooseFolderButton.setPrefSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);

        Label exportLabel = new Label("Export to: " + stackFilePath);
        GridPane.setHalignment(exportLabel, HPos.CENTER);
        GridPane.setValignment(exportLabel, VPos.CENTER);
        exportLabel.setMaxSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);
        exportLabel.setMinSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);
        exportLabel.setPrefSize(OVERALL_WIDTH - (PADDING*2), BASE_HEIGHT);

        chooseFolderButton.setOnAction(e -> {
            //TODO: implement or connect a function to choose where to export file
            String filePath = "get file path command here";
            qupath.setStackFilePath(filePath);
            exportLabel.setText("Export to: " + filePath);
        });

        Button confirmButton = new Button("Confirm");
        GridPane.setHalignment(confirmButton, HPos.CENTER);
        GridPane.setValignment(confirmButton, VPos.CENTER);
        confirmButton.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        confirmButton.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        confirmButton.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        confirmButton.setOnAction(e -> {
            int delayFieldInt = delay;
            try{
                delayFieldInt = Integer.parseInt(delayField.getText());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            qupath.setStackDelay(delayFieldInt);
        });

        Button cancelButton = new Button("Cancel");
        GridPane.setHalignment(cancelButton, HPos.CENTER);
        GridPane.setValignment(cancelButton, VPos.CENTER);
        cancelButton.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        cancelButton.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        cancelButton.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        cancelButton.setOnAction(e -> {
            qupath.setIsStack(isStack);
            qupath.setStackDelay(delay);
            qupath.setStackFilePath(stackFilePath);
            exportLabel.setText("Export to: " + stackFilePath);
            dialog.close();
        });

        overallPane.add(singleStackToggle, 0, 0, 2, 1);
        overallPane.add(delayLabel, 0, 1, 1, 1);
        overallPane.add(delayField, 1, 1, 1, 1);
        overallPane.add(chooseFolderButton, 0, 2, 2, 1);
        overallPane.add(exportLabel, 0, 3, 2, 1);
        overallPane.add(confirmButton, 0, 4, 1, 1);
        overallPane.add(cancelButton, 1, 4, 1, 1);

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
