package mostusedips.model.ipsniffer;

import java.util.ArrayList;

public class AppearanceCounterResults
{
	private AppearanceCounterPacketHandler packetHandler;
	
	public AppearanceCounterResults(AppearanceCounterPacketHandler packetHandler)
	{
		this.packetHandler = packetHandler;
	}
	
	public ArrayList<IpAppearancesCounter> getAppearanceCounterResults()
	{
		return packetHandler.getListOfIpAppearances();
	}
}
