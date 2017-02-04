package whowhatwhere.model.networksniffer.watchdog;

import org.apache.commons.collections.bidimap.TreeBidiMap;

public enum OutputMethod
{
	TTS, POPUP, TTS_AND_POPUP;
	
	private static TreeBidiMap bidiMap = new TreeBidiMap();
	
	static
	{
		bidiMap.put(TTS, "Say with text-to-speech");
		bidiMap.put(POPUP, "Show a pop-up message");
		bidiMap.put(TTS_AND_POPUP, "Say with text-to-speech and show a pop-up message");
	}
	
	public static String[] getValuesAsStrings()
	{
		String[] methods = {(String) bidiMap.get(TTS), (String) bidiMap.get(POPUP), (String) bidiMap.get(TTS_AND_POPUP)};
		
		return methods;
	}
	
	public static OutputMethod stringToEnum(String str)
	{
		return (OutputMethod) bidiMap.getKey(str);
	}
	
	@Override
	public String toString()
	{
		return (String) bidiMap.get(this);
	}
}
