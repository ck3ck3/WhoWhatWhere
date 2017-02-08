package whowhatwhere.model.networksniffer.watchdog;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

public enum OutputMethod
{
	TTS, POPUP, TTS_AND_POPUP;
	
	private static TreeBidiMap<OutputMethod, String> bidiMap = new TreeBidiMap<>();
	
	static
	{
		bidiMap.put(TTS, "Speaking with text-to-speech");
		bidiMap.put(POPUP, "Showing a pop-up message");
		bidiMap.put(TTS_AND_POPUP, "Saying with text-to-speech and showing a pop-up message");
	}
	
	public static String[] getValuesAsStrings()
	{
		String[] methods = {bidiMap.get(TTS), bidiMap.get(POPUP), bidiMap.get(TTS_AND_POPUP)};
		
		return methods;
	}
	
	public static OutputMethod stringToEnum(String str)
	{
		return bidiMap.getKey(str);
	}
	
	@Override
	public String toString()
	{
		return bidiMap.get(this);
	}
}
