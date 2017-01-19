package whowhatwhere.model.networksniffer.watchdog;

import org.apache.commons.collections.bidimap.TreeBidiMap;

public enum OutputMethod
{
	TTS, POPUP, BOTH;
	
	private static TreeBidiMap bidiMap = new TreeBidiMap();
	
	static
	{
		bidiMap.put(TTS, "Say with text-to-speech");
		bidiMap.put(POPUP, "Show a pop-up message");
		bidiMap.put(BOTH, "Say with TTS and show pop-up");
	}
	
	public static String[] getChoice()
	{
		String[] methods = {(String) bidiMap.get(TTS), (String) bidiMap.get(POPUP), (String) bidiMap.get(BOTH)};
		
		return methods;
	}
	
	public static OutputMethod stringToEnum(String str)
	{
		return (OutputMethod) bidiMap.getKey(str);
	}
}
