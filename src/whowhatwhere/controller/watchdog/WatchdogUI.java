package whowhatwhere.controller.watchdog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnetpcap.packet.PcapPacket;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.model.ipsniffer.firstsight.FirstSightListener;
import whowhatwhere.model.ipsniffer.firstsight.IPToMatch;

public class WatchdogUI implements FirstSightListener
{
	private final static Logger logger = Logger.getLogger(WatchdogUI.class.getPackage().getName());

	private final static String hotkeyID = "Watchdog hotkey";
	private final static String listFormLocation = "/whowhatwhere/view/WatchdogManageList.fxml";
	public static final String presetExtension = ".watchdogPreset";
	private static final String lastRunFilename = "Last run" + presetExtension;
	private final static String voiceForTTS = "kevin16";
	private final static int minCooldownValue = 3;
	private final static int maxCooldownValue = Integer.MAX_VALUE;
	

	private final static String propsChkboxHotkey = "chkboxWatchdogHotkey";
	private final static String propsHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsHotkeyModifiers = "watchdogHotkeyModifiers";
	private final static String propsChkboxUseTTS = "chkboxWatchdogUseTTS";
	private final static String propsChkboxUseAlert = "chkboxWatchdogUseAlert";
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
	private TextField textMessage;
	private Button btnManageList;
	private Button btnPreview;
	private Label labelEntryCount;
	private Button activeButton;
	private CheckBox chkboxUseTTS;
	private CheckBox chkboxUseAlert;
	private RadioButton radioStopAfterMatch;
	private RadioButton radioKeepLooking;
	private NumberTextField numFieldCooldown;
	private AnchorPane paneCooldown;
	private AnchorPane paneWatchdogConfig;

	private int hotkeyKeyCode;
	private int hotkeyModifiers;
	private ObservableList<IPToMatch> entryList = FXCollections.observableArrayList();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private IPSniffer sniffer = new IPSniffer();
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

		entryList.addListener((ListChangeListener<IPToMatch>) change -> labelEntryCount.setText("Match list contains " + change.getList().size() + " entries"));
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
		btnStart = controller.getBtnWatchdogStart();
		btnStop = controller.getBtnWatchdogStop();
		textMessage = controller.getTextWatchdogMessage();
		btnManageList = controller.getBtnWatchdogManageList();
		btnPreview = controller.getBtnWatchdogPreview();
		labelEntryCount = controller.getLabelWatchdogEntryCount();
		chkboxUseTTS = controller.getChkboxWatchdogUseTTS();
		chkboxUseAlert = controller.getChkboxWatchdogUseAlert();
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
			Stage stage = (Stage) controller.getTabPane().getScene().getWindow();

			try
			{
				manageListScreen = new ManageListScreen(listFormLocation, stage, stage.getScene(), thisObj);
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "Unable to load watchdog list screen", e);
				return;
			}

			manageListScreen.showScreenOnNewStage("Manage Watchdog list", manageListScreen.getCloseButton());
		});

		String bothAlertMethodsUncheckedError = "If both checkboxes are unchecked, you will not be informed when a match is found. Please select at least one of the checkboxes.";
		
		btnPreview.setOnAction(event ->
		{
			boolean useTTS = chkboxUseTTS.isSelected();
			boolean useAlert = chkboxUseAlert.isSelected(); 
			
			outputMessage();
			
			if (!useTTS && !useAlert)
				new Alert(AlertType.WARNING, bothAlertMethodsUncheckedError).showAndWait();
		});
		
		chkboxUseTTS.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (!newValue && !chkboxUseAlert.isSelected())
				new Alert(AlertType.WARNING, bothAlertMethodsUncheckedError).showAndWait();
		});
		
		chkboxUseAlert.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (!newValue && !chkboxUseTTS.isSelected())
				new Alert(AlertType.WARNING, bothAlertMethodsUncheckedError).showAndWait();
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

			String deviceIP = controller.getButtonToIpMap().get(controller.getTglGrpNIC().getSelectedToggle());
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						sniffer.startFirstSightCapture(deviceIP, entryList, radioKeepLooking.isSelected(), numFieldCooldown.getValue(), thisObj, new StringBuilder());
					}
					catch (IllegalArgumentException | UnknownHostException e)
					{
						logger.log(Level.SEVERE, "Unable to build Watchdog list", e);
						new Alert(AlertType.ERROR, "Unable to build Watchdog list: " + e.getMessage()).showAndWait();
					}
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
	public void firstSightOfIP(PcapPacket packetThatMatched)
	{
		outputMessage();

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
	
	private void outputMessage()
	{
		String msg = textMessage.getText();
		
		if (chkboxUseTTS.isSelected())
			tts.speak(msg);
		if (chkboxUseAlert.isSelected())
			Platform.runLater(() -> new Alert(AlertType.INFORMATION, msg).showAndWait());
	}

	public static void saveListToFile(List<IPToMatch> list, String msgToSay, String filename) throws IOException
	{
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(new ArrayList<>(list));
		oos.writeUTF(msgToSay);

		oos.close();
		fout.close();
	}

	@SuppressWarnings("unchecked")
	public static void loadListFromFile(ObservableList<IPToMatch> listToLoadInto, TextField messageField, String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);

		ArrayList<IPToMatch> temp = (ArrayList<IPToMatch>) ois.readObject();

		listToLoadInto.clear();
		listToLoadInto.addAll(temp);

		messageField.setText(ois.readUTF());

		ois.close();
		fin.close();

		for (IPToMatch entry : listToLoadInto)
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
		props.put(propsChkboxUseTTS, ((Boolean) chkboxUseTTS.isSelected()).toString());
		props.put(propsChkboxUseAlert, ((Boolean) chkboxUseAlert.isSelected()).toString());
		props.put(propsRadioStopAfterMatch, ((Boolean) radioStopAfterMatch.isSelected()).toString());
		props.put(propsRadioKeepLooking, ((Boolean) radioKeepLooking.isSelected()).toString());
		props.put(propsNumFieldCooldown, Integer.toString(numFieldCooldown.getValue()));

		try
		{
			saveListToFile(entryList, textMessage.getText(), lastRunFilename);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save Watchdog list: " + ioe.getMessage(), ioe);
		}
	}

	public void loadLastRunConfig(Properties props)
	{
		setWatchdogHotkey(props);
		
		chkboxUseTTS.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUseTTS, true));
		chkboxUseAlert.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUseAlert, false));
		numFieldCooldown.setText(PropertiesByType.getProperty(props, propsNumFieldCooldown, String.valueOf(minCooldownValue)));
		
		if (PropertiesByType.getBoolProperty(props, propsRadioStopAfterMatch, true))
			radioStopAfterMatch.fire(); //this way it activates the button handler
		if (PropertiesByType.getBoolProperty(props, propsRadioKeepLooking, false))
			radioKeepLooking.fire(); //this way it activates the button handler

		try
		{
			loadListFromFile(entryList, textMessage, lastRunFilename);
		}
		catch (IOException | ClassNotFoundException ioe) //ignore, don't load
		{
		}
	}

	public ObservableList<IPToMatch> getEntryList()
	{
		return entryList;
	}

	public TextField getTextMessage()
	{
		return textMessage;
	}
}
