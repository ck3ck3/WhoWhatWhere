package whowhatwhere.controller.watchdog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import numbertextfield.NumberTextField;

public class WatchdogController
{
	@FXML
	private CheckBox chkboxHotkey;
	@FXML
	private AnchorPane paneHotkeyConfig;
	@FXML
	private Button btnConfigureHotkey;
	@FXML
	private Label labelCurrHotkey;
	@FXML
	private Button btnStart;
	@FXML
	private Button btnStop;
	@FXML
	private Button btnManageList;
	@FXML
	private Label labelEntryCount;
	@FXML
	private RadioButton radioStopAfterMatch;
	@FXML
	private ToggleGroup tglStopOrContinue;
	@FXML
	private RadioButton radioKeepLooking;
	@FXML
	private AnchorPane paneCooldown;
	@FXML
	private NumberTextField numFieldCooldown;
	@FXML
	private AnchorPane paneConfig;
	
	
	public CheckBox getChkboxHotkey()
	{
		return chkboxHotkey;
	}

	public AnchorPane getPaneHotkeyConfig()
	{
		return paneHotkeyConfig;
	}

	public Button getBtnConfigureHotkey()
	{
		return btnConfigureHotkey;
	}

	public Label getLabelCurrHotkey()
	{
		return labelCurrHotkey;
	}

	public Button getBtnStart()
	{
		return btnStart;
	}

	public Button getBtnStop()
	{
		return btnStop;
	}

	public Button getBtnManageList()
	{
		return btnManageList;
	}

	public Label getLabelEntryCount()
	{
		return labelEntryCount;
	}
	
	public RadioButton getRadioStopAfterMatch()
	{
		return radioStopAfterMatch;
	}

	public RadioButton getRadioKeepLooking()
	{
		return radioKeepLooking;
	}

	public AnchorPane getPaneCooldown()
	{
		return paneCooldown;
	}

	public NumberTextField getNumFieldCooldown()
	{
		return numFieldCooldown;
	}

	public AnchorPane getPaneConfig()
	{
		return paneConfig;
	}
}
