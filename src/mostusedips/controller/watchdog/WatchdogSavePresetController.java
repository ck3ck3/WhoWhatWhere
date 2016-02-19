package mostusedips.controller.watchdog;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class WatchdogSavePresetController
{
	@FXML
	private TextField textFilename;
	@FXML
	private Button btnSave;
	
	public TextField getTextFilename()
	{
		return textFilename;
	}

	public Button getBtnSave()
	{
		return btnSave;
	}
}
