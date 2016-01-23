package mostusedips.model.hotkey;

import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class HotkeyManager implements NativeKeyListener
{
	HashMap<String, HotkeyConfiguration> hotkeyMap = new HashMap<String, HotkeyConfiguration>();

	private boolean isKeySelection = false;
	private String hotkeySelectionID; //value only matters when isKeySelection is true
	private int lastKeycode;

	private final static Logger logger = Logger.getLogger(HotkeyManager.class.getPackage().getName());

	public HotkeyManager()
	{
		init();

		Logger libLogger = LogManager.getLogManager().getLogger("org.jnativehook");
		libLogger.setLevel(Level.WARNING);

		Handler[] handlers = logger.getHandlers();
		for (Handler handler : handlers)
			libLogger.addHandler(handler);
	}

	public void init()
	{
		try
		{
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex)
		{
			logger.log(Level.SEVERE, "There was a problem registering the native hook.", ex);
		}

		GlobalScreen.addNativeKeyListener(this);
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e)
	{

		if (hotkeyMap.isEmpty() && !isKeySelection) //not for us
			return;

		int modifiers = e.getModifiers();
		HotkeyConfiguration hotkeyConfig;

		if (isKeySelection)
		{
			hotkeyConfig = hotkeyMap.get(hotkeySelectionID);
			hotkeyConfig.setHotkey(lastKeycode);
			hotkeyConfig.setModifiers(modifiers);
		}
		else
		{
			String id = hotkeyToString(modifiers, lastKeycode);
			hotkeyConfig = hotkeyMap.get(id);
			if (hotkeyConfig == null) //not a hotkey
				return;
		}

		hotkeyConfig.getExecutor().keyPressed(modifiers, lastKeycode, isKeySelection);

		isKeySelection = false;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e)
	{
		lastKeycode = e.getKeyCode();
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e)
	{
	}

	public void cleanup()
	{
		try
		{
			GlobalScreen.removeNativeKeyListener(this);
			GlobalScreen.unregisterNativeHook();
		}
		catch (NativeHookException ex)
		{
			logger.log(Level.SEVERE, "There was a problem unregistering the native hook.", ex);
		}
	}

	public boolean isKeySelection()
	{
		return isKeySelection;
	}

	public void setKeySelection(String hotkeyID, boolean isKeySelection)
	{
		if (this.isKeySelection && isKeySelection)
			throw new IllegalStateException("A hotkey selection is already taking place. You can use only one hotkey selection at a time.");

		this.isKeySelection = isKeySelection;
		hotkeySelectionID = hotkeyID;
	}

	/**
	 * 
	 * @param executer
	 * @param modifiers
	 * @param hotkey
	 * @return a string representing this hotkey, also to be used as an
	 *         identifier for this hotkey with the HotkeyManager instance.
	 */
	public String addHotkey(HotkeyExecuter executer, int modifiers, int hotkey)
	{
		if (isHotkeyTaken(modifiers, hotkey))
			throw new IllegalArgumentException("This key combination is already assigned to a different hotkey");

		HotkeyConfiguration hotkeyConfig = new HotkeyConfiguration(executer, modifiers, hotkey);
		String id = hotkeyToString(modifiers, hotkey);

		hotkeyMap.put(id, hotkeyConfig);

		return id;
	}

	/**
	 * 
	 * @param hotkeyID
	 *            - old hotkey ID
	 * @param modifiers
	 * @param hotkey
	 * @return new hotkey ID
	 */
	public String modifyHotkey(String hotkeyID, int modifiers, int hotkey)
	{
		HotkeyConfiguration hotkeyConfig = hotkeyMap.get(hotkeyID);

		if (hotkeyConfig == null)
			throw new IllegalArgumentException("Invalid hotkey ID");

		if (isHotkeyTaken(modifiers, hotkey))
			throw new IllegalArgumentException("This key combination is already assigned to a hotkey");

		HotkeyExecuter executer = hotkeyConfig.getExecutor();
		hotkeyMap.remove(hotkeyID);
		return addHotkey(executer, modifiers, hotkey);
	}
	
	public void removeHotkey(String hotkeyID)
	{
		hotkeyMap.remove(hotkeyID);
	}

	private boolean isHotkeyTaken(int modifiers, int hotkey)
	{
		return hotkeyMap.containsKey(hotkeyToString(modifiers, hotkey));
	}

	public static String hotkeyToString(int modifiers, int hotkey)
	{
		String keyText = NativeKeyEvent.getKeyText(hotkey);
		String modifiersText = NativeKeyEvent.getModifiersText(modifiers);

		return (modifiersText.isEmpty() ? "" : modifiersText + "+") + keyText;
	}

	public int getHotkeyModifiers(String hotkeyID)
	{
		HotkeyConfiguration config = hotkeyMap.get(hotkeyID);

		if (config == null)
			throw new IllegalArgumentException("Invalid hotkey ID");

		return config.getModifiers();
	}

	public int getHotkeyKeycode(String hotkeyID)
	{
		HotkeyConfiguration config = hotkeyMap.get(hotkeyID);

		if (config == null)
			throw new IllegalArgumentException("Invalid hotkey ID");

		return config.getHotkey();
	}
}
