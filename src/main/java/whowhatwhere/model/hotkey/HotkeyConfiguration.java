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
