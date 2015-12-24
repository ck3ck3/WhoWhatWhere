package mostusedips.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import mostusedips.view.SecondaryFXMLScreen;

public class CmdGUIController extends SecondaryFXMLScreen
{

	@FXML
	private TextArea textAreaOutput;
	@FXML
	private Button btnClose;
	@FXML
	private HBox hboxBottom;

	public TextArea getTextAreaOutput()
	{
		return textAreaOutput;
	}

	public HBox getHboxBottom()
	{
		return hboxBottom;
	}

	public Button getBtnClose()
	{
		return btnClose;
	}

}
