package whowhatwhere.controller.utilities;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class TraceUtilityController
{
	@FXML
	private TextField textTrace;
	@FXML
	private Button btnTrace;
	
	public TextField getTextTrace()
	{
		return textTrace;
	}
	
	public Button getBtnTrace()
	{
		return btnTrace;
	}
}
