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

import java.util.HashMap;
import java.util.Map;

import whowhatwhere.model.tts.TextToSpeech.ExistingTTSEngines;

public enum TTSVoice
{
	dfki_pavoque_neutral_hsmm	(ExistingTTSEngines.MaryTTS, TTSLanguage.DE,	Gender.MALE,	"dfki-pavoque-neutral-hsmm"),
	dfki_poppy_hsmm				(ExistingTTSEngines.MaryTTS, TTSLanguage.EB_GB,	Gender.FEMALE,	"dfki-poppy-hsmm"),
	dfki_prudence_hsmm			(ExistingTTSEngines.MaryTTS, TTSLanguage.EB_GB,	Gender.FEMALE,	"dfki-prudence-hsmm"),
	dfki_obadiah_hsmm			(ExistingTTSEngines.MaryTTS, TTSLanguage.EB_GB,	Gender.MALE,	"dfki-obadiah-hsmm"),
	dfki_spike_hsmm				(ExistingTTSEngines.MaryTTS, TTSLanguage.EB_GB,	Gender.MALE,	"dfki-spike-hsmm"),
//	kevin16						(ExistingTTSEngines.FreeTTS, TTSLanguage.EN_US,	Gender.MALE,	"kevin16"),
	cmu_bdl_hsmm				(ExistingTTSEngines.MaryTTS, TTSLanguage.EN_US,	Gender.MALE,	"cmu-bdl-hsmm"),
	cmu_rms_hsmm				(ExistingTTSEngines.MaryTTS, TTSLanguage.EN_US,	Gender.MALE,	"cmu-rms-hsmm"),
	enst_camille_hsmm			(ExistingTTSEngines.MaryTTS, TTSLanguage.FR_FR,	Gender.FEMALE,	"enst-camille-hsmm"),
	enst_dennys_hsmm			(ExistingTTSEngines.MaryTTS, TTSLanguage.FR_CA,	Gender.MALE,	"enst-dennys-hsmm"),
	upmc_pierre_hsmm			(ExistingTTSEngines.MaryTTS, TTSLanguage.FR_FR,	Gender.MALE,	"upmc-pierre-hsmm"),
	istc_lucia_hsmm				(ExistingTTSEngines.MaryTTS, TTSLanguage.IT,	Gender.FEMALE,	"istc-lucia-hsmm");
	
	private enum Gender {MALE, FEMALE}
	
	private static Map<String, TTSVoice> nameToVoiceMap = new HashMap<>();
	
	static
	{
		for (TTSVoice voice : TTSVoice.values())
			nameToVoiceMap.put(voice.getVoiceName(), voice);
	}
	
	private ExistingTTSEngines engine;
	private TTSLanguage language;
	private Gender gender;
	private String voiceName;
		
	private TTSVoice(ExistingTTSEngines engine, TTSLanguage language, Gender gender, String voiceName)
	{
		this.engine = engine;
		this.language = language;
		this.gender = gender;
		this.voiceName = voiceName;
	}
	
	public static TTSVoice nameToVoice(String voiceName)
	{
		return nameToVoiceMap.get(voiceName);
	}
	
	@Override
	public String toString()
	{
		return "[" + language + ", " + gender.toString().toLowerCase() + "] " + voiceName;
		
	}

	/*package*/ ExistingTTSEngines getEngine()
	{
		return engine;
	}

	public TTSLanguage getLanguage()
	{
		return language;
	}

	public String getVoiceName()
	{
		return voiceName;
	}

	public Gender getGender()
	{
		return gender;
	}
}