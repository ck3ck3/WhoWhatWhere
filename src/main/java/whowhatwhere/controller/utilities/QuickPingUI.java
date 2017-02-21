package whowhatwhere.controller.utilities;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
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
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;

public class QuickPingUI implements LoadAndSaveSettings
{
	private final static Logger logger = Logger.getLogger(QuickPingUI.class.getPackage().getName());

	private final static String propsChkboxHotkey = "chkboxQuickPingHotkey";
	private final static String propsHotkeyKeycode = "QuickPingHotkeyKeycode";
	private final static String propsHotkeyModifiers = "QuickPingHotkeyModifiers";
	private final static String propsComboValue = "QuickPingComboValue";
	private final static String propsOutputMethod = "QuickPingOutputMethod";

	private final static String historyFile = "QuickPingHistory";
	private final static String hotkeyID = "QuickPing hotkey";
	private final static String voiceForTTS = GUIController.voiceForTTS;

	private GUIController guiController;
	private QuickPingController controller;

	private int hotkeyKeyCode;
	private int hotkeyModifiers;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
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
				outputMessage("Please enter an address to ping", address);
				return;
			}

			if (!items.contains(address))
				items.add(address);

			String ping = NetworkSniffer.pingAsString(address, -1); //default timeout

			if (ping.contains("milliseconds"))
				outputMessage(ping, address);
			else
				if (ping.contains("Timeout"))
					outputMessage("Ping time out", address);
				else
					outputMessage("Ping failed", address);
		}

		private void outputMessage(String message, String pingAddress)
		{
			Alert popup = new Alert(AlertType.INFORMATION, message);
			popup.setTitle("Quick ping result");
			popup.setHeaderText("Ping result for " + pingAddress);

			switch (comboOutputMethod.getSelectionModel().getSelectedItem())
			{
				case TTS:
					tts.speak(message);
					break;
				case POPUP:
					popup.show();
					break;
				case TTS_AND_POPUP:
					tts.speak(message);
					popup.show();
					break;
			}
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
}
