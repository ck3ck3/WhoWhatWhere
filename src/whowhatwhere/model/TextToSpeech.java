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
		if (!isMuted())
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					voice.speak(line); //blocks the calling thread
				}
			}).start();
		}
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
