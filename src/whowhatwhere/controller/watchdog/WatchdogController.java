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
	private CheckBox chkboxWatchdogHotkey;
	@FXML
	private AnchorPane paneWatchdogHotkeyConfig;
	@FXML
	private Button btnWatchdogConfigureHotkey;
	@FXML
	private Label labelWatchdogCurrHotkey;
	@FXML
	private Button btnWatchdogStart;
	@FXML
	private Button btnWatchdogStop;
	@FXML
	private Button btnWatchdogManageList;
	@FXML
	private Label labelWatchdogEntryCount;
	@FXML
	private RadioButton radioWatchdogStopAfterMatch;
	@FXML
	private ToggleGroup tglWatchdogStopOrContinue;
	@FXML
	private RadioButton radioWatchdogKeepLooking;
	@FXML
	private AnchorPane paneWatchdogCooldown;
	@FXML
	private NumberTextField numFieldWatchdogCooldown;
	@FXML
	private AnchorPane paneWatchdogConfig;
	
	
	public CheckBox getChkboxWatchdogHotkey()
	{
		return chkboxWatchdogHotkey;
	}

	public AnchorPane getPaneWatchdogHotkeyConfig()
	{
		return paneWatchdogHotkeyConfig;
	}

	public Button getBtnWatchdogConfigureHotkey()
	{
		return btnWatchdogConfigureHotkey;
	}

	public Label getLabelWatchdogCurrHotkey()
	{
		return labelWatchdogCurrHotkey;
	}

	public Button getBtnWatchdogStart()
	{
		return btnWatchdogStart;
	}

	public Button getBtnWatchdogStop()
	{
		return btnWatchdogStop;
	}

	public Button getBtnWatchdogManageList()
	{
		return btnWatchdogManageList;
	}

	public Label getLabelWatchdogEntryCount()
	{
		return labelWatchdogEntryCount;
	}
	
	public RadioButton getRadioWatchdogStopAfterMatch()
	{
		return radioWatchdogStopAfterMatch;
	}

	public RadioButton getRadioWatchdogKeepLooking()
	{
		return radioWatchdogKeepLooking;
	}

	public AnchorPane getPaneWatchdogCooldown()
	{
		return paneWatchdogCooldown;
	}

	public NumberTextField getNumFieldWatchdogCooldown()
	{
		return numFieldWatchdogCooldown;
	}

	public AnchorPane getPaneWatchdogConfig()
	{
		return paneWatchdogConfig;
	}
}
