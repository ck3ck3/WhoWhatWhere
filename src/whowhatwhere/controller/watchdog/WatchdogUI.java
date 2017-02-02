package whowhatwhere.controller.watchdog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnetpcap.packet.PcapPacket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.networksniffer.watchdog.WatchdogListener;
import whowhatwhere.model.networksniffer.watchdog.WatchdogMessage;

public class WatchdogUI implements WatchdogListener
{
	private final static Logger logger = Logger.getLogger(WatchdogUI.class.getPackage().getName());

	private final static String hotkeyID = "Watchdog hotkey";
	private final static String listFormLocation = "/whowhatwhere/view/fxmls/watchdog/WatchdogManageList.fxml";
	public static final String presetExtension = ".watchdogPreset";
	private static final String lastRunFilename = "Last run" + presetExtension;
	private final static String voiceForTTS = "kevin16";
	private final static int minCooldownValue = 3;
	private final static int maxCooldownValue = Integer.MAX_VALUE;

	private final static String propsChkboxHotkey = "chkboxWatchdogHotkey";
	private final static String propsHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsHotkeyModifiers = "watchdogHotkeyModifiers";
	private final static String propsRadioStopAfterMatch = "radioStopAfterMatch";
	private final static String propsRadioKeepLooking = "radioKeepLooking";
	private final static String propsNumFieldCooldown = "numFieldCooldown";

	private GUIController controller;

	private CheckBox chkboxHotkey;
	private AnchorPane paneHotkeyConfig;
	private Button btnConfigureHotkey;
	private Label labelCurrHotkey;
	private Button btnStart;
	private Button btnStop;
	private Button btnManageList;
	private Label labelEntryCount;
	private Button activeButton;
	private RadioButton radioStopAfterMatch;
	private RadioButton radioKeepLooking;
	private NumberTextField numFieldCooldown;
	private AnchorPane paneCooldown;
	private AnchorPane paneWatchdogConfig;

	private int hotkeyKeyCode;
	private int hotkeyModifiers;
	private ObservableList<PacketTypeToMatch> entryList = FXCollections.observableArrayList();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private NetworkSniffer sniffer = new NetworkSniffer();
	private HotkeyRegistry hotkeyRegistry;

	private Runnable hotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String line;
			Button savedActiveButton = activeButton;

			activeButton.fire();

			if (savedActiveButton == btnStart)
			{
				if (entryList.isEmpty())
					return;

				line = "Starting watchdog";
				changeUIAccordingToListeningState(true);
			}
			else
			{
				line = "Stopping watchdog";
				changeUIAccordingToListeningState(false);
			}

