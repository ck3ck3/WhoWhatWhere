package whowhatwhere.controller.utilities;

import java.util.Properties;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.controller.commands.Commands;

public class TraceUtilityUI implements LoadAndSaveSettings
{
	private GUIController guiController;
	private TraceUtilityController controller;
	private TextField textTrace;
	private Button btnTrace;
	
	private final static String propsTraceAddress = "traceAddress";
	
	public TraceUtilityUI(GUIController guiController)
	{
		this.guiController = guiController;
		this.guiController.registerForSettingsHandler(this);
		controller = guiController.getTracePaneController();
		textTrace = controller.getTextTrace();
		btnTrace = controller.getBtnTrace();
		
		btnTrace.setOnAction(event -> Commands.traceCommand(textTrace.getText(), guiController.getStage()));

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
