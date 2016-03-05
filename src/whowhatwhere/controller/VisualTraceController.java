package whowhatwhere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class VisualTraceController
{

	@FXML
	private ScrollPane scrollerChkboxes;
	@FXML
	private ImageView imgView;
	@FXML
	private Button btnClose;
	@FXML
	private VBox vboxChkboxes;
	@FXML
	Label labelLoading;

	public ScrollPane getScrollerChkboxes()
	{
		return scrollerChkboxes;
	}

	public ImageView getImgView()
	{
		return imgView;
	}

	public Button getBtnClose()
	{
		return btnClose;
	}

	public VBox getVboxChkboxes()
	{
		return vboxChkboxes;
	}

	public Label getLoadingLabel()
	{
		return labelLoading;
	}

}
