package whowhatwhere.model.networksniffer.appearancecounter;

import java.util.List;

public class AppearanceCounterResults
{
	private AppearanceCounterPacketHandler packetHandler;
	
	public AppearanceCounterResults(AppearanceCounterPacketHandler packetHandler)
	{
		this.packetHandler = packetHandler;
	}
	
	public List<IpAppearancesCounter> getAppearanceCounterResults()
	{
		return packetHandler.getListOfIpAppearances();
	}
}
