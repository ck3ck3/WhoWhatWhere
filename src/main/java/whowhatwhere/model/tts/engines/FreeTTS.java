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
package whowhatwhere.model.tts.engines;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;

public class FreeTTS implements TTSEngine
{
//	private final static Logger logger = Logger.getLogger(FreeTTS.class.getPackage().getName());
	
	private final static String voicesCanonicalNames = KevinVoiceDirectory.class.getCanonicalName();
	private final static String defaultVoice = "kevin16";
	
	private Voice voice;
	

	public FreeTTS()
	{
		this(defaultVoice);
	}
	
	public FreeTTS(String voiceName)
	{
		System.setProperty("freetts.voices", voicesCanonicalNames);
		setVoice(voiceName);
	}
	
	@Override
	public void setVoice(String voiceName)
	{
		if (voice != null)
			voice.deallocate();
			
		voice = VoiceManager.getInstance().getVoice(voiceName);
		voice.allocate();
	}

	@Override
	public void speak(String text)
	{
		if (text != null)
			voice.speak(text);
	}
}
