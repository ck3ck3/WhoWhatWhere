package whowhatwhere.controller.utilities;

import java.util.Properties;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import whowhatwhere.controller.GUIController;

public class TraceUtilityUI
{
	private GUIController guiController;
	private TraceUtilityController controller;
	private TextField textTrace;
	private Button btnTrace;
	
	private final static String propsTraceAddress = "traceAddress";
	
	public TraceUtilityUI(GUIController guiController)
	{
		this.guiController = guiController;
		controller = guiController.getTracePaneController();
		textTrace = controller.getTextTrace();
		btnTrace = controller.getBtnTrace();
		
		btnTrace.setOnAction(event -> this.guiController.traceCommand(textTrace.getText()));

		textTrace.setOnKeyPressed(ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				btnTrace.fire();
		});		
	}
	
	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsTraceAddress, textTrace.getText());
	}
	
	public void loadLastRunConfig(Properties props)
	{
		textTrace.setText(props.getProperty(propsTraceAddress));
	}
}
