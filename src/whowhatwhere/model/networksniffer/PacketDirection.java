package whowhatwhere.model.networksniffer;

import org.apache.commons.collections.bidimap.TreeBidiMap;

public enum PacketDirection
{
	ANY, Incoming, Outgoing;
	
	private static TreeBidiMap bidiMap = new TreeBidiMap();
	
	static
	{
		bidiMap.put(ANY, "ANY");
		bidiMap.put(Incoming, "Incoming");
		bidiMap.put(Outgoing, "Outgoing");
	}
	
	public static String[] getValuesAsStrings()
	{
		String[] directions = {(String) bidiMap.get(ANY), (String) bidiMap.get(Incoming), (String) bidiMap.get(Outgoing)};
		
		return directions;		
	}
	
	public static PacketDirection stringToEnum(String str)
	{
		return (PacketDirection) bidiMap.getKey(str);
	}
	
	public static String enumToString(PacketDirection packetDirection)
	{
		return (String) bidiMap.get(packetDirection);
	}
}
