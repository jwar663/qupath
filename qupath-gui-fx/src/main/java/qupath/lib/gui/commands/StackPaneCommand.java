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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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


	/**
	 * Constructor.
	 * @param qupath
	 */
	//public StackPaneCommand(final QuPathGUI qupath, List<BufferedImage> images) {
		public StackPaneCommand(final QuPathGUI qupath) {
		this.qupath = qupath;
		//this.images = images;
	}

	protected String getImageLabel(List<BufferedImage> images, int indexOf) {
		return (indexOf + 1) + "/" + images.size() + "; " + images.get(indexOf).getWidth()
				+ "x" + images.get(indexOf).getHeight() + " pixels; " + "-insert image type-" + "; " + "-insert image size-";
	}

	protected Stage createDialog() {

		List<BufferedImage> images = new ArrayList<>();

		try {
			images.add(ImageIO.read(new File("D:\\Desktop\\01_albedo.jpg")));
			images.add(ImageIO.read(new File("D:\\Desktop\\02_albedo.jpg")));
			images.add(ImageIO.read(new File("D:\\Desktop\\03_albedo.jpg")));
			images.add(ImageIO.read(new File("D:\\Desktop\\04_albedo.jpg")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//project = qupath.getProject();
		Stage dialog = new Stage();
		String imageLabel = getImageLabel(images, 0);
		dialog.initOwner(qupath.getStage());
		dialog.setTitle("Stack Viewer");
		BorderPane overallPane = new BorderPane();


		Pane labelPane = new Pane();

		Label label = new Label(imageLabel);

		labelPane.getChildren().add(label);

		Pane imagePane = new Pane();
		ScrollBar scroll = new ScrollBar();
		scroll.setMin(0);
		scroll.setMax(images.size() - 1);
		scroll.setValue(0);
		scroll.setBlockIncrement(1);
		scroll.setUnitIncrement(1);

		//may need to change imageview to be a qupath viewer
		ImageView imageView = new ImageView();

		imageView.setImage(SwingFXUtils.toFXImage(images.get(0), null));

		imagePane.getChildren().add(imageView);



		scroll.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
								Number old_val, Number new_val) {
				imageView.setImage(SwingFXUtils.toFXImage(images.get(new_val.intValue()), null));
				label.setText(getImageLabel(images,new_val.intValue()));
			}
		});

		overallPane.setTop(labelPane);
		overallPane.setCenter(imagePane);
		overallPane.setBottom(scroll);

		Scene scene = new Scene(overallPane, 1000, 1000);
		dialog.setScene(scene);
		dialog.setMinWidth(300);
		dialog.setMinHeight(300);
		dialog.setMaxWidth(1000);
		dialog.setMaxHeight(1000);

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
