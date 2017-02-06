package whowhatwhere.controller.commands.trace;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ScrollPane;

public class VisualTraceController
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

}