			tts.speak(line);
		}
	};

	public WatchdogUI(GUIController controller)
	{
		this.controller = controller;

		initUIElementsFromController();
		initButtonHandlers();

		entryList.addListener((ListChangeListener<PacketTypeToMatch>) change -> labelEntryCount.setText("Match list contains " + change.getList().size() + " entries"));
		numFieldCooldown.setMinValue(minCooldownValue);
		numFieldCooldown.setMaxValue(maxCooldownValue);
	}

	private void initUIElementsFromController()
	{
		hotkeyRegistry = controller.getHotkeyRegistry();

		chkboxHotkey = controller.getChkboxWatchdogHotkey();
		paneHotkeyConfig = controller.getPaneWatchdogHotkeyConfig();
		btnConfigureHotkey = controller.getBtnWatchdogConfigureHotkey();
		labelCurrHotkey = controller.getLabelWatchdogCurrHotkey();
		labelEntryCount = controller.getLabelWatchdogEntryCount();
		btnStart = controller.getBtnWatchdogStart();
		btnStop = controller.getBtnWatchdogStop();
		btnManageList = controller.getBtnWatchdogManageList();
		radioKeepLooking = controller.getRadioWatchdogKeepLooking();
		radioStopAfterMatch = controller.getRadioWatchdogStopAfterMatch();
		numFieldCooldown = controller.getNumFieldWatchdogCooldown();
		paneCooldown = controller.getPaneWatchdogCooldown();
		paneWatchdogConfig = controller.getPaneWatchdogConfig();
	}

	private void initButtonHandlers()
	{
		WatchdogUI thisObj = this;

		chkboxHotkey.selectedProperty()
				.addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(hotkeyID, hotkeyModifiers, hotkeyKeyCode, chkboxHotkey, labelCurrHotkey, paneHotkeyConfig, hotkeyPressed));

		btnConfigureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(hotkeyID));

		btnManageList.setOnAction(event ->
		{
			ManageListScreen manageListScreen;
			Stage stage = controller.getStage();

			try
			{
				manageListScreen = new ManageListScreen(listFormLocation, stage, stage.getScene(), thisObj);
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "Unable to load watchdog list screen", e);
				return;
			}

			Stage newStage = manageListScreen.showScreenOnNewStage("Manage Watchdog list", null, manageListScreen.getCloseButton());
			newStage.setOnCloseRequest(windowEvent ->
			{
				windowEvent.consume();
				manageListScreen.getCloseButton().fire();
			});
		});

		radioStopAfterMatch.setOnAction(event -> paneCooldown.setDisable(true));
		radioKeepLooking.setOnAction(event -> paneCooldown.setDisable(false));

		btnStart.setOnAction(event ->
		{
			if (entryList.isEmpty())
			{
				new Alert(AlertType.ERROR, "The list must contain at least one entry").showAndWait();
				return;
			}

			NICInfo deviceInfo = controller.getSelectedNIC();
			new Thread(() ->
			{
				StringBuilder errorBuffer = new StringBuilder();

				try
				{
					sniffer.startWatchdogCapture(deviceInfo, entryList, radioKeepLooking.isSelected(), numFieldCooldown.getValue(), thisObj, errorBuffer);
				}
				catch (IllegalArgumentException | UnknownHostException e)
				{
					logger.log(Level.SEVERE, "Unable to build Watchdog list", e);
					Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to build Watchdog list: " + e.getMessage() + "\nError buffer: " + errorBuffer.toString()).showAndWait());
				}
			}).start();

			changeUIAccordingToListeningState(true);
		});

		btnStop.setOnAction(event ->
		{
			changeUIAccordingToListeningState(false);

			sniffer.stopCapture();
		});

		activeButton = btnStart;
	}

	@Override
	public void watchdogFoundMatchingPacket(PcapPacket packetThatMatched, WatchdogMessage message)
	{
		outputMessage(message);

		if (radioStopAfterMatch.isSelected())
			changeUIAccordingToListeningState(false);
	}

	private void changeUIAccordingToListeningState(boolean listening)
	{
		activeButton = (listening ? btnStop : btnStart);

		btnStop.setDisable(!listening);
		btnStart.setDisable(listening);
		paneWatchdogConfig.setDisable(listening);
	}

	private void outputMessage(WatchdogMessage message)
	{
		String msg = message.getMessage();

		switch (message.getMethod())
		{
			case TTS:
				tts.speak(msg);
				break;
			case POPUP:
				Platform.runLater(() -> new Alert(AlertType.INFORMATION, msg).showAndWait());
				break;
			case TTS_AND_POPUP:
				tts.speak(msg);
				Platform.runLater(() -> new Alert(AlertType.INFORMATION, msg).showAndWait());
				break;
		}
	}

	public static void saveListToFile(List<PacketTypeToMatch> list, String filename) throws IOException
	{
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(new ArrayList<>(list));

		oos.close();
		fout.close();
	}

	@SuppressWarnings("unchecked")
	public static void loadListFromFile(ObservableList<PacketTypeToMatch> listToLoadInto, String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);

		ArrayList<PacketTypeToMatch> temp = (ArrayList<PacketTypeToMatch>) ois.readObject();

		listToLoadInto.clear();
		listToLoadInto.addAll(temp);

		ois.close();
		fin.close();

		for (PacketTypeToMatch entry : listToLoadInto)
			entry.initAfterSerialization();
	}

	private void setWatchdogHotkey(Properties props)
	{
		chkboxHotkey.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxHotkey, false));
		hotkeyModifiers = PropertiesByType.getIntProperty(props, propsHotkeyModifiers);
		hotkeyKeyCode = PropertiesByType.getIntProperty(props, propsHotkeyKeycode);

		if (chkboxHotkey.isSelected())
			hotkeyRegistry.addHotkey(hotkeyID, hotkeyModifiers, hotkeyKeyCode, labelCurrHotkey, hotkeyPressed);
		else
			paneHotkeyConfig.setDisable(true);
	}

	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsChkboxHotkey, ((Boolean) chkboxHotkey.isSelected()).toString());
		props.put(propsHotkeyKeycode, Integer.toString(hotkeyRegistry.getHotkeyKeycode(hotkeyID)));
		props.put(propsHotkeyModifiers, Integer.toString(hotkeyRegistry.getHotkeyModifiers(hotkeyID)));
		props.put(propsRadioStopAfterMatch, ((Boolean) radioStopAfterMatch.isSelected()).toString());
		props.put(propsRadioKeepLooking, ((Boolean) radioKeepLooking.isSelected()).toString());
		props.put(propsNumFieldCooldown, Integer.toString(numFieldCooldown.getValue()));

		try
		{
			saveListToFile(entryList, lastRunFilename);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save Watchdog list: " + ioe.getMessage(), ioe);
		}
	}

	public void loadLastRunConfig(Properties props)
	{
		setWatchdogHotkey(props);

		numFieldCooldown.setText(PropertiesByType.getStringProperty(props, propsNumFieldCooldown, String.valueOf(minCooldownValue)));

		if (PropertiesByType.getBoolProperty(props, propsRadioStopAfterMatch, true))
			radioStopAfterMatch.fire(); //this way it activates the button handler
		if (PropertiesByType.getBoolProperty(props, propsRadioKeepLooking, false))
			radioKeepLooking.fire(); //this way it activates the button handler

		try
		{
			loadListFromFile(entryList, lastRunFilename);
		}
		catch (IOException | ClassNotFoundException ioe) //ignore, don't load
		{
		}
	}

	public ObservableList<PacketTypeToMatch> getEntryList()
	{
		return entryList;
	}

	/**
	 * @return a map that maps user note to a list of IPs that have that note
	 */
	public Map<String, List<String>> getUserNotesReverseMap()
	{
		return controller.getUserNotesReverseMap();
	}
}
