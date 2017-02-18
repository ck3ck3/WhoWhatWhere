package whowhatwhere.controller.commands.trace;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import whowhatwhere.controller.GUIController;

public class VisualTraceController implements Initializable
{
	@FXML
	private ImageView imgView;
	@FXML
	private Button btnClose;
	@FXML
	private Label labelLoading;
	@FXML
	private Pane paneTraceInfo;
	@FXML
	private AnchorPane anchorPaneScreen;
	@FXML
	private SplitPane splitPane;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private StackPane paneStackColor;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		GUIController.setCommonGraphicOnLabeled(btnClose, GUIController.CommonGraphicImages.OK);
	}
	
	public ScrollPane getScrollPane()
	{
		return scrollPane;
	}

	public SplitPane getSplitPane()
	{
		return splitPane;
	}
	
	public AnchorPane getAnchorPaneScreen()
	{
		return anchorPaneScreen;
	}
	
	public Pane getPaneTraceInfo()
	{
		return paneTraceInfo;
	}
	
	public ImageView getImgView()
	{
		return imgView;
	}

	public Button getBtnClose()
	{
		return btnClose;
	}

	public Label getLoadingLabel()
	{
		return labelLoading;
	}

	public StackPane getPaneStackColor()
	{
		return paneStackColor;
	}
}
