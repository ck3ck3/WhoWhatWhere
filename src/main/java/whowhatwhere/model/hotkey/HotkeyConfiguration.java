package whowhatwhere.model.hotkey;

public class HotkeyConfiguration
{
	private HotkeyExecuter executer;
	private int hotkey;
	private int modifiers;
	
	
	public HotkeyConfiguration(HotkeyExecuter executer, int modifiers, int hotkey)
	{
		this.executer = executer;
		this.modifiers = modifiers;
		this.hotkey = hotkey;
	}
	
	public void execute(boolean isNewKey)
	{
		executer.keyPressed(modifiers, hotkey, isNewKey);
	}
	
	public Integer getHotkey()
	{
		return hotkey;
	}

	public void setHotkey(int hotkeyToCatch)
	{
		this.hotkey = hotkeyToCatch;
	}
	
	public Integer getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	public HotkeyExecuter getExecutor()
	{
		return executer;
	}
}
