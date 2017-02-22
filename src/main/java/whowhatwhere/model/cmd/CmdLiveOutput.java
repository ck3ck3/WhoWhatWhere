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
package whowhatwhere.model.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CmdLiveOutput
{
	private String command;
	private LiveOutputListener outputListener;
	private Thread commandThread;
	private static final Logger logger = Logger.getLogger(CmdLiveOutput.class.getPackage().getName());

	public CmdLiveOutput()
	{
	}

	public CmdLiveOutput(String command)
	{
		this(command, null);
	}

	public CmdLiveOutput(String command, LiveOutputListener listener)
	{
		this.command = command;
		outputListener = listener;
	}

	public void runCommand()
	{
		if (command == null)
			return;

		commandThread = new Thread(() ->
		{
			Process process;
			try
			{
				process = Runtime.getRuntime().exec(command);
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;

				while ((line = inputStream.readLine()) != null && !commandThread.isInterrupted())
					if (outputListener != null)
						outputListener.lineReady(line);
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "Unable to execute command " + command, e);
			}
			finally
			{
				if (outputListener != null)
					outputListener.endOfOutput();
			}

		});
		
		commandThread.start();
	}
	
	public void stopCommand()
	{
		commandThread.interrupt();
	}

	public LiveOutputListener getOutputListener()
	{
		return outputListener;
	}

	public void setOutputListener(LiveOutputListener outputListener)
	{
		this.outputListener = outputListener;
	}

	public String getCommand()
	{
		return command;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

}
