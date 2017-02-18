package whowhatwhere.controller.utilities;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import whowhatwhere.controller.GUIController;

public class TraceUtilityController implements Initializable
{
	private final static String traceImageLocation = "/buttonGraphics/earth-16.png";
	
	@FXML
	private TextField textTrace;
	@FXML
	private Button btnTrace;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		GUIController.setGraphicForLabeledControl(btnTrace, traceImageLocation, ContentDisplay.LEFT);
	}
	
	public TextField getTextTrace()
	{
		return textTrace;
	}
	
	public Button getBtnTrace()
	{
		return btnTrace;
	}
}
