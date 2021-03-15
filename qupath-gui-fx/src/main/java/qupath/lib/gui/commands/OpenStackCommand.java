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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;

/**
 * Command to prompt the user to open a single image that could be a stack.
 *
 * @author Jaedyn Ward
 *
 */
public class OpenStackCommand implements Runnable {

	private QuPathGUI qupath;
	private QuPathViewer viewer;

	private Stage dialog;


	/**
	 * Constructor.
	 * @param qupath
	 */
	public OpenStackCommand(final QuPathGUI qupath) {
		this.qupath = qupath;
		this.viewer = qupath.getViewer();
	}

	protected Stage createDialog() {

		Stage dialog = new Stage();
		dialog.initOwner(qupath.getStage());
		dialog.setTitle("Open Stack");
		Pane overallPane = new Pane();

		Scene scene = new Scene(overallPane, 300, 300);
		dialog.setScene(scene);
		dialog.setMinWidth(300);
		dialog.setMinHeight(300);
		dialog.setMaxWidth(300);
		dialog.setMaxHeight(300);

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
