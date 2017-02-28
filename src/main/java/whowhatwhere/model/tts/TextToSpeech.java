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

import whowhatwhere.model.tts.engines.FreeTTS;
import whowhatwhere.model.tts.engines.MaryTTS;
import whowhatwhere.model.tts.engines.TTSEngine;

public class TextToSpeech
{
	/*package*/ enum ExistingTTSEngines {FreeTTS, MaryTTS}
	
//	private final static Logger logger = Logger.getLogger(TextToSpeech.class.getPackage().getName());
	
	private FreeTTS freeTTS;
	private MaryTTS maryTTS;
	private TTSEngine chosenEngine;
	private TTSVoice currentVoice;

	
	public TextToSpeech(TTSVoice voice)
	{
		this.currentVoice = voice;
		setVoice(voice);
	}
	
	public void speak(String text)
	{
		if (text != null && !text.isEmpty())
			new Thread(() -> chosenEngine.speak(text)).start();
	}

	public TTSVoice getCurrentVoice()
	{
		return currentVoice;
	}

	public void setVoice(TTSVoice voice)
	{
		currentVoice = voice;
		
		switch(voice.getEngine())
		{
			case FreeTTS:
				if (freeTTS == null)
					freeTTS = new FreeTTS(voice.getVoiceName());
				else
					freeTTS.setVoice(voice.getVoiceName());
				chosenEngine = freeTTS;
				break;
			case MaryTTS:
				if (maryTTS == null)
					maryTTS = new MaryTTS(voice.getVoiceName());
				else
					maryTTS.setVoice(voice.getVoiceName());
				chosenEngine = maryTTS;
				break;
		}
	}
}
