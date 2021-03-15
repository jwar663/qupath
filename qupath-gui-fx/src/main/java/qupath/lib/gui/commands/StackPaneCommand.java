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

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.projects.Project;

import java.awt.image.BufferedImage;

/**
 * Command to show the pane of the how to interact with a stack of images.
 *
 * @author Jaedyn Ward
 *
 */
public class StackPaneCommand implements Runnable {

	private QuPathGUI qupath;
	private QuPathViewer viewer;

	private Stage dialog;

	private Project<BufferedImage> project;


	/**
	 * Constructor.
	 * @param qupath
	 */
	public StackPaneCommand(final QuPathGUI qupath) {
		this.qupath = qupath;
		this.viewer = qupath.getViewer();
	}

	protected Stage createDialog() {

		project = qupath.getProject();
		Stage dialog = new Stage();

		if(project.isEmpty()) {
			dialog.close();
		} else {
			dialog.initOwner(qupath.getStage());
			dialog.setTitle("Image Viewer");
			BorderPane overallPane = new BorderPane();



			Scene scene = new Scene(overallPane, 300, 300);
			dialog.setScene(scene);
			dialog.setMinWidth(300);
			dialog.setMinHeight(300);
			dialog.setMaxWidth(300);
			dialog.setMaxHeight(300);
		}
		return dialog;
	}

	@Override
	public void run() {
		if (dialog == null) {
				dialog = createDialog();
		}
		dialog.show();
	}
}
