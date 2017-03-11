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
package whowhatwhere;

public class CheckForUpdateResult
{
	private boolean checkSuccessful;
	private String errorMsg;
	private boolean updateAvailable;
	private String newVersion;
	private String releaseNotes;
	
	public CheckForUpdateResult(boolean updateAvailable, String newVersion, String releaseNotes)
	{
		this.checkSuccessful = true;
		this.updateAvailable = updateAvailable;
		this.newVersion = newVersion;
		this.releaseNotes = releaseNotes;
	}
	
	public CheckForUpdateResult(String errorMessage)
	{
		this.checkSuccessful = false;
		this.errorMsg = errorMessage;
	}

	public boolean isCheckSuccessful()
	{
		return checkSuccessful;
	}
	
	public boolean isUpdateAvailable()
	{
		return updateAvailable;
	}

	public String getNewVersion()
	{
		return newVersion;
	}

	public String getReleaseNotes()
	{
		return releaseNotes;
	}
	
	public String getErrorMessage()
	{
		return errorMsg;
	}
}
