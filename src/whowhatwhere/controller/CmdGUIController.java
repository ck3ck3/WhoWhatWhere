package whowhatwhere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

public class CmdGUIController
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
