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

public interface HotkeyExecuter
{
	/**
	 * @param modifiers
	 *            - integer representing the modifier keys
	 * @param keyCode
	 *            - integer representing the code of the key
	 * @param isNewKey
	 *            - when true, means that the key was pressed during
	 *            hotkey-selection. when false, the actual hotkey was pressed
	 */
	public void keyPressed(int modifiers, int keyCode, boolean isNewKey);
}
