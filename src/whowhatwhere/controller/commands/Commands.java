package whowhatwhere.controller.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.Stage;
import whowhatwhere.controller.commands.ping.PingCommandScreen;
import whowhatwhere.controller.commands.trace.TraceCommandScreen;

public class Commands
{
	private final static Logger logger = Logger.getLogger(Commands.class.getPackage().getName());
	
	/**
	 * @param ip - ip to ping
	 * @param stage - stage to return to after this screen is closed
	 */
	public static void pingCommand(String ip, Stage stage)
	{
		PingCommandScreen cmdScreen;

		try
		{
			cmdScreen = new PingCommandScreen(stage, stage.getScene(), ip);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load Ping (command) screen", e);
			return;
		}

		cmdScreen.showScreenOnNewStage("Pinging " + ip, null, cmdScreen.getCloseButton());
		cmdScreen.runCommand();
	}

	/**
	 * @param ip - ip to ping
	 * @param stage - stage to return to after this screen is closed
	 */
	public static void traceCommand(String ip, Stage stage)
	{
		TraceCommandScreen cmdScreen;

		try
		{
			cmdScreen = new TraceCommandScreen(stage, stage.getScene(), ip);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load Trace (command) screen", e);
			return;
		}

		cmdScreen.showScreenOnNewStage("Tracing " + ip, null, cmdScreen.getCloseButton());
	}
}
