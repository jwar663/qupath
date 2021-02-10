package qupath.lib.gui.panes;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import qupath.lib.awt.common.BufferedImageTools;
import qupath.lib.color.ColorDeconvolutionHelper;
import qupath.lib.color.ColorDeconvolutionStains;
import qupath.lib.color.StainVector;
import qupath.lib.common.ColorTools;
import qupath.lib.common.ConcatChannelsABI;
import qupath.lib.common.GeneralTools;
import qupath.lib.display.ChannelDisplayInfo;
import qupath.lib.display.ImageDisplay;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.dialogs.ParameterPanelFX;
import qupath.lib.gui.prefs.PathPrefs;
import qupath.lib.images.ImageData;
import qupath.lib.images.ImageData.ImageType;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServerMetadata;
import qupath.lib.images.servers.PixelCalibration;
import qupath.lib.images.servers.ServerTools;
import qupath.lib.images.servers.WrappedBufferedImageServer;
import qupath.lib.plugins.parameters.ParameterList;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.RectangleROI;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QP;

/**
 * A panel used for showing the duplicate matrix of the image channels.
 *
 * @author Jaedyn Ward
 *
 */
public class DuplicateMatrixPane implements ChangeListener<ImageData<BufferedImage>>, PropertyChangeListener {

    final private static Logger logger = LoggerFactory.getLogger(ImageDetailsPane.class);

    private QuPathGUI qupath;

    private StackPane pane = new StackPane();

    private ListView<String> listAssociatedImages = new ListView<>();

    private ImageData<BufferedImage> imageData;

    /**
     * Constructor.
     * @param qupath QuPath instance
     */
    public DuplicateMatrixPane(final QuPathGUI qupath) {
        this.qupath = qupath;
        qupath.imageDataProperty().addListener(this);
        //TODO: setup pane

    }

    /**
     * Prompt the user to set the {@link ImageType} for the image.
     * @param imageData
     * @return
     */
    public static boolean promptToSetThresholdValue(ImageData<BufferedImage> imageData) {
        if(ConcatChannelsABI.isExcessChannels(imageData.getServer().nChannels())) {
            return true;
        }
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void changed(ObservableValue<? extends ImageData<BufferedImage>> observable, ImageData<BufferedImage> oldValue, ImageData<BufferedImage> newValue) {

    }
}