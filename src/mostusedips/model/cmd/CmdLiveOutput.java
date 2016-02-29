package mostusedips.model.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CmdLiveOutput
{
	private String command;
	private LiveOutputListener outputListener;
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

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Process process;
				try
				{
					process = Runtime.getRuntime().exec(command);
					BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;

					while ((line = inputStream.readLine()) != null)
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

			}
		}).start();

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
