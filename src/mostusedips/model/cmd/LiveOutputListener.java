package mostusedips.model.cmd;

public interface LiveOutputListener
{
	/**
	 * This method is called every time a CmdLiveOutput line is ready to be read
	 */
	public void lineReady(String line);

	/**
	 * This method is called when the output for a CmdLiveOutput is finished.
	 */
	public void endOfOutput();

}
