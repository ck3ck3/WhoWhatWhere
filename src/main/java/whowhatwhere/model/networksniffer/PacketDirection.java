package whowhatwhere.model.networksniffer;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

public enum PacketDirection
{
	Incoming, Outgoing;
	
	private static TreeBidiMap<PacketDirection, String> bidiMap = new TreeBidiMap<>();
	
	static
	{
		bidiMap.put	(Incoming, "Incoming");
		bidiMap.put(Outgoing, "Outgoing");
	}
	
	public static String[] getValuesAsStrings()
	{
		String[] directions = {bidiMap.get(Incoming), bidiMap.get(Outgoing)};
		
		return directions;		
	}
	
	public static PacketDirection stringToEnum(String str)
	{
		return bidiMap.getKey(str);
	}
	
	@Override
	public String toString()
	{
		return bidiMap.get(this);
	}
}