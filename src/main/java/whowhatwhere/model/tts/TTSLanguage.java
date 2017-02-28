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
package whowhatwhere.model.tts;

public enum TTSLanguage
{
	DE("German"),
	EB_GB("English (UK)"),
	EN_US("English (US)"),
	FR_CA("French (CA)"),
	FR_FR("French (FR)"),
	IT("Italian");
	
	private String fullLangName;
	
	private TTSLanguage(String fullLangName)
	{
		this.fullLangName = fullLangName;
	}
	
	@Override
	public String toString()
	{
		return fullLangName;
	}
	
	public String getLanguageCode()
	{
		switch(this)
		{
			case DE:	return "de";
			case FR_CA:	return "fr";
			case FR_FR:	return "fr";
			case IT:	return "it";
			default:	return "en";
		}
	}
}
