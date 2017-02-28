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

import java.util.logging.Level;
import java.util.logging.Logger;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;

public class MaryTTS 
{
	private final static Logger logger = Logger.getLogger(MaryTTS.class.getPackage().getName());
	
	private final static String defaultVoice = "cmu-bdl-hsmm";
	private MaryInterface maryTTS;

	public MaryTTS()
	{
		this(defaultVoice);
	}
	
	public MaryTTS(TTSVoice voice)
	{
		this(voice.getVoiceName());
	}
	
	public MaryTTS(String voiceName)
	{
		try
		{
			System.setProperty("log4j.logger.marytts", "OFF"); //default is INFO, to log/server.log
			System.setProperty("de.phonemiser.logunknown", "false");
			maryTTS = new LocalMaryInterface();
			maryTTS.setVoice(voiceName);
		}
		catch (MaryConfigurationException mce)
		{
			logger.log(Level.SEVERE, "Can't initialize LocalMaryInterface", mce);
		}
	}
	
	public TTSVoice getCurrentVoice()
	{
		return TTSVoice.nameToVoice(maryTTS.getVoice());
	}
	
	public void setVoice(TTSVoice voice)
	{
		setVoice(voice.getVoiceName());
	}
	
	public void setVoice(String voiceName)
	{
		maryTTS.setVoice(voiceName);
	}
	
	public void speak(String text)
	{
		new Thread(() -> speakBlocking(text)).start();
	}

	private synchronized void speakBlocking(String text) //synchronized so that together with the call to join(), calls to this method will wait until previous calls are finished
	{
		try
		{
			AudioPlayer ap = new AudioPlayer();
			ap.setAudio(maryTTS.generateAudio(text));
			ap.start();
			ap.join();
		}
		catch (SynthesisException se)
		{
			logger.log(Level.SEVERE, "MaryTTS failed to generate audio for text \"" + text + "\"", se);
		}
		catch (InterruptedException ie)
		{
			logger.log(Level.SEVERE, "MaryTTS AudioPlayer thread was interrupted.", ie);
		}
	}
}
