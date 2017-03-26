/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.model.hotkey;

import java.util.HashMap;
import java.util.Map;
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
	private Map<String, HotkeyConfiguration> hotkeyMap = new HashMap<>();
	private Map<String, String> hotkeyToID = new HashMap<>();

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

	private void init()
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
		}
		else
		{
			String hotkeyAsStr = hotkeyToString(modifiers, lastKeycode);
			String hotkeyID = hotkeyToID.get(hotkeyAsStr);
			hotkeyConfig = hotkeyMap.get(hotkeyID);
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

	public void setKeySelection(String hotkeyID, boolean isKeySelection) throws IllegalStateException
	{
		if (this.isKeySelection && isKeySelection)
			throw new IllegalStateException("A hotkey selection is already taking place. You can set only one hotkey selection at a time.");

		this.isKeySelection = isKeySelection;
		hotkeySelectionID = hotkeyID;
	}

	public void addHotkey(String hotkeyID, HotkeyExecuter executer, int modifiers, int hotkey) throws IllegalArgumentException
	{
		if (isHotkeyTaken(modifiers, hotkey))
			throw new IllegalArgumentException("This key combination is already assigned to a different hotkey");
		
		HotkeyConfiguration hotkeyConfig = new HotkeyConfiguration(executer, modifiers, hotkey);

		hotkeyMap.put(hotkeyID, hotkeyConfig);
		hotkeyToID.put(hotkeyToString(modifiers, hotkey), hotkeyID);
	}

	public void modifyHotkey(String hotkeyID, int modifiers, int hotkey) throws IllegalArgumentException
	{
		HotkeyConfiguration hotkeyConfig = hotkeyMap.get(hotkeyID);

		if (hotkeyConfig == null)
			throw new IllegalArgumentException("Invalid hotkey ID");

		if (isHotkeyTaken(modifiers, hotkey))
			throw new IllegalArgumentException("This key combination is already assigned to a hotkey");

		HotkeyExecuter executer = hotkeyConfig.getExecutor();
		
		hotkeyToID.remove(hotkeyToString(hotkeyConfig.getModifiers(), hotkeyConfig.getHotkey()));
		hotkeyToID.put(hotkeyToString(modifiers, hotkey), hotkeyID);
		hotkeyMap.replace(hotkeyID, new HotkeyConfiguration(executer, modifiers, hotkey));
	}
	
	public void removeHotkey(String hotkeyID)
	{
		HotkeyConfiguration hotkeyConfig = hotkeyMap.get(hotkeyID);
		
		if (hotkeyConfig != null)
			hotkeyToID.remove(hotkeyToString(hotkeyConfig.getModifiers(), hotkeyConfig.getHotkey()));
	}

	private boolean isHotkeyTaken(int modifiers, int hotkey)
	{
		return hotkeyToID.containsKey(hotkeyToString(modifiers, hotkey));
	}

	public static String hotkeyToString(int modifiers, int hotkey)
	{
		String keyText = NativeKeyEvent.getKeyText(hotkey);
		String modifiersText = NativeKeyEvent.getModifiersText(modifiers);
		
		if (keyText.equals("NumPad Separator")) //no one knows this key as separator
			keyText = "NumPad Del";

		return (modifiersText.isEmpty() ? "" : modifiersText + "+") + keyText;
	}

	public int getHotkeyModifiers(String hotkeyID) throws IllegalArgumentException
	{
		HotkeyConfiguration config = hotkeyMap.get(hotkeyID);

		if (config == null)
			throw new IllegalArgumentException("Invalid hotkey ID");

		return config.getModifiers();
	}

	public int getHotkeyKeycode(String hotkeyID) throws IllegalArgumentException
	{
		HotkeyConfiguration config = hotkeyMap.get(hotkeyID);

		if (config == null)
			throw new IllegalArgumentException("Invalid hotkey ID");

		return config.getHotkey();
	}
}
