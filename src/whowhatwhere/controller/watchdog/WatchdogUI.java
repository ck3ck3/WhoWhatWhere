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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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

	private final static String propsChkboxHotkey = "chkboxWatchdogHotkey";
	private final static String propsHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsHotkeyModifiers = "watchdogHotkeyModifiers";

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
				activeButton = btnStop;
				btnStart.setDisable(true);
				btnStop.setDisable(false);
			}
			else
			{
				line = "Stopping watchdog";
				activeButton = btnStart;
				btnStop.setDisable(true);
				btnStart.setDisable(false);
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
	}

	private void initButtonHandlers()
	{
		WatchdogUI thisObj = this;
		
		chkboxHotkey.selectedProperty()
				.addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(hotkeyID, hotkeyModifiers, hotkeyKeyCode, chkboxHotkey, labelCurrHotkey, paneHotkeyConfig, hotkeyPressed));

		btnConfigureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(hotkeyID));
		
		btnManageList.setOnAction(event ->
		{
			ManageListScreen watchdogManageListScreen;
			Stage stage = (Stage) controller.getTabPane().getScene().getWindow();

			try
			{
				watchdogManageListScreen = new ManageListScreen(listFormLocation, stage, stage.getScene(), thisObj);
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "Unable to load watchdog list screen", e);
				return;
			}

			watchdogManageListScreen.showScreenOnNewStage("Manage Watchdog list", watchdogManageListScreen.getCloseButton());
		});

		btnPreview.setOnAction(event -> tts.speak(textMessage.getText()));

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
						sniffer.startFirstSightCapture(deviceIP, entryList, thisObj, new StringBuilder());
					}
					catch (IllegalArgumentException | UnknownHostException e)
					{
						logger.log(Level.SEVERE, "Unable to build Watchdog list", e);
						new Alert(AlertType.ERROR, "Unable to build Watchdog list: " + e.getMessage()).showAndWait();
					}
				}
			}).start();

			activeButton = btnStop;
			btnStop.setDisable(false);
			btnStart.setDisable(true);
		});

		btnStop.setOnAction(event ->
		{
			activeButton = btnStart;
			btnStop.setDisable(true);
			btnStart.setDisable(false);

			sniffer.stopCapture();
		});

		activeButton = btnStart;
	}

	@Override
	public void firstSightOfIP(IPToMatch ipInfo)
	{
		tts.speak(textMessage.getText());

		activeButton = btnStart;
		btnStop.setDisable(true);
		btnStart.setDisable(false);
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
