package mostusedips.model.hotkey;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import mostusedips.Main;

public class HotKeyManager implements NativeKeyListener
{
	private HotKeyExecuter executer;
	private Integer hotkey;
	private Integer modifiers;
	private boolean isKeySelection = false;
	private int lastKeycode;

	private final static Logger logger = Logger.getLogger(Main.getAppName());

	public HotKeyManager(HotKeyExecuter executer)
	{
		this(executer, null, null);
	}

	public HotKeyManager(HotKeyExecuter executer, Integer modifiers, Integer hotkey)
	{
		this.executer = executer;
		this.modifiers = modifiers;
		this.hotkey = hotkey;

		registerNativeHook();
	}

	public void registerNativeHook()
	{
		try
		{
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex)
		{
			logger.log(Level.SEVERE, "There was a problem registering the native hook.", ex);
		}

		Logger libLogger = LogManager.getLogManager().getLogger("org.jnativehook");
		libLogger.setLevel(Level.WARNING);
		Handler[] handlers = Logger.getLogger(Main.getAppName()).getHandlers();
		libLogger.addHandler(handlers[0]);

		GlobalScreen.addNativeKeyListener(this);
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e)
	{
		if (hotkey == null && !isKeySelection) //not for us
			return;

		if (isKeySelection || (hotkey.equals(lastKeycode) && modifiers.equals(e.getModifiers())))
			executer.keyPressed(e.getModifiers(), lastKeycode, isKeySelection);
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

	public void unregisterNativeHook()
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

	public Integer getHotkeyToCatch()
	{
		return hotkey;
	}

	public void setHotkeyToCatch(Integer hotkeyToCatch)
	{
		this.hotkey = hotkeyToCatch;
	}

	public boolean isKeySelection()
	{
		return isKeySelection;
	}

	public void setKeySelection(boolean isKeySelection)
	{
		this.isKeySelection = isKeySelection;
	}

	public Integer getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(Integer modifiers)
	{
		this.modifiers = modifiers;
	}
}
