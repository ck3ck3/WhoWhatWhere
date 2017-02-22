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
package whowhatwhere.model;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TextToSpeech
{
	private Voice voice;
	private boolean isMuted = false;

	public TextToSpeech(String voiceName)
	{
		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		voice = VoiceManager.getInstance().getVoice(voiceName);
		voice.allocate();
	}

	public void speak(String line)
	{
		if (!isMuted() && line != null)
			new Thread(() -> voice.speak(line)).start();
	}

	public boolean isMuted()
	{
		return isMuted;
	}

	public void setMuted(boolean isMuted)
	{
		this.isMuted = isMuted;
	}

	public void cleanup()
	{
		voice.deallocate();
	}
}
