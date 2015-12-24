package mostusedips.controller.commands.ping;

import javafx.scene.Scene;
import javafx.stage.Stage;
import mostusedips.controller.commands.CommandScreen;

public class PingCommandScreen extends CommandScreen
{

	public PingCommandScreen(Stage stage, Scene scene, String ip)
	{
		this(stage, scene, ip, "");
	}

	public PingCommandScreen(Stage stage, Scene scene, String ip, String parameters)
	{
		super(stage, scene);

		setCommandStr("ping " + parameters + " " + ip);
	}

}
