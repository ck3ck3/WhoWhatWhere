package whowhatwhere.model.networksniffer;

public enum PacketDirection
{
	ANY, Incoming, Outgoing;
	
	public static String[] getPacketDirectionStrings()
	{
		String[] directions = {"ANY", "Incoming", "Outgoing"};
		
		return directions;		
	}
}
