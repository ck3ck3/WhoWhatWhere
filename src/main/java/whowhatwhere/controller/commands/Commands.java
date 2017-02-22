/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
