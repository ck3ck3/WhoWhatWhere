package whowhatwhere.controller.utilities;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class PingToSpeechController
{
	@FXML
	private ComboBox<String> comboPTSipToPing;
	@FXML
	private Button btnPTSConfigureHotkey;
	@FXML
	private Label labelPTSCurrentHotkey;
	@FXML
	private CheckBox chkboxPTSHotkey;
	@FXML
	private AnchorPane panePTSHotkey;
	
	
	public ComboBox<String> getComboPTSipToPing()
	{
		return comboPTSipToPing;
	}

	public Button getBtnPTSConfigureHotkey()
	{
		return btnPTSConfigureHotkey;
	}

	public Label getLabelPTSCurrentHotkey()
	{
		return labelPTSCurrentHotkey;
	}

	public CheckBox getChkboxPTSHotkey()
	{
		return chkboxPTSHotkey;
	}

	public AnchorPane getPanePTSHotkey()
	{
		return panePTSHotkey;
	}

}
