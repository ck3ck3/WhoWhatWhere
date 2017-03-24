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
package whowhatwhere.controller.quickping;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import whowhatwhere.controller.ConfigurableTTS;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.controller.MessagesI18n;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;
import whowhatwhere.model.tts.MaryTTS;
import whowhatwhere.model.tts.TTSVoice;

public class QuickPingUI implements LoadAndSaveSettings, ConfigurableTTS
{
	private final static Logger logger = Logger.getLogger(QuickPingUI.class.getPackage().getName());

	private final static String propsChkboxHotkey = "chkboxQuickPingHotkey";
	private final static String propsHotkeyKeycode = "QuickPingHotkeyKeycode";
	private final static String propsHotkeyModifiers = "QuickPingHotkeyModifiers";
	private final static String propsComboValue = "QuickPingComboValue";
	private final static String propsOutputMethod = "QuickPingOutputMethod";
	private final static String propsTTSVoiceName = "quickPingTTSVoice";

	private final static String historyFile = "QuickPingHistory";
	private final static String hotkeyID = "QuickPing hotkey";
	private final static String voiceForTTS = GUIController.defaultTTSVoiceName;

	private GUIController guiController;
	private QuickPingController controller;

	private int hotkeyKeyCode;
	private int hotkeyModifiers;
	private MaryTTS tts;
	private HotkeyRegistry hotkeyRegistry;

	private ComboBox<String> comboIPToPing;
	private Button btnConfigureHotkey;
	private Label labelCurrentHotkey;
	private ComboBox<OutputMethod> comboOutputMethod;
	private CheckBox chkboxHotkey;
	private AnchorPane paneHotkey;

	private Runnable hotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String address = comboIPToPing.getEditor().getText();
			List<String> items = comboIPToPing.getItems();

			if (address.isEmpty())
			{
				outputMessage(MessagesI18n.emptyAddress, address);
				return;
			}

			if (!items.contains(address))
				items.add(address);

			String ping = NetworkSniffer.pingAsString(address, NetworkSniffer.defaultPingTimeout);

			if (ping.equals(NetworkSniffer.pingTimeout))
				outputMessage(MessagesI18n.pingTimeout, address);
			else
				if (ping.equals(NetworkSniffer.pingError))
					outputMessage(MessagesI18n.pingFailed, address);
				else
					outputMessage(ping, address);
		}

		private void outputMessage(String message, String pingAddress)
		{
			switch (comboOutputMethod.getSelectionModel().getSelectedItem())
			{
				case TTS:
					sayWithTTS(message);
					break;
				case POPUP:
					showPopup(message, pingAddress);
					break;
				case TTS_AND_POPUP:
					sayWithTTS(message);
					showPopup(message, pingAddress);
					break;
			}
		}
		
		private void sayWithTTS(String message)
		{
			String langCode = tts.getCurrentVoice().getLanguage().getLanguageCode();
			ResourceBundle bundle = ResourceBundle.getBundle(MessagesI18n.i18nBundleLocation, new Locale(langCode));
			boolean isPingResult = message.contains(" milliseconds");
			String translatedMessage;
			
			if (isPingResult)
			{
				int spaceIndex = message.indexOf(' ');
				translatedMessage = message.substring(0, spaceIndex + 1) + bundle.getString(MessagesI18n.milliseconds_speak);
			}
			else
				translatedMessage = bundle.getString(message);
			
			tts.speak(translatedMessage);
		}
		
		private void showPopup(String message, String pingAddress)
		{
			ResourceBundle bundle = ResourceBundle.getBundle(MessagesI18n.i18nBundleLocation, new Locale("en"));
			boolean isPingResult = message.contains(" milliseconds");
			
			Alert popup = new Alert(AlertType.INFORMATION, isPingResult ? message : bundle.getString(message));
			popup.setTitle("Quick Ping Result");
			popup.setHeaderText("Ping result for " + pingAddress);
			popup.show();
		}
	};

	public QuickPingUI(GUIController guiController)
	{
		this.controller = guiController.getQuickPingController();
		this.guiController = guiController;
		this.guiController.registerForSettingsHandler(this);
		comboOutputMethod = controller.getComboOutputMethod();

		initUIElementsFromController();
		initButtonHandlers();
	}

	private void initUIElementsFromController()
	{
		hotkeyRegistry = guiController.getHotkeyRegistry();

		comboIPToPing = controller.getComboToPing();
		btnConfigureHotkey = controller.getBtnConfigureHotkey();
		labelCurrentHotkey = controller.getLabelCurrentHotkey();
		chkboxHotkey = controller.getChkboxHotkey();
		paneHotkey = controller.getPaneHotkey();
	}

	private void initButtonHandlers()
	{
		btnConfigureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(hotkeyID));
	}

	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsChkboxHotkey, ((Boolean) chkboxHotkey.isSelected()).toString());
		props.put(propsHotkeyKeycode, Integer.toString(hotkeyRegistry.getHotkeyKeycode(hotkeyID)));
		props.put(propsHotkeyModifiers, Integer.toString(hotkeyRegistry.getHotkeyModifiers(hotkeyID)));
		props.put(propsComboValue, comboIPToPing.getEditor().getText());
		props.put(propsOutputMethod, comboOutputMethod.getSelectionModel().getSelectedItem().name());
		props.put(propsTTSVoiceName, tts.getCurrentVoice().getVoiceName());

		StringBuilder historyBuilder = new StringBuilder();
		for (String item : comboIPToPing.getItems())
			historyBuilder.append(item + "\n");

		try
		{
			FileUtils.writeStringToFile(new File(historyFile), historyBuilder.toString(), "UTF-8");
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save Ping-to-Speech history: " + e.getMessage(), e);
		}
	}
	
	private void loadTTS(Properties props)
	{
		String voiceName = PropertiesByType.getStringProperty(props, propsTTSVoiceName, voiceForTTS);
		tts = new MaryTTS(voiceName);
	}

	public void loadLastRunConfig(Properties props)
	{
		try
		{
			List<String> lines = FileUtils.readLines(new File(historyFile), "UTF-8");
			comboIPToPing.getItems().addAll(lines);
		}
		catch (IOException e) //ignore, maybe it's first run
		{
		}

		comboIPToPing.getEditor().setText((String) props.get(propsComboValue));
		comboOutputMethod.getSelectionModel().select(OutputMethod.valueOf((String) props.getOrDefault(propsOutputMethod, OutputMethod.TTS)));

		setHotkey(props);
		loadTTS(props);
	}

	private void setHotkey(Properties props)
	{
		hotkeyModifiers = PropertiesByType.getIntProperty(props, propsHotkeyModifiers);
		hotkeyKeyCode = PropertiesByType.getIntProperty(props, propsHotkeyKeycode);

		ChangeListener<Boolean> generatedListener = hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(hotkeyID, hotkeyModifiers, hotkeyKeyCode, chkboxHotkey, labelCurrentHotkey, paneHotkey,
				hotkeyPressed);
		chkboxHotkey.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			generatedListener.changed(observable, oldValue, newValue);
			controller.getPaneAllButHotkeyChkbox().setDisable(!newValue);
		});

		chkboxHotkey.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxHotkey, false));

		if (!chkboxHotkey.isSelected())
		{
			paneHotkey.setDisable(true);
			controller.getPaneAllButHotkeyChkbox().setDisable(true);
		}
	}
	
	@Override
	public void setTTSVoice(TTSVoice voice)
	{
		tts.setVoice(voice);
	}

	@Override
	public TTSVoice getTTSVoice()
	{
		return tts.getCurrentVoice();
	}
}
