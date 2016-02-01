package mostusedips.controller;

import mostusedips.view.SecondaryFXMLScreen;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class WatchdogManageListScreen extends SecondaryFXMLScreen
{

	@FXML
	private Button btnAddRow;
	@FXML
	private Button btnEditRow;
	@FXML
	private Button btnRemoveRow;
	@FXML
	private Button btnClose;
	@FXML
	private Button btnLoadPreset;
	@FXML
	private Button btnSavePreset;

	public Button getBtnClose()
	{
		return btnClose;
	}
	
	public void showScreen()
	{
		System.out.println("watchdog manage list screen");
	}
}
