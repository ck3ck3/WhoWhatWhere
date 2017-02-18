package whowhatwhere.controller.utilities;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import whowhatwhere.controller.GUIController;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;

public class QuickPingController implements Initializable
{
	@FXML
	private ComboBox<String> comboIPToPing;
	@FXML
	private Button btnConfigureHotkey;
	@FXML
	private Label labelCurrentHotkey;
	@FXML
	private CheckBox chkboxHotkey;
	@FXML
	private AnchorPane paneHotkey;
	@FXML
	private ComboBox<OutputMethod> comboOutputMethod;
	@FXML
	private AnchorPane paneAllButHotkeyChkbox;
	@FXML
	private Label labelIP;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		comboOutputMethod.setItems(FXCollections.observableArrayList(OutputMethod.values()));
		
		GUIController.setTooltipGraphic(labelIP);
		labelIP.setTooltip(new Tooltip("IP or hostname to ping when the hotkey is pressed"));
		
		GUIController.setConfigureHotkeyGraphic(btnConfigureHotkey);
	}
	
	public ComboBox<String> getComboToPing()
	{
		return comboIPToPing;
	}
	
	public ComboBox<OutputMethod> getComboOutputMethod()
	{
		return comboOutputMethod;
	}

	public Button getBtnConfigureHotkey()
	{
		return btnConfigureHotkey;
	}

	public Label getLabelCurrentHotkey()
	{
		return labelCurrentHotkey;
	}

	public CheckBox getChkboxHotkey()
	{
		return chkboxHotkey;
	}

	public AnchorPane getPaneHotkey()
	{
		return paneHotkey;
	}
	
	public AnchorPane getPaneAllButHotkeyChkbox()
	{
		return paneAllButHotkeyChkbox;
	}
}
