package whowhatwhere.controller.commands.ping;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.stage.Stage;
import whowhatwhere.controller.commands.CommandScreen;

public class PingCommandScreen extends CommandScreen
{
	public PingCommandScreen(Stage stage, Scene scene, String ip) throws IOException
	{
		this(stage, scene, ip, "");
	}

	public PingCommandScreen(Stage stage, Scene scene, String ip, String parameters) throws IOException
	{
		super(stage, scene);
		
		setStageOnShowing(windowEvent ->
		{
			Stage currentStage = (Stage) getTextArea().getScene().getWindow();
			
			currentStage.setWidth(400);
			currentStage.setHeight(300);	
		});

		setCommandStr("ping " + parameters + " " + ip);
	}
}
