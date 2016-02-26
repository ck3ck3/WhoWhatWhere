package mostusedips.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import mostusedips.model.PropertiesByType;
import mostusedips.model.TextToSpeech;
import mostusedips.model.ipsniffer.IPSniffer;

public class PingToSpeechUI
{
	private final static Logger logger = Logger.getLogger(PingToSpeechUI.class.getPackage().getName());
	
	private final static String propsChkboxPTSHotkey = "chkboxPTSHotkey";
	private final static String propsPTSHotkey = "PTSHotkey";
	private final static String propsPTSModifiers = "PTSModifiers";
	private final static String propsPTSComboValue = "PTSComboValue";
	
	private final static String ptsHistoryFile = "ptsHistory";
	private final static String ptsHotkeyID = "PTS hotkey";
	private final static String voiceForTTS = "kevin16";
	
	private GUIController controller;
	
	private int ptsHotkeyKeyCode;
	private int ptsHotkeyModifiers;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyRegistry hotkeyRegistry;
	
	private ComboBox<String> comboPTSipToPing;
	private Button btnPTSConfigureHotkey;
	private Label labelPTSCurrentHotkey;
	private CheckBox chkboxPTSHotkey;
	private AnchorPane panePTSHotkey;
	
	
	private Runnable ptsHotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String address = comboPTSipToPing.getEditor().getText();
			List<String> items = comboPTSipToPing.getItems();

			if (!items.contains(address))
				items.add(address);

			String ping = IPSniffer.pingAsString(address, -1); //default timeout

			if (ping.contains("milliseconds"))
				tts.speak(ping);
			else
				if (ping.contains("Timeout"))
					tts.speak("Ping time out");
				else
					tts.speak("Ping failed");
		}
	};
	
	
	public PingToSpeechUI(GUIController controller)
	{
		this.controller = controller;
		
		initUIElementsFromController();
		initButtonHandlers();
	}

	private void initUIElementsFromController()
	{
		hotkeyRegistry = controller.getHotkeyRegistry();
		
		comboPTSipToPing = controller.getComboPTSipToPing();
		btnPTSConfigureHotkey = controller.getBtnPTSConfigureHotkey();
		labelPTSCurrentHotkey = controller.getLabelPTSCurrentHotkey();
		chkboxPTSHotkey = controller.getChkboxPTSHotkey();
		panePTSHotkey = controller.getPanePTSHotkey();
	}

	private void initButtonHandlers()
	{
		chkboxPTSHotkey.selectedProperty()
		.addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(ptsHotkeyID, ptsHotkeyModifiers, ptsHotkeyKeyCode, chkboxPTSHotkey, labelPTSCurrentHotkey, panePTSHotkey, ptsHotkeyPressed));
		
		btnPTSConfigureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(ptsHotkeyID));
	}



	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsChkboxPTSHotkey, ((Boolean) chkboxPTSHotkey.isSelected()).toString());
		props.put(propsPTSHotkey, Integer.toString(hotkeyRegistry.getHotkeyKeycode(ptsHotkeyID)));
		props.put(propsPTSModifiers, Integer.toString(hotkeyRegistry.getHotkeyModifiers(ptsHotkeyID)));
		props.put(propsPTSComboValue, comboPTSipToPing.getEditor().getText());

		StringBuilder ptsHistoryBuilder = new StringBuilder();
		for (String item : comboPTSipToPing.getItems())
			ptsHistoryBuilder.append(item + "\n");

		
		try
		{
			FileUtils.writeStringToFile(new File(ptsHistoryFile), ptsHistoryBuilder.toString(), "UTF-8");
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save Ping-to-Speech history: " + e.getMessage(), e);
		}

	}
	
	public void loadLastRunConfig(Properties props)
	{
		try
		{
			List<String> lines = FileUtils.readLines(new File(ptsHistoryFile), "UTF-8");
			comboPTSipToPing.getItems().addAll(lines);
		}
		catch (IOException e) //ignore, maybe it's first run
		{
		}

		comboPTSipToPing.getEditor().setText((String) props.get(propsPTSComboValue));

		setPTSHotkey(props);
	}
	
	private void setPTSHotkey(Properties props)
	{
		chkboxPTSHotkey.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxPTSHotkey));
		ptsHotkeyModifiers = PropertiesByType.getIntProperty(props, propsPTSModifiers);
		ptsHotkeyKeyCode = PropertiesByType.getIntProperty(props, propsPTSHotkey);

		if (chkboxPTSHotkey.isSelected())
			hotkeyRegistry.addHotkey(ptsHotkeyID, ptsHotkeyModifiers, ptsHotkeyKeyCode, labelPTSCurrentHotkey, ptsHotkeyPressed);
		else
			panePTSHotkey.setDisable(true);
	}

}
