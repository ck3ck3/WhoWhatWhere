package whowhatwhere.controller.commands;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import whowhatwhere.controller.GUIController;

public class CmdGUIController implements Initializable
{
	@FXML
	private TextArea textAreaOutput;
	@FXML
	private Button btnClose;
	@FXML
	private HBox hboxBottom;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		GUIController.setCommonGraphicOnLabeled(btnClose, GUIController.CommonGraphicImages.OK);
	}

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
