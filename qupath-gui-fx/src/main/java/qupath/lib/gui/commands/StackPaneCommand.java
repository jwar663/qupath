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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Open the pane of the how to interact with a stack of images.
 *
 * @author Jaedyn Ward
 *
 */
public class StackPaneCommand implements Runnable {

	private QuPathGUI qupath;

	private Stage dialog;

	private List<BufferedImage> images;


	/**
	 * Constructor.
	 * @param qupath
	 */
	public StackPaneCommand(final QuPathGUI qupath, List<BufferedImage> images) {
		this.qupath = qupath;
		this.images = images;
	}

	protected String getImageLabel(BufferedImage currentImage) {
		return images.indexOf(currentImage) + "/" + images.size() + "; " + currentImage.getWidth()
				+ "x" + currentImage.getHeight() + " pixels; " + "-insert image type-" + "; " + "-insert image size-";
	}

	protected Stage createDialog() {

		//project = qupath.getProject();
		Stage dialog = new Stage();
		String imageLabel = getImageLabel(images.get(0));
		dialog.initOwner(qupath.getStage());
		dialog.setTitle("Stack Viewer");
		BorderPane overallPane = new BorderPane();

		Pane labelPane = new Pane();

		Label label = new Label(imageLabel);

		labelPane.getChildren().add(label);

		Pane imagePane = new Pane();
		ScrollBar scroll = new ScrollBar();
		scroll.setMin(0);
		scroll.setMax(images.size());
		scroll.setValue(0);

		ImageView imageView = new ImageView();

		imageView.setImage(SwingFXUtils.toFXImage(images.get(0), null));

		imagePane.getChildren().add(imageView);



		scroll.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
								Number old_val, Number new_val) {
				imageView.setImage(SwingFXUtils.toFXImage(images.get(new_val.intValue()), null));
				label.setText(getImageLabel(images.get(new_val.intValue())));
			}
		});


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
